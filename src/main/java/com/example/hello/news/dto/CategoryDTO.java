package com.example.hello.news.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// POJO : Plain Old Java Object

@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {
    private String id;
    private String name;
    private String memo;
}
