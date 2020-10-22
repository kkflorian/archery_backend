package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import property.abolish.archery.http.model.requests.GetUsersRequest;
import property.abolish.archery.http.model.requests.ShotRequest;
import property.abolish.archery.utilities.Validation;

public class ShotController {

    public static void handleAddShot(Context ctx) {
        ShotRequest req = ctx.bodyValidator(ShotRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.username), "username cannot be null or empty")
                .check(r -> r.animalNumber > 0, "animalNumber cannot less or equal to zero")
                .check(r -> r.points >= 0, "points cannot be less than zero")
                .check(r -> r.shotNumber > 0, "shotNumber cannot be less or equal to zero")
                .get();

        int eventId = ctx.pathParam("eventId", Integer.class).get();

    }
}
