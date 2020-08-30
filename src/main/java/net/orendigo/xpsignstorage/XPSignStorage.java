package net.orendigo.xpsignstorage;

import net.orendigo.xpsignstorage.events.MainListener;
import java.io.File;
import java.io.IOException;
import net.orendigo.xpsignstorage.database.Database;
import net.orendigo.xpsignstorage.database.SQLite;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class XPSignStorage extends JavaPlugin {
    
    // config file
    private File configFile;
    private FileConfiguration config;
    
    // instance
    private static XPSignStorage instance;
    
    private Database db;
    
    @Override
    public void onEnable(){
        createConfig();
        this.db = new SQLite(this);
        this.db.load();
        getServer().getPluginManager().registerEvents(new MainListener(), this);
        System.out.println("XPSignStorage Loaded Successfully!");
    }
    
    @Override
    public void onLoad() {
        setInstance(this);
    }
    
    @Override
    public void onDisable(){
        System.out.println("XPSignStorage Unloaded Successfully!");
    }
    
    public static XPSignStorage getInstance() {
        return XPSignStorage.instance;
    }

    private void setInstance(final XPSignStorage instance) {
        XPSignStorage.instance = instance;
    }
    
    public Database getDB() {
        return this.db;
    }
    
    public void createConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
         }

        config = new YamlConfiguration();
        try {config.load(configFile);}
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public File getConfigFile() {
        return configFile;
    }
}
