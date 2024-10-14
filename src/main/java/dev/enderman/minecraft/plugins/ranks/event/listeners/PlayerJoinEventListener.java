package dev.enderman.minecraft.plugins.ranks.event.listeners;

import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

import dev.enderman.minecraft.plugins.ranks.RanksPlugin;
import dev.enderman.minecraft.plugins.ranks.managers.NameTagManager;
import dev.enderman.minecraft.plugins.ranks.managers.RankManager;
import dev.enderman.minecraft.plugins.ranks.types.Rank;
import dev.enderman.minecraft.plugins.ranks.utility.Utility;

public final class PlayerJoinEventListener implements Listener {
  private final RanksPlugin plugin;
  private final YamlConfiguration config;
  private final RankManager rankManager;
  private final NameTagManager nameTagManager;
  private final Map<UUID, PermissionAttachment> permissions;

  public PlayerJoinEventListener(@NotNull final RanksPlugin plugin) {
    this.plugin = plugin;
    this.config = (YamlConfiguration) plugin.getConfig();
    this.rankManager = plugin.getRankManager();
    this.nameTagManager = plugin.getNameTagManager();
    this.permissions = rankManager.getPermissions();
  }

  @EventHandler
  public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
    final String defaultRank = config.getString("default-rank");
    final Player player = event.getPlayer();
    final UUID playerUUID = player.getUniqueId();

    // Attempt to assign the player the default rank, if it exists.
    if (defaultRank != null && rankManager.getPlayerRank(playerUUID, false) == Rank.getNullRank()) {
      final boolean success = rankManager.setRank(playerUUID, defaultRank, true);

      if (!success) {
        Utility.log("Invalid configuration! Default rank does not exist in rank list.");

        return;
      }
    }

    // Give the player a scoreboard with name tags of other players.
    nameTagManager.setNameTags(player);
    // Add the player to everyone's scoreboard (including the player themselves).
    nameTagManager.addNewNameTag(player);

    // Add permissions to the player.
    final PermissionAttachment attachment;

    if (permissions.containsKey(playerUUID)) {
      attachment = permissions.get(playerUUID);
    } else {
      attachment = player.addAttachment(plugin);
      permissions.put(playerUUID, attachment);
    }

    for (final String permission : rankManager.getPlayerRank(playerUUID, false).getPermissions()) {
      attachment.setPermission(permission, true);
    }
  }
}
