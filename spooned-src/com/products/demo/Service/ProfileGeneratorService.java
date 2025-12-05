package com.products.demo.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
@Service
public class ProfileGeneratorService {
    private static final Logger logger = LogManager.getLogger(ProfileGeneratorService.class);

    private static final String LOG_FILE = "logs/profile.log";

    private static final String OUTPUT_DIR = "logs/profiles/";

    private static final ObjectMapper mapper = new ObjectMapper();

    // Regex to parse log lines (adjust based on your LOG_PATTERN)
    private static final Pattern LOG_PATTERN = Pattern.compile("\\[(?:requestId=(\\S+?), )?username=(\\S+?), operation=(\\S+?), entity=(\\S+?)(?:, resourceId=(\\S+?))?(?:, priceThreshold=(\\S+?))?\\] - Performing (\\S+?) operation: (\\S+?) on (\\S+?), user: (\\S+)");

    public void generateProfiles() {
        logger.info("Starting profile generation from log file: {}", LOG_FILE);
        Map<String, UserProfile> profiles = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String username = matcher.group(2);// username from MDC

                    String operation = matcher.group(3);// operation from MDC

                    String entity = matcher.group(4);// entity from MDC

                    String resourceId = matcher.group(5);// resourceId (optional)

                    String priceThreshold = matcher.group(6);// priceThreshold (optional)

                    String timestamp = line.substring(0, 23);// Extract timestamp from log

                    profiles.computeIfAbsent(username, k -> new UserProfile(username)).addOperation(operation, entity, resourceId, priceThreshold, timestamp);
                }
            } 
            // Generate profiles and save to JSON
            profiles.values().forEach(profile -> {
                profile.determineProfileType();
                saveProfile(profile);
            });
            logger.info("Generated {} user profiles", profiles.size());
        } catch (Exception e) {
            logger.error("Failed to generate profiles. Error: {}", e.getMessage());
        }
    }

    private void saveProfile(UserProfile profile) {
        try {
            // Ensure output directory exists
            File outDir = new File(OUTPUT_DIR);
            if (!outDir.exists()) {
                boolean created = outDir.mkdirs();
                if (!created) {
                    logger.warn("Could not create output directory: {}", OUTPUT_DIR);
                }
            }
            String fileName = (OUTPUT_DIR + profile.getEmail().replace("@", "_")) + ".json";
            File outFile = new File(fileName);
            mapper.writeValue(outFile, profile);
            logger.debug("Saved profile for user: {} to {}", profile.getEmail(), fileName);
        } catch (Exception e) {
            logger.error("Failed to save profile for user: {}. Error: {}", profile.getEmail(), e.getMessage());
        }
    }

    // Inner class to represent a user profile
    public static class UserProfile {
        private String email;

        private String profileType;

        private Map<String, OperationStats> operationStats;

        private String lastUpdated;

        public UserProfile(String email) {
            this.email = email;
            this.operationStats = new HashMap<>();
            this.operationStats.put("readOperations", new OperationStats());
            this.operationStats.put("writeOperations", new OperationStats());
            this.operationStats.put("expensiveProductSearches", new OperationStats());
            this.lastUpdated = ZonedDateTime.now().toString();
        }

        public void addOperation(String operation, String entity, String resourceId, String priceThreshold, String timestamp) {
            String opKey = (operation.equals("READ")) ? "readOperations" : operation.equals("WRITE") ? "writeOperations" : "expensiveProductSearches";
            OperationStats stats = operationStats.get(opKey);
            stats.incrementCount();
            stats.addDetail(new OperationDetail(operation, entity, resourceId, priceThreshold, timestamp));
            this.lastUpdated = ZonedDateTime.now().toString();
        }

        public void determineProfileType() {
            int readCount = operationStats.get("readOperations").getCount();
            int writeCount = operationStats.get("writeOperations").getCount();
            int searchCount = operationStats.get("expensiveProductSearches").getCount();
            int total = (readCount + writeCount) + searchCount;
            if (total == 0) {
                profileType = "UNKNOWN";
                return;
            }
            double readRatio = ((double) (readCount)) / total;
            double writeRatio = ((double) (writeCount)) / total;
            double searchRatio = ((double) (searchCount)) / total;
            if (readRatio >= 0.6) {
                profileType = "READ_HEAVY";
            } else if (writeRatio >= 0.6) {
                profileType = "WRITE_HEAVY";
            } else if (searchRatio >= 0.6) {
                profileType = "EXPENSIVE_PRODUCT_SEARCHER";
            } else {
                profileType = "MIXED";
            }
        }

        // Getters and setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getProfileType() {
            return profileType;
        }

        public void setProfileType(String profileType) {
            this.profileType = profileType;
        }

        public Map<String, OperationStats> getOperationStats() {
            return operationStats;
        }

        public void setOperationStats(Map<String, OperationStats> operationStats) {
            this.operationStats = operationStats;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }

    public static class OperationStats {
        private int count;

        private List<OperationDetail> details;

        public OperationStats() {
            this.count = 0;
            this.details = new ArrayList<>();
        }

        public void incrementCount() {
            this.count++;
        }

        public void addDetail(OperationDetail detail) {
            this.details.add(detail);
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<OperationDetail> getDetails() {
            return details;
        }

        public void setDetails(List<OperationDetail> details) {
            this.details = details;
        }
    }

    public static class OperationDetail {
        private String operation;

        private String entity;

        private String resourceId;

        private String priceThreshold;

        private String timestamp;

        public OperationDetail(String operation, String entity, String resourceId, String priceThreshold, String timestamp) {
            this.operation = operation;
            this.entity = entity;
            this.resourceId = resourceId;
            this.priceThreshold = priceThreshold;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getEntity() {
            return entity;
        }

        public void setEntity(String entity) {
            this.entity = entity;
        }

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getPriceThreshold() {
            return priceThreshold;
        }

        public void setPriceThreshold(String priceThreshold) {
            this.priceThreshold = priceThreshold;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}