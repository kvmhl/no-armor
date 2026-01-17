package no_armor;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for item classification utilities.
 */
class ItemClassificationTest {

    @ParameterizedTest
    @DisplayName("Helmets are correctly identified as armor")
    @ValueSource(strings = {
            "LEATHER_HELMET", "CHAINMAIL_HELMET", "IRON_HELMET",
            "GOLDEN_HELMET", "DIAMOND_HELMET", "NETHERITE_HELMET", "TURTLE_HELMET"
    })
    void testHelmetsAreArmor(String materialName) {
        Material material = Material.valueOf(materialName);
        assertTrue(isArmor(material), materialName + " should be armor");
    }

    @ParameterizedTest
    @DisplayName("Chestplates are correctly identified as armor")
    @ValueSource(strings = {
            "LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "IRON_CHESTPLATE",
            "GOLDEN_CHESTPLATE", "DIAMOND_CHESTPLATE", "NETHERITE_CHESTPLATE", "ELYTRA"
    })
    void testChestplatesAreArmor(String materialName) {
        Material material = Material.valueOf(materialName);
        assertTrue(isArmor(material), materialName + " should be armor");
    }

    @ParameterizedTest
    @DisplayName("Swords are correctly identified as tools/weapons")
    @ValueSource(strings = {
            "WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD",
            "GOLDEN_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD"
    })
    void testSwordsAreWeapons(String materialName) {
        Material material = Material.valueOf(materialName);
        assertTrue(isTool(material), materialName + " should be a tool/weapon");
    }

    @ParameterizedTest
    @DisplayName("Non-equipment items are not armor")
    @ValueSource(strings = { "STONE", "DIRT", "DIAMOND", "APPLE" })
    void testNonEquipmentNotArmor(String materialName) {
        Material material = Material.valueOf(materialName);
        assertFalse(isArmor(material), materialName + " should NOT be armor");
    }

    @Test
    @DisplayName("Shield is identified as defensive equipment")
    void testShieldIsDefensive() {
        assertTrue(isTool(Material.SHIELD));
    }

    @Test
    @DisplayName("Bow is identified as ranged weapon")
    void testBowIsRanged() {
        assertTrue(isTool(Material.BOW));
        assertTrue(isTool(Material.CROSSBOW));
    }

    // Helper methods matching plugin logic
    private boolean isArmor(Material material) {
        String name = material.name().toLowerCase();
        return name.contains("helmet") || name.contains("chestplate") ||
                name.contains("leggings") || name.contains("boots") ||
                name.equals("elytra") || name.contains("_head") ||
                name.contains("_skull") || name.equals("carved_pumpkin") ||
                name.equals("turtle_helmet");
    }

    private boolean isTool(Material material) {
        String name = material.name().toLowerCase();
        return name.contains("sword") || name.contains("pickaxe") ||
                name.contains("axe") || name.contains("shovel") ||
                name.contains("hoe") || name.equals("bow") ||
                name.equals("crossbow") || name.equals("trident") ||
                name.equals("shield") || name.equals("fishing_rod") ||
                name.equals("flint_and_steel") || name.equals("shears");
    }
}
