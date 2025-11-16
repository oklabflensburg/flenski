package com.flenski.assistant.requests;

import java.io.IOException;
// import org.json.JSONObject; // entfernt
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchRequestIndexer {

    public String search(String query) throws IOException {
        String urlString = "http://localhost:8081/api/search?q=" + java.net.URLEncoder.encode(query, "UTF-8");
        URL url = java.net.URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Failed : HTTP error code : " + responseCode);
        }

        String response;
        try (Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8")) {
            response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
        conn.disconnect();
        try {
            List<String> payloads = extractPayloads(response);
            return String.join("\n", payloads);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract payloads", e);
        }
    }

    private List<String> extractPayloads(String response) throws IOException {
        List<String> payloads = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode points = root.path("result").path("points");
        if (points.isArray()) {
            for (JsonNode point : points) {
                JsonNode payload = point.path("payload");
                payloads.add(payload.toString());
            }
        }
        return payloads;
    }
}
