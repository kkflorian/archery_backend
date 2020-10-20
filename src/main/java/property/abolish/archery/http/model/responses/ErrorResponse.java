package property.abolish.archery.http.model.responses;

public class ErrorResponse {
    public String status = "error";
    public String errorCode;
    public String message;

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /*
    errorCode: USER_NOT_FOUND, message: User could not be found
     */
}
