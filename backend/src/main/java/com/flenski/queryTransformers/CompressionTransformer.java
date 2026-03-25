package com.flenski.queryTransformers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service

public class CompressionTransformer {

    private static final Logger logger = LoggerFactory.getLogger(TranslationTransformer.class);
    private final ChatClient.Builder chatClientBuilder;
    private final ChatClient chatClient;

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
			
            Aufgabe: Komprimiere die Nutzeranfrage für Vektorensuche maximal, ohne Bedeutung zu ändern.
                     Regeln: Entferne Füllwörter, Höflichkeit, Wiederholungen, allgemeine Phrasen.
                     Behalte unbedingt: Kernintention, Schlüsselbegriffe, Named Entities (Person/Ort/Firma/Produkt), Fachbegriffe, Zahlen/Datum/Zeit, Maßeinheiten, Bedingungen, Negationen (nicht/kein/ohne), Einschränkungen (nur/außer/bis/ab).
                     Verboten: Keine neuen Infos, keine Synonyme/Paraphrasen, keine Interpretation. Unklare/seltene Wörter behalten.
                     Ausgabe: Nur die komprimierte schlüsselwörter, eine Zeile, keine Erklärungen.

			Original Anfrage: {query}
			""");

    public CompressionTransformer(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
        this.chatClient = chatClientBuilder.build();
    }

    public String transform(String message) {
        Query query = new Query(message);
        return transform(query);
    }

    public String transform(Query query) {

        logger.info("CompressTransformer called.");

        try {
            var compressedQueryText = this.chatClient.prompt()
                    .user(user -> user.text(this.DEFAULT_PROMPT_TEMPLATE.getTemplate())
                    .param("query", query.text()))
                    .call()
                    .content();

            if (compressedQueryText == null || compressedQueryText.isBlank()) {
                logger.warn("Compression returned empty result, using original query.");
                return query.text();
            }

            logger.info("Transformed message to: {}", compressedQueryText);
            return compressedQueryText;
        } catch (HttpClientErrorException httpException) {
            if (httpException.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("Compression transformer unauthorized. Falling back to original query: {}", httpException.getMessage());
                return query.text();
            }
            throw httpException;
        } catch (RestClientException restClientException) {
            logger.warn("Compression transformer failed due to chat client error. Using original query instead.", restClientException);
            return query.text();
        } catch (RuntimeException runtimeException) {
            logger.warn("Compression transformer failed unexpectedly. Using original query instead.", runtimeException);
            return query.text();
        }
    }
}
