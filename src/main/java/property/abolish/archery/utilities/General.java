package property.abolish.archery.utilities;

import java.security.SecureRandom;

public class General {

    public static String createRandomAlphanumeric(int length) {
        String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

        SecureRandom rand = new SecureRandom();
        StringBuilder sessionId = new StringBuilder();

        for (int i = 0; i < length; i++){
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

}
