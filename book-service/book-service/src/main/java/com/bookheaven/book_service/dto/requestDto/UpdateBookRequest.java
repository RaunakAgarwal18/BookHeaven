package com.bookheaven.book_service.dto.requestDto;

import lombok.Data;

@Data
public class UpdateBookRequest {
    private String title;
    private String author;
    private String description;
    private String category;
    private String img;
    private String isbn;
}