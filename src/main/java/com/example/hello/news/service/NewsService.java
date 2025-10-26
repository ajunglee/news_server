package com.example.hello.news.service;

import com.example.hello.news.dto.*;
import com.example.hello.news.entity.Article;
import com.example.hello.news.entity.Category;
import com.example.hello.news.entity.Source;
import com.example.hello.news.repository.ArticleRepository;
import com.example.hello.news.repository.CategoryRepository;
import com.example.hello.news.repository.SourceRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsService {
    @Value("${newsapi.source_url}")
    private String sourceURL;

    @Value("${newsapi.article_url}")
    private String articleURL;

    @Value("${newsapi.apikey}")
    private String apiKey;

    private final CategoryRepository categoryRepository;
    private final SourceRepository sourceRepository;
    private final ArticleRepository articleRepository;

//    @Autowired
//    private CategoryRepository categoryRepository;

    public Page<ArticleDTO> getArticles(Pageable pageable) {
        // 페이징 리퀘스트를 발행일자로 내림차순 정렬하여 만든다.
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC,"publishedAt"));

        return articleRepository.findAll(sorted).map(Article::toDTO);
    }


    public NewsResponse getGeneral() throws URISyntaxException, IOException, InterruptedException {
        String url = "https://newsapi.org/v2/top-headlines?country=us&apiKey=739904b4b49c4cd1b425a7dc29361092";

        // client instance를 생성한다.
        HttpClient client = HttpClient.newBuilder().build();

        // request 인스턴스를 생성한다.(필수: url, method(요청방법))
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        // client에서 request를 보내고 response를 문자열 형태로 받아온다.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String resBody = response.body();

        Gson gson = new Gson();
        NewsResponse newsResponse = gson.fromJson(resBody, NewsResponse.class);
        System.out.println( newsResponse.getStatus() );

        return newsResponse;
    }


    public List<CategoryDTO> getCategories() {
        // categoryRepository.findAll() == select * from category; ==> fetch
        List<Category> categories = categoryRepository.findAll();

        // 비어있는 category dto 리스트 인스턴스를 생성한다
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        for(Category category : categories) {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(category.getId().toString());
            dto.setName(category.getName());
            dto.setMemo(category.getMemo());
            categoryDTOList.add( dto );
        }

        return categoryDTOList;
    }

    public String inputCategory(Category category) {
        if (category != null) {
            try {
                Category saved = categoryRepository.save(category);
                // saved.getName().equals(category.getName())
            } catch (Exception e) {
                return String.format("ERROR: %s", e.getMessage());
            }

            return "SUCCESS";
        }
        return "ERROR: 카테고리 정보가 없습니다.";
    }
    // @Transactional : 일괄처리 작업 /Transaction : 데이터베이스에 데이터를 입력하거나 데이터베이스에서 데이터를 가져올 때 처리하는 단위
    @Transactional
    public void inputSources() throws URISyntaxException, IOException, InterruptedException {
        String url = sourceURL+apiKey; //apikey는 개인정보라 따로 분리
        System.out.println(url); // source url

        // client instance를 생성한다.
        HttpClient client = HttpClient.newBuilder().build();

        // request 인스턴스를 생성한다.(필수: url, method(요청방법))
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        // client에서 request를 보내고 response를 문자열 형태로 받아온다.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String resBody = response.body();

        Gson gson = new Gson();
        SourceResponse sourceResponse = gson.fromJson(resBody, SourceResponse.class);

        System.out.println(sourceResponse.getStatus());
        System.out.println(sourceResponse.getSources().length);

        // sourceResponse에 있는 모든 SourceDTO 인스턴스의 데이터를 이용하여
        // Source Entity 인스턴스를 생성하고 데이터베이스에 저장한다.
        // SourceDTO ====> Source
        try{
            for(SourceDTO dto:sourceResponse.getSources()){
                // dto의 getName()을 호출하여 발행처 이름을 구하고
                // 발행처 이름으로 db에서 검색을 한 뒤 있으면 다음 데이터를 가져오도록 수정
                Optional<Source> srcOpt = sourceRepository.findByName(dto.getName());
                if(srcOpt.isPresent())
                    continue;

                Source source = new Source(); // 빈 Source Entity 인스턴스를 생성
                source.setSid(dto.getId());
                source.setName(dto.getName());
                source.setDescription(dto.getDescription());
                source.setUrl(dto.getUrl());
                source.setCategory(dto.getCategory());
                source.setLanguage(dto.getLanguage());
                source.setCountry(dto.getCountry());
                sourceRepository.save( source );
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    public Page<SourceDTO> getSources(Pageable pageable){
        // 데이터베이스로부터 Source Entity 리스트를 가져와서
        // 모든 Source Entity 인스턴스를 SourceDTO 인스턴스로 변환하여 반환한다.
        Page<Source> sources = sourceRepository.findAll(pageable);

        // for(Source source : sources){}
        // foreach : sources에서 값을 가져와서 source의 값을 변경, 반환하지 않는다.
        // map : sources에서 값을 가져와서 처리를 해서 다른 값으로 변환해서 내보내야할 때, 반환한다.

        // stream().foreach( Funtional Interface -> 익명 클래스 -> 람다식)
        // stream().map( Funtional Interface -> 익명 클래스 -> 람다식 )

        // map(source ->{
        // Source.toDTO(source)
        // )}
        // sources.stream().forEach(source -> System.out.println(source.getName()));

        // 람다식 source 매개변수 하나를 그냥 생략해서 Source::toDTO로 표시
        // entity를 dto로 변환해서 내보내야하기 때문에 map을 사용
        // DTO로 변환하고 List형태로 내보내기.
        // return sources.map(source -> {Source.toDTO(source)});
        return sources.map(Source::toDTO);
    }
    @Transactional
    public void updateCategory(String categoryId, String categoryName, String categoryMemo) {
        Category category = categoryRepository.findById(Long.parseLong(categoryId))
                .orElseThrow(()->new RuntimeException("카테고리를 찾을 수 없습니다."));

        category.setName(categoryName);
        category.setMemo(categoryMemo);

        categoryRepository.save( category );
    }
    @Transactional
    public void deleteCategory(String categoryId) {
        Category category = categoryRepository.findById(Long.parseLong(categoryId))
                .orElseThrow(()->new RuntimeException("카테고리를 찾을 수 없습니다."));
        try {
            categoryRepository.delete( category );
        } catch (Exception e) {
            throw new RuntimeException("카테고리 데이터 삭제중에 오류가 발생했습니다.");
        }

    }

    public HashMap<String, Long> getRecordCount(){
        HashMap<String, Long> counts = new HashMap<>();
        counts.put("articles", articleRepository.count());
        counts.put("sources", sourceRepository.count());
        counts.put("categories", categoryRepository.count());

        return counts;
    }
    // http://localhost:8090/admin/inputArticles?category=business --> AdminController(/inputArticle) -> NewsService.inputArticle
    // ?category=business : @Getmapping이기때문에 ? 사용
//    @Transactional
//    public void inputArticles(String category) throws URISyntaxException, IOException, InterruptedException, RuntimeException {
//        String url = String.format("%scategory=%s&%s", articleURL,category,apiKey);
//        System.out.println( url );
//        // https://newsapi.org/v2/top-headlines?country=us&category=business&apiKey=e8b002a0895a4ca4b96195b2690ba307
//
//        // client instance를 생성한다.
//        HttpClient client = HttpClient.newBuilder().build();
//
//        // request 인스턴스를 생성한다.(필수: url, method(요청방법))
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI(url))
//                .GET()
//                .build();
//
//        // client에서 request를 보내고 response를 문자열 형태로 받아온다.
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        String resBody = response.body();
//
//        Gson gson = new Gson();
//        NewsResponse newsResponse = gson.fromJson(resBody, NewsResponse.class);
//        // NewsResponse 안에 articleDTO가 있어서..
//        System.out.println(newsResponse.getStatus());
//        System.out.println(newsResponse.getTotalResults());
//        // 20250927 추가
//        System.out.println(newsResponse.getArticles()[0].getAuthor());
//
//        saveArticles(newsResponse, category);
//
//    }
//
//    public void saveArticles(NewsResponse newsResponse, String category){
//
//        try {
//            for(ArticleDTO article : newsResponse.getArticles()){
//
//                // 이미 기존에 입력되어 있는 source가 있다면 DB에서 찾아서 인스턴스를 만들고
//                // Source인스턴스를 포장해둠.. 내용이 Null인 경우(null pointer exception) 안전하게 처리하기 위해
//                Optional<Source> srcOpt = sourceRepository.findByName(article.getSource().getName());
//
//                // 없으면 새로 생성(srcOpt안에 인스턴스의 값이 null임. id가 Null값이 된 것도 있고 newyorkpost를 못가져오는 경우도 있음..)
//                // Optional에서만 orElseGet 사용 가능.
//                Source src = srcOpt.orElseGet( () ->{
//                    Source s1 = new Source();
//                    s1.setName(article.getSource().getName());
//                    return sourceRepository.save(s1);
//                });
//
//                Optional<Category> catOpt = categoryRepository.findByName(category);
//                Category cat = catOpt.orElseGet( () ->{
//                    Category c = new Category();
//                    c.setName(category);
//                    return categoryRepository.save(c);
//                });
//
//                Article article1 = Article.fromDTO(article, src, cat);
//                articleRepository.save(article1);
//
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
