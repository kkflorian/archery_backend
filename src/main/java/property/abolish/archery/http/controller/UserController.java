package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.JdbiException;
import property.abolish.archery.Archery;
import property.abolish.archery.http.model.ErrorResponse;
import property.abolish.archery.http.model.RegisterRequest;

import java.util.List;
import java.util.Map;

public class UserController {


    public static void handleLogin(Context ctx) {
        ctx.result("hello world");
    }

    public static void handleRegister(Context ctx) {
        Validator<RegisterRequest> validator = ctx.bodyValidator(RegisterRequest.class)
                .check(r -> r.firstName != null, "alarm");
        if (validator.hasError()) {
            Map<String, List<String>> errors = validator.errors();
            String error = errors.values().iterator().next().get(0);
            //"ERRORCODE: VALIDATION_ERROR"
            // Error MEssage: error


            return;
        }



        RegisterRequest req = validator.get();

        try (Handle dbConnection = Archery.getJdbi().open()){



            dbConnection.commit();
        }

        //todo proper exception handling
        //todo proper error handling

        System.out.println(req.firstName);
        ctx.json(new ErrorResponse("mama", "hilfe"));
    }
}
