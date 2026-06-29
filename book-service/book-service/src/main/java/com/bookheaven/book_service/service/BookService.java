package com.bookheaven.book_service.service;

import com.bookheaven.book_service.dto.requestDto.AddBookRequest;
import com.bookheaven.common.dto.request.StockUpdateRequest;
import com.bookheaven.book_service.dto.requestDto.UpdateBookRequest;
import com.bookheaven.book_service.dto.requestDto.UpdateListingRequest;
import com.bookheaven.book_service.dto.responseDto.BookDetailResponse;
import com.bookheaven.common.dto.response.BookPublicResponse;
import com.bookheaven.common.dto.response.PaginatedResponse;
import com.bookheaven.book_service.entity.Book;
import com.bookheaven.book_service.entity.SellerListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookService {

    // Seller: add a listing (creates Book if ISBN is new, otherwise links to existing)
    SellerListing addListing(AddBookRequest dto, UUID sellerId, String token);

    // Admin: update canonical book metadata
    Book updateBookMetadata(Long bookId, UpdateBookRequest dto);

    // Seller: update their own listing's price / copies / currency
    SellerListing updateListing(Long listingId, UpdateListingRequest dto, UUID sellerId);

    // Seller or Admin: delete a listing
    void deleteListing(Long listingId, UUID sellerId, boolean isAdmin);

    // Stock operations (called by order-service via internal endpoints)
    void reduceStockBookList(List<StockUpdateRequest> list);
    void restoreStockBookList(List<StockUpdateRequest> list);

    // Public queries
    Page<Book> getAllBooks(Pageable  pageable);
    public PaginatedResponse<BookPublicResponse> getAllBooksPublicResponse(Integer pageNumber, Integer pageSize);
    Book getBookById(Long bookId);
    public BookDetailResponse getBookByIdDetails(Long bookId);

    // Seller dashboard
    Page<SellerListing> getListingsBySeller(UUID sellerId, Integer pageNumber, Integer pageSize);

    // Internal: bulk listings by listing IDs (for cart/order)
    List<SellerListing> getBulkListings(List<Long> listingIds);

    // Listing detail: all listings for a book, sorted by price
    List<SellerListing> getListingsForBook(Long bookId);

    // Update rating
    void updateBookRating(Long bookId, Double averageRating, Integer totalReviews);

    // Internal: bulk fetch canonical books by ID (for wishlist)
    List<BookPublicResponse> getBulkBooks(List<Long> bookIds);

    // Sync all existing books to search engine
    void syncAllBooksToSearch();
}
