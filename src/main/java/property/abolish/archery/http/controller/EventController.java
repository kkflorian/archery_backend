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
            List<Event> eventList = eventQuery.getEventListByUserId(user.getId());

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
                eventInfo.currentAnimal = shot == null ? 1 : shot.getAnimalNumber();

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
        EventRequest req = ctx.bodyValidator(EventRequest.class)
                .check(r -> r.parkourId != 0, "parkourId cannot be zero")
                .check(r -> r.gameModeId != 0, "gamemodeId cannot be zero")
                .get();

        try (Handle dbConnection = Archery.getConnection()) {
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

            // Also add event creator as member
            eventMemberSQL.add(new EventMember(eventId, user.getId()));

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
            User currentUser = ctx.use(User.class);

            if (event == null) {
                ctx.status(400).json(new ErrorResponse("EVENT_DOES_NOT_EXIST", "The event does not exist"));
                return;
            }

            EventResponse eventResponse = new EventResponse();
            eventResponse.eventId = event.getId();

            eventResponse.isCreator = currentUser.getId() == event.getUserIdCreator();

            GameMode gameMode = dbConnection.attach(GameModeQuery.class).getGameModeById(event.getGamemodeId());
            eventResponse.gameMode = new EventResponse.GameMode(gameMode.getId(), gameMode.getGameMode());

            Parkour parkour = dbConnection.attach(ParkourQuery.class).getParkourById(event.getParkourId());
            eventResponse.parkour = new EventResponse.Parkour(parkour.getId(), parkour.getName(), parkour.getCountAnimals());

            eventResponse.eventIsFinished = event.getTimestampEnd() != null;

            List<EventResponse.Member> member = new ArrayList<>();
            for (User user : eventQuery.getEventMembersByEventId(eventId)) {
                EventResponse.Member memberTemp = new EventResponse.Member();
                memberTemp.username = user.getUsername();
                memberTemp.firstName = user.getFirstName();
                memberTemp.lastName = user.getLastName();

                List<Shot> shots = dbConnection.attach(ShotQuery.class).getShots(eventId, user.getId());

                if (shots.size() > 0) {
                    int animalNumberTemp = 1;
                    List<EventResponse.Shot> shotResponse = new ArrayList<>();
                    List<ShotRequest.ShotInfo> shotInfos = new ArrayList<>();

                    for (int i = 0; i <= shots.size(); i++) {
                        Shot shot = new Shot();
                        if (i < shots.size()) {
                            shot = shots.get(i);
                        }

                        EventResponse.Shot shotResponseTemp = new EventResponse.Shot();
                        ShotRequest.ShotInfo shotInfo = new ShotRequest.ShotInfo();

                        if (animalNumberTemp != shot.getAnimalNumber()) {
                            shotResponseTemp.animalNumber = animalNumberTemp;
                            shotResponseTemp.shotInfos = shotInfos;
                            shotInfos = new ArrayList<>();
                            shotResponse.add(shotResponseTemp);
                            animalNumberTemp = shot.getAnimalNumber();
                        }

                        shotInfo.shotNumber = shot.getShotNumber();
                        shotInfo.points = shot.getPoints();
                        shotInfos.add(shotInfo);
                    }
                    memberTemp.shots = shotResponse;
                } else {
                    memberTemp.shots = new ArrayList<>();
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

