package com.example.vicariusproject.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
import com.example.vicariusproject.model.dto.Document;
import com.example.vicariusproject.model.request.DocumentRequest;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ElasticSearchServiceTest {

    @MockBean
    private ElasticsearchClient esClient;

    @MockBean
    private ElasticsearchIndicesClient elasticsearchIndicesClient;

    @SpyBean
    private ElasticSearchService elasticSearchService;

    @Test
    public void testCreateIndex_success() throws IOException {
        String indexName = "testIndexName";
        CreateIndexResponse createIndexResponse = CreateIndexResponse.of(r -> r.index("generatedIndex")
                .shardsAcknowledged(true)
                .acknowledged(true));

        when(esClient.indices()).thenReturn(elasticsearchIndicesClient);
        when(elasticsearchIndicesClient.create(any(CreateIndexRequest.class))).thenReturn(createIndexResponse);

        String result = elasticSearchService.createIndex(indexName);

        verify(esClient.indices(), times(1)).create(any(CreateIndexRequest.class));
        assertEquals("Index created name: testIndexName id: generatedIndex", result);
    }

    @Test
    public void testCreateIndex_failure() throws IOException {
        String indexName = "testIndex";
        when(esClient.indices()).thenReturn(elasticsearchIndicesClient);
        when(elasticsearchIndicesClient.create(any(CreateIndexRequest.class))).thenThrow(new IOException());

        String result = elasticSearchService.createIndex(indexName);

        verify(esClient.indices(), times(1)).create(any(CreateIndexRequest.class));
        assertEquals("Index creation testIndex was interrupted", result);
    }

    @Test
    public void testAddDocument_Success() throws IOException {
        String indexName = "testIndex";
        DocumentRequest documentRequest = DocumentRequest.builder()
                .title("Title")
                .text("Text")
                .build();
        IndexResponse indexResponse = IndexResponse.of(r -> r.index("generatedIndex")
                .id("generatedDocumentId")
                .primaryTerm(1L)
                .result(Result.Created)
                .seqNo(1L)
                .shards(ShardStatistics.of(s -> s.successful(1)
                        .failed(0)
                        .total(1)))
                .version(1));

        when(esClient.index(any(IndexRequest.class))).thenReturn(indexResponse);
        when(esClient.indices()).thenReturn(elasticsearchIndicesClient);

        String result = elasticSearchService.addDocument(indexName, documentRequest);

        verify(esClient, times(1)).index(any(IndexRequest.class));
        verify(esClient.indices(), times(1)).refresh(any(RefreshRequest.class));
        assertEquals("Document: id generatedDocumentId was added to index testIndex", result);
    }

    @Test
    public void testAddDocument_Failure() throws IOException {
        String indexName = "testIndex";
        DocumentRequest documentRequest = DocumentRequest.builder()
                .title("Title")
                .text("Text")
                .build();

        when(esClient.index(any(IndexRequest.class))).thenThrow(new IOException());
        when(esClient.indices()).thenReturn(elasticsearchIndicesClient);

        String result = elasticSearchService.addDocument(indexName, documentRequest);

        verify(esClient, times(1)).index(any(IndexRequest.class));
        verify(esClient.indices(), never()).refresh(any(RefreshRequest.class));
        assertEquals("Adding document to index testIndex was interrupted", result);
    }

    @Test
    public void testGetDocumentById_Found() throws IOException {
        String indexName = "testIndex";
        String documentId = "documentId";
        Document document = new Document("FoundTitle", "FoundText");
        GetResponse<Document> getResponse = GetResponse.of(r -> r.index("testIndex")
                .id("documentId")
                .source(document)
                .found(true));

        when(esClient.get(any(GetRequest.class), eq(Document.class))).thenReturn(getResponse);

        String result = elasticSearchService.getDocumentById(indexName, documentId);

        verify(esClient, times(1)).get(any(GetRequest.class), eq(Document.class));
        assertEquals(document.toString(), result);
    }

    @Test
    public void testGetDocumentById_NotFound() throws IOException {
        String indexName = "testIndex";
        String documentId = "documentId";
        Document document = new Document("FoundTitle", "FoundText");
        GetResponse<Document> getResponse = GetResponse.of(r -> r.index("testIndex")
                .id("documentId")
                .source(document)
                .found(false));

        when(esClient.get(any(GetRequest.class), eq(Document.class))).thenReturn(getResponse);

        String result = elasticSearchService.getDocumentById(indexName, documentId);

        verify(esClient, times(1)).get(any(GetRequest.class), eq(Document.class));
        assertEquals("No document found by this id documentId", result);
    }

    @Test
    public void testGetDocumentById_Exception() throws IOException {
        String indexName = "testIndex";
        String documentId = "documentId";
        when(esClient.get(any(GetRequest.class), eq(Document.class))).thenThrow(new IOException());

        String result = elasticSearchService.getDocumentById(indexName, documentId);

        verify(esClient, times(1)).get(any(GetRequest.class), eq(Document.class));
        assertEquals("Document or index was corrupted", result);
    }
}