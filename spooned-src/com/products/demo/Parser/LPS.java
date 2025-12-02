package com.products.demo.Parser;
import com.fasterxml.jackson.databind.JsonNode;
public class LPS {
    private String timestamp;

    private String event;

    private UserInfo user;

    private ActionInfo action;

    // Getters
    public String getTimestamp() {
        return timestamp;
    }

    public String getEvent() {
        return event;
    }

    public UserInfo getUser() {
        return user;
    }

    public ActionInfo getAction() {
        return action;
    }

    @Override
    public String toString() {
        return (((((((((("Timestamp: " + timestamp) + "\n") + "Event: ") + event) + "\n") + "User: ") + user) + "\n") + "Action: ") + action) + "\n";
    }

    // Nested classes for user and action
    public static class UserInfo {
        public String name;

        public Long id;

        public String email;

        public Integer age;

        @Override
        public String toString() {
            return (((((("Name: " + name) + ", ID: ") + id) + ", Email: ") + email) + ", Age: ") + age;
        }
    }

    public static class ActionInfo {
        public String operation;

        public String endpoint;

        public String productId;

        @Override
        public String toString() {
            return (((("Operation: " + operation) + ", Endpoint: ") + endpoint) + ", ProductID: ") + productId;
        }
    }

    // Builder
    public static class LPSBuilder {
        private final LPS lps = new LPS();

        public LPSBuilder withTimestamp(String timestamp) {
            lps.timestamp = timestamp;
            return this;
        }

        public LPSBuilder withEvent(String event) {
            lps.event = event;
            return this;
        }

        public LPSBuilder withUser(UserInfo user) {
            lps.user = user;
            return this;
        }

        public LPSBuilder withAction(ActionInfo action) {
            lps.action = action;
            return this;
        }

        public LPS build() {
            return lps;
        }

        // Utility parsers
        public static UserInfo parseUser(JsonNode node) {
            UserInfo user = new UserInfo();
            user.name = node.path("name").asText();
            user.id = node.path("userId").asLong();
            user.email = node.path("email").asText();
            if (!node.path("age").isNull())
                user.age = node.path("age").asInt();

            return user;
        }

        public static ActionInfo parseAction(JsonNode node) {
            ActionInfo action = new ActionInfo();
            action.operation = node.path("operation").asText();
            action.endpoint = node.path("endpoint").asText();
            action.productId = (node.has("productId")) ? node.path("productId").asText() : null;
            return action;
        }
    }
}