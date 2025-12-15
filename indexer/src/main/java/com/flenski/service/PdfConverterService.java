package com.flenski.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.flenski.dto.DocumentDto;
import com.flenski.type.SourceType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfConverterService {

    private final HttpClient httpClient;

    public PdfConverterService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Async
    public CompletableFuture<DocumentDto> convertPdfToDocument(String fileUrl) throws RuntimeException {
        try {
            log.info("Starting PDF processing for URL: {}", fileUrl);
            InputStream pdfInputStream = downloadPdf(fileUrl);
            String extractedText = extractTextFromPdf(pdfInputStream);
            DocumentDto documentDto = createDocument(fileUrl, extractedText);
            log.info("Successfully processed PDF from URL: {}", fileUrl);
            return CompletableFuture.completedFuture(documentDto);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            log.error("Error processing PDF from URL: {}", fileUrl, e);
            throw new RuntimeException("Failed to process PDF from URL: " + fileUrl, e);
        }
    }

    
    private InputStream downloadPdf(String fileUrl) throws IOException, InterruptedException, URISyntaxException {
        log.debug("Downloading PDF from: {}", fileUrl);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(fileUrl))
                .timeout(Duration.ofMinutes(5))
                .header("User-Agent", "Flenski-Indexer/1.0")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download PDF. HTTP Status: " + response.statusCode());
        }
        
        log.debug("Successfully downloaded PDF from: {}", fileUrl);
        return response.body();
    }

    private String extractTextFromPdf(InputStream pdfInputStream) throws IOException {
        log.debug("Extracting text from PDF");
        
        try {
            InputStreamResource inputStreamResource = new InputStreamResource(pdfInputStream);
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(inputStreamResource);
            
            List<Document> documents = pdfReader.get();
            
            StringBuilder extractedText = new StringBuilder();
            for (Document document : documents) {
                extractedText.append(document.getText()).append("\n");
            }
            
            String result = extractedText.toString().trim();
            log.debug("Successfully extracted {} characters of text from PDF", result.length());
            return result;
            
        } finally {
            try {
                pdfInputStream.close();
            } catch (IOException e) {
                log.warn("Error closing PDF input stream", e);
            }
        }
    }

    private DocumentDto createDocument(String fileUrl, String extractedText) {
        log.debug("Creating record for URL: {}", fileUrl);
        
        DocumentDto documentDto = new DocumentDto();
        documentDto.setIdentifier(generateSourceIdentifier(fileUrl));
        documentDto.setName(extractFileNameFromUrl(fileUrl));
        documentDto.setUrl(fileUrl);

        //TODO: Replace with date from PDF metadata if available
        documentDto.setSourceDateTime(Instant.now()); 
        
        documentDto.setDiscoveryDateTime(Instant.now());
        documentDto.setType(SourceType.PDF);
        documentDto.setContent(extractedText);
        
        log.debug("Created record with {} characters of content", extractedText.length());
        return documentDto;
    }

    private String generateSourceIdentifier(String fileUrl) {
        return "pdf_" + UUID.nameUUIDFromBytes(fileUrl.getBytes()).toString();
    }

    private String extractFileNameFromUrl(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            log.warn("Could not extract filename from URL: {}", fileUrl);
            return "unknown pdf";
        }
    }
}
