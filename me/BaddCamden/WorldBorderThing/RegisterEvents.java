package me.BaddCamden.WorldBorderThing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;



public class RegisterEvents implements Listener, CommandExecutor, TabCompleter{
	Main mainPlugin;
	public FileConfiguration database;
	public File datafile;
	public boolean hasStarted = false;
	
	public RegisterEvents(Main main) {
		mainPlugin = main;
		database = main.getDataBase();
		datafile = main.getDataFile();
        boolean foundW = false;
		for(String key : database.getKeys(false)) {
			
			if(key.equalsIgnoreCase("BorderEntity")) {
				foundW = true;
			}
		}
		if(!foundW) {
			database.createSection("BorderEntity");
			database.set("BorderEntity.uuid", "none");
			database.set("BorderEntity.hits", 0);
			database.set("BorderEntity.level", 0);
			database.set("BorderEntity.bordersize", 10);
			mainPlugin.saveDataBase();
		}
	}
	
	public int calculateBorder() {
		if(database.getInt("BorderEntity.hits") >= 20 * Math.pow(1.5, database.getInt("level"))) {
			database.set("BorderEntity.level", database.getInt("BorderEntity.level") + 1);
			database.set("BorderEntity.hits", 0);
			mainPlugin.saveDataBase();
		}
		return database.getInt("BorderEntity.level");
	}
	
	
	@EventHandler
	public void entitydamageevent(EntityDamageEvent e) {
		if(e.getEntity().getUniqueId().equals(UUID.fromString(database.getString("BorderEntity.uuid")))) {
			e.getEntity().getWorld().getWorldBorder().setSize(database.getInt("BorderEntity.bordersize") + calculateBorder(), 200);
			e.getEntity().getWorld().getWorldBorder().setCenter(e.getEntity().getLocation());
			database.set("BorderEntity.hits",database.getInt("BorderEntity.hits") + 1);
			mainPlugin.saveDataBase();
			e.setDamage(0);
		}
	}
	
	
	
	@Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        List<String> list = new ArrayList<String>();
        List<String> results = new ArrayList<String>();
        if (sender instanceof Player) {
            if (cmd.getName().equalsIgnoreCase("borderentity")) {
                if (args.length == 0) {
                    list.add("spawn");
                    list.add("bordersize");
                    Collections.sort(list);
                    return list;
                } else if (args.length == 1) {
                	list.add("spawn");
                	list.add("bordersize");
                    for (String s : list){
                        if (s.toLowerCase().startsWith(args[0].toLowerCase())){
                        	results.add(s);
                        }
                    }
                    Collections.sort(results);
                    return results;
                }
                	
                	
                
                
            }
        }
        return list;
    }

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if(arg0.isOp() && arg0 instanceof Player) {
			if(arg2.equalsIgnoreCase("borderentity")) {
				if(arg3[0] != null) {
					if(arg3[0].equalsIgnoreCase("spawn")) {
						Entity e = ((Player) arg0).getWorld().spawnEntity(((Player) arg0).getLocation().add(0,1,0), EntityType.CHICKEN);
						e.setPersistent(true);
						if(database.getString("BorderEntity.uuid") != "none" && Bukkit.getEntity(UUID.fromString(database.getString("BorderEntity.uuid"))) != null) {
							Bukkit.getEntity(UUID.fromString(database.getString("BorderEntity.uuid"))).remove();
						}
						e.getWorld().getWorldBorder().setSize(database.getInt("BorderEntity.bordersize") + calculateBorder());
						e.getWorld().getWorldBorder().setCenter(e.getLocation());
						e.setCustomName(ChatColor.LIGHT_PURPLE+"BORDER ENTITY");
						database.set("BorderEntity.uuid", e.getUniqueId().toString());
						database.set("BorderEntity.hits", 0);
						database.set("BorderEntity.level", 0);
						mainPlugin.saveDataBase();
					} else if(arg3[0].equalsIgnoreCase("bordersize")) {
						try {
							if(Integer.parseInt(arg3[1]) > 0) {
								database.set("BorderEntity.bordersize", arg3[1]);
								mainPlugin.saveDataBase();
								((Player)arg0).getWorld().getWorldBorder().setSize(Integer.parseInt(arg3[1]) + calculateBorder());
								arg0.sendMessage(ChatColor.GREEN+"Border size set to: "+arg3[1]);
							}
						} catch(NumberFormatException e) {
							arg0.sendMessage(ChatColor.RED+"Missing Argument, use integer number!");
						}
						
					}
				}
			}
		}

		return false;
	}
	
	
}
