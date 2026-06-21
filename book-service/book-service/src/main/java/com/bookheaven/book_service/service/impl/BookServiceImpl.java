package com.bookheaven.book_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookheaven.book_service.dto.requestDto.AddBookRequest;
import com.bookheaven.book_service.dto.requestDto.StockUpdateRequest;
import com.bookheaven.book_service.dto.requestDto.UpdateBookRequest;
import com.bookheaven.book_service.dto.requestDto.UpdateListingRequest;
import com.bookheaven.book_service.dto.responseDto.BookDetailResponse;
import com.bookheaven.book_service.dto.responseDto.BookPublicResponse;
import com.bookheaven.book_service.dto.responseDto.PaginatedResponse;
import com.bookheaven.book_service.entity.Book;
import com.bookheaven.book_service.entity.SellerListing;
import com.bookheaven.book_service.exception.BookNotFoundException;
import com.bookheaven.book_service.exception.DuplicateIsbnException;
import com.bookheaven.book_service.exception.InvalidBookDataException;
import com.bookheaven.book_service.exception.InvalidCopiesException;
import com.bookheaven.book_service.producer.BookEventProducer;
import com.bookheaven.book_service.repository.BookRepository;
import com.bookheaven.book_service.repository.SellerListingRepository;
import com.bookheaven.book_service.service.BookService;
import com.bookheaven.book_service.service.RedisService;
import com.bookheaven.book_service.util.AppUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.bookheaven.book_service.util.AppUtil.normalizeIsbn;

