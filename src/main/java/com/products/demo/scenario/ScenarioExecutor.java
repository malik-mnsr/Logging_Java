package com.products.demo.scenario;

import java.net.http.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

public class ScenarioExecutor {

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        List<String> tokens = new ArrayList<>();

        // 1. CREATE 10 USERS + LOGIN
        for (int i = 1; i <= 10; i++) {
            String email = "user" + i + "@mail.com";
            String password = "pass" + i;

            registerUser(email, password);
            String token = login(email, password);
            tokens.add(token);
        }

        // 2. EXECUTE SCENARIOS FOR EACH USER
        Random random = new Random();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            for (int s = 1; s <= 20; s++) {
                int scenario = random.nextInt(7) + 1;

                switch (scenario) {
                    case 1 -> createProduct(token);
                    case 2 -> getAllProducts(token);
                    case 3 -> getProductById(token, random.nextInt(15) + 1);
                    case 4 -> deleteProduct(token, random.nextInt(15) + 1);
                    case 5 -> getExpensiveProducts(token);
                    case 6 -> updateProduct(token, random.nextInt(15) + 1);
                    case 7 -> searchByPrice(token, random.nextDouble(50));
                }

                Thread.sleep(200); // simulate real usage
            }
        }

        System.out.println("🎉 Scénarios exécutés. Vérifie application.log !");
    }

    // ============================
    //  USER AUTH
    // ============================

    private static void registerUser(String email, String pass) throws Exception {
        String body = """
        {
          "name":"TestUser",
          "email":"%s",
          "password":"%s",
          "age":25
        }
        """.formatted(email, pass);

        sendPost("/auth/register", body, null);
    }

    private static String login(String email, String pass) throws Exception {
        String body = """
        {
          "email":"%s",
          "password":"%s"
        }
        """.formatted(email, pass);

        HttpResponse<String> response = sendPost("/auth/login", body, null);

        // extract JWT from response JSON
        String json = response.body();
        return json.substring(json.indexOf(":\"") + 2, json.lastIndexOf("\""));
    }

    // ============================
    //  PRODUCT OPERATIONS
    // ============================

    private static void createProduct(String token) throws Exception {
        String body = """
        {
            "name": "Product-%s",
            "price": %s,
            "expirationDate": "%s"
        }
        """.formatted(UUID.randomUUID(), new Random().nextInt(50) + 1, LocalDate.now().plusDays(30));

        sendPost("/api/products", body, token);
    }

    private static void getAllProducts(String token) throws Exception {
        sendGet("/api/products", token);
    }

    private static void getProductById(String token, int id) throws Exception {
        sendGet("/api/products/" + id, token);
    }

    private static void deleteProduct(String token, int id) throws Exception {
        sendDelete("/api/products/" + id, token);
    }

    private static void updateProduct(String token, int id) throws Exception {
        String body = """
        {
            "name": "UpdatedProduct-%s",
            "price": %s,
            "expirationDate": "%s"
        }
        """.formatted(id, new Random().nextInt(70), LocalDate.now().plusDays(50));

        sendPut("/api/products/" + id, body, token);
    }

    private static void getExpensiveProducts(String token) throws Exception {
        sendGet("/api/products/expensive", token);
    }

    private static void searchByPrice(String token, double minPrice) throws Exception {
        sendGet("/api/products/price/" + minPrice, token);
    }

    // ============================
    // HTTP HELPERS
    // ============================

    private static HttpResponse<String> sendGet(String path, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET();

        if (token != null) req.setHeader("Authorization", "Bearer " + token);

        return client.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendPost(String path, String body, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (token != null) req.setHeader("Authorization", "Bearer " + token);

        return client.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendPut(String path, String body, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body));

        if (token != null) req.setHeader("Authorization", "Bearer " + token);

        return client.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendDelete(String path, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .DELETE();

        if (token != null) req.setHeader("Authorization", "Bearer " + token);

        return client.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }
}
