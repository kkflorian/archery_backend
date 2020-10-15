package property.abolish.archery.http.controller;

import com.google.gson.Gson;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.query.EventQuery;
import property.abolish.archery.http.model.ErrorResponse;
import property.abolish.archery.http.model.EventRequest;
import property.abolish.archery.http.model.SuccessResponse;
import property.abolish.archery.utilities.Validation;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class EventController {

    public static void handleGetEventList(Context ctx) {
        User user = ctx.use(User.class);

        try (Handle dbConnection = Archery.getConnection()) {
            EventQuery eventQuery = dbConnection.attach(EventQuery.class);
            List<Event> eventList = eventQuery.getEventListbyUserId(user.getId());

            if (eventList == null) {
                ctx.status(418).json(new ErrorResponse("NO_EVENTS_FOUND", "No previous events found"));
                return;
            }

            Gson gson = new Gson();

            for (Event event : eventList) {
                List<User> members = eventQuery.getEventMembersByEventId(event.getId());
                String test = gson.toJson(members);
                //TODO: handleCreateEvent zuerst implementieren
            }

            /*

             */
        }
    }

    public static void handleCreateEvent(Context ctx) {
        Validator<EventRequest> validator = ctx.bodyValidator(EventRequest.class)
                .check(r -> !(r.parkourId == 0), "parkourId cannot be zero")
                .check(r -> !(r.gamemodeId == 0), "gamemodeId cannot be zero");
        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        try (Handle dbConnection = Archery.getConnection()) {
            EventRequest req = validator.get();
            Event event = new Event();

            event.setGamemodeId(req.gamemodeId);
            event.setParkourId(req.parkourId);
            event.setTimestamp(Instant.now());

            User user = ctx.use(User.class);
            event.setUserIdCreator(user.getId());

            EventQuery eventQuery = dbConnection.attach(EventQuery.class);
            int evendId = eventQuery.insertEvent(event);

            // Add event members
            String eventMember = req.eventMember.stream()
                    .map(n -> String.valueOf(n))
                    .collect(Collectors.joining("\",\"", "(\"", "\")"));



            dbConnection.commit();
            ctx.json(new SuccessResponse());
        }
    }
}
