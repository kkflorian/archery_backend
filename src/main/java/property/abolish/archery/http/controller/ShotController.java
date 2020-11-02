package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.*;
import property.abolish.archery.db.query.*;
import property.abolish.archery.http.misc.PointsDreipfeilwertung;
import property.abolish.archery.http.misc.PointsZweipfeilwertung;
import property.abolish.archery.http.model.requests.ShotRequest;
import property.abolish.archery.http.model.responses.ErrorResponse;
import property.abolish.archery.http.model.responses.SuccessResponse;
import property.abolish.archery.utilities.Validation;

import java.time.Instant;
import java.util.List;


public class ShotController {

    public static void handleAddShot(Context ctx) {
        ShotRequest req = ctx.bodyValidator(ShotRequest.class)
                .check(r -> Validation.checkList("shots", r.shots, v -> v
                        .check(si -> si.points >= 0, "points cannot be less than zero")
                        .check(si -> si.shotNumber > 0, "shotNumber cannot be less or equal to zero")),
                        "shots cannot be null")
                .check(r -> !Validation.isNullOrEmpty(r.username))
                .check(r -> r.animalNumber > 0, "animalNumber cannot be less or equal to zero")
                .get();

        try (Handle dbConnection = Archery.getConnection()) {
            int eventId = ctx.pathParam("eventId", Integer.class).get();
            EventQuery eventQuery = dbConnection.attach(EventQuery.class);
            Event event = eventQuery.getEventByEventId(eventId);
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            User user = userQuery.getUserByUsername(req.username);

            if (user == null){
                ctx.status(404).json(new ErrorResponse("USER_NOT_FOUND", "This user does not exist"));
                return;
            }

            GameMode gameMode = dbConnection.attach(GameModeQuery.class).getGameModeById(event.getGamemodeId());
            if (gameMode == null) {
                ctx.status(404).json(new ErrorResponse("GAMEMODE_NOT_FOUND", "This gamemode does not exist"));
                return;
            }

            // Dreipfeilwertung
            if (event.getGamemodeId() == 1) {
                if (!PointsDreipfeilwertung.ValidateInput(ctx, req)) return;
            }
            // Zweipfeilwertung
            else if (event.getGamemodeId() == 2) {
                if (!PointsZweipfeilwertung.ValidateInput(ctx, req)) return;
            }

            ShotQuery shotQuery = dbConnection.attach(ShotQuery.class);
            List<Shot> shots = shotQuery.getShots(eventId, user.getId(), req.animalNumber);

            if (!shots.isEmpty()){
                ctx.status(409).json(new ErrorResponse("SHOT_ALREADY_EXISTS", "This animal was already played by this user"));
                return;
            }

            for(ShotRequest.ShotInfo shotInfo : req.shots){
                Shot shot = new Shot();
                shot.setAnimalNumber(req.animalNumber);
                shot.setPoints(shotInfo.points);
                shot.setShotNumber(shotInfo.shotNumber);
                shot.setUserId(user.getId());
                shot.setEventId(eventId);

                shotQuery.insertShot(shot);
            }

            // Finish event when last player is finished with the last animal
            List<User> eventMember = eventQuery.getEventMembersByEventId(eventId);
            if ((req.animalNumber == dbConnection.attach(ParkourQuery.class).getParkourById(event.getParkourId()).getCountAnimals())
                    && eventMember.get(eventMember.size() - 1).getId() == user.getId()) {

                    event.setTimestampEnd(Instant.now());
                    eventQuery.setEventAsFinished(event);
            }

            dbConnection.commit();

            ctx.json(new SuccessResponse());
        }
    }

}
