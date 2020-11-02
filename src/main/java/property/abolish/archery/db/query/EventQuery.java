package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindBeanList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.model.EventMember;
import property.abolish.archery.db.model.User;

import java.util.List;

public interface EventQuery {

    @SqlQuery("SELECT event.* FROM event JOIN eventMember ON event.id = eventMember.eventId WHERE eventMember.userId = :userId GROUP BY eventId, event.timestamp order by event.timestamp")
    @RegisterBeanMapper(Event.class)
    List<Event> getEventListByUserId(int userId);

    @SqlQuery("SELECT user.* FROM user JOIN eventMember ON user.id = eventMember.userId WHERE eventMember.eventId = :eventId")
    @RegisterBeanMapper(User.class)
    List<User> getEventMembersByEventId(int eventId);

    @SqlUpdate("INSERT INTO event (parkourId, timestamp, gamemodeId, userIdCreator) VALUES (:parkourId, :timestamp , :gamemodeId, :userIdCreator)")
    @GetGeneratedKeys
    int insertEvent(@BindBean Event event);

    @SqlUpdate("INSERT INTO eventMember (eventId, userId) VALUES <values>")
    void insertEventMember(@BindBeanList(propertyNames = {"eventId", "userId"}) List<EventMember> values);

    @SqlQuery("SELECT * FROM event WHERE id = :eventId")
    @RegisterBeanMapper(Event.class)
    Event getEventByEventId(int eventId);

    @SqlUpdate("UPDATE event set timestampEnd = :timestampEnd where id = :id")
    void setEventAsFinished(@BindBean Event event);
}
