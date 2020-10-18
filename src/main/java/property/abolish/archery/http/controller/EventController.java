package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.model.Parkour;
import property.abolish.archery.db.model.User;
import property.abolish.archery.db.query.EventQuery;
import property.abolish.archery.db.query.ParkourQuery;
import property.abolish.archery.db.query.UserQuery;
import property.abolish.archery.http.model.*;
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

            ParkourQuery parkourQuery = dbConnection.attach(ParkourQuery.class);
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            List<EventInfos> listOfEventInfos = null;

            for (Event event : eventList) {
                EventInfos eventInfos = new EventInfos();
                eventInfos.timestamp = event.getTimestamp();

                Parkour parkour = parkourQuery.getParkourById(event.getParkourId());
                eventInfos.parkour = parkour.getName();

                User creator =  userQuery.getUserByUserId(event.getUserIdCreator());
                String[] creatorInfo = {creator.getUsername(), creator.getFirstName(), creator.getLastName()};
                eventInfos.creator = creatorInfo;

                List<User> members = eventQuery.getEventMembersByEventId(event.getId());
                List<String[]> memberInfo = null;
                for (User member: members) {
                    String[] temp = {member.getUsername(), member.getFirstName(), member.getLastName()};
                    memberInfo.add(temp);
                }

                eventInfos.member = memberInfo;

                listOfEventInfos.add(eventInfos);
            }

            ctx.json(new EventListResponse(listOfEventInfos));
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
            int eventId = eventQuery.insertEvent(event);

            // Add event members
            String eventMember = req.eventMember.stream()
                    .map(n -> String.valueOf(n))
                    .collect(Collectors.joining("\",\"", "(\"", "\")"));

            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            List<User> users = userQuery.getUsersByUsername(eventMember);

            List<Integer> membersId = null;
            StringBuilder eventMemberSQL = new StringBuilder("(");

            for (User member: users){
                eventMemberSQL.append(eventId).append(",").append(member.getId());
                    eventMemberSQL.append("),(");
            }

            eventMemberSQL.append(eventId).append(",").append(user.getId()).append(")");

            eventQuery.insertEventMember(eventMemberSQL.toString());

            dbConnection.commit();
            ctx.json(new SuccessResponse());
        }
    }
}

