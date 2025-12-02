package com.products.demo.spoon;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.products.demo.Model.Products;
import com.products.demo.Model.User;
import com.products.demo.Service.UserService;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
@Component
public class UserProfileLogger {
    private static final Logger logger = LogManager.getLogger(UserProfileLogger.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static UserService userService;// static access to UserService


    @Autowired
    public void setUserService(UserService service) {
        UserProfileLogger.userService = service;
    }

    // Field constants
    private static final String FIELD_USER_DETAILS = "userDetails";

    private static final String FIELD_NAME = "name";

    private static final String FIELD_USER_ID = "userId";

    private static final String FIELD_EMAIL = "email";

    private static final String FIELD_AGE = "age";

    private static final String FIELD_OPERATION = "operation";

    private static final String FIELD_ENDPOINT = "endpoint";

    private static final String FIELD_PRODUCT = "product";

    private static final String FIELD_PRODUCTS = "products";

    private static final String FIELD_PRODUCT_ID = "productId";

    private static final String FIELD_ID = "id";

    private static final String FIELD_TIMESTAMP = "timestamp";

    public static void logRequest(Authentication auth, String operation, String endpoint, Object productOrListOrId) {
        try {
            ObjectNode root = mapper.createObjectNode();
            // Add timestamp
            root.put(FIELD_TIMESTAMP, ZonedDateTime.now().toString());
            // User and action info
            root.set(FIELD_USER_DETAILS, buildUserDetails(auth));
            root.put(FIELD_OPERATION, sanitize(operation));
            root.put(FIELD_ENDPOINT, sanitize(endpoint));
            // Product info
            if (productOrListOrId instanceof Products product) {
                root.set(FIELD_PRODUCT, buildProductNode(product));
            } else if (((productOrListOrId instanceof List<?> list) && (!list.isEmpty())) && (list.get(0) instanceof Products)) {
                root.set(FIELD_PRODUCTS, buildProductsArray(((List<Products>) (list))));
            } else if (productOrListOrId instanceof Long productId) {
                root.put(FIELD_PRODUCT_ID, productId);
            } else {
                root.putNull(FIELD_PRODUCT_ID);
            }
            // Log to console/file via Log4j
            logger.info(mapper.writeValueAsString(root));
            // Write to per-user JSON file
            Long userId = root.path(FIELD_USER_DETAILS).path(FIELD_USER_ID).asLong(-1);
            if (userId != (-1)) {
                writeJsonToFile(root, userId);
            }
        } catch (Exception e) {
            logger.error("Failed to build structured log", e);
        }
    }

    // Writes JSON events as an array per user
    private static void writeJsonToFile(ObjectNode newEvent, Long userId) {
        try {
            File dir = new File("logs/users");
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(dir, ("user_" + userId) + ".json");
            ArrayNode eventsArray;
            if (file.exists()) {
                eventsArray = ((ArrayNode) (mapper.readTree(file)));
            } else {
                eventsArray = mapper.createArrayNode();
            }
            eventsArray.add(newEvent);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, eventsArray);
        } catch (Exception e) {
            logger.error("Failed to write JSON file for user {}", userId, e);
        }
    }

    private static ObjectNode buildUserDetails(Authentication auth) {
        ObjectNode userDetails = mapper.createObjectNode();
        if ((auth == null) || (auth.getPrincipal() == null)) {
            return withNullUserDetails(userDetails);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            userDetails.put(FIELD_NAME, user.getName());
            userDetails.put(FIELD_USER_ID, user.getId());
            userDetails.put(FIELD_EMAIL, user.getEmail());
            userDetails.put(FIELD_AGE, user.getAge());
        } else if (principal instanceof UserDetails springUser) {
            User appUser = null;
            if (userService != null) {
                appUser = userService.getUserByEmail(springUser.getUsername()).orElse(null);
            }
            if (appUser != null) {
                userDetails.put(FIELD_NAME, appUser.getName());
                userDetails.put(FIELD_USER_ID, appUser.getId());
                userDetails.put(FIELD_EMAIL, appUser.getEmail());
                userDetails.put(FIELD_AGE, appUser.getAge());
            } else {
                userDetails.put(FIELD_NAME, springUser.getUsername());
                userDetails.put(FIELD_USER_ID, -1);
                userDetails.put(FIELD_EMAIL, springUser.getUsername());
                userDetails.putNull(FIELD_AGE);
            }
        } else {
            userDetails.put("principalType", principal.getClass().getSimpleName());
            return withNullUserDetails(userDetails);
        }
        return userDetails;
    }

    private static ObjectNode withNullUserDetails(ObjectNode userDetails) {
        userDetails.putNull(FIELD_NAME);
        userDetails.putNull(FIELD_USER_ID);
        userDetails.putNull(FIELD_EMAIL);
        userDetails.putNull(FIELD_AGE);
        return userDetails;
    }

    private static ObjectNode buildProductNode(Products product) {
        ObjectNode productNode = mapper.createObjectNode();
        productNode.put(FIELD_ID, product.getId());
        productNode.put(FIELD_NAME, product.getName());
        return productNode;
    }

    private static ArrayNode buildProductsArray(List<Products> products) {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (Products p : products) {
            arrayNode.add(buildProductNode(p));
        }
        return arrayNode;
    }

    private static String sanitize(String input) {
        return input == null ? null : input.replaceAll("[\\n\\r\\t]", "_");
    }
}