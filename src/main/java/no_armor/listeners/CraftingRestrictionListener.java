package no_armor.listeners;

import no_armor.NoArmorPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents crafting and picking up restricted items.
 */
public class CraftingRestrictionListener implements Listener {

    private final NoArmorPlugin plugin;

    public CraftingRestrictionListener(NoArmorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevent crafting restricted items - shows empty result
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getView().getPlayer() instanceof Player player) {
            if (player.hasPermission("noarmor.bypass")) {
                return;
            }

            ItemStack result = event.getInventory().getResult();
            if (result != null && !result.getType().isAir()) {
                String itemKey = result.getType().name().toLowerCase();
                if (!plugin.isItemAllowed(itemKey)) {
                    // Clear the result so they can't craft it
                    event.getInventory().setResult(null);
                }
            }
        }
    }

    /**
     * Double-check: prevent taking crafted restricted items
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result != null && !result.getType().isAir()) {
            String itemKey = result.getType().name().toLowerCase();
            if (!plugin.isItemAllowed(itemKey)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.craft-blocked",
                                "&cYou cannot craft this item!")));
            }
        }
    }

    /**
     * Prevent picking up restricted items from the ground
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();
        String itemKey = item.getType().name().toLowerCase();

        if (!plugin.isItemAllowed(itemKey)) {
            event.setCancelled(true);
            // Optionally notify - but this can be spammy
            // player.sendMessage(ChatColor.RED + "You cannot pick up this item!");
        }
    }
}
