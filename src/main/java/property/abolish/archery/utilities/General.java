package property.abolish.archery.utilities;

import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class General {

    public static String createRandomAlphanumeric(int length) {
        String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

        SecureRandom rand = new SecureRandom();
        StringBuilder sessionId = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int num = rand.nextInt(symbols.length());
            sessionId.append(symbols.charAt(num));
        }

        return sessionId.toString();
    }

    public static void handleException(String message, Exception e) {
        System.out.println(message);
        e.printStackTrace();
        System.exit(1);
    }

    public static <T> T copyFields(Object fromObject, Class<T> toClass) {
        final Gson gson = new Gson();
        final String json = gson.toJson(fromObject, fromObject.getClass());
        return gson.fromJson(json, toClass);
    }

    public static <T> List<T> copyLists(List<?> fromList, Class<T> toClass) {
        return fromList.stream()
                .map(fromObject -> copyFields(fromObject, toClass))
                .collect(Collectors.toList());
    }

}
