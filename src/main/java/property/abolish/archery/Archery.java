package property.abolish.archery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.plugin.json.JavalinJson;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import io.javalin.Javalin;

import java.io.IOException;

public class Archery {
    private static Jdbi jdbi;

    public static void main(String[] args) {
        ConnectionConfig load;

        try {
            load = ConnectionConfig.load();
        } catch (IOException e) {
            handleException("Config couldn't be loaded", e);
            return;
        }

        jdbi = Jdbi.create(String.format("jdbc:mysql://%s:%d/%s", load.dbIp, load.dbPort, load.dbName), load.dbUser, load.dbPw);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        try (Handle ignored = jdbi.open()){
            System.out.println("Connection successfully established!");

            Javalin app = Javalin.create().start(load.webPort);

        } catch (JdbiException e) {
            handleException("Connection couldn't be established", e);
            return;
        }
    }

    private static void handleException(String message, Exception e) {
        System.out.println(message);
        e.printStackTrace();
        System.exit(1);
    }

    public static Jdbi getJdbi() {
        return jdbi;
    }
}
