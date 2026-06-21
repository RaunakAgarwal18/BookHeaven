package com.bookheaven.book_service.repository;

import com.bookheaven.book_service.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    // Homepage: only books that have at least one listing with available copies
    @Query("SELECT b FROM Book b WHERE EXISTS (SELECT 1 FROM SellerListing sl WHERE sl.book = b AND sl.copiesAvailable > 0)")
    Page<Book> findBooksWithStock(Pageable pageable);

    // Search by title (books with any listing, in or out of stock)
    @Query("SELECT b FROM Book b WHERE EXISTS (SELECT 1 FROM SellerListing sl WHERE sl.book = b) AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findByTitleContaining(String title, Pageable pageable);

    // Search by author
    @Query("SELECT b FROM Book b WHERE EXISTS (SELECT 1 FROM SellerListing sl WHERE sl.book = b) AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    Page<Book> findByAuthorContaining(String author, Pageable pageable);

    // Search by category
    @Query("SELECT b FROM Book b WHERE EXISTS (SELECT 1 FROM SellerListing sl WHERE sl.book = b) AND LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%'))")
    Page<Book> findByCategoryContaining(String category, Pageable pageable);

    // Search by isbn
    @Query("SELECT b FROM Book b WHERE EXISTS (SELECT 1 FROM SellerListing sl WHERE sl.book = b) AND b.isbn = :isbn")
    Page<Book> findByIsbnWithListings(String isbn, Pageable pageable);
}
