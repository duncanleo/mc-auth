package me.duncanleo.mc_auth.util

import org.bukkit.entity.Player
import org.bukkit.ChatColor

val Player.displayNameStripped: String
  get() = ChatColor.stripColor(this.displayName)!!
