package com.bookheaven.book_service.controller;

import com.bookheaven.book_service.dto.requestDto.AddBookRequest;
import com.bookheaven.common.dto.request.StockUpdateRequest;
import com.bookheaven.book_service.dto.requestDto.UpdateBookRequest;
import com.bookheaven.book_service.dto.requestDto.UpdateListingRequest;
import com.bookheaven.book_service.dto.responseDto.*;
import com.bookheaven.common.dto.response.PaginatedResponse;
import com.bookheaven.common.dto.response.BookPublicResponse;
import com.bookheaven.book_service.entity.Book;
import com.bookheaven.book_service.entity.SellerListing;
import com.bookheaven.book_service.service.BookService;
import com.bookheaven.book_service.util.AppUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    // ===================== PUBLIC ENDPOINTS =====================

    /** Homepage: paginated list of books that have stock */
    @GetMapping
    public ResponseEntity<PaginatedResponse<BookPublicResponse>> getAllBooks(@RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        PaginatedResponse<BookPublicResponse> books = bookService.getAllBooksPublicResponse(pageNumber, pageSize);
        return ResponseEntity.ok(books);
    }

    /** Book detail page: canonical book + all listings sorted by price */
    @GetMapping("/{bookId}")
    public ResponseEntity<BookDetailResponse> getBookById(@PathVariable Long bookId) {
        BookDetailResponse response = bookService.getBookByIdDetails(bookId);
        return ResponseEntity.ok(response);
    }



    // ===================== SELLER ENDPOINTS =====================

    /** Seller: add a listing (creates book if ISBN is new, otherwise links to existing) */
    @PostMapping
    public ResponseEntity<BookSellerResponse> addListing(@RequestBody AddBookRequest dto) {
        UUID sellerId = getAuthenticatedUserId();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) auth.getCredentials();
        SellerListing listing = bookService.addListing(dto, sellerId, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppUtil.toSellerResponse(listing));
    }

    /** Seller: update their own listing (price, currency, copies only) */
    @PutMapping("/listing/{listingId}")
    public ResponseEntity<BookSellerResponse> updateListing(
            @PathVariable Long listingId,
            @RequestBody UpdateListingRequest dto) {
        UUID sellerId = getAuthenticatedUserId();
        SellerListing listing = bookService.updateListing(listingId, dto, sellerId);
        return ResponseEntity.ok(AppUtil.toSellerResponse(listing));
    }

    /** Seller (or Admin): delete a listing */
    @DeleteMapping("/listing/{listingId}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long listingId) {
        UUID sellerId = getAuthenticatedUserId();
        boolean isAdmin = checkIsAdmin();
        bookService.deleteListing(listingId, sellerId, isAdmin);
        return ResponseEntity.ok().build();
    }

    /** Seller: get their own listings (inventory dashboard) */
    @GetMapping("/seller")
    public ResponseEntity<PaginatedResponse<BookSellerResponse>> getSellerListings(
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        UUID sellerId = getAuthenticatedUserId();
        
        Page<BookSellerResponse> pageResponse = bookService
                .getListingsBySeller(sellerId, pageNumber, pageSize)
                .map(AppUtil::toSellerResponse);
        
        PaginatedResponse<BookSellerResponse> response = PaginatedResponse.fromPage(pageResponse);
        return ResponseEntity.ok(response);
    }

    // ===================== ADMIN ENDPOINTS =====================

    /** Admin: update canonical book metadata */
    @PutMapping("/{bookId}")
    public ResponseEntity<BookDetailResponse> updateBookMetadata(
            @PathVariable Long bookId,
            @RequestBody UpdateBookRequest dto) {
        if (!checkIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Book book = bookService.updateBookMetadata(bookId, dto);
        List<SellerListing> listings = bookService.getListingsForBook(bookId);
        return ResponseEntity.ok(AppUtil.toDetailResponse(book, listings));
    }

    /** Admin: Force sync all existing books to Elasticsearch */
    @PostMapping("/admin/sync-search")
    public ResponseEntity<String> syncAllBooksToSearch() {
        if (!checkIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required.");
        }
        bookService.syncAllBooksToSearch();
        return ResponseEntity.ok("Successfully dispatched sync events for all books to Elasticsearch.");
    }

    // ===================== INTERNAL ENDPOINTS (service-to-service) =====================

    /** Bulk fetch listings by listing IDs — used by cart-service and order-service */
    @PostMapping("/bulk")
    public ResponseEntity<List<BookInternalResponse>> getBulkListings(@RequestBody List<Long> listingIds) {
        List<BookInternalResponse> response = bookService.getBulkListings(listingIds).stream()
                .map(AppUtil::toInternalResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** Stock reduce — called by order-service on order confirmation */
    @PutMapping("/reduce-stock")
    public ResponseEntity<Void> reduceStock(@RequestBody List<StockUpdateRequest> list) {
        bookService.reduceStockBookList(list);
        return ResponseEntity.ok().build();
    }

    /** Stock restore — called by order-service on order cancellation */
    @PutMapping("/restore-stock")
    public ResponseEntity<Void> restoreStock(@RequestBody List<StockUpdateRequest> list) {
        bookService.restoreStockBookList(list);
        return ResponseEntity.ok().build();
    }

    /** Bulk fetch canonical books by IDs — used by user-service for wishlist */
    @PostMapping("/internal/bulk-books")
    public ResponseEntity<List<BookPublicResponse>> getBulkBooks(@RequestBody List<Long> bookIds) {
        List<BookPublicResponse> response = bookService.getBulkBooks(bookIds);
        return ResponseEntity.ok(response);
    }

    // ===================== PRIVATE HELPERS =====================

    private UUID getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) auth.getPrincipal();
    }

    private boolean checkIsAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN")
        );
    }
}
