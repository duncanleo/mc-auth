package me.duncanleo.mc_auth

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.EventHandler

class App : JavaPlugin(), Listener {
  override fun onEnable() {
    logger.info("Hello there!")

    server.pluginManager.registerEvents(this, this)
  }

  @EventHandler
  public fun onPlayerJoin(event: PlayerJoinEvent) {
    logger.info("Damn! ${event.player.displayName}")
  }
}
