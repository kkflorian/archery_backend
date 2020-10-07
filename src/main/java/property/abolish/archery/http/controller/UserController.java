package property.abolish.archery.http.controller;

import com.kosprov.jargon2.api.Jargon2;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.query.UserQuery;
import property.abolish.archery.http.model.ErrorResponse;
import property.abolish.archery.http.model.RegisterRequest;
import property.abolish.archery.http.model.SuccessResponse;
import property.abolish.archery.utilities.Validation;

import static com.kosprov.jargon2.api.Jargon2.jargon2Hasher;

public class UserController {


    public static void handleLogin(Context ctx) {
        ctx.result("hello world");
    }

    public static void handleRegister(Context ctx) {
        Validator<RegisterRequest> validator = ctx.bodyValidator(RegisterRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.firstName), "firstName cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.lastName), "lastName cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.username), "username cannot be null or empty");
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

            userQuery.insertUser(user);

            dbConnection.commit();
        }

        ctx.json(new SuccessResponse());

        //todo Session cookie
    }

    private static String encodeHash(String password) {
        Jargon2.Hasher hasher = jargon2Hasher()
                .type(Jargon2.Type.ARGON2id)
                .memoryCost(65536)
                .timeCost(1)
                .parallelism(1)
                .saltLength(16)
                .hashLength(32);

        return hasher.password(password.getBytes()).encodedHash();
    }
}
