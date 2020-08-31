package net.orendigo.xpsignstorage.events;


import net.orendigo.xpsignstorage.XPSignStorage;
import net.orendigo.xpsignstorage.util.Experience;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import static org.bukkit.block.BlockFace.EAST;
import static org.bukkit.block.BlockFace.NORTH;
import static org.bukkit.block.BlockFace.SOUTH;
import static org.bukkit.block.BlockFace.UP;
import static org.bukkit.block.BlockFace.WEST;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MainListener implements Listener {
    
    private final XPSignStorage plugin;
    private final BlockFace wallFaces[] = {EAST, NORTH, SOUTH, WEST, UP};
    
    private final String signTitle;
    private final String ownerError;
    private final String xpAmountError;
    private final String xpLeftoverError;
    private final String xpAttachError;
    private final String signExistLoc;
    
    public MainListener() { 
        this.plugin = XPSignStorage.getInstance();
        signTitle = "&a&l[XP]";
        ownerError = this.plugin.getConfig().getString("Messages.owner-error");
        xpAmountError = this.plugin.getConfig().getString("Messages.xp-amount-error");
        xpLeftoverError = this.plugin.getConfig().getString("Messages.xp-leftover-error");
        xpAttachError = this.plugin.getConfig().getString("Messages.sign-attached-error");
        signExistLoc = this.plugin.getConfig().getString("Messages.sign-exist-error");
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getState() instanceof Sign) {
            
            Sign clickedSign = (Sign) e.getClickedBlock().getLocation().getBlock().getState();
            Player player = e.getPlayer();
            
            // if sign has prefix.
            if (clickedSign.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', signTitle))) {
                
                // if player is shift + rightclicking.
                if (player.isSneaking()) if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    
                    // if player is not the sign owner.
                    if (!this.plugin.getDB().getSignOwner(clickedSign.getLocation()).equals(player.getUniqueId().toString())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', ownerError));
                        return;
                    }
                    
                    // getting XP from database.
                    int signXP = this.plugin.getDB().getXP(player, clickedSign.getLocation());
                    
                    if (signXP > 0) {
                        clickedSign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&r&2" + "0"));
                        clickedSign.update();
                        this.plugin.getDB().setXP(player, clickedSign.getLocation(), 0);
                        player.giveExp(signXP);
                        player.updateInventory();
                    }
                    else 
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', xpAmountError));
                    return;
                }
                
                // if player is just rightclicking.
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    
                    // if player is not the sign owner.
                    if (!this.plugin.getDB().getSignOwner(clickedSign.getLocation()).equals(player.getUniqueId().toString())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', ownerError));
                        return;
                    }
                    
                    // getting XP from database.
                    int signXP = this.plugin.getDB().getXP(player, clickedSign.getLocation()) + Experience.getExp(player);
                    
                    player.setExp(0.0f);
                    player.setLevel(0);
                    player.updateInventory();
                    this.plugin.getDB().setXP(player, clickedSign.getLocation(), signXP);
                    clickedSign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&r&2" + signXP));
                    clickedSign.update();
                }
            }
        }
    }
    
    // initial sign creation event
    @EventHandler
    public void onSignChangeEvent(SignChangeEvent e){
        try {
            // if the event sign is an XP sign
            if (e.getLine(0).equals("[XP]") && e.getLine(1).equals("")) {
                // if the sign location is not in the database, create new sign.
                if (this.plugin.getDB().getSignOwner(e.getBlock().getLocation()) == null) {
                    this.plugin.getDB().createSign(e.getPlayer(), e.getBlock().getLocation());
                    e.setLine(0, ChatColor.translateAlternateColorCodes('&', signTitle));
                    e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&r&20"));
                    e.setLine(3, e.getPlayer().getName());
                }
               /*  
                *  else if it does exist, this means that the sign was broken somehow
                *  and was not properly removed from the database.  Recreate the sign
                *  using the information already in the database if the owner is creating the sign.
                */ 
                else if (this.plugin.getDB().getSignOwner(e.getBlock().getLocation()).equals(e.getPlayer().getUniqueId().toString())) {
                    e.setLine(0, ChatColor.translateAlternateColorCodes('&', signTitle));
                    e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&r&2" + 
                        this.plugin.getDB().getXP(e.getPlayer(), e.getBlock().getLocation())));
                    e.setLine(3, e.getPlayer().getName());
                }
                // if not the owner, inform the player that the location is already in use.
                else {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', signExistLoc));
                }
            }
        } catch(Exception ignored){}
    }
            
    
    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        // initial check of locations around event block where a sign can be attached.
        if (checkForSign(e.getBlock())) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', xpAttachError));
            e.setCancelled(true);
            return;
        }
        
        
        if (e.getBlock().getState() instanceof Sign) {
            Sign breakSign = (Sign) e.getBlock().getLocation().getBlock().getState();
            
            // if block being broken is an XP sign.
            if (breakSign.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', signTitle))) {
                
                Player player = e.getPlayer();
                
                // if player is not owner of the XP sign.
                if (!this.plugin.getDB().getSignOwner(breakSign.getLocation()).equals(e.getPlayer().getUniqueId().toString())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', ownerError));
                    e.setCancelled(true);
                    return;
                }
                
                // if there is still XP within the sign.
                if (this.plugin.getDB().getXP(e.getPlayer(), breakSign.getLocation()) > 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', xpLeftoverError));
                    e.setCancelled(true);
                    return;
                }
                
                // remove the sign from the database.
                this.plugin.getDB().removeSign(e.getBlock().getLocation());
            }
        }
    }
    
    // Check directions from event-block for attached signs
    private Boolean checkForSign(Block e) {
        for (BlockFace face : wallFaces) {
            Block faceBlock = e.getRelative(face);
            if (face == UP)
                if (faceBlock != null && faceBlock.getState() instanceof Sign)
                    if (((Sign) faceBlock.getState()).getLine(0).equals(ChatColor.translateAlternateColorCodes('&', signTitle)))
                        return true;
            if (faceBlock != null && faceBlock.getBlockData() instanceof WallSign) {
                if (((Sign) faceBlock.getState()).getLine(0).equals(ChatColor.translateAlternateColorCodes('&', signTitle))) 
                    if ( ((WallSign) faceBlock.getBlockData()).getFacing() == face) 
                        return true; 
            }
        }
        return false;
    }
}
