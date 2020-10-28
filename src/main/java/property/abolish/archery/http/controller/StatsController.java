package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.query.EventQuery;

public class StatsController {
    public static void handleGetEventStats(Context ctx) {
        int eventId = ctx.pathParam("eventId", Integer.class).get();

        try (Handle dbConnection = Archery.getConnection()) {



            //TODO: Summe aller Punkte
            // Accuracy
            // Average
            // for every player

        }
    }

    public static void handleGetOverallStats(Context ctx) {


        /*
        /stats/numbers
        /stats/graph
         */

    }
}
