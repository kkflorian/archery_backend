package property.abolish.archery.http.misc;

import io.javalin.http.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CookieBaker {

    /* Factory methods */
    public static CookieBaker create(String name, String value) {
        return new CookieBaker(name, value);
    }

    public static CookieBaker create(String name, String value, String path) {
        return create(name, value).path(path);
    }

    public static CookieBaker create(String name, String value, String path, int maxAge) {
        return create(name, value, path).maxAge(maxAge);
    }

    public static CookieBaker createSecure(String name, String value) {
        return new CookieBaker(name, value).secure(true).httpOnly(true);
    }

    public static CookieBaker createSecure(String name, String value, String path) {
        return createSecure(name, value).path(path);
    }

    public static CookieBaker createSecure(String name, String value, String path, int maxAge) {
        return createSecure(name, value, path).maxAge(maxAge);
    }

    public static CookieBaker deletionCookie(String name) {
        return new CookieBaker(name, "-").maxAge(0);
    }

    /* Attributes */
    public static final String ATTRIBUTE_HTTP_ONLY = "HttpOnly";
    public static final String ATTRIBUTE_SECURE = "Secure";
    public static final String ATTRIBUTE_EXPIRES = "Expires";
    public static final String ATTRIBUTE_MAX_AGE = "Max-Age";
    public static final String ATTRIBUTE_PATH = "Path";
    public static final String ATTRIBUTE_DOMAIN = "Domain";
    public static final String ATTRIBUTE_SAME_SITE = "SameSite";

    private static final DateFormat EXPIRES_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",
            Locale.ENGLISH);

    static {
        EXPIRES_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final String name, value;
    private final Map<String, Object> attributes = new HashMap<>();

    private CookieBaker(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public CookieBaker httpOnly(boolean value) {
        return this.attribute(ATTRIBUTE_HTTP_ONLY, value);
    }

    public CookieBaker secure(boolean value) {
        return this.attribute(ATTRIBUTE_SECURE, value);
    }

    public CookieBaker expires(Date date) {
        return this
                .attribute(ATTRIBUTE_EXPIRES, EXPIRES_FORMAT.format(date))
                .attribute(ATTRIBUTE_MAX_AGE, TimeUnit.MILLISECONDS
                        .toSeconds(date.getTime() - System.currentTimeMillis()));
    }

    public CookieBaker maxAge(int maxAge) {
        return this.expires(new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxAge)));
    }

    public CookieBaker path(String path) {
        return this.attribute(ATTRIBUTE_PATH, path);
    }

    public CookieBaker domain(String domain) {
        return this.attribute(ATTRIBUTE_DOMAIN, domain);
    }

    public CookieBaker sameSite(SameSiteValue value) {
        this.attribute(ATTRIBUTE_SAME_SITE, value);
        return this;
    }

    public CookieBaker attribute(String name, Object value) {
        if (value instanceof Boolean && !((Boolean) value)) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
        return this;
    }

    public void addToJavalinContext(Context ctx) {
        ctx.header("Set-Cookie", this.toString());
    }

    @Override
    public String toString() {
        final List<String> cookieParts = new ArrayList<>();

        cookieParts.add(name + "=" + value);
        attributes.forEach((name, value) -> {
            if (value instanceof Boolean) { // Boolean attributes are true if they exist, false if not
                cookieParts.add(name);
            } else {
                cookieParts.add(name + "=" + value);
            }
        });

        return String.join("; ", cookieParts);
    }

    public enum SameSiteValue {
        LAX("Lax"),
        STRICT("Strict"),
        NONE("None");

        private final String value;

        SameSiteValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
