package no_armor.listeners;

import no_armor.NoArmorPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles visual feedback for restricted armor slots by showing grey glass
 * panes.
 */
public class InventorySlotListener implements Listener {

    private final NoArmorPlugin plugin;
    private final Map<UUID, ItemStack[]> savedArmorSlots = new HashMap<>();

    // Armor slot indices in player inventory
    private static final int HELMET_SLOT = 39;
    private static final int CHESTPLATE_SLOT = 38;
    private static final int LEGGINGS_SLOT = 37;
    private static final int BOOTS_SLOT = 36;

    public InventorySlotListener(NoArmorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * When player opens their inventory, show grey panes in restricted empty armor
     * slots
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Delay to allow inventory to fully open
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    return;
                applyRestrictedOverlays(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * When player closes inventory, remove any overlay items
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        removeRestrictedOverlays(player);
    }

    /**
     * Prevent clicking on the overlay items
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Check if clicking on an overlay item
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && isOverlayItem(clicked)) {
                event.setCancelled(true);

                // Check what type of armor slot this is
                int slot = event.getSlot();
                String armorType = getArmorTypeForSlot(slot);
                if (armorType != null) {
                    String message = plugin.getConfig().getString("messages.armor-blocked",
                            "&cYou cannot equip this armor piece!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        }
    }

    /**
     * Apply grey glass pane overlays to restricted empty armor slots
     */
    private void applyRestrictedOverlays(Player player) {
        PlayerInventory inv = player.getInventory();

        // Save current armor state
        ItemStack[] savedArmor = new ItemStack[4];
        savedArmor[0] = inv.getHelmet() != null ? inv.getHelmet().clone() : null;
        savedArmor[1] = inv.getChestplate() != null ? inv.getChestplate().clone() : null;
        savedArmor[2] = inv.getLeggings() != null ? inv.getLeggings().clone() : null;
        savedArmor[3] = inv.getBoots() != null ? inv.getBoots().clone() : null;
        savedArmorSlots.put(player.getUniqueId(), savedArmor);

        // Check each armor slot and apply overlay if all armor of that type is
        // restricted
        if (inv.getHelmet() == null && areAllHelmetsRestricted()) {
            inv.setHelmet(createOverlayItem());
        }
        if (inv.getChestplate() == null && areAllChestplatesRestricted()) {
            inv.setChestplate(createOverlayItem());
        }
        if (inv.getLeggings() == null && areAllLeggingsRestricted()) {
            inv.setLeggings(createOverlayItem());
        }
        if (inv.getBoots() == null && areAllBootsRestricted()) {
            inv.setBoots(createOverlayItem());
        }
    }

    /**
     * Remove overlay items from armor slots
     */
    private void removeRestrictedOverlays(Player player) {
        PlayerInventory inv = player.getInventory();

        // Remove overlay items
        if (inv.getHelmet() != null && isOverlayItem(inv.getHelmet())) {
            inv.setHelmet(null);
        }
        if (inv.getChestplate() != null && isOverlayItem(inv.getChestplate())) {
            inv.setChestplate(null);
        }
        if (inv.getLeggings() != null && isOverlayItem(inv.getLeggings())) {
            inv.setLeggings(null);
        }
        if (inv.getBoots() != null && isOverlayItem(inv.getBoots())) {
            inv.setBoots(null);
        }

        savedArmorSlots.remove(player.getUniqueId());
    }

    /**
     * Create the grey glass pane overlay item
     */
    private ItemStack createOverlayItem() {
        String materialName = plugin.getConfig().getString("visuals.blocked-slot-material", "GRAY_STAINED_GLASS_PANE");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString("visuals.blocked-slot-name", "&c&lRestricted");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "This armor slot is restricted");
            lore.add(ChatColor.DARK_GRAY + "NoArmor-Overlay");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Check if an item is our overlay item
     */
    private boolean isOverlayItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return false;
        }
        List<String> lore = meta.getLore();
        return lore != null && lore.stream().anyMatch(line -> line.contains("NoArmor-Overlay"));
    }

    private String getArmorTypeForSlot(int slot) {
        return switch (slot) {
            case HELMET_SLOT -> "helmet";
            case CHESTPLATE_SLOT -> "chestplate";
            case LEGGINGS_SLOT -> "leggings";
            case BOOTS_SLOT -> "boots";
            default -> null;
        };
    }

    private boolean areAllHelmetsRestricted() {
        String[] helmets = { "leather_helmet", "chainmail_helmet", "iron_helmet",
                "golden_helmet", "diamond_helmet", "netherite_helmet", "turtle_helmet" };
        for (String helmet : helmets) {
            if (plugin.isItemAllowed(helmet)) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllChestplatesRestricted() {
        String[] chestplates = { "leather_chestplate", "chainmail_chestplate", "iron_chestplate",
                "golden_chestplate", "diamond_chestplate", "netherite_chestplate", "elytra" };
        for (String chestplate : chestplates) {
            if (plugin.isItemAllowed(chestplate)) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllLeggingsRestricted() {
        String[] leggings = { "leather_leggings", "chainmail_leggings", "iron_leggings",
                "golden_leggings", "diamond_leggings", "netherite_leggings" };
        for (String legging : leggings) {
            if (plugin.isItemAllowed(legging)) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllBootsRestricted() {
        String[] boots = { "leather_boots", "chainmail_boots", "iron_boots",
                "golden_boots", "diamond_boots", "netherite_boots" };
        for (String boot : boots) {
            if (plugin.isItemAllowed(boot)) {
                return false;
            }
        }
        return true;
    }
}
