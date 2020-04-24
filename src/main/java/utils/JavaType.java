package utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JavaType {

    private static Map<String, Class<?>> types;

    static {
         types = new HashMap<String, Class<?>>() {
             {
                 put("VARCHAR", String.class);
                 put("INTEGER", Integer.class);
                 put("TIMESTAMP", Date.class);
             }
        };
    }

    public static Class<?> get(String type) {
        type = type.toUpperCase();
        return types.get(type);
    }

}
