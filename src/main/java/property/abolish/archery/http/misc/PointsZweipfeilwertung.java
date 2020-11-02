package property.abolish.archery.http.misc;

import io.javalin.http.Context;
import property.abolish.archery.http.model.requests.ShotRequest;
import property.abolish.archery.http.model.responses.ErrorResponse;

import java.util.Arrays;
import java.util.List;

public class PointsZweipfeilwertung {
    public List<Integer> points = Arrays.asList(11, 10, 8, 5);

    public static boolean ValidateInput(Context ctx, ShotRequest req) {
        if (req.shots.size() != 2){
            ctx.status(400).json(new ErrorResponse("VALIDATION_ERROR", "There have to be 2 shots"));
            return false;
        }

        for(ShotRequest.ShotInfo shotInfo : req.shots){
            if (shotInfo.points > 0 && !(new PointsZweipfeilwertung().points.contains(shotInfo.points))){
                ctx.status(400).json(new ErrorResponse("VALIDATION_ERROR", "Value of points is incorrect"));
                return false;
            }
        }
        return true;
    }
}
