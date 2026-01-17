package no_armor.listeners;

import no_armor.NoArmorPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Handles inventory slot restrictions - blocks specific inventory slots
 * entirely.
 * Uses a periodic task to maintain overlays and enforce restrictions.
 */
public class SlotRestrictionListener implements Listener {

    private final NoArmorPlugin plugin;
    private final Set<Integer> restrictedSlots = new HashSet<>();
    private final Map<UUID, Integer> enforcementTasks = new HashMap<>();

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

    public void loadRestrictedSlots() {
        restrictedSlots.clear();

        List<String> slotConfig = plugin.getConfig().getStringList("restricted-slots.slots");
        for (String entry : slotConfig) {
            parseAndAddSlots(entry);
        }

        plugin.getLogger().info("Loaded " + restrictedSlots.size() + " restricted inventory slots");
    }

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

    public boolean isSlotRestricted(int slot) {
        return restrictedSlots.contains(slot);
    }

    /**
     * Block direct clicks on restricted slots only
     * Let shift-clicks through - the enforcer will handle items landing in wrong
     * slots
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Only block direct interaction with restricted slots
        if (event.getClickedInventory() instanceof PlayerInventory) {
            int slot = event.getSlot();
            if (isSlotRestricted(slot)) {
                // Block picking up overlay items
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && isOverlayItem(clicked)) {
                    event.setCancelled(true);
                    return;
                }

                // Block placing items directly in restricted slots
                ClickType click = event.getClick();
                if (click == ClickType.LEFT || click == ClickType.RIGHT ||
                        click == ClickType.MIDDLE || click == ClickType.CREATIVE) {
                    if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                        event.setCancelled(true);
                        sendBlockedMessage(player);
                        return;
                    }
                }
            }
        }

        // Handle hotbar swap (number keys) to restricted slots
        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarSlot = event.getHotbarButton();
            if (isSlotRestricted(hotbarSlot)) {
                event.setCancelled(true);
                return;
            }
            // Also block swapping TO a restricted slot
            if (event.getClickedInventory() instanceof PlayerInventory) {
                if (isSlotRestricted(event.getSlot())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Block dragging items into restricted slots
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Check if any dragged slot is restricted in player inventory
        for (int rawSlot : event.getRawSlots()) {
            if (event.getView().getInventory(rawSlot) instanceof PlayerInventory) {
                int slot = event.getView().convertSlot(rawSlot);
                if (isSlotRestricted(slot)) {
                    event.setCancelled(true);
                    sendBlockedMessage(player);
                    return;
                }
            }
        }
    }

    /**
     * Start enforcement task when player joins
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("noarmor.bypass")) {
            return;
        }

        // Start periodic enforcement task
        startEnforcementTask(player);
    }

    /**
     * Stop enforcement task when player leaves
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopEnforcementTask(event.getPlayer());
        removeSlotOverlays(event.getPlayer());
    }

    /**
     * Start a periodic task to enforce slot restrictions AND maintain overlays
     */
    private void startEnforcementTask(Player player) {
        UUID uuid = player.getUniqueId();

        // Cancel existing task if any
        stopEnforcementTask(player);

        // Run every 5 ticks (0.25 seconds) to catch bypasses and maintain overlays
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    enforcementTasks.remove(uuid);
                    return;
                }
                if (player.hasPermission("noarmor.bypass")) {
                    return;
                }

                // ALWAYS maintain overlays and enforce restrictions
                enforceAndOverlay(player);
            }
        }.runTaskTimer(plugin, 10L, 5L).getTaskId();

        enforcementTasks.put(uuid, taskId);
    }

    /**
     * Stop the enforcement task for a player
     */
    private void stopEnforcementTask(Player player) {
        UUID uuid = player.getUniqueId();
        Integer taskId = enforcementTasks.remove(uuid);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Enforce restrictions AND apply/maintain overlays
     */
    private void enforceAndOverlay(Player player) {
        PlayerInventory inv = player.getInventory();

        for (int slot : restrictedSlots) {
            if (slot < 0 || slot > 40)
                continue;

            ItemStack current = inv.getItem(slot);

            if (current == null || current.getType().isAir()) {
                // Empty slot - place overlay
                inv.setItem(slot, createOverlayItem());
            } else if (!isOverlayItem(current)) {
                // Real item in restricted slot - drop it
                player.getWorld().dropItemNaturally(player.getLocation(), current.clone());
                inv.setItem(slot, createOverlayItem());
            }
            // If it's already an overlay, leave it alone
        }
    }

    /**
     * Remove overlay items from inventory
     */
    private void removeSlotOverlays(Player player) {
        PlayerInventory inv = player.getInventory();

        for (int slot = 0; slot <= 40; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item != null && isOverlayItem(item)) {
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
    public boolean isOverlayItem(ItemStack item) {
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

    private void sendBlockedMessage(Player player) {
        String message = plugin.getConfig().getString("messages.slot-blocked", "&cThis inventory slot is restricted!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public Set<Integer> getRestrictedSlots() {
        return Collections.unmodifiableSet(restrictedSlots);
    }
}
