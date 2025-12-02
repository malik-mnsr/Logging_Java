package com.products.demo.Parser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class LogParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<LPS> parseUserLogs(File userJsonFile) throws Exception {
        List<LPS> profiles = new ArrayList<>();
        JsonNode array = mapper.readTree(userJsonFile);
        for (JsonNode logNode : array) {
            String timestamp = (logNode.has("timestamp")) ? logNode.path("timestamp").asText() : "N/A";
            LPS.UserInfo user = LPS.LPSBuilder.parseUser(logNode.path("userDetails"));
            LPS.ActionInfo action = LPS.LPSBuilder.parseAction(logNode);
            LPS lps = new LPS.LPSBuilder().withTimestamp(timestamp).withEvent("Event from log").withUser(user).withAction(action).build();
            profiles.add(lps);
        }
        return profiles;
    }

    public static List<LPS> parseAllUsersLogs() throws Exception {
        File dir = new File("logs/users");
        List<LPS> allProfiles = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    allProfiles.addAll(parseUserLogs(file));
                }
            }
        }
        return allProfiles;
    }
}