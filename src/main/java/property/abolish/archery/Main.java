package property.abolish.archery;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ConnectionConfig load;

        try {
            load = ConnectionConfig.load();
        } catch (IOException e) {
            System.out.println("Config konnte nicht geladen werden!");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        System.out.println(load.dbIp);


    }
}
