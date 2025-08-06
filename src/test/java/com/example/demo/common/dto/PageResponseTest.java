package com.example.demo.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PageResponse class.
 */
@SpringBootTest
class PageResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateFromSpringDataPage() {
        // Given
        List<String> content = List.of("item1", "item2", "item3");
        Pageable pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageable, 25);
        
        // When
        PageResponse<String> response = PageResponse.of(page);
        
        // Then
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isFalse();
        assertThat(response.getNumberOfElements()).isEqualTo(3);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    void testCreateFromCustomParameters() {
        // Given
        List<String> content = List.of("item1", "item2");
        
        // When
        PageResponse<String> response = PageResponse.of(content, 1, 5, 12, 3);
        
        // Then
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(12);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isTrue();
        assertThat(response.getNumberOfElements()).isEqualTo(2);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    void testFirstPage() {
        // Given
        List<String> content = List.of("item1", "item2");
        
        // When
        PageResponse<String> response = PageResponse.of(content, 0, 5, 10, 2);
        
        // Then
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isFalse();
    }

    @Test
    void testLastPage() {
        // Given
        List<String> content = List.of("item1", "item2");
        
        // When
        PageResponse<String> response = PageResponse.of(content, 1, 5, 7, 2);
        
        // Then
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isTrue();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.isHasPrevious()).isTrue();
    }

    @Test
    void testSinglePage() {
        // Given
        List<String> content = List.of("item1", "item2");
        
        // When
        PageResponse<String> response = PageResponse.of(content, 0, 5, 2, 1);
        
        // Then
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.isHasPrevious()).isFalse();
    }

    @Test
    void testEmptyPage() {
        // Given
        List<String> content = List.of();
        
        // When
        PageResponse<String> response = PageResponse.of(content, 0, 5, 0, 0);
        
        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.isEmpty()).isTrue();
        assertThat(response.getNumberOfElements()).isEqualTo(0);
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testJsonSerialization() throws Exception {
        // Given
        List<String> content = List.of("item1", "item2");
        PageResponse<String> response = PageResponse.of(content, 0, 5, 10, 2);
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then
        assertThat(json).contains("\"content\":[\"item1\",\"item2\"]");
        assertThat(json).contains("\"page\":0");
        assertThat(json).contains("\"size\":5");
        assertThat(json).contains("\"totalElements\":10");
        assertThat(json).contains("\"totalPages\":2");
        assertThat(json).contains("\"first\":true");
        assertThat(json).contains("\"last\":false");
        assertThat(json).contains("\"hasNext\":true");
        assertThat(json).contains("\"hasPrevious\":false");
        assertThat(json).contains("\"numberOfElements\":2");
        assertThat(json).contains("\"empty\":false");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = """
            {
                "content": ["item1", "item2"],
                "page": 1,
                "size": 5,
                "totalElements": 15,
                "totalPages": 3,
                "first": false,
                "last": false,
                "hasNext": true,
                "hasPrevious": true,
                "numberOfElements": 2,
                "empty": false
            }
            """;
        
        // When
        PageResponse<?> response = objectMapper.readValue(json, PageResponse.class);
        
        // Then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(15);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isTrue();
        assertThat(response.getNumberOfElements()).isEqualTo(2);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    void testBuilderPattern() {
        // Given & When
        PageResponse<String> response = PageResponse.<String>builder()
                .content(List.of("test"))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(1)
                .empty(false)
                .build();
        
        // Then
        assertThat(response.getContent()).containsExactly("test");
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.isHasPrevious()).isFalse();
        assertThat(response.getNumberOfElements()).isEqualTo(1);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        List<String> content = List.of("item1", "item2");
        PageResponse<String> response1 = PageResponse.of(content, 0, 5, 10, 2);
        PageResponse<String> response2 = PageResponse.of(content, 0, 5, 10, 2);
        
        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        List<String> content = List.of("item1");
        PageResponse<String> response = PageResponse.of(content, 0, 5, 1, 1);
        
        // When
        String toString = response.toString();
        
        // Then
        assertThat(toString).contains("PageResponse");
        assertThat(toString).contains("content=[item1]");
        assertThat(toString).contains("page=0");
        assertThat(toString).contains("totalElements=1");
    }

    @Test
    void testJsonIncludeNonNull() throws Exception {
        // Given
        PageResponse<String> response = PageResponse.<String>builder()
                .content(List.of("test"))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then - null fields should not be included in JSON
        assertThat(json).contains("\"content\":");
        assertThat(json).contains("\"page\":");
        assertThat(json).contains("\"size\":");
        assertThat(json).contains("\"totalElements\":");
        assertThat(json).contains("\"totalPages\":");
    }
}