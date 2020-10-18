package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.Parkour;

public interface ParkourQuery {

    @SqlQuery("SELECT * FROM parkour WHERE UPPER(name) = UPPER(:name) AND countAnimals = :countAnimals AND UPPER(countryCode) = UPPER(:countryCode) AND UPPER(city) = UPPER(:city) AND UPPER(street) = UPPER(:street) AND UPPER(zip) = UPPER(:zip)")
    @RegisterBeanMapper(Parkour.class)
    Parkour getParkourByName_countAnimals_countryCode_city_street_zip(@BindBean Parkour parkour);

    @SqlUpdate("INSERT INTO parkour (name, countAnimals, countryCode, city, street, zip, latitude, longitude) VALUES (:name, :countAnimals, :countryCode, :city, :street, :zip, :latitude, :longitude)")
    void insertParkour(@BindBean Parkour parkour);

    @SqlQuery("SELECT * FROM parkour WHERE parkour.id = :id")
    @RegisterBeanMapper(Parkour.class)
    Parkour getParkourById(int id);
}
