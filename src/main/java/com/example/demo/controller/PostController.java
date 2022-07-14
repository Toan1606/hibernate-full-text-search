package com.example.demo.controller;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;

@RestController
@RequestMapping(value = "/post")
public class PostController {
	@Autowired
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @GetMapping(value = "/search")
    public List<Post> fullTextSearch(@RequestParam(value = "searchKey") String searchKey) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Post.class)
                .get();

        org.apache.lucene.search.Query query = queryBuilder
                .keyword()
                .onFields("title", "content")
                .matching(searchKey)
                .createQuery();

        org.hibernate.search.jpa.FullTextQuery jpaQuery
                = fullTextEntityManager.createFullTextQuery(query, Post.class);

        return jpaQuery.getResultList();

    }

    @GetMapping(value = "/insert")
    public ResponseEntity<String> insert(){
        RestTemplate restTemplate = new RestTemplate();
        String fakeApiUrl = "https://jsonplaceholder.typicode.com/posts";

        ResponseEntity<String> responseEntity = restTemplate.exchange(fakeApiUrl, HttpMethod.GET, null, String.class);

        JSONArray jsonArray = new JSONArray(Objects.requireNonNull(responseEntity.getBody()));
        int length = jsonArray.length();

        for (int i = 0; i < length; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Post post = new Post();
            post.setTitle(jsonObject.getString("title"));
            post.setContent(jsonObject.getString("body"));
            postRepository.save(post);
        }

        return ResponseEntity.ok("success");
    }
}
