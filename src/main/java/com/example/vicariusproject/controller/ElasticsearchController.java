package com.example.vicariusproject.controller;

import com.example.vicariusproject.service.ElasticSearchService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/elasticsearch")
public class ElasticsearchController {

    private final ElasticSearchService elasticSearchService;

    @PostMapping("/createIndex/{indexName}")
    public String createIndex(@PathVariable String indexName) throws IOException {
        return elasticSearchService.createIndex(indexName);
    }

    @PostMapping("/add-document/{indexName}")
    public String addDocument(@PathVariable String indexName) throws IOException {
        return elasticSearchService.addDocument(indexName);
    }

    @GetMapping("/getDocument/{indexName}/{id}")
    public String getDocumentById(@PathVariable String indexName, @PathVariable String id) throws IOException {
        return elasticSearchService.getDocumentById(indexName, id);
    }
}
