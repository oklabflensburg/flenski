package com.flenski.queryTransformers;

import com.flenski.value.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service
public class DateRangeTransformer {

    private static final Logger logger = LoggerFactory.getLogger(DateRangeTransformer.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
            Analysiere, ob die Anfrage eine Datumsangabe oder einen Datumsbereich enthält. 
            Wenn ja, extrahiere das Datum/den Bereich.
            Versuche eine von und bis Angabe zu erkennen, Wenn zum Beispiel nach Januar gefragt wird setze den ersten und letzten Tag des Monats.
            Wenn relative Zeiträume gefragt werden wie letzten monat, letzte woche, gestern, heute, beziehe das auf das aktuelle datum.
            das aktuelle datum lautet: {currentDate}.   
            Wenn kein Jahr angegeben ist, ergänze das jahr in Bezug auf das aktuelle datum.
            Das Datum liegt immer in der Vergangenheit.
            
            Gib ein JSON-Objekt zurück mit den Feldern 'startDate' und 'endDate' im RFC 3339 Format (JJJJ-MM-TT oder JJJJ-MM-TTTHH:MM:SSZ). Wenn nur ein Datum vorhanden ist, setze 'endDate' auf null. Gebe nur das reine JSON zurück, keinen weiteren Text
            
            Anfrage: {query}
            """);

    public static class DateRangeResult {
        public String startDate; // RFC 3339
        public String endDate;   // RFC 3339

        public DateRangeResult(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public DateRangeTransformer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public DateRange transform(String message) {
        Query query = new Query(message);
        return transform(query);
    }

    public DateRange transform(Query query) {
        logger.info("DateRangeTransformer called.");
        java.time.LocalDate now = java.time.LocalDate.now();
        String currentDate = now.toString();
        String currentYear = String.valueOf(now.getYear());
        String result;
        try {
            result = this.chatClient.prompt()
                    .user(user -> user.text(DateRangeTransformer.DEFAULT_PROMPT_TEMPLATE.getTemplate())
                            .param("query", query.text())
                            .param("currentDate", currentDate)
                    )
                    .call()
                    .content();
        } catch (HttpClientErrorException httpException) {
            if (httpException.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("DateRangeTransformer unauthorized. Falling back to null range.", httpException);
                return new DateRange(null, null);
            }
            throw httpException;
        } catch (RestClientException restClientException) {
            logger.warn("DateRangeTransformer failed due to chat client error. Returning null range.", restClientException);
            return new DateRange(null, null);
        } catch (RuntimeException runtimeException) {
            logger.warn("DateRangeTransformer failed unexpectedly. Returning null range.", runtimeException);
            return new DateRange(null, null);
        }
        try {
            if (result == null) {
                logger.warn("Result from chatClient was null");
                return new DateRange(null, null);
            }
            String json = result.trim();
            if (!json.startsWith("{")) {
                int start = json.indexOf('{');
                int end = json.lastIndexOf('}');
                if (start != -1 && end != -1 && end > start) {
                    json = json.substring(start, end + 1);
                }
            }
            logger.info("Parsed result: {}", json);
            JsonNode node = objectMapper.readTree(json);
            String startDate = node.has("startDate") ? node.get("startDate").asText(null) : null;
            String endDate = node.has("endDate") ? node.get("endDate").asText(null) : null;
            if (startDate != null && startDate.isBlank()) startDate = null;
            if (endDate != null && endDate.isBlank()) endDate = null;
            return new DateRange(startDate, endDate);
        } catch (Exception e) {
            logger.warn("Could not parse date range result: {}", result, e);
            return new DateRange(null, null);
        }
    }
}
