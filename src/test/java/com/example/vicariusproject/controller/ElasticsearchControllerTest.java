package com.example.vicariusproject.controller;

import com.example.vicariusproject.model.request.DocumentRequest;
import com.example.vicariusproject.service.ElasticSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElasticsearchController.class)
public class ElasticsearchControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElasticSearchService elasticSearchService;

    @Test
    public void testCreateIndex_Success() throws Exception {
        String indexName = "testIndex";
        mockMvc.perform(post("/elasticsearch/create-index/{indexName}", indexName))
                .andExpect(status().isOk());

        verify(elasticSearchService, times(1)).createIndex(indexName);
    }

    @Test
    public void testAddDocument_Success() throws Exception {
        String indexName = "testIndex";
        DocumentRequest documentRequest = DocumentRequest.builder()
                .title("Sample Title")
                .text("Sample Text")
                .build();

        mockMvc.perform(post("/elasticsearch/add-document/{indexName}", indexName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentRequest)))
                .andExpect(status().isOk());

        verify(elasticSearchService, times(1)).addDocument(any(), any());
    }

    @Test
    public void testGetDocumentById_Success() throws Exception {
        String indexName = "testIndex";
        String documentId = "123";

        when(elasticSearchService.getDocumentById(indexName, documentId))
                .thenReturn("Document content");

        mockMvc.perform(get("/elasticsearch/get-document/{indexName}/{id}", indexName, documentId))
                .andExpect(status().isOk())
                .andExpect(content().string("Document content"));

        verify(elasticSearchService, times(1)).getDocumentById("testIndex", "123");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null",
            "''"
    }, nullValues={"null"})
    public void testCreateIndex_invalid_indexName(String indexName) throws Exception {
        mockMvc.perform(post("/elasticsearch/create-index/{indexName}", indexName))
                .andExpect(status().isNotFound());

        verifyNoInteractions(elasticSearchService);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null",
            "''"
    }, nullValues={"null"})
    public void testAddDocument_invalid_indexName(String indexName) throws Exception {
        DocumentRequest documentRequest = DocumentRequest.builder()
                .title("title")
                .text("text")
                .build();

        mockMvc.perform(post("/elasticsearch/add-document/{indexName}", indexName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(documentRequest)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(elasticSearchService);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, text",
            "'', text",
            "' ', text",
            "title, null",
            "title,''",
            "title,' '"
    }, nullValues={"null"})
    public void testAddDocument_invalid_documentRequest(String title, String text) throws Exception {
        DocumentRequest documentRequest = DocumentRequest.builder()
                .title(title)
                .text(text)
                .build();

        mockMvc.perform(post("/elasticsearch/add-document/{indexName}", "indexName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(elasticSearchService);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, 1",
            "'', 1",
            "indexName, null",
            "indexName,''"
    }, nullValues={"null"})
    public void testGetDocumentById_invalid_params(String indexName, String id) throws Exception {
        mockMvc.perform(get("/elasticsearch/get-document/{indexName}/{id}", indexName, id))
                .andExpect(status().isNotFound());

        verifyNoInteractions(elasticSearchService);
    }
}