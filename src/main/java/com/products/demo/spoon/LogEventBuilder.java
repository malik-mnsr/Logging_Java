package com.products.demo.spoon;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.products.demo.Model.Products;
import com.products.demo.Model.User;

import java.time.ZonedDateTime;
import java.util.List;

public class LogEventBuilder {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ObjectNode root;

    public LogEventBuilder() {
        root = mapper.createObjectNode();
        root.put("timestamp", ZonedDateTime.now().toString());
    }

    public LogEventBuilder withUser(User user) {
        ObjectNode userNode = mapper.createObjectNode();
        if (user != null) {
            userNode.put("userId", user.getId());
            userNode.put("name", user.getName());
            userNode.put("email", user.getEmail());
            userNode.put("age", user.getAge());
        } else {
            userNode.putNull("userId");
            userNode.putNull("name");
            userNode.putNull("email");
            userNode.putNull("age");
        }
        root.set("userDetails", userNode);
        return this;
    }

    public LogEventBuilder withOperation(String operation) {
        root.put("operation", sanitize(operation));
        return this;
    }

    public LogEventBuilder withEndpoint(String endpoint) {
        root.put("endpoint", sanitize(endpoint));
        return this;
    }

    public LogEventBuilder withProduct(Products product) {
        if (product == null) return this;
        ObjectNode p = mapper.createObjectNode();
        p.put("id", product.getId());
        p.put("name", product.getName());
        root.set("product", p);
        return this;
    }

    public LogEventBuilder withProducts(List<Products> products) {
        if (products == null || products.isEmpty()) return this;

        ArrayNode array = mapper.createArrayNode();
        for (Products p : products) {
            ObjectNode pn = mapper.createObjectNode();
            pn.put("id", p.getId());
            pn.put("name", p.getName());
            array.add(pn);
        }
        root.set("products", array);
        return this;
    }

    public LogEventBuilder withProductId(Long id) {
        if (id != null) {
            root.put("productId", id);
        }
        return this;
    }

    private String sanitize(String input) {
        return input == null ? null : input.replaceAll("[\\n\\r\\t]", "_");
    }

    public ObjectNode build() {
        return root;
    }
}
