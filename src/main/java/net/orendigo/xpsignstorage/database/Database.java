package net.orendigo.xpsignstorage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import net.orendigo.xpsignstorage.XPSignStorage;
import org.bukkit.Location;


public abstract class Database {
    
    XPSignStorage plugin;
    Connection connection;
    
    // The name of the table we created back in SQLite class.
    public String signTable = "xpSigns";
    
    public Database(XPSignStorage instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + signTable + " WHERE player = ?");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
   
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }
    
    // get the sign owner from a given location
    public String getSignOwner(Location signLoc) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "SELECT player FROM " + signTable + " WHERE " 
                            + signTable + ".world = \"" + signLoc.getWorld().getName() + "\" AND "
                            + signTable + ".x = " + signLoc.getX() + " AND "
                            + signTable + ".y = " + signLoc.getY() + " AND "
                            + signTable + ".z = " + signLoc.getZ());
            rs = ps.executeQuery();
            
            while(rs.next())
                return rs.getString("player");
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }
    
    public void createSign(Player player, Location signLoc) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // obtain rowID
            int rowID = 0;
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT MAX(id) FROM " + signTable + ";");
            rs = ps.executeQuery();
                        
            while(rs.next())
                rowID = rs.getInt(1);
            rowID = rowID + 1;
            
            // insert sign data
            ps = conn.prepareStatement("INSERT INTO " + signTable + " (id, player, world, x, y, z) VALUES(?,?,?,?,?,?)");
            ps.setInt(1, rowID);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, signLoc.getWorld().getName());
            ps.setFloat(4, signLoc.getBlockX());
            ps.setFloat(5, signLoc.getBlockY());
            ps.setFloat(6, signLoc.getBlockZ());
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        } 
    }
    
    public void removeSign(Location signLoc) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            int rowID = 1;
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "SELECT id FROM " + signTable + " WHERE "
                            + signTable + ".world = \"" + signLoc.getWorld().getName() + "\" AND "
                            + signTable + ".x = " + signLoc.getX() + " AND "
                            + signTable + ".y = " + signLoc.getY() + " AND "
                            + signTable + ".z = " + signLoc.getZ());
            rs = ps.executeQuery();
                        
            while(rs.next())
                rowID = rs.getInt(1);
            
            // delete sign data
            ps = conn.prepareStatement("DELETE FROM " + signTable + " WHERE " + signTable + ".id = " + rowID);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        } 
    }
    
    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}