package com.example.hello.news.dto;

import lombok.*;

@Data
@NoArgsConstructor  // 매개변수들이 없는 생성자 코드를 생성
@AllArgsConstructor
@Builder
public class ArticleDTO extends Object{
    private SourceDTO source;
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String content;


}
