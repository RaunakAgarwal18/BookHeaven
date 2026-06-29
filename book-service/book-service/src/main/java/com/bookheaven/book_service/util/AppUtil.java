package com.bookheaven.book_service.util;

import com.bookheaven.book_service.dto.requestDto.AddBookRequest;
import com.bookheaven.book_service.dto.responseDto.*;
import com.bookheaven.common.dto.response.PaginatedResponse;
import com.bookheaven.common.dto.response.BookPublicResponse;
import com.bookheaven.book_service.entity.Book;
import com.bookheaven.book_service.entity.SellerListing;
import com.bookheaven.book_service.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AppUtil {

    private BookService bookService;

    public static Pageable createPageable(Integer pageNumber, Integer pageSize) {
        int safePage = pageNumber == null ? 0 : Math.max(pageNumber, 0);
        int safeSize = pageSize == null ? 10 : Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage, safeSize);
    }

    /** Homepage card — needs the cheapest available listing for price display */
    public static BookPublicResponse toPublicResponse(Book book, SellerListing cheapestListing) {
        if (book == null) return null;
        BookPublicResponse.BookPublicResponseBuilder builder = BookPublicResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .category(book.getCategory())
                .img(book.getImg())
                .isbn(book.getIsbn())
                .averageRating(book.getAverageRating())
                .totalReviews(book.getTotalReviews());
        if (cheapestListing != null) {
            builder.cheapestListingId(cheapestListing.getId())
                   .lowestPrice(cheapestListing.getPrice())
                   .lowestCurrency(cheapestListing.getCurrency())
                   .totalCopiesAvailable(cheapestListing.getCopiesAvailable());
        }
        return builder.build();
    }

    /** Detail page — book + all listings sorted by price */
    public static BookDetailResponse toDetailResponse(Book book, List<SellerListing> listings) {
        if (book == null) return null;
        List<SellerListingDto> listingDtos = listings == null ? Collections.emptyList() :
                listings.stream().map(AppUtil::toListingDto).collect(Collectors.toList());
        return BookDetailResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .category(book.getCategory())
                .img(book.getImg())
                .isbn(book.getIsbn())
                .averageRating(book.getAverageRating())
                .totalReviews(book.getTotalReviews())
                .listings(listingDtos)
                .build();
    }

    /** Single listing DTO embedded in detail response */
    public static SellerListingDto toListingDto(SellerListing listing) {
        if (listing == null) return null;
        return SellerListingDto.builder()
                .listingId(listing.getId())
                .sellerUsername(listing.getSellerUsername())
                .price(listing.getPrice())
                .currency(listing.getCurrency())
                .copiesAvailable(listing.getCopiesAvailable())
                .build();
    }

    /** Seller dashboard view of their own listing */
    public static BookSellerResponse toSellerResponse(SellerListing listing) {
        if (listing == null) return null;
        Book book = listing.getBook();
        return BookSellerResponse.builder()
                .listingId(listing.getId())
                .bookId(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .img(book.getImg())
                .price(listing.getPrice())
                .currency(listing.getCurrency())
                .copies(listing.getCopies())
                .copiesAvailable(listing.getCopiesAvailable())
                .sellerId(listing.getSellerId())
                .sellerUsername(listing.getSellerUsername())
                .build();
    }

    /** Internal response for cart/order bulk lookup */
    public static BookInternalResponse toInternalResponse(SellerListing listing) {
        if (listing == null) return null;
        Book book = listing.getBook();
        return BookInternalResponse.builder()
                .listingId(listing.getId())
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .img(book.getImg())
                .price(listing.getPrice())
                .currency(listing.getCurrency())
                .copiesAvailable(listing.getCopiesAvailable())
                .sellerId(listing.getSellerId())
                .sellerUsername(listing.getSellerUsername())
                .sellerEmail(listing.getSellerEmail())
                .build();
    }

    public static Book addBookRequestMapper(AddBookRequest dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(normalizeIsbn(dto.getIsbn()));
        book.setImg(dto.getImg());
        book.setDescription(dto.getDescription());
        book.setCategory(dto.getCategory());
        return book;
    }



    public static String normalizeIsbn(String isbn) {
        return isbn.replaceAll("[^0-9Xx]", "").trim().toUpperCase();
    }
}
