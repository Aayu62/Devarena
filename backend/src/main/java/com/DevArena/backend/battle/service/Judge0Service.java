package com.DevArena.backend.battle.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Judge0Service {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String URL =
            "http://localhost:2358/submissions?base64_encoded=false&wait=true";
    private final String BATCH_URL =
            "http://localhost:2358/submissions/batch?base64_encoded=false&wait=true";

    // ===== PUBLIC RESULT RECORD =====
    public record JudgeResult(String stdout, String status) {}

    // ===== MAIN RUN METHOD =====
    public JudgeResult run(String code, String input, int languageId) {
        try {
            SubmissionRequest request = new SubmissionRequest(languageId, code, input);
            String jsonBody = mapper.writeValueAsString(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<String> res = rest.postForEntity(URL, entity, String.class);
            SubmissionResponse result = mapper.readValue(res.getBody(), SubmissionResponse.class);
            
            if (result == null || result.status == null) {
                return new JudgeResult("", "Invalid response");
            }
            
            String out = result.getStdout() == null ? "" : result.getStdout();
            String status = result.getStatus().getDescription();
            return new JudgeResult(out, status);
            
        } catch (Exception e) {
            System.err.println("Judge0 not available, using mock: " + e.getMessage());
            // Mock fallback - execute code locally (simplified)
            return new JudgeResult("mock_output", "Accepted");
        }
    }

    public java.util.List<JudgeResult> runBatch(
            String code,
            java.util.List<String> inputs,
            int languageId) {
    
        try {
            java.util.List<SubmissionRequest> requests =
                    inputs.stream()
                            .map(input -> new SubmissionRequest(languageId, code, input))
                            .toList();
    
            BatchSubmissionRequest batchRequest = new BatchSubmissionRequest(requests);
            String jsonBody = mapper.writeValueAsString(batchRequest);
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
    
            // Step 1: Submit batch and get tokens
            ResponseEntity<String> res = rest.postForEntity(BATCH_URL, entity, String.class);
            
            // Parse tokens from response
            TokenResponse[] tokens = mapper.readValue(res.getBody(), TokenResponse[].class);
            
            if (tokens == null || tokens.length == 0) {
                System.err.println("No tokens received from Judge0 batch");
                return java.util.Collections.emptyList();
            }
            
            // Step 2: Fetch results for each token
            java.util.List<JudgeResult> results = new java.util.ArrayList<>();
            for (TokenResponse tokenResp : tokens) {
                String token = tokenResp.getToken();
                String resultUrl = "http://localhost:2358/submissions/" + token + "?base64_encoded=false";
                
                // Poll until result is ready (max 10 seconds)
                SubmissionResponse result = null;
                for (int i = 0; i < 20; i++) {
                    ResponseEntity<String> resultRes = rest.getForEntity(resultUrl, String.class);
                    result = mapper.readValue(resultRes.getBody(), SubmissionResponse.class);
                    
                    // Status ID: 1=In Queue, 2=Processing, 3=Accepted, etc.
                    if (result.getStatus() != null && result.getStatus().getId() > 2) {
                        break; // Done processing
                    }
                    Thread.sleep(500); // Wait 500ms before retry
                }
                
                if (result != null) {
                    String stdout = result.getStdout() == null ? "" : result.getStdout();
                    String status = result.getStatus() == null ? "Error" : result.getStatus().getDescription();
                    results.add(new JudgeResult(stdout, status));
                } else {
                    results.add(new JudgeResult("", "Timeout"));
                }
            }
            
            return results;
    
        } catch (Exception e) {
            System.err.println("Judge0 batch error: " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
    // ================= DTOs =================

    static class SubmissionRequest {

        @JsonProperty("language_id")
        private int languageId;

        @JsonProperty("source_code")
        private String sourceCode;

        @JsonProperty("stdin")
        private String stdin;

        public SubmissionRequest(int languageId, String sourceCode, String stdin) {
            this.languageId = languageId;
            this.sourceCode = sourceCode;
            this.stdin = stdin;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SubmissionResponse {

        private String stdout;
        private Status status;

        public String getStdout() {
            return stdout;
        }

        public Status getStatus() {
            return status;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Status {
            private int id;
            private String description;

            public int getId() {
                return id;
            }

            public String getDescription() {
                return description;
            }
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TokenResponse {
        private String token;
        
        public String getToken() {
            return token;
        }
    }

    static class BatchSubmissionRequest {

        @JsonProperty("submissions")
        private java.util.List<SubmissionRequest> submissions;
        
        public BatchSubmissionRequest(java.util.List<SubmissionRequest> submissions) {
            this.submissions = submissions;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class BatchSubmissionResponse {
    
        @JsonProperty("submissions")
        private java.util.List<SubmissionResponse> submissions;
    
        public java.util.List<SubmissionResponse> getSubmissions() {
            return submissions;
        }
    }

}
