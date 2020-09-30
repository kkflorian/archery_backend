package property.abolish.archery;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;

public class ConnectionConfig {

    public String dbName;
    public String dbIp;
    public Integer dbPort;
    public String dbUser;
    public String dbPw;
    public Integer webPort;

    public static ConnectionConfig load() throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader("dev/config.json"))) {
            return new Gson().fromJson(reader, ConnectionConfig.class);
        }
    }

}
