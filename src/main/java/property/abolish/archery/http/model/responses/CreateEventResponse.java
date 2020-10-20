package property.abolish.archery.http.model.responses;

public class CreateEventResponse extends SuccessResponse {
    public CreateEventResponse(int eventId) {
        this.eventId = eventId;
    }

    public int eventId;
}
