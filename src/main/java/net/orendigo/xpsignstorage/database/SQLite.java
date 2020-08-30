package net.orendigo.xpsignstorage.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import net.orendigo.xpsignstorage.XPSignStorage; // import your main class


public class SQLite extends Database{
    String dbName;
    public SQLite(XPSignStorage instance){
        super(instance);
        dbName = "xpSigns"; // Set the table name here e.g player_kills
    }
    
    // table containing all the sign positions
    // id = unique primary key
    public String SQLiteCreateSignsTable = "CREATE TABLE IF NOT EXISTS xpSigns (" +
            "`id` int(6) NOT NULL," +
            "`player` varchar(36) NOT NULL," +
            "`world` varchar(10) NOT NULL," +
            "`x` int(6) NOT NULL," +
            "`y` int(6) NOT NULL," +
            "`z` int(6) NOT NULL," +
            "PRIMARY KEY (`id`)" +  
            ");";

    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbName + ".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+ dbName +".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateSignsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}