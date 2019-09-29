package me.duncanleo.spigot_plugin_base

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener

class App : JavaPlugin() {
class App : JavaPlugin(), Listener {
  override fun onEnable() {
    logger.info("Hello there!")

    server.pluginManager.registerEvents(this, this)
  }
}
