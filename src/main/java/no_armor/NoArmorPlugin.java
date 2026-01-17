package no_armor;

import no_armor.listeners.ArmorEquipListener;
import no_armor.listeners.InventorySlotListener;
import no_armor.listeners.SlotRestrictionListener;
import no_armor.listeners.ToolUseListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * NoArmor - A lightweight Paper plugin for restricting armor and tool usage.
 */
public class NoArmorPlugin extends JavaPlugin {

    private static NoArmorPlugin instance;
    private SlotRestrictionListener slotRestrictionListener;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if not exists
        saveDefaultConfig();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolUseListener(this), this);
        getServer().getPluginManager().registerEvents(new InventorySlotListener(this), this);

        slotRestrictionListener = new SlotRestrictionListener(this);
        getServer().getPluginManager().registerEvents(slotRestrictionListener, this);

        getLogger().info("NoArmor plugin enabled! Restricting items as configured.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoArmor plugin disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("noarmor")) {
            if (!sender.hasPermission("noarmor.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                if (slotRestrictionListener != null) {
                    slotRestrictionListener.loadRestrictedSlots();
                }
                sender.sendMessage("§aNoArmor configuration reloaded!");
                return true;
            }

            sender.sendMessage("§6NoArmor Commands:");
            sender.sendMessage("§e/noarmor reload §7- Reload configuration");
            return true;
        }
        return false;
    }

    public static NoArmorPlugin getInstance() {
        return instance;
    }

    /**
     * Check if an item is allowed based on config
     */
    public boolean isItemAllowed(String itemKey) {
        return getConfig().getBoolean("allowed-items." + itemKey, true);
    }

    public SlotRestrictionListener getSlotRestrictionListener() {
        return slotRestrictionListener;
    }
}
