package property.abolish.archery;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;

public class Config {

    public String dbName;
    public String dbIp;
    public Integer dbPort;
    public String dbUser;
    public String dbPw;
    public Integer webPort;
    public String dbTimezone;
    public boolean allowInsecureCookies;
    public String devModeURL;

    public static Config load() throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader("config.json"))) {
            return new Gson().fromJson(reader, Config.class);
        }
    }

}
