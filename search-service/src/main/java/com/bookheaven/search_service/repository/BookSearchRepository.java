package com.bookheaven.search_service.repository;

import com.bookheaven.search_service.document.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {
    
    @Query("{\"bool\": {\"should\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"isbn^5\", \"title^3\", \"author^2\", \"category\", \"description\"]}}, {\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"author^2\", \"description\"], \"type\": \"phrase_prefix\"}}]}}")
    Page<BookDocument> searchEverything(String searchTerm, Pageable pageable);
}
