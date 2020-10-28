package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import property.abolish.archery.db.model.User;

import java.util.List;

public interface UserQuery {

    @SqlQuery("SELECT * FROM user WHERE UPPER(username) = UPPER(:username)")
    @RegisterBeanMapper(User.class)
    User getUserByUsername(String username);

    @SqlUpdate("INSERT INTO user (username, firstName, lastName, passwordHash) VALUES (:username, :firstName, :lastName, :passwordHash)")
    @GetGeneratedKeys
    int insertUser(@BindBean User user);

    @SqlQuery("SELECT * FROM user WHERE id = :userId")
    @RegisterBeanMapper(User.class)
    User getUserByUserId(int userId);

    @SqlQuery("SELECT * FROM user WHERE UPPER(username) in (<usernames>)")
    @RegisterBeanMapper(User.class)
    List<User> getUsersByUsernames(@BindList List<String> usernames);

    @SqlQuery("SELECT * FROM user WHERE (username like CONCAT('%', :searchTerm, '%') or firstName like CONCAT('%', :searchTerm, '%') or lastName like CONCAT('%', :searchTerm, '%')) and id != :userId ORDER BY username LIMIT :limit")
    @RegisterBeanMapper(User.class)
    List<User> getUsersBySearchTerm(String searchTerm, int limit, int userId);
}
