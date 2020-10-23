package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.http.model.requests.ShotRequest;
import property.abolish.archery.utilities.Validation;

public class ShotController {


    public static void handleAddShot(Context ctx) {
        ShotRequest req = ctx.bodyValidator(ShotRequest.class)
                .check(r -> Validation.checkList("shots", r.shots, v -> v
                        .check(si -> !Validation.isNullOrEmpty(si.username), "username cannot be null or empty")
                        .check(si -> si.animalNumber > 0, "animalNumber cannot less or equal to zero")
                        .check(si -> si.points >= 0, "points cannot be less than zero")
                        .check(si -> si.shotNumber > 0, "shotNumber cannot be less or equal to zero")),
                        "shots cannot be null")
                .get();

        try (Handle dbConnection = Archery.getConnection()) {
//            int eventId = ctx.pathParam("eventId", Integer.class).get();
//
//            Shot shot = new Shot();
//            shot.setEventId(eventId);
//            shot.setAnimalNumber(req.animalNumber);
//            shot.setShotNumber(req.shotNumber);
//            shot.setPoints(req.points);
//
//            UserQuery userQuery = dbConnection.attach(UserQuery.class);
//            shot.setUserId(userQuery.getUserByUsername(req.username).getId());
//
//            ShotQuery shotQuery = dbConnection.attach(ShotQuery.class);
//            shotQuery.insertShot(shot);
        }
    }
}
