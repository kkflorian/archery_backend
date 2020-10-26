package property.abolish.archery.http.model.responses;

import java.util.List;

public class GameModeResponse extends SuccessResponse {
    public GameModeResponse(List<GameModeInfo> gamemodes) {
        this.gamemodes = gamemodes;
    }

    List<GameModeInfo> gamemodes;

    public static class GameModeInfo{
        public int id;
        public String gamemode;
    }
}
