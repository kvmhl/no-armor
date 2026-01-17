package no_armor.listeners;

import no_armor.NoArmorPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles tool and item usage prevention for restricted items.
 */
public class ToolUseListener implements Listener {

    private final NoArmorPlugin plugin;

    public ToolUseListener(NoArmorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevent using restricted items (right-click actions)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        // Skip armor - handled by ArmorEquipListener
        if (isArmor(item.getType())) {
            return;
        }

        if (!isItemAllowed(item.getType())) {
            // Block all interactions with restricted items
            if (event.getAction() != Action.PHYSICAL) {
                event.setCancelled(true);
                sendBlockedMessage(player);
            }
        }
    }

    /**
     * Prevent attacking with restricted weapons
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!mainHand.getType().isAir() && !isItemAllowed(mainHand.getType())) {
            event.setCancelled(true);
            sendBlockedMessage(player);
        }
    }

    /**
     * Prevent shooting bows/crossbows
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        ItemStack bow = event.getBow();
        if (bow != null && !isItemAllowed(bow.getType())) {
            event.setCancelled(true);
            sendBlockedMessage(player);
        }
    }

    /**
     * Prevent consuming restricted food/potions
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        if (!isItemAllowed(event.getItem().getType())) {
            event.setCancelled(true);
            sendBlockedMessage(player);
        }
    }

    /**
     * Notify player when switching to a restricted item
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem != null && !newItem.getType().isAir() && !isItemAllowed(newItem.getType())) {
            // Just notify, don't prevent holding
            String message = plugin.getConfig().getString("messages.item-blocked",
                    "&cYou are not allowed to use this item!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private boolean isArmor(Material material) {
        String name = material.name().toLowerCase();
        return name.contains("helmet") || name.contains("chestplate") ||
                name.contains("leggings") || name.contains("boots") ||
                name.equals("elytra") || name.equals("turtle_helmet");
    }

    private boolean isItemAllowed(Material material) {
        if (material == null || material.isAir()) {
            return true;
        }
        String key = material.name().toLowerCase();
        return plugin.isItemAllowed(key);
    }

    private void sendBlockedMessage(Player player) {
        String message = plugin.getConfig().getString("messages.item-blocked",
                "&cYou are not allowed to use this item!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
