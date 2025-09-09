package com.fkhrayef.motor.DTOout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    // Core response fields (from FastAPI)
    private String answer;
    private List<Map<String, Object>> context;
    private List<Map<String, Object>> sources;
    private Map<String, Object> metadata;
    
    // API response fields (for Spring Boot API)
    private String document_name;
    private List<String> source_pages;
    
    // Method to extract page numbers from sources
    public void extractSourcePages() {
        if (sources != null) {
            this.source_pages = sources.stream()
                    .map(source -> "Page " + source.get("page_number"))
                    .distinct()
                    .toList();
        }
    }
}
