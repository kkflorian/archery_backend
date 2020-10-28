package property.abolish.archery.db.model;

public class EventStats {
    private int userId;
    private int averagePoints;
    private int totalPoints;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAveragePoints() {
        return averagePoints;
    }

    public void setAveragePoints(int averagePoints) {
        this.averagePoints = averagePoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
