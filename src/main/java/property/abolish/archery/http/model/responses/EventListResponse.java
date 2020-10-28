package property.abolish.archery.http.model.responses;

import java.time.Instant;
import java.util.List;

public class EventListResponse extends SuccessResponse {
    public List<EventInfo> eventInfos;

    public EventListResponse(List<EventInfo> eventInfos) {
        this.eventInfos = eventInfos;
    }

    public static class EventInfo {
        public String parkour;
        public Instant timestamp;
        public Instant timestampEnd;
        public String[] creator;
        public List<String[]> member;
        public int eventId;
        public String gameMode;
        public int currentAnimal;
        public int totalAnimals;
    }
}
