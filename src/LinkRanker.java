import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LinkRanker {

    private static final String COHERE_API_URL = "https://api.cohere.ai/v1/embed";
    private static final String COHERE_API_KEY = "bqSekY8PSDl30ld5REJyocicxuEmVyvyHzZE8jFN";
    private static final int BATCH_SIZE = 96; // Maximum texts per API call

    //get embeddings from the Cohere API
    private List<Double[]> getEmbeddings(List<String> texts) {
        List<Double[]> embeddings = new ArrayList<>();

        // Split texts into batches
        for (int start = 0; start < texts.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(start, end);

            try {
                URL url = new URL(COHERE_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + COHERE_API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Prepare JSON input with model, texts, inputType, embeddingTypes, and truncate
                StringBuilder jsonInputString = new StringBuilder("{ \"model\": \"embed-multilingual-v2.0\", \"input_type\": \"classification\", \"embedding_types\": [\"float\"], \"truncate\": \"END\", \"texts\": [");
                for (int i = 0; i < batch.size(); i++) {
                    jsonInputString.append("\"").append(batch.get(i).replace("\"", "\\\"")).append("\""); // Escape quotes
                    if (i < batch.size() - 1) {
                        jsonInputString.append(", ");
                    }
                }
                jsonInputString.append("] }");

                //System.out.println("Sending JSON input: " + jsonInputString);

                // Sending the request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Reading the response
                int responseCode = connection.getResponseCode();
                //System.out.println("Received response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray embeddingsArray = jsonResponse.getJSONObject("embeddings").getJSONArray("float");

                    // Parse embeddings
                    for (int i = 0; i < embeddingsArray.length(); i++) {
                        JSONArray embedding = embeddingsArray.getJSONArray(i);
                        Double[] embeddingArray = new Double[embedding.length()];
                        for (int j = 0; j < embedding.length(); j++) {
                            embeddingArray[j] = embedding.getDouble(j);
                        }
                        embeddings.add(embeddingArray);
                    }
                } else {
                    BufferedReader errorStream = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorStream.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorStream.close();
                    System.out.println("Error response from API: " + errorResponse.toString());
                }
            } catch (Exception e) {
                System.out.println("Error accessing Cohere API: " + e.getMessage());
            }
        }

        return embeddings;
    }

    // Method to calculate cosine similarity
    private double cosineSimilarity(Double[] vecA, Double[] vecB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += Math.pow(vecA[i], 2);
            normB += Math.pow(vecB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Method to rank links based on similarity to the goal word
    public List<String> rankLinks(List<String> links, String goalWord) {

        System.out.println("Ranking links: " + links);
        // Get embeddings for links and the goal word
        List<String> textsToEmbed = new ArrayList<>(links);
        textsToEmbed.add(goalWord);

        List<Double[]> embeddings = getEmbeddings(textsToEmbed);

        if (embeddings.size() < links.size() + 1) {
            System.out.println("Error: Not enough embeddings returned.");
            return Collections.emptyList();
        }

        Double[] goalEmbedding = embeddings.get(links.size());
        Map<String, Double> linkScores = new HashMap<>();

        for (int i = 0; i < links.size(); i++) {
            Double[] linkEmbedding = embeddings.get(i);
            double similarity = cosineSimilarity(goalEmbedding, linkEmbedding);
            linkScores.put(links.get(i), similarity);
        }

        // Sort links based on similarity scores
        List<Map.Entry<String, Double>> sortedLinks = new ArrayList<>(linkScores.entrySet());
        sortedLinks.sort(Map.Entry.comparingByValue());

        List<String> rankedLinks = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedLinks) {
            rankedLinks.add(entry.getKey());
        }

        return rankedLinks;
    }
}
