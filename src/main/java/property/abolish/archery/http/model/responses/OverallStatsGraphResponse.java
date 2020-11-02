package property.abolish.archery.http.model.responses;

import java.util.List;

public class OverallStatsGraphResponse {
    public OverallStatsGraphResponse(List<Double> graphEntries) {
        this.graphEntries = graphEntries;
    }

    public List<Double> graphEntries;
}
