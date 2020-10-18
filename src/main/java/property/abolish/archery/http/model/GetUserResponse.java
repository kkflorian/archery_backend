package property.abolish.archery.http.model;

public class GetUserResponse extends SuccessResponse {
    public String username;

    public GetUserResponse(String username) {
        this.username = username;
    }
}
