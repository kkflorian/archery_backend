package property.abolish.archery.http.model.requests;

import java.util.List;

public class ShotRequest {
    public List<ShotInfo> shots;

    public static class ShotInfo {
        public String username;
        public int animalNumber;
        public int points;
        public int shotNumber;
    }
}
