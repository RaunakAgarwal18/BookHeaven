package com.bookheaven.search_service.consumer;

import com.bookheaven.search_service.document.BookDocument;
import com.bookheaven.search_service.repository.BookSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookEventConsumer {

    private final BookSearchRepository searchRepository;

    @RabbitListener(queues = "book.search.queue")
    public void consumeBookUpdateEvent(BookEventDto bookEvent) {
        log.info("Received book event for id: {}", bookEvent.getBookId());
        try {
            BookDocument document = BookDocument.builder()
                    .id(String.valueOf(bookEvent.getBookId()))
                    .title(bookEvent.getTitle())
                    .author(bookEvent.getAuthor())
                    .description(bookEvent.getDescription()) 
                    .category(bookEvent.getCategory())
                    .isbn(bookEvent.getIsbn())
                    .img(bookEvent.getImg())
                    .cheapestListingId(bookEvent.getCheapestListingId())
                    .lowestPrice(bookEvent.getLowestPrice())
                    .lowestCurrency(bookEvent.getLowestCurrency())
                    .totalCopiesAvailable(bookEvent.getTotalCopiesAvailable())
                    .averageRating(bookEvent.getAverageRating())
                    .totalReviews(bookEvent.getTotalReviews())
                    .build();
                    
            searchRepository.save(document);
            log.info("Successfully synced book {} to Elasticsearch", bookEvent.getBookId());
        } catch (Exception e) {
            log.error("Failed to sync book to Elasticsearch: ", e);
        }
    }
}
