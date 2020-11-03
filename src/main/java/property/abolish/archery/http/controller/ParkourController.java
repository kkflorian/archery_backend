package property.abolish.archery.http.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Parkour;
import property.abolish.archery.db.query.ParkourQuery;
import property.abolish.archery.http.model.responses.ErrorResponse;
import property.abolish.archery.http.model.requests.ParkourRequest;
import property.abolish.archery.http.model.responses.ParkourResponse;
import property.abolish.archery.http.model.responses.SuccessResponse;
import property.abolish.archery.utilities.OSM;
import property.abolish.archery.utilities.Validation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ParkourController {
    public static void handleCreateParkour(Context ctx) {
        ParkourRequest req = ctx.bodyValidator(ParkourRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.name), "name cannot be null or empty")
                .check(r -> !(r.countAnimals == 0), "countAnimals cannot be zero")
                .check(r -> !Validation.isNullOrEmpty(r.countryCode), "countryCode cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.city), "city cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.street), "street cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.zip), "zip cannot be null or empty")
                .get();

        try (Handle dbConnection = Archery.getConnection()) {
            Parkour parkour = new Parkour();
            parkour.setCountAnimals(req.countAnimals);
            parkour.setCity(req.city);
            parkour.setCountryCode(req.countryCode);
            parkour.setName(req.name);
            parkour.setStreet(req.street);
            parkour.setZip(req.zip);

            // Check if parkour already exists
            ParkourQuery parkourQuery = dbConnection.attach(ParkourQuery.class);

            if (parkourQuery.getParkour(parkour) != null){
                ctx.status(409).json(new ErrorResponse("PARKOUR_ALREADY_EXISTS", "Dieser Parkour existiert bereits"));
                return;
            }

            StringBuilder urlBuilder = new StringBuilder("https://nominatim.openstreetmap.org/search?street=")
                    .append(URLEncoder.encode(parkour.getStreet(), "UTF-8"))
                    .append("&city=").append(URLEncoder.encode(parkour.getCity(), "UTF-8"))
                    .append("&countryCodes=").append(URLEncoder.encode(parkour.getCountryCode(), "UTF-8"))
                    .append("&postalcode=").append(URLEncoder.encode(parkour.getZip(), "UTF-8"))
                    .append("&format=json&limit=1");

            JsonArray locations = (JsonArray)OSM.httpGetAsJson(urlBuilder.toString());

            if (!isLocationValid(locations) && !req.ignoreCoordinates) {
                ctx.json(new ErrorResponse("ADDRESS_NOT_FOUND", "Koordinaten für diese Adresse wurde nicht gefunden"));
                return;
            }

            if (locations.size() > 0) {
                JsonObject location = locations.get(0).getAsJsonObject();
                parkour.setLongitude(location.get("lon").getAsDouble());
                parkour.setLatitude(location.get("lat").getAsDouble());
            }

            parkourQuery.insertParkour(parkour);
            dbConnection.commit();

            ctx.json(new SuccessResponse());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void handleGetParkourList(Context ctx) {
        try (Handle dbConnection = Archery.getConnection()){
            ParkourQuery parkourQuery = dbConnection.attach(ParkourQuery.class);

            ctx.json(new ParkourResponse(parkourQuery.getParkourList()));
        }
    }

    private static boolean isLocationValid(JsonArray locations) {
        if (locations.size() < 1) {
            return false;
        }
        if (locations.get(0).getAsJsonObject().get("importance").getAsDouble() < 0.45){
            return false;
        }
        return true;
    }
}


