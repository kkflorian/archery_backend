package property.abolish.archery.http.model.misc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointsDreipfeilwertung {

    public Map<Integer, List<Integer>> points = new HashMap<Integer, List<Integer>>() {{
        put(0, Arrays.asList(20, 18, 16));
        put(1, Arrays.asList(14, 12, 10));
        put(2, Arrays.asList(8, 6, 4));
    }};
}
