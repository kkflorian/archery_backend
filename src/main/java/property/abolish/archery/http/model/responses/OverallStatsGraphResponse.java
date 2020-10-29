package property.abolish.archery.http.model.responses;

import java.util.List;

public class OverallStatsGraphResponse {
    public OverallStatsGraphResponse(List<Integer> graphEntries) {
        this.graphEntries = graphEntries;
    }

    public List<Integer> graphEntries;
}
