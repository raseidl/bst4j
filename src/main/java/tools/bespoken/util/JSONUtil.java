package tools.bespoken.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by jpk on 11/10/16.
 */
public class JSONUtil {
    public static JsonNode toJSON(String s) {
        ObjectMapper mapper = new ObjectMapper();

        if (s == null || s.trim().isEmpty()) {
            return mapper.createObjectNode();
        }

        try {
            return mapper.readTree(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
