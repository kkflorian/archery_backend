package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.Event;
import property.abolish.archery.db.model.User;

import java.util.List;

public interface EventQuery {

    @SqlQuery("SELECT * FROM event JOIN eventmember On event.id = eventmember.eventId WHERE event.userIdCreator = :userId OR eventmember.userId = :userId")
    @RegisterBeanMapper(Event.class)
    List<Event> getEventListbyUserId(int userId);

    @SqlQuery("SELECT user.* FROM user JOIN eventmember On user.id = eventmember.userId WHERE eventmember.eventId = :eventId")
    @RegisterBeanMapper(User.class)
    List<User> getEventMembersByEventId(int eventId);

    @SqlUpdate("INSERT INTO event (parkourId, timestamp, gamemodeId, userIdCreator) VALUES (:parkourId, :timestamp , :gamemodeId, :userIdCreator)")
    @GetGeneratedKeys
    int insertEvent(@BindBean Event event);



}
