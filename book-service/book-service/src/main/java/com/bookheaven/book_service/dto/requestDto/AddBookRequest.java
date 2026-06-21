package com.bookheaven.book_service.dto.requestDto;

import lombok.Data;

@Data
public class AddBookRequest {

    private String title;
    private String author;
    private String description;
    private String category;
    private String img;
    private int copies;   // required for creation
    private String isbn;
    private double price;
    private String currency;
}