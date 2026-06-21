package com.bookheaven.book_service.producer;

import com.bookheaven.book_service.dto.responseDto.BookPublicResponse;
import com.bookheaven.book_service.entity.Book;
import com.bookheaven.book_service.entity.SellerListing;
import com.bookheaven.book_service.repository.BookRepository;
import com.bookheaven.book_service.repository.SellerListingRepository;
import com.bookheaven.book_service.util.AppUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final BookRepository bookRepository;
    private final SellerListingRepository sellerListingRepository;

    public void publishBookUpdate(Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            log.warn("Cannot publish book update: Book with id {} not found", bookId);
            return;
        }

        List<SellerListing> listings = sellerListingRepository.findByBookIdOrderByPriceAsc(bookId);
        
        SellerListing cheapest = listings.stream()
                .filter(l -> l.getCopiesAvailable() > 0)
                .min(Comparator.comparingDouble(SellerListing::getPrice))
                .orElse(null);

        BookPublicResponse eventDto = AppUtil.toPublicResponse(book, cheapest);
        
        int totalCopies = listings.stream().mapToInt(SellerListing::getCopiesAvailable).sum();
        eventDto.setTotalCopiesAvailable(totalCopies);

        rabbitTemplate.convertAndSend("book.event.exchange", "book.updated", eventDto);
        log.info("Published book update to RabbitMQ for bookId: {}", bookId);
    }
}
