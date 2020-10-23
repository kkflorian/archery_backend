package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Shot;
import property.abolish.archery.db.query.ShotQuery;
import property.abolish.archery.db.query.UserQuery;
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

        try (Handle dbConnection = Archery.getConnection()) {
            int eventId = ctx.pathParam("eventId", Integer.class).get();

            Shot shot = new Shot();
            shot.setEventId(eventId);
            shot.setAnimalNumber(req.animalNumber);
            shot.setShotNumber(req.shotNumber);
            shot.setPoints(req.points);

            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            shot.setUserId(userQuery.getUserByUsername(req.username).getId());

            ShotQuery shotQuery = dbConnection.attach(ShotQuery.class);
            shotQuery.insertShot(shot);
        }
    }
}
