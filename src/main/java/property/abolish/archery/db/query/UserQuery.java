package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import property.abolish.archery.db.model.User;

public interface UserQuery {

    @SqlQuery("SELECT * FROM user WHERE username = :username")
    @RegisterBeanMapper(User.class)
    User getUserByUsername(String username);
}
