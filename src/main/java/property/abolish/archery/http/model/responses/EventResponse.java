package property.abolish.archery.http.model.responses;

import property.abolish.archery.http.model.requests.ShotRequest;

import java.util.List;

public class EventResponse extends SuccessResponse {

    public int eventId;
    public boolean eventIsFinished;
    public EventResponse.GameMode gameMode;
    public EventResponse.Parkour parkour;
    public List<Member> members;

    public static class Member extends GetUsersResponse.UserInfo {
        public List<Shot> shots;
    }

    public static class Shot {
        public int animalNumber;
        public List<ShotRequest.ShotInfo> shotInfos;
    }

    public static class GameMode {
        public GameMode(int id, String gameMode) {
            this.id = id;
            this.gameMode = gameMode;
        }

        public int id;
        public String gameMode;
    }

    public static class Parkour {
        public Parkour(int id, String parkourName, int countAnimals) {
            this.id = id;
            this.parkourName = parkourName;
            this.countAnimals = countAnimals;
        }

        public int id;
        public String parkourName;
        public int countAnimals;
    }
}
