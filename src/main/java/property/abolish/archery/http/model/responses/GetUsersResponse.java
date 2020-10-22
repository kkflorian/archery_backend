package property.abolish.archery.http.model.responses;

import java.util.List;

public class GetUsersResponse extends SuccessResponse {

    public GetUsersResponse(List<UserInfo> userList) {
        this.userList = userList;
    }

    public List<UserInfo> userList;

    public static class UserInfo {
        public String username;
        public String lastName;
        public String firstName;
    }
}
