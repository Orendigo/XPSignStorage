package net.orendigo.xpsignstorage.database;

import java.util.logging.Level;
import net.orendigo.xpsignstorage.XPSignStorage;

public class Error {
    public static void execute(XPSignStorage plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(XPSignStorage plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}