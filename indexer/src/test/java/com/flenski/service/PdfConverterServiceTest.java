package com.flenski.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.flenski.dto.DocumentDto;

class PdfConverterServiceTest {

    @Test
    void testConvertPdfToDocument() {
            try {
                PdfConverterService pdfConverterService = new PdfConverterService();
                String testPdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
                DocumentDto result = pdfConverterService.convertPdfToDocument(testPdfUrl).get();
                assertNotNull(result);
                assertEquals(testPdfUrl, result.getUrl());
                assertNotNull(result.getContent());
                assertTrue(result.getContent().length() > 0);
            } catch (Exception e) {
                e.printStackTrace();
                org.junit.jupiter.api.Assertions.fail("Exception occurred: " + e.getMessage());
            }
    }
}
