package property.abolish.archery.http.model.requests;

import java.util.List;

public class ShotRequest {
    public List<ShotInfo> shots;
    public String username;
    public int animalNumber;

    public static class ShotInfo {
        public int points;
        public int shotNumber;
    }
}
