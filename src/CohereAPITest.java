import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class CohereAPITest {

    private static final String COHERE_API_KEY = "bqSekY8PSDl30ld5REJyocicxuEmVyvyHzZE8jFN";
    private static final String COHERE_API_URL = "https://api.cohere.ai/v1/embed";

    public static void main(String[] args) {
        CohereAPITest test = new CohereAPITest();
        String targetWord = "circus";
        String[] wordsList = {"pizza", "spectacle", "clown", "computer", "dog"};
        String mostRelatedWord = test.getMostRelatedWord(targetWord, wordsList);
        System.out.println("The most related word to '" + targetWord + "' is: " + mostRelatedWord);
    }

    public String getMostRelatedWord(String targetWord, String[] wordsList) {
        double[] targetEmbedding = getEmbedding(targetWord);
        double[][] listEmbeddings = new double[wordsList.length][];

        printArray(targetEmbedding);

        for (int i = 0; i < wordsList.length; i++) {
            listEmbeddings[i] = getEmbedding(wordsList[i]);
            System.out.println("Embedding for '" + wordsList[i] + "':");
            printArray(listEmbeddings[i]);
        }

        // Find the most similar word
        double maxSimilarity = -1;
        String mostRelatedWord = null;

        for (int i = 0; i < listEmbeddings.length; i++) {
            double similarity = cosineSimilarity(targetEmbedding, listEmbeddings[i]);
            System.out.printf("Similarity between '%s' and '%s': %.4f%n", targetWord, wordsList[i], similarity);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostRelatedWord = wordsList[i];
            }
        }

        return mostRelatedWord;
    }

    private double[] getEmbedding(String text) {
        try {
            URL url = new URL(COHERE_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + COHERE_API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{ \"texts\": [\"" + text + "\"] }";

            // Sending the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Reading the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray embeddingsArray = jsonResponse.getJSONArray("embeddings");
                return toDoubleArray(embeddingsArray.getJSONArray(0)); // Get the first embedding
            } else {
                System.out.println("Error: Server returned HTTP response code: " + responseCode);
            }
        } catch (IOException e) {
            System.out.println("Error accessing Cohere API: " + e.getMessage());
            e.printStackTrace();
        }
        return new double[0]; // Return an empty array in case of error
    }

    private double[] toDoubleArray(JSONArray jsonArray) {
        double[] result = new double[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            result[i] = jsonArray.getDouble(i);
        }
        return result;
    }

    private double cosineSimilarity(double[] vecA, double[] vecB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += Math.pow(vecA[i], 2);
            normB += Math.pow(vecB[i], 2);
        }

        if (normA == 0 || normB == 0) return 0.0; // To avoid division by zero
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private void printArray(double[] array) {
        for (double value : array) {
            System.out.printf("%.4f ", value);
        }
        System.out.println();
    }
}
