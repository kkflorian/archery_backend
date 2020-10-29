package property.abolish.archery.http.model.responses;

import property.abolish.archery.db.model.EventStats;

import java.util.List;

public class EventStatsResponse extends SuccessResponse {
    public EventStatsResponse(List<EventStats> stats) {
        this.stats = stats;
    }

    public List<EventStats> stats;
}
