package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.*;
import property.abolish.archery.db.query.*;
import property.abolish.archery.http.model.requests.EventRequest;
import property.abolish.archery.http.model.requests.ShotRequest;
import property.abolish.archery.http.model.responses.*;
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
                eventInfo.timestamp = event.getTimestamp();

                if (event.getTimestampEnd() != null)
                    eventInfo.timestampEnd = event.getTimestampEnd();

                Parkour parkour = parkourQuery.getParkourById(event.getParkourId());
                eventInfo.parkour = parkour.getName();
                eventInfo.totalAnimals = parkour.getCountAnimals();

                GameMode gameMode = gameModeQuery.getGameModeById(event.getGamemodeId());
                eventInfo.gameMode = gameMode.getGameMode();

                Shot shot = shotQuery.getLatestShot(event.getId());

                if (shot != null)
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
                .check(r -> r.gameModeId != 0, "gamemodeId cannot be zero");
        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        try (Handle dbConnection = Archery.getConnection()) {
            EventRequest req = validator.get();
            Event event = new Event();

            event.setGamemodeId(req.gameModeId);
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
        try (Handle dbConnection = Archery.getConnection()) {
            int eventId = ctx.pathParam("eventId", Integer.class).get();
            EventQuery eventQuery = dbConnection.attach(EventQuery.class);
            Event event = eventQuery.getEventByEventId(eventId);

            if (event == null) {
                ctx.status(400).json(new ErrorResponse("EVENT_DOES_NOT_EXIST", "The event does not exist"));
                return;
            }

            EventResponse eventResponse = new EventResponse();
            eventResponse.gameModeId = event.getGamemodeId();
            eventResponse.parkourId = event.getParkourId();
            eventResponse.eventIsFinished = event.getTimestampEnd() != null;

            List<EventResponse.Member> member = new ArrayList<>();
            for (User user : eventQuery.getEventMembersByEventId(eventId)) {
                EventResponse.Member memberTemp = new EventResponse.Member();
                memberTemp.username = user.getUsername();
                memberTemp.firstName = user.getFirstName();
                memberTemp.lastName = user.getLastName();

                List<Shot> shots = dbConnection.attach(ShotQuery.class).getShots(eventId, user.getId());

                if (shots != null) {
                    int animalNumberTemp = 1;
                    List<EventResponse.Shots> shotsResponse = new ArrayList<>();
                    List<ShotRequest.ShotInfo> shotInfos = new ArrayList<>();

                    for (Shot shot : shots) {
                        EventResponse.Shots shotsResponseTemp = new EventResponse.Shots();
                        ShotRequest.ShotInfo shotInfo = new ShotRequest.ShotInfo();

                        shotInfo.shotNumber = shot.getShotNumber();
                        shotInfo.points = shot.getPoints();
                        shotInfos.add(shotInfo);

                        if (animalNumberTemp != shot.getAnimalNumber()) {
                            shotsResponseTemp.animalNumber = shot.getAnimalNumber();
                            shotsResponseTemp.shotInfos = shotInfos;
                            shotsResponse.add(shotsResponseTemp);
                            animalNumberTemp = shot.getAnimalNumber();
                        }
                    }
                    memberTemp.shots = shotsResponse;
                }
                member.add(memberTemp);
            }
            eventResponse.members = member;
            ctx.json(eventResponse);
        }
    }

    public static void handleGetGameModes(Context ctx) {
        try (Handle dbConnection = Archery.getConnection()) {
            GameModeQuery gameModeQuery = dbConnection.attach(GameModeQuery.class);

            ctx.json(new GameModeResponse(General.copyLists(gameModeQuery.getGameModes(), GameModeResponse.GameModeInfo.class)));
        }
    }
}

