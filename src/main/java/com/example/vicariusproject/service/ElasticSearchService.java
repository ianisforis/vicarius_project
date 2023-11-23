package com.example.vicariusproject.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.example.vicariusproject.model.dto.Document;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticSearchService {

    private final ElasticsearchClient esClient;

    public String createIndex(String indexName) {
        CreateIndexResponse response;

        try {
            response = esClient
                    .indices()
                    .create(c -> c
                            .index(indexName)
                    );
        } catch (IOException e) {
            log.error("Index creation {} was interrupted", indexName);
            return "Index creation " + indexName + " was interrupted";
        }

        log.info ("Index created name {} id {}", indexName, response.index());
        return "Index created name: " + indexName + " id: " + response.index();
    }

    public String addDocument(String indexName) {
        IndexResponse response;
        try {
            response = esClient
                    .index(i -> i
                            .index(indexName)
                            .document(new Document("Story", "Once upon a time")));

            // Refresh the index to make the document available for search immediately
            esClient
                    .indices()
                    .refresh(refresh -> refresh.index(indexName));
        } catch (IOException e) {
            log.error("Adding document to index {} was interrupted", indexName);
            return "Adding document to index " + indexName + " was interrupted";
        }

        log.info ("Document: id {} added to index {} ", response.id(), indexName);
        return "Document: id " + response.id() + " added to index " + indexName;
    }

    public String getDocumentById(String indexName, String id) {
        GetResponse<Document> response;

        try {
            response = esClient.get(g -> g
                    .index(indexName)
                    .id(id), Document.class);
        } catch (IOException e) {
            log.error("Document or index was corrupted", e);
            return "Document or index was corrupted";
        }

        if (response.found()) {
            Document document = response.source();
            if (nonNull(document)) {
                log.info("Document title: {} text: {} was found successfully", document.title(), document.text());
                return document.toString();
            } else {
                log.info("Document with id {} for indexName {} not found", id, indexName);
            }
        }

        return "No document by this id " + id;
    }
}
