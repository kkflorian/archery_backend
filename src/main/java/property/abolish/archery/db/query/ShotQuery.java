package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.EventStats;
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
    List<Shot> getShots(int eventId, int userId, int animalNumber);

    @SqlQuery("SELECT * FROM shot WHERE eventId = :eventId AND userId = :userId order by animalNumber, shotNumber")
    @RegisterBeanMapper(Shot.class)
    List<Shot> getShots(int eventId, int userId);


    /*
    select userid, animalNumber, sum(points) as totalPoints from shots group by userid, animalNumber

    1, 1, 10
    1, 2, 0
    1, 3, 20

    avg(10, 0, 20)

    select userid, avg(points) from (select userid, animalNumber, sum(points) as totalPoints from shots group by userid, animalNumber) group by userid

    userid, 10



select a.userId, hitShots as shotsTotal, hitShots / totalShots as accuracy
from (select userid, count(*) as totalShots from shot group by userid) a
join (select userid, count(*) as hitShots from shot where points != 0 group by userid) b on a.userId = b.userId

join (select userid, avg(totalPoints) as averagePoints, sum(totalPoints) from (select userid, animalNumber, sum(points) as totalPoints from shots group by userid, animalNumber) group by userid) c on b.userId = c.userId
*/


    @SqlQuery("select user.username, user.firstName, user.lastName, hitShots as shotsTotal, hitShots / totalShots as accuracy, c.averagePoints, c.totalPoints\n" +
            "            from (select userid, count(*) as totalShots from shot group by userid) a\n" +
            "            join (select userid, count(*) as hitShots from shot where points != 0 group by userid) b on a.userId = b.userId\n" +
            "            join (select userid, avg(e.totalPoints) as averagePoints, sum(e.totalPoints) as totalPoints\n" +
            "                        from (select d.userid, sum(d.points) as totalPoints\n" +
            "                               from shot d group by d.userid, d.animalNumber) e group by e.userid) c on b.userId = c.userId\n" +
            "            join user on a.userId = user.id")
    List<EventStats> getEventStats(int eventId);

    @SqlQuery("")
    void getOverallStats(int userId);
}

