package com.products.demo.spoon;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.products.demo.Model.Products;
import com.products.demo.Model.User;
import com.products.demo.Service.UserService;
import java.io.File;
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

    private static UserService userService;

    @Autowired
    public void setUserService(UserService service) {
        UserProfileLogger.userService = service;
    }

    public static void logRequest(Authentication auth, String operation, String endpoint, Object productOrListOrId) {
        try {
            User user = extractUser(auth);
            LogEventBuilder builder = new LogEventBuilder().withUser(user).withOperation(operation).withEndpoint(endpoint);
            if (productOrListOrId instanceof Products product) {
                builder.withProduct(product);
            } else if (((productOrListOrId instanceof List<?> list) && (!list.isEmpty())) && (list.get(0) instanceof Products)) {
                builder.withProducts(((List<Products>) (list)));
            } else if (productOrListOrId instanceof Long id) {
                builder.withProductId(id);
            }
            ObjectNode event = builder.build();
            logger.info(event.toString());
            if (user != null) {
                writeJsonToFile(event, user.getId());
            }
        } catch (Exception e) {
            logger.error("Error during logging", e);
        }
    }

    private static User extractUser(Authentication auth) {
        if ((auth == null) || (auth.getPrincipal() == null))
            return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof User)
            return ((User) (principal));

        if (principal instanceof UserDetails springUser) {
            return userService.getUserByEmail(springUser.getUsername()).orElse(null);
        }
        return null;
    }

    private static void writeJsonToFile(ObjectNode newEvent, Long userId) {
        try {
            File dir = new File("logs/users");
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(dir, ("user_" + userId) + ".json");
            var array = (file.exists()) ? mapper.readTree(file) : mapper.createArrayNode();
            ((ArrayNode) (array)).add(newEvent);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, array);
        } catch (Exception e) {
            logger.error("Failed to write JSON log for user {}", userId, e);
        }
    }
}