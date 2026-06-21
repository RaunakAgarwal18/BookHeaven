package com.bookheaven.search_service.controller;

import com.bookheaven.search_service.document.BookDocument;
import com.bookheaven.search_service.repository.BookSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final BookSearchRepository searchRepository;

    @GetMapping
    public Map<String, Object> searchBooks(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
            
        Page<BookDocument> page = searchRepository.searchEverything(q, PageRequest.of(pageNumber, pageSize));
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        return response;
    }
}
