package property.abolish.archery.http.model.responses;

import java.util.List;

public class GameModeResponse extends SuccessResponse {
    public GameModeResponse(List<GameModeInfo> gameModes) {
        this.gameModes = gameModes;
    }

    List<GameModeInfo> gameModes;

    public static class GameModeInfo{
        public int id;
        public String gameMode;
    }
}
