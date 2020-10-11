package property.abolish.archery.db.model;

import java.time.Instant;

public class Event {

    private int id;
    private int parkourId;
    private Instant timestamp;
    private int gamemodeId;
    private int userIdCreator;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParkourId() {
        return parkourId;
    }

    public void setParkourId(int parkourId) {
        this.parkourId = parkourId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getGamemodeId() {
        return gamemodeId;
    }

    public void setGamemodeId(int gamemodeId) {
        this.gamemodeId = gamemodeId;
    }

    public int getUserIdCreator() {
        return userIdCreator;
    }

    public void setUserIdCreator(int userIdCreator) {
        this.userIdCreator = userIdCreator;
    }


}
