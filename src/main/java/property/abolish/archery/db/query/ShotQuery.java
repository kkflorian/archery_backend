package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.EventStats;
import property.abolish.archery.db.model.Shot;
import property.abolish.archery.http.model.responses.OverallStatsNumbersResponse;

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

    @SqlQuery("select user.username, user.firstName, user.lastName, totalShots, hitShots / totalShots as accuracy, c.averagePoints, c.totalPoints\n" +
            "            from (select userid, count(*) as totalShots from shot join event on event.id = eventId where eventId = :eventId and event.timestampEnd is not null group by userid) a\n" +
            "            left join (select userid, count(*) as hitShots from shot join event on event.id = eventId where points != 0 and eventId = :eventId and event.timestampEnd is not null group by userid) b on a.userId = b.userId\n" +
            "            left join (select userid, avg(e.totalPoints) as averagePoints, sum(e.totalPoints) as totalPoints\n" +
            "                        from (select d.userid, sum(d.points) as totalPoints\n" +
            "                               from shot d join event on event.id = d.eventId where d.eventId = :eventId and event.timestampEnd is not null group by d.userid, d.animalNumber) e group by e.userid) c on b.userId = c.userId\n" +
            "            join user on a.userId = user.id")
    @RegisterBeanMapper(EventStats.class)
    List<EventStats> getEventStats(int eventId);

    @SqlQuery("select (sum(points) / max(animalNumber)) as graphEntry from shot join event on eventId = event.id where userId = :userId and gamemodeId = :gameModeId and event.timestampEnd is not null group by eventId")
    List<Double> getOverallStatsGraphEntries(int userId, int gameModeId);

    @SqlQuery("select totalHits, totalShots, totalEvents, averageOverall from (select count(*) as totalHits from shot join event on eventId = event.id where points > 0 and userId = :userId and event.timestampEnd is not null and event.gamemodeId = :gameModeId) a\n" +
            "join (select count(*) as totalShots from shot join event on event.id = eventId where userId = :userId and event.timestampEnd is not null and event.gamemodeId = :gameModeId) b\n" +
            "join (select count(*) as totalEvents from event\n" +
            "            left join eventMember on event.id = eventMember.eventId\n" +
            "            where eventMember.userId = :userId and event.timestampEnd is not null and event.gamemodeId = :gameModeId) c\n" +
            "join (select avg(cu.graphEntry) as averageOverall from\n" +
            "                (select (sum(points) / max(animalNumber)) as graphEntry from shot sh\n" +
            "                        join event ev on sh.eventId = ev.id\n" +
            "                        where sh.userId = :userId and ev.gamemodeId = :gameModeId and ev.timestampEnd is not null\n" +
            "                        group by sh.eventId) cu) d")
    @RegisterBeanMapper(OverallStatsNumbersResponse.class)
    OverallStatsNumbersResponse getOverallStatsNumbers(int userId, int gameModeId);
}

