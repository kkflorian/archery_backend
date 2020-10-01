package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import kotlin.jvm.functions.Function1;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.query.UserQuery;
import property.abolish.archery.http.model.ErrorResponse;
import property.abolish.archery.http.model.RegisterRequest;
import property.abolish.archery.utilities.Validation;

import java.util.List;
import java.util.Map;

import property.abolish.archery.utilities.Validation;

public class UserController {


    public static void handleLogin(Context ctx) {
        ctx.result("hello world");
    }

    public static void handleRegister(Context ctx) {
        Validator<RegisterRequest> validator = ctx.bodyValidator(RegisterRequest.class)
                .check(r -> Validation.isNullOrEmpty(r.firstName), "firstName cannot be null or empty")
                .check(r -> Validation.isNullOrEmpty(r.lastName), "lastName cannot be null or empty")
                .check(r -> Validation.isNullOrEmpty(r.username), "username cannot be null or empty");
        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        RegisterRequest req = validator.get();

        try (Handle dbConnection = Archery.getJdbi().open()){

            UserQuery userQuery = dbConnection.attach(UserQuery.class);

            User user = userQuery.getUserByUsername(req.username);

            if (user != null){
                ctx.status(409).json(new ErrorResponse("USER_ALREADY_EXISTS", "Dieser Benutzer existiert bereits"));
                return;
            }




            //    dbConnection.commit();
        }

        //todo proper exception handling
        //todo proper error handling

        System.out.println(req.firstName);

    }
}
