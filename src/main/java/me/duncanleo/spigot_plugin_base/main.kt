package me.duncanleo.spigot_plugin_base

import org.bukkit.plugin.java.JavaPlugin

class App : JavaPlugin() {
  override fun onEnable() {
    logger.info("Hello there!")
  }
}
