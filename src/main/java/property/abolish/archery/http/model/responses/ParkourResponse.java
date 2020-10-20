package property.abolish.archery.http.model.responses;

import property.abolish.archery.db.model.Parkour;

import java.util.List;

public class ParkourResponse extends SuccessResponse {
    public List<Parkour> parkours;

    public ParkourResponse(List<Parkour> parkours) {
        this.parkours = parkours;
    }
}
