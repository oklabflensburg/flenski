package com.flenski.assistant.requests;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flenski.assistant.dto.SourceInfo;

public class SearchRequestIndexer {

    public List<SourceInfo> search(String query) throws IOException {
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
            List<SourceInfo> sourceInfos = extractSourceInfos(response);
            return sourceInfos;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract payloads", e);
        }
    }

    private List<SourceInfo> extractSourceInfos(String response) throws IOException {
        List<SourceInfo> sources = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode points = root.path("result").path("points");
            for (JsonNode point : points) {
                JsonNode payload = point.path("payload");
                SourceInfo info = mapper.readValue(payload.toString(), SourceInfo.class);
                sources.add(info);
        }
        return sources;
    }
}
