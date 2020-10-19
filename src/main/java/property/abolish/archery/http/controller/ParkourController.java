package property.abolish.archery.http.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.jdbi.v3.core.Handle;
import property.abolish.archery.Archery;
import property.abolish.archery.db.model.Parkour;
import property.abolish.archery.db.query.ParkourQuery;
import property.abolish.archery.http.model.ErrorResponse;
import property.abolish.archery.http.model.ParkourRequest;
import property.abolish.archery.http.model.ParkourResponse;
import property.abolish.archery.http.model.SuccessResponse;
import property.abolish.archery.utilities.Validation;

public class ParkourController {
    public static void handleCreateParkour(Context ctx) {
        Validator<ParkourRequest> validator = ctx.bodyValidator(ParkourRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.name), "name cannot be null or empty")
                .check(r -> !(r.countAnimals == 0), "countAnimals cannot be zero")
                .check(r -> !Validation.isNullOrEmpty(r.countryCode), "countryCode cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.city), "city cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.street), "street cannot be null or empty")
                .check(r -> !Validation.isNullOrEmpty(r.zip), "zip cannot be null or empty");

        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        try (Handle dbConnection = Archery.getConnection()) {
            ParkourRequest req = validator.get();
            Parkour parkour = new Parkour();

            parkour.setCountAnimals(req.countAnimals);
            parkour.setCity(req.city);
            parkour.setCountryCode(req.countryCode);
            parkour.setName(req.name);
            parkour.setStreet(req.street);
            parkour.setZip(req.zip);

            // Check if parkour already exists
            ParkourQuery parkourQuery = dbConnection.attach(ParkourQuery.class);

            if (parkourQuery.getParkourByName_countAnimals_countryCode_city_street_zip(parkour) != null){
                ctx.status(404).json(new ErrorResponse("PARKOUR_ALREADY_EXISTS", "Dieser Parkour existiert bereits"));
                return;
            }

            //TODO: latitude und longitude ermitteln
            parkour.setLongitude(0);
            parkour.setLatitude(0);

            parkourQuery.insertParkour(parkour);
            dbConnection.commit();

            ctx.json(new SuccessResponse());
        }
    }

    public static void handleGetParkourList(Context ctx) {
        Validator<ParkourRequest> validator = ctx.bodyValidator(ParkourRequest.class)
                .check(r -> !Validation.isNullOrEmpty(r.name), "name cannot be null or empty");

        if (validator.hasError()) {
            Validation.handleValidationError(ctx, validator);
            return;
        }

        try (Handle dbConnection = Archery.getConnection()){
            ParkourRequest req = validator.get();
            ParkourQuery parkourQuery = dbConnection.attach(ParkourQuery.class);

            ctx.json(new ParkourResponse(parkourQuery.getParkourListByName(req.name)));
        }
    }
}
