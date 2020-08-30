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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MainListener implements Listener {
    
    private final XPSignStorage plugin;
    private final BlockFace wallFaces[] = {EAST, NORTH, SOUTH, WEST};
    
    private final String signTitle;
    private final String ownerError;
    private final String xpAmountError;
    private final String xpLeftoverError;
    private final String xpAttachError;
    
    public MainListener() { 
        this.plugin = XPSignStorage.getInstance();
        signTitle = "&a&l[XP]";
        ownerError = this.plugin.getConfig().getString("Messages.owner-error");
        xpAmountError = this.plugin.getConfig().getString("Messages.xp-amount-error");
        xpLeftoverError = this.plugin.getConfig().getString("Messages.xp-leftover-error");
        xpAttachError = this.plugin.getConfig().getString("Messages.sign-attached-error");
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getState() instanceof Sign) {
            Sign clickedSign = (Sign) e.getClickedBlock().getLocation().getBlock().getState();
            // if sign has prefix
            if (clickedSign.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', signTitle))){
                if (!this.plugin.getDB().getSignOwner(clickedSign.getLocation()).equals(e.getPlayer().getUniqueId().toString())) {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', ownerError));
                    return;
                }
                if(e.getPlayer().isSneaking()) if(e .getAction() == Action.RIGHT_CLICK_BLOCK) {
                    int signXP = Integer.parseInt( 
                        clickedSign.getLine(1).replace(ChatColor.translateAlternateColorCodes('&', "&2"), "")
                    );
                    
                    if (signXP > 0) {
                        clickedSign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&r&2" + "0"));
                        clickedSign.update();
                        e.getPlayer().giveExp(signXP);
                        e.getPlayer().updateInventory();
                    }
                    else 
                        e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', xpAmountError));
                    return;
                }
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    // obtaining xp from sign
                    // may substitute this with getting/setting from Database in the future
                    int signXP = Integer.parseInt( 
                        clickedSign.getLine(1).replace(ChatColor.translateAlternateColorCodes('&', "&2"), "")
                    );
                    
                    signXP = signXP + Experience.getExp(e.getPlayer());
                    e.getPlayer().setExp(0.0f);
                    e.getPlayer().setLevel(0);
                    e.getPlayer().updateInventory();
                    clickedSign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&r&2" + signXP));
                    clickedSign.update();
                }
            }
        }
    }
    
    @EventHandler
    public void onSignChangeEvent(SignChangeEvent e){
        try {
            if (e.getLine(0).equals("[XP]") && e.getLine(1).equals("")) {
                this.plugin.getDB().createSign(e.getPlayer(), e.getBlock().getLocation());
                e.setLine(0, ChatColor.translateAlternateColorCodes('&', signTitle));
                e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&r&20"));
                e.setLine(3, e.getPlayer().getName());
            }
        } catch(Exception ignored){}
    }
            
    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        if (e.getBlock() != null && e.getBlock().getState() instanceof Sign) {
            Sign breakSign = (Sign) e.getBlock().getLocation().getBlock().getState();
            if (breakSign.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', signTitle))) {
                
                if (!this.plugin.getDB().getSignOwner(breakSign.getLocation()).equals(e.getPlayer().getUniqueId().toString())) {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', ownerError));
                    e.setCancelled(true);
                    return;
                }
                
                int signXP = Integer.parseInt( 
                    breakSign.getLine(1).replace(ChatColor.translateAlternateColorCodes('&', "&2"), "")
                );
                
                if (signXP > 0) {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', xpLeftoverError));
                    e.setCancelled(true);
                    return;
                }
                
                this.plugin.getDB().removeSign(e.getBlock().getLocation());
            }
        }
        else {
            for (BlockFace face : wallFaces) {
                Block faceBlock = e.getBlock().getRelative(face);
                if (faceBlock != null && faceBlock.getBlockData() instanceof WallSign)
                    if (((Sign) faceBlock.getState()).getLine(0).equals(ChatColor.translateAlternateColorCodes('&', signTitle)))
                        if ( ((WallSign) faceBlock.getBlockData()).getFacing() == face) {
                            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', xpAttachError));
                            e.setCancelled(true);
                            return;
                        }
            }
            Block faceBlock = e.getBlock().getRelative(UP);
            if (faceBlock != null && faceBlock.getState() instanceof Sign)
                if (((Sign) faceBlock.getState()).getLine(0).equals(ChatColor.translateAlternateColorCodes('&', "&a&l[XP]"))) {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', xpAttachError));
                    e.setCancelled(true);
                }
        }
                        
                        
                   /*     
                else if (face == UP && faceBlock != null && faceBlock.getBlockData() instanceof org.bukkit.block.data.type.Sign)
                    if (((Sign) faceBlock.getState()).getLine(0).equals(ChatColor.translateAlternateColorCodes('&', "&a&l[XP]")))
                        e.setCancelled(true);*/
    
        
    }
}
