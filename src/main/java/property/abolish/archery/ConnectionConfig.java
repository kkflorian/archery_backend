package property.abolish.archery;
import java.util.Properties;

public class ConnectionConfig {

    Properties databaseConnection = new Properties();

    public Properties GetConfig(){
        databaseConnection.setProperty("database", "archery");
        databaseConnection.setProperty("ip", "159.69.188.205");
        databaseConnection.setProperty("port", "88");
        databaseConnection.setProperty("dbUser", "root");
        databaseConnection.setProperty("dbPw", "root");

        return databaseConnection;
    }

}
