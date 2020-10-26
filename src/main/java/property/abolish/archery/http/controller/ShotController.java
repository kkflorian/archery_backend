package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.model.Shot;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.query.EventQuery;
import property.abolish.archery.db.query.ShotQuery;
import property.abolish.archery.db.query.UserQuery;
import property.abolish.archery.http.model.misc.PointsDreipfeilwertung;
import property.abolish.archery.http.model.misc.PointsZweipfeilwertung;
import property.abolish.archery.http.model.requests.ShotRequest;
import property.abolish.archery.http.model.responses.ErrorResponse;
import property.abolish.archery.http.model.responses.SuccessResponse;
import property.abolish.archery.utilities.Validation;


public class ShotController {


    public static void handleAddShot(Context ctx) {
        ShotRequest req = ctx.bodyValidator(ShotRequest.class)
                .check(r -> Validation.checkList("shots", r.shots, v -> v
                        .check(si -> si.animalNumber > 0, "animalNumber cannot less or equal to zero")
                        .check(si -> si.points >= 0, "points cannot be less than zero")
                        .check(si -> si.shotNumber > 0, "shotNumber cannot be less or equal to zero")),
                        "shots cannot be null")
                .check(r -> !Validation.isNullOrEmpty(r.username))
                .get();

        try (Handle dbConnection = Archery.getConnection()) {
            int eventId = ctx.pathParam("eventId", Integer.class).get();
            EventQuery eventQuery = dbConnection.attach(EventQuery.class);
            Event event = eventQuery.getEventByEventId(eventId);
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            User user = userQuery.getUserByUsername(req.username);

            if (user == null){
                ctx.json(new ErrorResponse("USER_NOT_FOUND", "This user does not exist"));
                return;
            }

            // Dreipfeilwertung
            if (event.getGamemodeId() == 1) {
                if (req.shots.size() < 1 || req.shots.size() > 3){
                    ctx.json(new ErrorResponse("VALIDATION_ERROR", "Only 1-3 shots are allowed"));
                    return;
                }

                for (int i = 0; i < req.shots.size(); i++){
                    if (i < req.shots.size() - 1 && req.shots.get(i).points != 0) {
                        ctx.json(new ErrorResponse("VALIDATION_ERROR","Only the last shot may contain points"));
                        return;
                    }
                }

                int indexOfLastShot = req.shots.size() - 1;
                if (req.shots.get(indexOfLastShot).points > 0 && !(new PointsDreipfeilwertung().points.get(indexOfLastShot).contains(req.shots.get(indexOfLastShot).points))){
                    ctx.json(new ErrorResponse("VALIDATION_ERROR", "Value of points is incorrect"));
                    return;
                }

                ShotQuery shotQuery = dbConnection.attach(ShotQuery.class);

                insertShotToDatabase(req, eventId, user, shotQuery);
            }
            // Zweipfeilwertung
            else if (event.getGamemodeId() == 2) {
                if (req.shots.size() != 2){
                    ctx.json(new ErrorResponse("VALIDATION_ERROR", "There have to be 2 shots"));
                    return;
                }

                for(ShotRequest.ShotInfo shotInfo : req.shots){
                    if (shotInfo.points > 0 && !(new PointsZweipfeilwertung().points.contains(shotInfo.points))){
                        new ErrorResponse("VALIDATION_ERROR", "Value of points is incorrect");
                        return;
                    }
                }

                ShotQuery shotQuery = dbConnection.attach(ShotQuery.class);
                insertShotToDatabase(req, eventId, user, shotQuery);
            }

            //TODO: event beenden wenn letzter spieler geschossen hat

            ctx.json(new SuccessResponse());
        }
    }

    private static void insertShotToDatabase(ShotRequest req, int eventId, User user, ShotQuery shotQuery) {
        for(ShotRequest.ShotInfo shotInfo : req.shots){
            Shot shot = new Shot();
            shot.setAnimalNumber(shotInfo.animalNumber);
            shot.setPoints(shotInfo.points);
            shot.setShotNumber(shotInfo.shotNumber);
            shot.setUserId(user.getId());
            shot.setEventId(eventId);

            shotQuery.insertShot(shot);
        }
    }
}
