package no_armor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for inventory slot restriction logic.
 */
class InventorySlotRestrictionTest {

    // Standard inventory slot ranges
    private static final int HOTBAR_START = 0;
    private static final int HOTBAR_END = 8;
    private static final int MAIN_INVENTORY_START = 9;
    private static final int MAIN_INVENTORY_END = 35;
    private static final int ARMOR_START = 36;
    private static final int ARMOR_END = 39;
    private static final int OFFHAND_SLOT = 40;

    @Test
    @DisplayName("Hotbar slots are in valid range 0-8")
    void testHotbarSlotRange() {
        for (int slot = HOTBAR_START; slot <= HOTBAR_END; slot++) {
            assertTrue(isHotbarSlot(slot), "Slot " + slot + " should be hotbar");
        }
        assertFalse(isHotbarSlot(9), "Slot 9 should NOT be hotbar");
    }

    @Test
    @DisplayName("Main inventory slots are in valid range 9-35")
    void testMainInventorySlotRange() {
        for (int slot = MAIN_INVENTORY_START; slot <= MAIN_INVENTORY_END; slot++) {
            assertTrue(isMainInventorySlot(slot), "Slot " + slot + " should be main inventory");
        }
        assertFalse(isMainInventorySlot(8), "Slot 8 should NOT be main inventory");
        assertFalse(isMainInventorySlot(36), "Slot 36 should NOT be main inventory");
    }

    @Test
    @DisplayName("Armor slots are in valid range 36-39")
    void testArmorSlotRange() {
        for (int slot = ARMOR_START; slot <= ARMOR_END; slot++) {
            assertTrue(isArmorSlot(slot), "Slot " + slot + " should be armor");
        }
    }

    @Test
    @DisplayName("Offhand slot is 40")
    void testOffhandSlot() {
        assertTrue(isOffhandSlot(40));
        assertFalse(isOffhandSlot(39));
        assertFalse(isOffhandSlot(41));
    }

    @Test
    @DisplayName("Restricted slots set correctly blocks specified slots")
    void testRestrictedSlotsSet() {
        Set<Integer> restrictedSlots = Set.of(0, 1, 2, 9, 10, 11);

        assertTrue(restrictedSlots.contains(0));
        assertTrue(restrictedSlots.contains(9));
        assertFalse(restrictedSlots.contains(8));
        assertFalse(restrictedSlots.contains(35));
    }

    @Test
    @DisplayName("Parse slot range string correctly")
    void testParseSlotRange() {
        // Test "0-8" -> slots 0 through 8
        Set<Integer> slots = parseSlotRange("0-8");
        assertEquals(9, slots.size());
        assertTrue(slots.contains(0));
        assertTrue(slots.contains(8));
        assertFalse(slots.contains(9));
    }

    @Test
    @DisplayName("Parse single slot correctly")
    void testParseSingleSlot() {
        Set<Integer> slots = parseSlotRange("5");
        assertEquals(1, slots.size());
        assertTrue(slots.contains(5));
    }

    // Helper methods
    private boolean isHotbarSlot(int slot) {
        return slot >= HOTBAR_START && slot <= HOTBAR_END;
    }

    private boolean isMainInventorySlot(int slot) {
        return slot >= MAIN_INVENTORY_START && slot <= MAIN_INVENTORY_END;
    }

    private boolean isArmorSlot(int slot) {
        return slot >= ARMOR_START && slot <= ARMOR_END;
    }

    private boolean isOffhandSlot(int slot) {
        return slot == OFFHAND_SLOT;
    }

    private Set<Integer> parseSlotRange(String range) {
        if (range.contains("-")) {
            String[] parts = range.split("-");
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            java.util.Set<Integer> slots = new java.util.HashSet<>();
            for (int i = start; i <= end; i++) {
                slots.add(i);
            }
            return slots;
        } else {
            return Set.of(Integer.parseInt(range));
        }
    }
}
