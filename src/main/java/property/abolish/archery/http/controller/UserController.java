package property.abolish.archery.http.controller;

import com.kosprov.jargon2.api.Jargon2;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.NotNull;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.model.UserSession;
import property.abolish.archery.db.query.UserQuery;
import property.abolish.archery.db.query.UserSessionQuery;
import property.abolish.archery.http.model.ErrorResponse;
import property.abolish.archery.http.model.LoginRequest;
import property.abolish.archery.http.model.RegisterRequest;
import property.abolish.archery.http.model.SuccessResponse;
import property.abolish.archery.utilities.Validation;

import javax.servlet.http.Cookie;
import java.time.Instant;

import static com.kosprov.jargon2.api.Jargon2.jargon2Hasher;
import static com.kosprov.jargon2.api.Jargon2.jargon2Verifier;
import static property.abolish.archery.utilities.General.createRandomAlphanumeric;

public class UserController {

    private static final int SESSION_MAX_AGE = 60*60*24;
    private static final int MEMORY_COST = 65536;
    private static final int TIME_COST = 1;
    private static final int PARALLELISM = 1;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;

    public static void handleLogin(Context ctx) {
        Validator<LoginRequest> validator = ctx.bodyValidator(LoginRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.username), "username cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.password), "password cannot be null or empty");
        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
        }

        LoginRequest req = validator.get();

        try (Handle dbConnection = Archery.getConnection()) {
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            User user = userQuery.getUserByUsername(req.username);

            if (user == null) {
                ctx.status(409).json(new ErrorResponse("USER_DOES_NOT_EXIST", "Es existiert kein Benutzer unter diesem Nutzernamen"));
                return;
            }

            if (!isPasswordCorrect(req.password, user.getPasswordHash())){
                ctx.status(409).json(new ErrorResponse("WRONG_PASSWORD", "Das Passwort ist falsch"));
                return;
            }

            String sessionId = ctx.cookie("Session");

            if (sessionId == null || sessionId.isEmpty()){
                // TODO create new session
            }

            if (sessionId != null && !sessionId.isEmpty()){
                // TODO check for valid session
            }

            ctx.json(new SuccessResponse());
        }

    }

    public static void handleRegister(Context ctx) {
        Validator<RegisterRequest> validator = ctx.bodyValidator(RegisterRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.firstName), "firstName cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.lastName), "lastName cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.username), "username cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.password), "password cannot be null or empty");
        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        RegisterRequest req = validator.get();

        try (Handle dbConnection = Archery.getConnection()) {
            // Check if user with this username already exists
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            User user = userQuery.getUserByUsername(req.username);

            if (user != null){
                ctx.status(409).json(new ErrorResponse("USER_ALREADY_EXISTS", "Dieser Benutzer existiert bereits"));
                return;
            }

            // Create new user in DB
            user = new User();
            user.setUsername(req.username);
            user.setFirstName(req.firstName);
            user.setLastName(req.lastName);
            user.setPasswordHash(encodeHash(req.password));
            int userId = userQuery.insertUser(user);

            // Create session
            String sessionId = createSession(dbConnection, userId);

            dbConnection.commit();

            ctx.cookie(getSessionCookie(sessionId));
        }

        ctx.json(new SuccessResponse());
    }

    @NotNull
    private static String createSession(Handle dbConnection, int userId) {
        String sessionId = createRandomAlphanumeric(32);
        UserSession userSession = new UserSession();
        userSession.setSessionId(sessionId);
        userSession.setUserId(userId);
        userSession.setExpiryDate(Instant.now().plusSeconds(SESSION_MAX_AGE));

        UserSessionQuery userSessionQuery = dbConnection.attach(UserSessionQuery.class);
        userSessionQuery.insertUserSession(userSession);
        return sessionId;
    }

    @NotNull
    private static Cookie getSessionCookie(String sessionId) {
        Cookie cookie = new Cookie("Session", sessionId);
        cookie.setMaxAge(SESSION_MAX_AGE);
        cookie.setSecure(!Archery.getConfig().allowInsecureCookies);
        cookie.setHttpOnly(true);
        return cookie;
    }

    private static String encodeHash(String password) {
        Jargon2.Hasher hasher = jargon2Hasher()
                .type(Jargon2.Type.ARGON2id)
                .memoryCost(MEMORY_COST)
                .timeCost(TIME_COST)
                .parallelism(PARALLELISM)
                .saltLength(SALT_LENGTH)
                .hashLength(HASH_LENGTH);

        return hasher.password(password.getBytes()).encodedHash();
    }

    private static boolean isPasswordCorrect(String password, String hash){
        Jargon2.Verifier verifier = jargon2Verifier()
                .type(Jargon2.Type.ARGON2id)
                .memoryCost(MEMORY_COST)
                .timeCost(TIME_COST)
                .parallelism(PARALLELISM);

        return verifier.hash(hash).password(password.getBytes()).verifyEncoded();
    }
}
