package com.example.hello.news.entity;

import com.example.hello.news.dto.ArticleDTO;
import com.example.hello.news.dto.SourceDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="article") // mySQL에서 사용한 테이블명
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_id",foreignKey = @ForeignKey(name = "article_ibfk_1"))
    private Source source; //Source : entity, source : 외부키 이름

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id",foreignKey = @ForeignKey(name = "article_ibfk_2"))
    private Category category;

    @Column(length = 150)
    private String author;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String url;

    @Column(name = "url_to_image", length = 500)
    private String urlToImage;

    @Column(name = "published_at", length = 100)
    private String publishedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name="created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", insertable = false)
    private LocalDateTime updatedAt;
    
    // ArticleDTO를 이용하여 Article(Entity)를 생성하여 반환하는 함수
    public static Article fromDTO(ArticleDTO dto, Source src, Category cat){
        Article article = new Article();

        article.setSource(src);
        article.setCategory(cat);

        article.setAuthor(dto.getAuthor());
        article.setTitle(dto.getTitle());
        article.setDescription(dto.getDescription());
        article.setUrl(dto.getUrl());
        article.setUrlToImage(dto.getUrlToImage());
        article.setPublishedAt(dto.getPublishedAt());
        article.setContent(dto.getContent());

        return article;
    }

    public static ArticleDTO toDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        dto.setAuthor(article.getAuthor());
        dto.setTitle(article.getTitle());
        dto.setUrl(article.getUrl());
        dto.setDescription(article.getDescription());
        dto.setUrlToImage(article.getUrlToImage());
        dto.setPublishedAt(article.getPublishedAt());
        dto.setContent(article.getContent());

        SourceDTO srcDTO = Source.toDTO(article.getSource());
        dto.setSource(srcDTO);

        return dto;
    }

}
