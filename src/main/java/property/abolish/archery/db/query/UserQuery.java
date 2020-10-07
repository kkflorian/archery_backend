package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.User;

public interface UserQuery {

    @SqlQuery("SELECT * FROM user WHERE username = :username")
    @RegisterBeanMapper(User.class)
    User getUserByUsername(String username);

    @SqlUpdate("INSERT INTO user (username, firstName, lastName, passwordHash) VALUES (:username, :firstName, :lastName, :passwordHash)")
    @GetGeneratedKeys
    int insertUser(@BindBean User user);
}
