package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.*;
import property.abolish.archery.db.query.*;
import property.abolish.archery.http.model.requests.EventRequest;
import property.abolish.archery.http.model.responses.CreateEventResponse;
import property.abolish.archery.http.model.responses.ErrorResponse;
import property.abolish.archery.http.model.responses.EventListResponse;
import property.abolish.archery.http.model.responses.GameModeResponse;
import property.abolish.archery.utilities.General;
import property.abolish.archery.utilities.Validation;

import java.time.Instant;
import java.util.ArrayList;
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
            GameModeQuery gameModeQuery = dbConnection.attach(GameModeQuery.class);
            ShotQuery shotQuery = dbConnection.attach(ShotQuery.class);
            List<EventListResponse.EventInfo> listOfEventInfos = new ArrayList<>();

            for (Event event : eventList) {
                EventListResponse.EventInfo eventInfo = new EventListResponse.EventInfo();
                eventInfo.eventId = event.getId();
                eventInfo.timestamp = event.getTimestamp().toEpochMilli();
                eventInfo.timestampEnd = event.getTimestampEnd().toEpochMilli();

                Parkour parkour = parkourQuery.getParkourById(event.getParkourId());
                eventInfo.parkour = parkour.getName();
                eventInfo.totalAnimals = parkour.getCountAnimals();

                GameMode gameMode = gameModeQuery.getGameModeById(event.getGamemodeId());
                eventInfo.gameMode = gameMode.getGamemode();

                Shot shot = shotQuery.getLatestShot(event.getId());
                eventInfo.currentAnimal = shot.getAnimalNumber();

                User creator = userQuery.getUserByUserId(event.getUserIdCreator());
                eventInfo.creator = new String[]{creator.getUsername(), creator.getFirstName(), creator.getLastName()};

                List<User> members = eventQuery.getEventMembersByEventId(event.getId());
                List<String[]> memberInfo = new ArrayList<>();
                for (User member : members) {
                    String[] temp = {member.getUsername(), member.getFirstName(), member.getLastName()};
                    memberInfo.add(temp);
                }

                eventInfo.member = memberInfo;

                listOfEventInfos.add(eventInfo);
            }

            ctx.json(new EventListResponse(listOfEventInfos));
        }
    }

    public static void handleCreateEvent(Context ctx) {
        Validator<EventRequest> validator = ctx.bodyValidator(EventRequest.class)
                .check(r -> r.parkourId != 0, "parkourId cannot be zero")
                .check(r -> r.gamemodeId != 0, "gamemodeId cannot be zero");
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
            UserQuery userQuery = dbConnection.attach(UserQuery.class);
            req.eventMember.replaceAll(String::toUpperCase);

            List<EventMember> eventMemberSQL = userQuery.getUsersByUsernames(req.eventMember).stream()
                    .map(u -> new EventMember(eventId, u.getId()))
                    .collect(Collectors.toList());

            eventQuery.insertEventMember(eventMemberSQL);

            dbConnection.commit();
            ctx.json(new CreateEventResponse(eventId));
        }
    }

    public static void handleGetEventInfo(Context ctx) {

    }

    public static void handleGetGameModes(Context ctx) {
        try (Handle dbConnection = Archery.getConnection()){
            GameModeQuery gameModeQuery = dbConnection.attach(GameModeQuery.class);

            ctx.json(new GameModeResponse(General.copyLists(gameModeQuery.getGameModes(), GameModeResponse.GameModeInfo.class)));
        }
    }
}

