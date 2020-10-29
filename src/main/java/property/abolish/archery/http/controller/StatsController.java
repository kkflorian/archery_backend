package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.query.ShotQuery;
import property.abolish.archery.http.model.responses.EventStatsResponse;

public class StatsController {
    public static void handleGetEventStats(Context ctx) {
        int eventId = ctx.pathParam("eventId", Integer.class).get();

        try (Handle dbConnection = Archery.getConnection()) {
            ctx.json(new EventStatsResponse(dbConnection.attach(ShotQuery.class).getEventStats(eventId)));
        }
    }

    public static void handleGetOverallStatsNumbers(Context ctx) {
        
    }

    public static void handleGetOverallStatsGraph(Context ctx) {

    }
}
