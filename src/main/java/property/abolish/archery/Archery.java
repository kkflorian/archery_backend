package property.abolish.archery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.Api;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJson;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;
import property.abolish.archery.http.controller.UserController;

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

        jdbi = Jdbi.create(String.format("jdbc:mysql://%s:%d/%s?serverTimezone=%s", load.dbIp, load.dbPort, load.dbName, load.dbTimezone), load.dbUser, load.dbPw);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        try (Handle dbConnection = jdbi.open()){
            System.out.println("Connection successfully established!");

            Javalin app = Javalin.create().start("localhost", load.webPort);

            app.routes(() -> {
                ApiBuilder.path("api/v1", () -> {
                    ApiBuilder.path("users", () -> {
                        ApiBuilder.get("login", UserController::handleLogin);
                        ApiBuilder.post(UserController::handleRegister);
                    });

                    ApiBuilder.path("events", () -> {
                        ApiBuilder.put(UserController::handleLogin);

                        ApiBuilder.post("create", UserController::handleLogin);
                    });
                });
            });

            app.exception(Exception.class, (exception, ctx) -> {
                exception.printStackTrace();
                ctx.status(500);
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

            app.post("/api/v1/user/login", Archery::handleLogin);
        } catch (JdbiException e) {
            handleException("Connection couldn't be established", e);
            return;
        }
    }

    private static void handleLogin(Context ctx) throws Exception {

    }

    public static void handleException(String message, Exception e) {
        System.out.println(message);
        e.printStackTrace();
        System.exit(1);
    }

    public static Jdbi getJdbi() {
        return jdbi;
    }
}
