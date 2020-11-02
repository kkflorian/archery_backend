package property.abolish.archery.http.misc;

import io.javalin.http.Context;
import property.abolish.archery.http.model.requests.ShotRequest;
import property.abolish.archery.http.model.responses.ErrorResponse;

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

    public static boolean ValidateInput(Context ctx, ShotRequest req) {
        if (req.shots.size() < 1 || req.shots.size() > 3){
            ctx.status(400).json(new ErrorResponse("VALIDATION_ERROR", "Only 1-3 shots are allowed"));
            return false;
        }

        for (int i = 0; i < req.shots.size(); i++){
            if (i < req.shots.size() - 1 && req.shots.get(i).points != 0) {
                ctx.status(400).json(new ErrorResponse("VALIDATION_ERROR","Only the last shot may contain points"));
                return false;
            }
        }

        int indexOfLastShot = req.shots.size() - 1;
        if (req.shots.get(indexOfLastShot).points > 0 && !(new PointsDreipfeilwertung().points.get(indexOfLastShot).contains(req.shots.get(indexOfLastShot).points))){
            ctx.status(400).json(new ErrorResponse("VALIDATION_ERROR", "Value of points is incorrect"));
            return false;
        }
        return true;
    }
}
