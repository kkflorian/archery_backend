package property.abolish.archery.http.controller;

import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.query.EventQuery;
import property.abolish.archery.http.model.ErrorResponse;

import java.util.List;

public class EventController {

    public static void handleGetEventList(Context ctx) {
        User user = ctx.use(User.class);

        Handle dbConnection = Archery.getConnection();
        EventQuery eventQuery = dbConnection.attach(EventQuery.class);
        List<Event> eventList = eventQuery.getEventListbyUserId(user.getId());

        if (eventList == null){
            ctx.status(418).json(new ErrorResponse("NO_EVENTS_FOUND", "No previous events found"));
            return;
        }

        /*

        */
    }
}
