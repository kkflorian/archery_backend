package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import property.abolish.archery.db.model.Event;

import java.util.List;

public interface EventQuery {

    @SqlQuery("SELECT * FROM event JOIN eventmember On event.id = eventmember.eventId WHERE event.userIdCreator = :userId OR eventmember.userId = :userId")
    @RegisterBeanMapper(Event.class)
    List<Event> getEventListbyUserId(int userId);

}
