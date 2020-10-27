package property.abolish.archery.http.controller;

import com.kosprov.jargon2.api.Jargon2;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.model.UserSession;
import property.abolish.archery.db.query.UserQuery;
import property.abolish.archery.db.query.UserSessionQuery;
import property.abolish.archery.http.misc.CookieBaker;
import property.abolish.archery.http.model.requests.GetUsersRequest;
import property.abolish.archery.http.model.responses.ErrorResponse;
import property.abolish.archery.http.model.requests.LoginRequest;
import property.abolish.archery.http.model.requests.RegisterRequest;
import property.abolish.archery.http.model.responses.GetUserResponse;
import property.abolish.archery.http.model.responses.GetUsersResponse;
import property.abolish.archery.http.model.responses.SuccessResponse;
import property.abolish.archery.utilities.General;
import property.abolish.archery.utilities.Validation;

import java.time.Instant;
import java.util.List;

import static com.kosprov.jargon2.api.Jargon2.jargon2Hasher;
import static com.kosprov.jargon2.api.Jargon2.jargon2Verifier;
import static property.abolish.archery.http.misc.CookieBaker.SameSiteValue.LAX;
import static property.abolish.archery.http.misc.CookieBaker.SameSiteValue.NONE;
import static property.abolish.archery.utilities.General.createRandomAlphanumeric;

public class UserController {

    private static final int SESSION_MAX_AGE = 60 * 60 * 24;
    private static final int MEMORY_COST = 65536;
    private static final int TIME_COST = 1;
    private static final int PARALLELISM = 1;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    public static final String COOKIE_NAME_SESSION = "Session";

    public static void handleLogin(Context ctx) {
        Validator<LoginRequest> validator = ctx.bodyValidator(LoginRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.username), "username cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.password), "password cannot be null or empty");
        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        LoginRequest req = validator.get();

        try (Handle dbConnection = Archery.getConnection()) {
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            User user = userQuery.getUserByUsername(req.username);

            if (user == null) {
                ctx.status(404).json(new ErrorResponse("USER_DOES_NOT_EXIST", "Es existiert kein Benutzer unter diesem Nutzernamen"));
                return;
            }

            if (!isPasswordCorrect(req.password, user.getPasswordHash())) {
                ctx.status(401).json(new ErrorResponse("WRONG_PASSWORD", "Das Passwort ist falsch"));
                return;
            }

            String sessionId = ctx.cookie(COOKIE_NAME_SESSION);

            if (Validation.isNullOrEmpty(sessionId)) {
                createSession(dbConnection.attach(UserSessionQuery.class), user.getId(), ctx);
                dbConnection.commit();
                ctx.json(new SuccessResponse());
                return;
            }

            // Check if sessionId is valid
            UserSessionQuery userSessionQuery = dbConnection.attach(UserSessionQuery.class);
            UserSession userSession = userSessionQuery.getUserSessionBySessionId(sessionId);

            if (userSession == null || userSession.getExpiryDate().isBefore(Instant.now())) {
                // Create new session
                createSession(userSessionQuery, user.getId(), ctx);
                dbConnection.commit();
                ctx.json(new SuccessResponse());
                return;
            }

            // Session is valid -> User is already logged in
            ctx.status(409).json(new ErrorResponse("ALREADY_LOGGED_IN", "Der User ist bereits angemeldet"));
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

            if (user != null) {
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
            createSession(dbConnection.attach(UserSessionQuery.class), userId, ctx);

            dbConnection.commit();
            ctx.json(new SuccessResponse());
        }
    }

    public static void handleGetUser(Context ctx) {
        User user = ctx.use(User.class);

        if (user == null || user.getUsername().isEmpty()) {
            ctx.status(404).json(new ErrorResponse("USER_NOT_FOUND", "This user does not exist"));
            return;
        }

        ctx.json(new GetUserResponse(user.getUsername()));
    }

    public static void handleSignOff(Context ctx) {
        UserSession userSession = ctx.use(UserSession.class);
        try (Handle dbConnection = Archery.getConnection()) {
            UserSessionQuery userSessionQuery = dbConnection.attach(UserSessionQuery.class);
            userSessionQuery.invalidateUserSession(userSession.getSessionId());

            CookieBaker.deletionCookie(COOKIE_NAME_SESSION)
                    .path("/")
                    .httpOnly(true)
                    .secure(!Archery.getConfig().allowInsecureCookies)
                    .sameSite(Archery.getConfig().devModeURL != null ? NONE : LAX)
                    .addToJavalinContext(ctx);

            ctx.json(new SuccessResponse());
        }
    }

    public static void handleGetUsersBySearchTerm(Context ctx) {
        Validator<GetUsersRequest> validator = ctx.bodyValidator(GetUsersRequest.class)
                .check(r -> r.searchTerm != null, "searchTerm cannot be null")
                .check(r -> r.limit != 0, "limit cannot be zero");
        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        try (Handle dbConnection = Archery.getConnection()){
            GetUsersRequest req = validator.get();
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            User user = ctx.use(User.class);

            List<User> users = userQuery.getUsersBySearchTerm(req.searchTerm, req.limit, user.getId());

            ctx.json(new GetUsersResponse(General.copyLists(users, GetUsersResponse.UserInfo.class)));
        }
    }

    private static void createSession(UserSessionQuery userSessionQuery, int userId, Context ctx) {
        String sessionId = createRandomAlphanumeric(32);
        UserSession userSession = new UserSession();
        userSession.setSessionId(sessionId);
        userSession.setUserId(userId);
        userSession.setExpiryDate(Instant.now().plusSeconds(SESSION_MAX_AGE));

        userSessionQuery.insertUserSession(userSession);

        CookieBaker.create(COOKIE_NAME_SESSION, sessionId)
                .maxAge(SESSION_MAX_AGE)
                .httpOnly(true)
                .path("/")
                .secure(!Archery.getConfig().allowInsecureCookies)
                .sameSite(Archery.getConfig().devModeURL != null ? NONE : LAX)
                .addToJavalinContext(ctx);
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

    private static boolean isPasswordCorrect(String password, String hash) {
        Jargon2.Verifier verifier = jargon2Verifier();

        return verifier.hash(hash).password(password.getBytes()).verifyEncoded();
    }

}
