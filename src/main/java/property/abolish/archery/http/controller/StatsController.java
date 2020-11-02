package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.query.EventQuery;
import property.abolish.archery.db.query.GameModeQuery;
import property.abolish.archery.db.query.ShotQuery;
import property.abolish.archery.http.model.responses.ErrorResponse;
import property.abolish.archery.http.model.responses.EventStatsResponse;
import property.abolish.archery.http.model.responses.OverallStatsGraphResponse;

public class StatsController {
    public static void handleGetEventStats(Context ctx) {
        int eventId = ctx.pathParam("eventId", Integer.class).get();

        try (Handle dbConnection = Archery.getConnection()) {

            Event event = dbConnection.attach(EventQuery.class).getEventByEventId(eventId);

            if (event == null) {
                ctx.status(404).json(new ErrorResponse("EVENT_NOT_FOUND", "The event does not exist"));
                return;
            }

            if (event.getTimestampEnd() == null) {
                ctx.status(409).json(new ErrorResponse("EVENT_NOT_FINISHED", "The event is still ongoing"));
                return;
            }

            ctx.json(new EventStatsResponse(dbConnection.attach(ShotQuery.class).getEventStats(eventId)));
        }
    }

    public static void handleGetOverallStatsNumbers(Context ctx) {
        try (Handle dbConnection = Archery.getConnection()) {
            User user = ctx.use(User.class);
            int gameModeId = ctx.pathParam("gameModeId", Integer.class).get();

            if (!isGameModeValid(gameModeId, dbConnection, ctx)){
                return;
            }

            ctx.json(dbConnection.attach(ShotQuery.class).getOverallStatsNumbers(user.getId(), gameModeId));
        }
    }

    public static void handleGetOverallStatsGraph(Context ctx) {
        try (Handle dbConnection = Archery.getConnection()) {
            User user = ctx.use(User.class);
            int gameModeId = ctx.pathParam("gameModeId", Integer.class).get();

            if (!isGameModeValid(gameModeId, dbConnection, ctx)){
                return;
            }

            ctx.json(new OverallStatsGraphResponse(dbConnection.attach(ShotQuery.class).getOverallStatsGraphEntries(user.getId(), gameModeId)));
        }
    }

    private static boolean isGameModeValid(int gameModeId, Handle dbConnection, Context ctx) {
        if (dbConnection.attach(GameModeQuery.class).getGameModeById(gameModeId) == null) {
            ctx.status(404).json(new ErrorResponse("GAMEMODE_DOES_NOT_EXIST", "The gameMode does not exist"));
            return false;
        }
        return true;
    }
}
