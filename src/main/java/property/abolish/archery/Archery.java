package property.abolish.archery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Handles;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import property.abolish.archery.http.controller.UserController;
import property.abolish.archery.http.model.ErrorResponse;

import java.io.IOException;

import static property.abolish.archery.utilities.General.handleException;

public class Archery {
    private static Jdbi jdbi;
    private static Config config;

    public static void main(String[] args) {

        try {
            config = Config.load();
        } catch (IOException e) {
            handleException("Config couldn't be loaded", e);
            return;
        }

        jdbi = Jdbi.create(String.format("jdbc:mysql://%s:%d/%s?serverTimezone=%s", config.dbIp, config.dbPort, config.dbName, config.dbTimezone), config.dbUser, config.dbPw);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.getConfig(Handles.class).setForceEndTransactions(false);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        try (Handle dbConnection = jdbi.open()){
            System.out.println("Connection successfully established!");

            Javalin app = Javalin.create().start("localhost", config.webPort);

            app.routes(() -> {
                ApiBuilder.path("api/v1", () -> {
                    ApiBuilder.path("users", () -> {
                        ApiBuilder.get("login", UserController::handleLogin);
                        ApiBuilder.put(UserController::handleRegister);
                    });

                    ApiBuilder.path("events", () -> {
                        ApiBuilder.put(UserController::handleLogin);

                        ApiBuilder.post("create", UserController::handleLogin);
                    });
                });
            });

            app.exception(Exception.class, (exception, ctx) -> {
                exception.printStackTrace();
                ctx.status(500).json(new ErrorResponse("INTERNAL_SERVER_ERROR", "A internal server error has occurred"));
            });

            // PUT /api/v1/users
            // POST /api/v1/users/create

            /*
            app.routes(() -> {
    path("users", () -> {
        get(UserController::getAllUsers);
        post(UserController::createUser);
        path(":id", () -> {
            get(UserController::getUser);
            patch(UserController::updateUser);
            delete(UserController::deleteUser);
        });
        ws("events", userController::webSocketEvents);
    });
});
             */

        } catch (JdbiException e) {
            handleException("Connection couldn't be established", e);
            return;
        }
    }

    public static Jdbi getJdbi() {
        return jdbi;
    }

    public static Config getConfig() {
        return config;
    }

    public static Handle getConnection() {
        Handle connection = getJdbi().open();
        try {
            connection.getConnection().setAutoCommit(false);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return connection;
    }
}
