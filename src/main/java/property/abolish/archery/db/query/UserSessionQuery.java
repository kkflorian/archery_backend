package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.UserSession;

public interface UserSessionQuery {

    @SqlQuery("SELECT * FROM userSession WHERE sessionId = :sessionId ORDER BY expiryDate")
    @RegisterBeanMapper(UserSession.class)
    UserSession getUserSessionBySessionId(String sessionId);

    @SqlUpdate("INSERT INTO userSession (sessionId, userId, expiryDate) VALUES (:sessionId, :userId, :expiryDate)")
    int insertUserSession(@BindBean UserSession userSession);
}
