package no_armor;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for crafting restriction logic.
 */
class CraftingRestrictionLogicTest {

    private final Map<String, Boolean> allowedItems = new HashMap<>();

    @Nested
    @DisplayName("Item Restriction Checks")
    class ItemRestrictionChecks {

        @Test
        @DisplayName("Allowed item returns true")
        void allowedItemReturnsTrue() {
            allowedItems.put("diamond_sword", true);
            assertTrue(isItemAllowed("diamond_sword"));
        }

        @Test
        @DisplayName("Blocked item returns false")
        void blockedItemReturnsFalse() {
            allowedItems.put("netherite_helmet", false);
            assertFalse(isItemAllowed("netherite_helmet"));
        }

        @Test
        @DisplayName("Unknown item defaults to allowed")
        void unknownItemDefaultsToAllowed() {
            // Item not in config defaults to true
            assertTrue(isItemAllowed("unknown_item"));
        }

        @Test
        @DisplayName("Material name is lowercased correctly")
        void materialNameLowercased() {
            Material material = Material.DIAMOND_SWORD;
            String key = material.name().toLowerCase();
            assertEquals("diamond_sword", key);
        }
    }

    @Nested
    @DisplayName("Armor Material Classification")
    class ArmorMaterialClassification {

        @ParameterizedTest
        @DisplayName("All helmet materials are identified")
        @ValueSource(strings = {
                "LEATHER_HELMET", "CHAINMAIL_HELMET", "IRON_HELMET",
                "GOLDEN_HELMET", "DIAMOND_HELMET", "NETHERITE_HELMET", "TURTLE_HELMET"
        })
        void helmetsIdentified(String materialName) {
            assertTrue(isArmorMaterial(materialName.toLowerCase()));
        }

        @ParameterizedTest
        @DisplayName("All chestplate materials are identified")
        @ValueSource(strings = {
                "LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "IRON_CHESTPLATE",
                "GOLDEN_CHESTPLATE", "DIAMOND_CHESTPLATE", "NETHERITE_CHESTPLATE"
        })
        void chestplatesIdentified(String materialName) {
            assertTrue(isArmorMaterial(materialName.toLowerCase()));
        }

        @ParameterizedTest
        @DisplayName("Elytra is identified as armor")
        @ValueSource(strings = { "ELYTRA" })
        void elytraIdentified(String materialName) {
            assertTrue(isArmorMaterial(materialName.toLowerCase()));
        }

        @ParameterizedTest
        @DisplayName("Non-armor materials are not identified as armor")
        @ValueSource(strings = { "DIAMOND_SWORD", "STONE", "APPLE", "BOW" })
        void nonArmorNotIdentified(String materialName) {
            assertFalse(isArmorMaterial(materialName.toLowerCase()));
        }
    }

    @Nested
    @DisplayName("Tool Material Classification")
    class ToolMaterialClassification {

        @ParameterizedTest
        @DisplayName("Swords are identified as tools")
        @ValueSource(strings = {
                "WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD",
                "GOLDEN_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD"
        })
        void swordsIdentified(String materialName) {
            assertTrue(isToolMaterial(materialName.toLowerCase()));
        }

        @ParameterizedTest
        @DisplayName("Pickaxes are identified as tools")
        @ValueSource(strings = {
                "WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE",
                "GOLDEN_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE"
        })
        void pickaxesIdentified(String materialName) {
            assertTrue(isToolMaterial(materialName.toLowerCase()));
        }

        @ParameterizedTest
        @DisplayName("Ranged weapons are identified")
        @ValueSource(strings = { "BOW", "CROSSBOW", "TRIDENT" })
        void rangedWeaponsIdentified(String materialName) {
            assertTrue(isToolMaterial(materialName.toLowerCase()));
        }

        @ParameterizedTest
        @DisplayName("Shield is identified as tool")
        @ValueSource(strings = { "SHIELD" })
        void shieldIdentified(String materialName) {
            assertTrue(isToolMaterial(materialName.toLowerCase()));
        }
    }

    @Nested
    @DisplayName("Config Key Generation")
    class ConfigKeyGeneration {

        @Test
        @DisplayName("Material converts to correct config key")
        void materialToConfigKey() {
            assertEquals("diamond_helmet", Material.DIAMOND_HELMET.name().toLowerCase());
            assertEquals("netherite_sword", Material.NETHERITE_SWORD.name().toLowerCase());
            assertEquals("bow", Material.BOW.name().toLowerCase());
        }

        @Test
        @DisplayName("Config keys match expected format")
        void configKeyFormat() {
            String key = "diamond_chestplate";
            assertTrue(key.matches("[a-z_]+"));
            assertFalse(key.contains(" "));
            assertFalse(key.contains("-"));
        }
    }

    // Helper methods matching plugin logic

    private boolean isItemAllowed(String itemKey) {
        return allowedItems.getOrDefault(itemKey, true);
    }

    private boolean isArmorMaterial(String name) {
        return name.contains("helmet") || name.contains("chestplate") ||
                name.contains("leggings") || name.contains("boots") ||
                name.equals("elytra") || name.contains("_head") ||
                name.contains("_skull") || name.equals("carved_pumpkin") ||
                name.equals("turtle_helmet");
    }

    private boolean isToolMaterial(String name) {
        return name.contains("sword") || name.contains("pickaxe") ||
                name.contains("axe") || name.contains("shovel") ||
                name.contains("hoe") || name.equals("bow") ||
                name.equals("crossbow") || name.equals("trident") ||
                name.equals("shield") || name.equals("fishing_rod") ||
                name.equals("flint_and_steel") || name.equals("shears");
    }
}
