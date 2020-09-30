package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import property.abolish.archery.http.model.ErrorResponse;
import property.abolish.archery.http.model.RegisterRequest;

public class UserController {


    public static void handleLogin(Context ctx) {
        ctx.result("hello world");
    }

    public static void handleRegister(Context ctx) {
        RegisterRequest req = ctx.bodyValidator(RegisterRequest.class)
                .check(r -> r.firstName != null, "alarm")
                .get();

        //todo proper exception handling
        //todo proper error handling

        System.out.println(req.firstName);
        ctx.json(new ErrorResponse("mama", "hilfe"));
    }
}
