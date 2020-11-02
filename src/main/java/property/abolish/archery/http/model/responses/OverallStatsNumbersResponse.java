package property.abolish.archery.http.model.responses;

public class OverallStatsNumbersResponse {
    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public int getTotalShots() {
        return totalShots;
    }

    public void setTotalShots(int totalShots) {
        this.totalShots = totalShots;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public double getAverageOverall() {
        return averageOverall;
    }

    public void setAverageOverall(double averageOverall) {
        this.averageOverall = averageOverall;
    }

    public int totalHits;
    public int totalShots;
    public int totalEvents;
    public double averageOverall;
}