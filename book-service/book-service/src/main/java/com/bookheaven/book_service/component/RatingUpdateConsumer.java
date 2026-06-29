package com.bookheaven.book_service.component;

import com.bookheaven.common.dto.event.BookRatingUpdateEvent;
import com.bookheaven.book_service.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingUpdateConsumer {

    private final BookService bookService;

    @RabbitListener(queues = "book.rating.update.queue")
    public void consumeRatingUpdate(BookRatingUpdateEvent event) {
        log.info("Received rating update event for bookId: {}", event.getBookId());
        try {
            bookService.updateBookRating(event.getBookId(), event.getAverageRating(), event.getTotalReviews());
            log.info("Successfully processed rating update for bookId: {}", event.getBookId());
        } catch (Exception e) {
            log.error("Failed to update rating for bookId: {}", event.getBookId(), e);
        }
    }
}
