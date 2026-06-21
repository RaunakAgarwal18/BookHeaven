package com.bookheaven.search_service.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDocument {
    @Id
    @JsonProperty("bookId")
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String author;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword) 
    private String category;
    
    @Field(type = FieldType.Keyword) 
    private String isbn;
    
    @Field(type = FieldType.Text)
    private String img;

    @Field(type = FieldType.Double)
    private Double lowestPrice;

    @Field(type = FieldType.Keyword)
    private String lowestCurrency;

    @Field(type = FieldType.Integer)
    private Integer totalCopiesAvailable;

    @Field(type = FieldType.Double)
    private Double averageRating;
    
    @Field(type = FieldType.Integer)
    private Integer totalReviews;
}
