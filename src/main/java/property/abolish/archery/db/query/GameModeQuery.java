package property.abolish.archery.db.query;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import property.abolish.archery.db.model.GameMode;

import java.util.List;

public interface GameModeQuery {
    @SqlQuery("SELECT * FROM gamemode")
    @RegisterBeanMapper(GameMode.class)
    List<GameMode> getGameModes();

    @SqlQuery("SELECT * FROM gamemode WHERE id = :id")
    @RegisterBeanMapper(GameMode.class)
    GameMode getGameModeById(int id);
}
