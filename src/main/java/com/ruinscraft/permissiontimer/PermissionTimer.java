package com.ruinscraft.permissiontimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;

public class PermissionTimer extends JavaPlugin implements Listener {

	private LuckPermsApi luckPermsApi;

	@Override
	public void onEnable() {
		RegisteredServiceProvider<LuckPermsApi> provider = 
				Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);
		this.getServer().getPluginManager().registerEvents(this, this);
		if (provider != null) {
			getLogger().info("LuckPerms found!");
			luckPermsApi = provider.getProvider();
		} else {
			getLogger().info("LuckPerms not found! Disabling.");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (getConfig() == null || getConfig().getConfigurationSection("timed") == null) {
			return;
		}
		for (String path : getConfig().getConfigurationSection("timed").getKeys(false)) {
			long time = System.currentTimeMillis();
			if (time >= getConfig().getLong("timed." + path + ".startTime", 0) &&
					time <= getConfig().getLong("timed." + path + ".endTime", 0)) {
				Player player = event.getPlayer();
				User user = luckPermsApi.getUser(player.getName());
				for (String permission : getConfig().getStringList("timed." + path + ".permissions")) {
					Node node = luckPermsApi.buildNode(permission).setValue(true).build();
					if (user.setPermission(node).asBoolean()) {
						luckPermsApi.getUserManager().saveUser(user);
						String message = ChatColor.translateAlternateColorCodes("&".toCharArray()[0], 
								getConfig().getString("timed." + path + ".message", ""));
						if (!message.equals("")) {
							player.sendMessage(message);
						}
					}
				}
			}
		}
	}

}