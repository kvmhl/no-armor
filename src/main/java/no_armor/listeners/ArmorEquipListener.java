package no_armor.listeners;

import no_armor.NoArmorPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles armor equip prevention for restricted armor pieces.
 */
public class ArmorEquipListener implements Listener {

    private final NoArmorPlugin plugin;

    public ArmorEquipListener(NoArmorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevent equipping armor via inventory click
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Bypass permission check
        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Check if clicking on armor slot
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir()) {
                if (!isArmorAllowed(cursor.getType())) {
                    event.setCancelled(true);
                    sendBlockedMessage(player);
                    return;
                }
            }
        }

        // Check shift-click equip
        if (event.isShiftClick() && event.getCurrentItem() != null) {
            ItemStack item = event.getCurrentItem();
            if (isArmor(item.getType()) && !isArmorAllowed(item.getType())) {
                event.setCancelled(true);
                sendBlockedMessage(player);
            }
        }

        // Check number key swap to armor slot
        if (event.getClick().name().contains("NUMBER_KEY") &&
                event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (hotbarItem != null && !isArmorAllowed(hotbarItem.getType())) {
                event.setCancelled(true);
                sendBlockedMessage(player);
            }
        }
    }

    /**
     * Prevent equipping armor via drag
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Check if dragging to armor slots (5-8 in player inventory)
        for (int slot : event.getRawSlots()) {
            if (slot >= 5 && slot <= 8) {
                ItemStack item = event.getOldCursor();
                if (item != null && !isArmorAllowed(item.getType())) {
                    event.setCancelled(true);
                    sendBlockedMessage(player);
                    return;
                }
            }
        }
    }

    /**
     * Prevent right-click equip armor
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (isArmor(item.getType()) && !isArmorAllowed(item.getType())) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                event.setCancelled(true);
                sendBlockedMessage(player);
            }
        }
    }

    /**
     * Prevent dispenser equipping armor
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (!(event.getTargetEntity() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        if (!isArmorAllowed(event.getItem().getType())) {
            event.setCancelled(true);
            sendBlockedMessage(player);
        }
    }

    private boolean isArmor(Material material) {
        String name = material.name().toLowerCase();
        return name.contains("helmet") || name.contains("chestplate") ||
                name.contains("leggings") || name.contains("boots") ||
                name.equals("elytra") || name.contains("_head") ||
                name.contains("_skull") || name.equals("carved_pumpkin") ||
                name.equals("turtle_helmet");
    }

    private boolean isArmorAllowed(Material material) {
        if (material == null || material.isAir()) {
            return true;
        }
        String key = material.name().toLowerCase();
        return plugin.isItemAllowed(key);
    }

    private void sendBlockedMessage(Player player) {
        String message = plugin.getConfig().getString("messages.armor-blocked", "&cYou cannot equip this armor piece!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
