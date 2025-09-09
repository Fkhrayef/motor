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
public class DocumentsInfoResponse {
    private int total_documents;
    private List<String> document_names;
    private Map<String, Integer> documents_count;
    private int vectorstores_ready;
    private int rag_chains_ready;
    private boolean s3_enabled;
}
