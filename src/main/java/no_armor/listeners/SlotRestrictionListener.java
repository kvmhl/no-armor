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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Handles inventory slot restrictions - blocks specific inventory slots
 * entirely.
 * Allows greying out whole inventory slots (not just armor).
 */
public class SlotRestrictionListener implements Listener {

    private final NoArmorPlugin plugin;
    private final Set<Integer> restrictedSlots = new HashSet<>();

    // Inventory slot constants
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int MAIN_INV_START = 9;
    public static final int MAIN_INV_END = 35;
    public static final int BOOTS_SLOT = 36;
    public static final int LEGGINGS_SLOT = 37;
    public static final int CHESTPLATE_SLOT = 38;
    public static final int HELMET_SLOT = 39;
    public static final int OFFHAND_SLOT = 40;

    public SlotRestrictionListener(NoArmorPlugin plugin) {
        this.plugin = plugin;
        loadRestrictedSlots();
    }

    /**
     * Load restricted slots from config
     */
    public void loadRestrictedSlots() {
        restrictedSlots.clear();

        List<String> slotConfig = plugin.getConfig().getStringList("restricted-slots.slots");
        for (String entry : slotConfig) {
            parseAndAddSlots(entry);
        }

        plugin.getLogger().info("Loaded " + restrictedSlots.size() + " restricted inventory slots");
    }

    /**
     * Parse slot config entry (supports ranges like "0-8" and single slots like
     * "5")
     */
    private void parseAndAddSlots(String entry) {
        entry = entry.trim();
        if (entry.isEmpty())
            return;

        try {
            if (entry.contains("-")) {
                String[] parts = entry.split("-");
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                for (int i = start; i <= end; i++) {
                    restrictedSlots.add(i);
                }
            } else if (entry.equalsIgnoreCase("hotbar")) {
                for (int i = HOTBAR_START; i <= HOTBAR_END; i++) {
                    restrictedSlots.add(i);
                }
            } else if (entry.equalsIgnoreCase("main")) {
                for (int i = MAIN_INV_START; i <= MAIN_INV_END; i++) {
                    restrictedSlots.add(i);
                }
            } else if (entry.equalsIgnoreCase("armor")) {
                for (int i = BOOTS_SLOT; i <= HELMET_SLOT; i++) {
                    restrictedSlots.add(i);
                }
            } else if (entry.equalsIgnoreCase("offhand")) {
                restrictedSlots.add(OFFHAND_SLOT);
            } else {
                restrictedSlots.add(Integer.parseInt(entry));
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid slot config entry: " + entry);
        }
    }

    /**
     * Check if a slot is restricted
     */
    public boolean isSlotRestricted(int slot) {
        return restrictedSlots.contains(slot);
    }

    /**
     * Block clicking on restricted slots
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Check if clicking on a restricted slot in player inventory
        if (event.getClickedInventory() != null &&
                event.getClickedInventory().equals(player.getInventory())) {

            int slot = event.getSlot();
            if (isSlotRestricted(slot)) {
                // Check if it's our overlay item
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && isOverlayItem(clicked)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.slot-blocked",
                                    "&cThis inventory slot is restricted!")));
                    return;
                }

                // Block placing items in restricted slots
                if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.slot-blocked",
                                    "&cThis inventory slot is restricted!")));
                }
            }
        }

        // Block shift-click into restricted slots
        if (event.isShiftClick() && event.getCurrentItem() != null) {
            // Find where the item would go
            for (int slot : restrictedSlots) {
                ItemStack existing = player.getInventory().getItem(slot);
                if (existing == null || existing.getType().isAir() || isOverlayItem(existing)) {
                    // Item might go to this restricted slot
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.slot-blocked",
                                    "&cThis inventory slot is restricted!")));
                    return;
                }
            }
        }
    }

    /**
     * Apply overlays when player opens inventory
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    return;
                applySlotOverlays(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * Remove overlays when inventory closes
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        removeSlotOverlays(player);
    }

    /**
     * Apply overlays when player joins
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Clear any items in restricted slots
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    return;
                clearRestrictedSlots(player);
            }
        }.runTaskLater(plugin, 5L);
    }

    /**
     * Apply grey glass overlays to empty restricted slots
     */
    private void applySlotOverlays(Player player) {
        Inventory inv = player.getInventory();

        for (int slot : restrictedSlots) {
            if (slot < 0 || slot > 40)
                continue;

            ItemStack current = inv.getItem(slot);
            if (current == null || current.getType().isAir()) {
                inv.setItem(slot, createOverlayItem());
            }
        }
    }

    /**
     * Remove overlay items from inventory
     */
    private void removeSlotOverlays(Player player) {
        Inventory inv = player.getInventory();

        for (int slot = 0; slot <= 40; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item != null && isOverlayItem(item)) {
                inv.setItem(slot, null);
            }
        }
    }

    /**
     * Clear items from restricted slots (for enforcement)
     */
    private void clearRestrictedSlots(Player player) {
        Inventory inv = player.getInventory();

        for (int slot : restrictedSlots) {
            if (slot < 0 || slot > 40)
                continue;

            ItemStack current = inv.getItem(slot);
            if (current != null && !current.getType().isAir() && !isOverlayItem(current)) {
                // Drop item on ground
                player.getWorld().dropItemNaturally(player.getLocation(), current);
                inv.setItem(slot, null);
            }
        }
    }

    /**
     * Create overlay item for restricted slots
     */
    private ItemStack createOverlayItem() {
        String materialName = plugin.getConfig().getString(
                "visuals.blocked-slot-material", "GRAY_STAINED_GLASS_PANE");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString(
                    "visuals.blocked-slot-name", "&c&lRestricted");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "This slot is restricted");
            lore.add(ChatColor.DARK_GRAY + "NoArmor-SlotOverlay");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Check if item is our overlay
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
        return lore != null && lore.stream()
                .anyMatch(line -> line.contains("NoArmor-SlotOverlay") || line.contains("NoArmor-Overlay"));
    }

    public Set<Integer> getRestrictedSlots() {
        return Collections.unmodifiableSet(restrictedSlots);
    }
}
