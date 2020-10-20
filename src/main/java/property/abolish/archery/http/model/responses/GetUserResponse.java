package property.abolish.archery.http.model.responses;

public class GetUserResponse extends SuccessResponse {
    public String username;

    public GetUserResponse(String username) {
        this.username = username;
    }
}