@AllArgsConstructor
@Service
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final SellerListingRepository sellerListingRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final com.bookheaven.book_service.util.JwtUtil jwtUtil;
    private final BookEventProducer bookEventProducer;

    // ===================== ADD LISTING =====================

    @Transactional
    @Override
    public SellerListing addListing(AddBookRequest dto, UUID sellerId, String token) {
        String sellerUsername = jwtUtil.extractUsername(token);
        String sellerEmail = jwtUtil.extractEmail(token);
        
        if (dto.getIsbn() == null || dto.getIsbn().isBlank()) {
            throw new InvalidBookDataException("ISBN cannot be null or blank");
        }
        String isbn = normalizeIsbn(dto.getIsbn());

        // Find or create canonical book
        Book book = bookRepository.findByIsbn(isbn).orElseGet(() -> {
            Book newBook = AppUtil.addBookRequestMapper(dto);
            return bookRepository.save(newBook);
        });

        // Prevent duplicate listing by same seller for same book
        if (sellerListingRepository.existsBySellerIdAndBookId(sellerId, book.getId())) {
            throw new DuplicateIsbnException("You already have a listing for ISBN: " + dto.getIsbn());
        }

        SellerListing listing = SellerListing.builder()
                .book(book)
                .sellerId(sellerId)
                .sellerUsername(sellerUsername)
                .sellerEmail(sellerEmail)
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .copies(dto.getCopies())
                .copiesAvailable(dto.getCopies())
                .build();

        SellerListing saved = sellerListingRepository.save(listing);
        bookEventProducer.publishBookUpdate(book.getId());
        redisService.deleteKeyWithPattern("books:list:*");
        return saved;
    }

    // ===================== UPDATE BOOK METADATA (Admin only) =====================

    @Transactional
    @Override
    public Book updateBookMetadata(Long bookId, UpdateBookRequest dto) {
        Book existing = getBookById(bookId);
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) existing.setAuthor(dto.getAuthor());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getCategory() != null) existing.setCategory(dto.getCategory());
        if (dto.getImg() != null) existing.setImg(dto.getImg());
        if (dto.getIsbn() != null && !normalizeIsbn(dto.getIsbn()).equals(normalizeIsbn(existing.getIsbn()))) {
            if (bookRepository.existsByIsbn(normalizeIsbn(dto.getIsbn()))) {
                throw new DuplicateIsbnException("ISBN already exists: " + dto.getIsbn());
            }
            existing.setIsbn(normalizeIsbn(dto.getIsbn()));
        }
        Book saved = bookRepository.save(existing);
        bookEventProducer.publishBookUpdate(saved.getId());
        redisService.deleteKeyWithPattern("books:list:*");
        redisService.deleteKey("books:detail:" + bookId);
        return saved;
    }

    // ===================== UPDATE LISTING (Seller only) =====================

    @Transactional
    @Override
    public SellerListing updateListing(Long listingId, UpdateListingRequest dto, UUID sellerId) {
        SellerListing listing = getListingById(listingId);
        if (!listing.getSellerId().equals(sellerId)) {
            throw new AccessDeniedException("You do not own this listing");
        }
        if (dto.getPrice() != null) listing.setPrice(dto.getPrice());
        if (dto.getCurrency() != null) listing.setCurrency(dto.getCurrency());
        if (dto.getCopies() != null) {
            int issued = listing.getCopies() - listing.getCopiesAvailable();
            if (dto.getCopies() < issued) {
                throw new InvalidCopiesException("Cannot reduce copies below already issued count of: " + issued);
            }
            listing.setCopies(dto.getCopies());
            listing.setCopiesAvailable(dto.getCopies() - issued);
        }
        SellerListing saved = sellerListingRepository.save(listing);
        bookEventProducer.publishBookUpdate(listing.getBook().getId());
        redisService.deleteKeyWithPattern("books:list:*");
        redisService.deleteKey("books:detail:" + listing.getBook().getId());
        redisService.deleteKeyWithPattern("books:seller:" + sellerId + ":*");
        return saved;
    }

    // ===================== DELETE LISTING =====================

    @Transactional
    @Override
    public void deleteListing(Long listingId, UUID sellerId, boolean isAdmin) {
        SellerListing listing = getListingById(listingId);
        if (!isAdmin && !listing.getSellerId().equals(sellerId)) {
            throw new AccessDeniedException("You do not own this listing");
        }
        Long bookId = listing.getBook().getId();
        sellerListingRepository.delete(listing);
        // Canonical Book is never deleted — book stays as out-of-stock ghost
        bookEventProducer.publishBookUpdate(bookId);
        redisService.deleteKeyWithPattern("books:list:*");
        redisService.deleteKey("books:detail:" + bookId);
        redisService.deleteKeyWithPattern("books:seller:" + listing.getSellerId() + ":*");
    }

    // ===================== GET ALL BOOKS (Homepage) =====================

    @Override
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findBooksWithStock(pageable);
    }

    public PaginatedResponse<BookPublicResponse> getAllBooksPublicResponse(Integer pageNumber, Integer pageSize) {
        Pageable pageable = AppUtil.createPageable(pageNumber, pageSize);
        String cacheKey = "books:list:page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize();
        try {
            if (redisService.containsKey(cacheKey)) {
                String cacheJson = (String) redisService.getKey(cacheKey);
                return objectMapper.readValue(
                        cacheJson,
                        new TypeReference<>() {
                        }
                );
            }
        } catch (Exception ex) {
            redisService.deleteKey(cacheKey);
        }
        Page<Book> books = getAllBooks(pageable);
        PaginatedResponse<BookPublicResponse> responseEntity = getPageResponseEntity(books);
        try {
            String json = objectMapper.writeValueAsString(responseEntity);
            redisService.saveKeyWithTimeout(cacheKey, json, 10);
        } catch (Exception ex) {
            log.error("Error fetching books", ex);
        }
        return responseEntity;
    }

    // ===================== SEARCH =====================

    @Override
    public PaginatedResponse<BookPublicResponse> searchBooks(String title, String author, String category, String isbn,
                                   Integer pageNumber, Integer pageSize) {
        Pageable pageable = AppUtil.createPageable(pageNumber, pageSize);
        Page<Book> books;
        if (title != null)    books = bookRepository.findByTitleContaining(title, pageable);
        else if (author != null)   books = bookRepository.findByAuthorContaining(author, pageable);
        else if (category != null) books = bookRepository.findByCategoryContaining(category, pageable);
        else if (isbn != null)     books =  bookRepository.findByIsbnWithListings(normalizeIsbn(isbn), pageable);
        else books = getAllBooks(pageable);
        return getPageResponseEntity(books);
    }

    // ===================== GET BOOK BY ID =====================

    @Override
    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
    }
    public BookDetailResponse getBookByIdDetails(Long bookId) {
        String cacheKey = "books:detail:" + bookId;
        try {
            if (redisService.containsKey(cacheKey)) {
                String cacheJson = (String) redisService.getKey(cacheKey);
                return objectMapper.readValue(cacheJson, BookDetailResponse.class);
            }
        } catch (Exception ex) {
            redisService.deleteKey(cacheKey);
        }
        Book book = getBookById(bookId);
        List<SellerListing> listings = getListingsForBook(bookId);
        BookDetailResponse response = AppUtil.toDetailResponse(book, listings);
        try {
            String json = objectMapper.writeValueAsString(response);
            redisService.saveKeyWithTimeout(cacheKey, json, 10);
        } catch (Exception ex) {
            log.error("Error fetching book", ex);
        }
        return response;
    }

    // ===================== SELLER LISTINGS =====================

    @Override
    public Page<SellerListing> getListingsBySeller(UUID sellerId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = AppUtil.createPageable(pageNumber, pageSize);
        String cacheKey = "books:seller:" + sellerId + ":page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize();
        try {
            if (redisService.containsKey(cacheKey)) {
                String cacheJson = (String) redisService.getKey(cacheKey);
                return objectMapper.readValue(cacheJson, new TypeReference<PageImpl<SellerListing>>() {});
            }
        } catch (JsonProcessingException ex) {
            redisService.deleteKey(cacheKey);
        }
        Page<SellerListing> listings = sellerListingRepository.findBySellerId(sellerId, pageable);
        try {
            String json = objectMapper.writeValueAsString(listings);
            redisService.saveKeyWithTimeout(cacheKey, json, 10);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to cache seller listings", ex);
        }
        return listings;
    }

    // ===================== LISTINGS FOR BOOK (Detail page) =====================

    @Override
    public List<SellerListing> getListingsForBook(Long bookId) {
        return sellerListingRepository.findByBookIdOrderByPriceAsc(bookId);
    }

    // ===================== BULK (Internal — cart/order) =====================

    @Override
    public List<SellerListing> getBulkListings(List<Long> listingIds) {
        return sellerListingRepository.findAllById(listingIds);
    }

    // ===================== STOCK OPERATIONS =====================

    @Transactional
    @Override
    public void reduceStockBookList(List<StockUpdateRequest> list) {
        for (StockUpdateRequest req : list) {
            SellerListing listing = getListingById(req.getBookId());
            if (listing.getCopiesAvailable() - req.getQuantity() < 0) {
                throw new InvalidCopiesException("Not enough copies available for listing id: " + req.getBookId());
            }
            listing.setCopiesAvailable(listing.getCopiesAvailable() - req.getQuantity());
            bookEventProducer.publishBookUpdate(listing.getBook().getId());
            String cacheKey = "books:detail:"+req.getBookId();
            if(redisService.containsKey(cacheKey)){
                redisService.deleteKey(cacheKey);
            }
        }
    }

    @Transactional
    @Override
    public void restoreStockBookList(List<StockUpdateRequest> list) {
        for (StockUpdateRequest req : list) {
            SellerListing listing = getListingById(req.getBookId());
            listing.setCopiesAvailable(listing.getCopiesAvailable() + req.getQuantity());
            bookEventProducer.publishBookUpdate(listing.getBook().getId());
            String cacheKey = "books:detail:"+req.getBookId();
            if(redisService.containsKey(cacheKey)){
                redisService.deleteKey(cacheKey);
            }
        }
    }

    // ===================== RATING OPERATIONS =====================

    @Transactional
    @Override
    public void updateBookRating(Long bookId, Double averageRating, Integer totalReviews) {
        Book book = getBookById(bookId);
        book.setAverageRating(averageRating);
        book.setTotalReviews(totalReviews);
        bookRepository.save(book);
        bookEventProducer.publishBookUpdate(bookId);
        redisService.deleteKeyWithPattern("books:list:*");
        redisService.deleteKey("books:detail:" + bookId);
    }

    @Override
    public void syncAllBooksToSearch() {
        List<Book> books = bookRepository.findAll();
        int count = 0;
        for (Book book : books) {
            bookEventProducer.publishBookUpdate(book.getId());
            count++;
        }
        log.info("Successfully published sync events for {} books to the search service.", count);
    }

    // ===================== PRIVATE HELPERS =====================

    private SellerListing getListingById(Long listingId) {
        return sellerListingRepository.findById(listingId)
                .orElseThrow(() -> new BookNotFoundException("Listing not found with id: " + listingId));
    }

    @Override
    public List<BookPublicResponse> getBulkBooks(List<Long> bookIds) {
        List<Book> books = bookRepository.findAllById(bookIds);
        
        List<SellerListing> allListings = sellerListingRepository.findByBookIdIn(bookIds);
        Map<Long, List<SellerListing>> listingsByBookId = allListings.stream()
                .collect(Collectors.groupingBy(listing -> listing.getBook().getId()));

        return books.stream().map(book -> {
            List<SellerListing> listings = listingsByBookId.getOrDefault(book.getId(), Collections.emptyList());
            SellerListing cheapest = listings.stream()
                    .filter(l -> l.getCopiesAvailable() > 0)
                    .min(Comparator.comparingDouble(SellerListing::getPrice))
                    .orElse(null);
            return AppUtil.toPublicResponse(book, cheapest);
        }).toList();
    }

    @NonNull
    public PaginatedResponse<BookPublicResponse> getPageResponseEntity(Page<Book> books) {
        if (books.isEmpty()) {
            return PaginatedResponse.fromPage(books.map(b -> AppUtil.toPublicResponse(b, null)));
        }
        
        List<Long> bookIds = books.getContent().stream().map(Book::getId).toList();
        List<SellerListing> allListings = sellerListingRepository.findByBookIdIn(bookIds);
        java.util.Map<Long, List<SellerListing>> listingsByBookId = allListings.stream()
                .collect(java.util.stream.Collectors.groupingBy(listing -> listing.getBook().getId()));

        Page<BookPublicResponse> response = books.map(book -> {
            List<SellerListing> listings = listingsByBookId.getOrDefault(book.getId(), java.util.Collections.emptyList());
            SellerListing cheapest = listings.stream()
                    .filter(l -> l.getCopiesAvailable() > 0)
                    .min(java.util.Comparator.comparingDouble(SellerListing::getPrice))
                    .orElse(null);
            return AppUtil.toPublicResponse(book, cheapest);
        });
        return PaginatedResponse.fromPage(response);
    }
}
