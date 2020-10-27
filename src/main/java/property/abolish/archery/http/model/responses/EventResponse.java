package property.abolish.archery.http.model.responses;

import property.abolish.archery.http.model.requests.ShotRequest;

import java.util.List;

public class EventResponse extends SuccessResponse {

    public boolean eventIsFinished;
    public int gameModeId;
    public int parkourId;
    public List<Member> members;

    public static class Member extends GetUsersResponse.UserInfo {
        public List<Shots> shots;
    }

    public static class Shots {
        public int animalNumber;
        public List<ShotRequest.ShotInfo> shotInfos;
    }
}
