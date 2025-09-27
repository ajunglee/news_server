package com.example.hello.news.service;

import com.example.hello.news.dto.CategoryDTO;
import com.example.hello.news.dto.NewsResponse;
import com.example.hello.news.dto.SourceDTO;
import com.example.hello.news.dto.SourceResponse;
import com.example.hello.news.entity.Category;
import com.example.hello.news.entity.Source;
import com.example.hello.news.repository.CategoryRepository;
import com.example.hello.news.repository.SourceRepository;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

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

//    @Autowired
//    private CategoryRepository categoryRepository;


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

    public List<SourceDTO> getSources(){
        // 데이터베이스로부터 Source Entity 리스트를 가져와서
        // 모든 Source Entity 인스턴스를 SourceDTO 인스턴스로 변환하여 반환한다.
        List<Source> sources = sourceRepository.findAll();

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
        return sources.stream().map(Source::toDTO).toList();
    }

    // http://localhost:8090/admin/inputArticles?category=business --> AdminController(/inputArticle) -> NewsService.inputArticle
    // ?category=business : @Getmapping이기때문에 ? 사용
    public void inputArticles(String category) throws URISyntaxException, IOException, InterruptedException {
        String url = String.format("%scategory=%s&%s", articleURL,category,apiKey);
        System.out.println( url );
        // https://newsapi.org/v2/top-headlines?country=us&category=business&apiKey=e8b002a0895a4ca4b96195b2690ba307

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
        // NewsResponse 안에 articleDTO가 있어서..
        System.out.println(newsResponse.getStatus());
        System.out.println(newsResponse.getTotalResults());

    }
}
