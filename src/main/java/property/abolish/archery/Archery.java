package property.abolish.archery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Handles;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import property.abolish.archery.db.model.UserSession;
import property.abolish.archery.db.query.UserSessionQuery;
import property.abolish.archery.http.controller.UserController;
import property.abolish.archery.http.model.ErrorResponse;

import java.io.IOException;
import java.time.Instant;

import static io.javalin.core.security.SecurityUtil.roles;
import static property.abolish.archery.utilities.General.handleException;

public class Archery {
    private static Jdbi jdbi;
    private static Config config;

    public enum MyRole implements Role {
        ANYONE, LOGGED_IN;
    }

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

            Javalin httpServer = Javalin.create(config -> {
                config.enableCorsForOrigin(Archery.config.devModeURL);
                config.accessManager((handler, ctx, permittedRoles) -> {
                    if (ctx.method().equals("OPTIONS")) {
                        handler.handle(ctx);
                        return;
                    }

                    MyRole userRole = getUserRole(ctx);

                    if (permittedRoles.contains(userRole)) {
                        handler.handle(ctx);
                    } else {
                        ctx.status(401).json(new ErrorResponse("UNAUTHORIZED_USER", "User is not authorized for this action"));
                    }
                });
            }).start("localhost", config.webPort);

            httpServer.routes(() -> {
                ApiBuilder.path("api/v1", () -> {
                    ApiBuilder.path("users", () -> {
                        ApiBuilder.post("login", UserController::handleLogin, roles(MyRole.ANYONE));
                        ApiBuilder.put(UserController::handleRegister, roles(MyRole.ANYONE));
                        ApiBuilder.get(UserController::handleGetUser, roles(MyRole.LOGGED_IN));
                        ApiBuilder.post("signoff", UserController::handleSignOff, roles(MyRole.LOGGED_IN));
                    });

                    ApiBuilder.path("events", () -> {
                        ApiBuilder.put(UserController::handleLogin);

                        ApiBuilder.post("create", UserController::handleLogin);
                    });
                });
            });

            httpServer.exception(Exception.class, (exception, ctx) -> {
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

    private static MyRole getUserRole(Context ctx) {
        String sessionId = ctx.cookie("Session");

        if (sessionId == null || sessionId.isEmpty()){
            return MyRole.ANYONE;
        }

        Handle dbConnection = getConnection();

        // Check if sessionId is valid
        UserSessionQuery userSessionQuery = dbConnection.attach(UserSessionQuery.class);
        UserSession userSession = userSessionQuery.getUserSessionBySessionId(sessionId);

        if (userSession == null || userSession.getExpiryDate().isBefore(Instant.now())){
            return MyRole.ANYONE;
        }

        ctx.register(UserSession.class, userSession);
        return MyRole.LOGGED_IN;
    }
}
