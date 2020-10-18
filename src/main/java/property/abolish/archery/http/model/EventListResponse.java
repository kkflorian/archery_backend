package property.abolish.archery.http.model;

import java.util.List;

public class EventListResponse extends SuccessResponse {
    public List<EventInfos> eventInfos;

    public EventListResponse(List<EventInfos> eventInfos) {
        this.eventInfos = eventInfos;
    }
}
