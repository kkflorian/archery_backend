package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.Shot;

import java.util.List;

public interface ShotQuery {

    @SqlUpdate("INSERT INTO shot (eventId, userId, animalNumber, shotNumber, points) VALUES (:eventId, :userId, :animalNumber, :shotNumber, :points)")
    void insertShot(@BindBean Shot shot);

    @SqlQuery("SELECT * FROM shot WHERE eventId = :eventId order by animalNumber DESC LIMIT 1")
    @RegisterBeanMapper(Shot.class)
    Shot getLatestShot(int eventId);

    @SqlQuery("SELECT * FROM shot WHERE eventId = :eventId AND userId = :userId AND animalNumber = :animalNumber")
    @RegisterBeanMapper(Shot.class)
    List<Shot> getShot(int eventId, int userId, int animalNumber);
}
