package no_armor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for slot restriction logic and overlay detection.
 */
class SlotRestrictionLogicTest {

    // Inventory slot constants (matching SlotRestrictionListener)
    static final int HOTBAR_START = 0;
    static final int HOTBAR_END = 8;
    static final int MAIN_INV_START = 9;
    static final int MAIN_INV_END = 35;
    static final int BOOTS_SLOT = 36;
    static final int LEGGINGS_SLOT = 37;
    static final int CHESTPLATE_SLOT = 38;
    static final int HELMET_SLOT = 39;
    static final int OFFHAND_SLOT = 40;

    private Set<Integer> restrictedSlots;

    @BeforeEach
    void setUp() {
        restrictedSlots = new HashSet<>();
    }

    @Nested
    @DisplayName("Slot Range Parsing")
    class SlotRangeParsing {

        @Test
        @DisplayName("Parse single slot number")
        void parseSingleSlot() {
            parseAndAddSlots("5");
            assertTrue(restrictedSlots.contains(5));
            assertEquals(1, restrictedSlots.size());
        }

        @Test
        @DisplayName("Parse slot range (0-8)")
        void parseSlotRange() {
            parseAndAddSlots("0-8");
            assertEquals(9, restrictedSlots.size());
            for (int i = 0; i <= 8; i++) {
                assertTrue(restrictedSlots.contains(i), "Should contain slot " + i);
            }
        }

        @Test
        @DisplayName("Parse 'hotbar' keyword")
        void parseHotbarKeyword() {
            parseAndAddSlots("hotbar");
            assertEquals(9, restrictedSlots.size());
            for (int i = HOTBAR_START; i <= HOTBAR_END; i++) {
                assertTrue(restrictedSlots.contains(i));
            }
        }

        @Test
        @DisplayName("Parse 'main' keyword")
        void parseMainKeyword() {
            parseAndAddSlots("main");
            assertEquals(27, restrictedSlots.size());
            for (int i = MAIN_INV_START; i <= MAIN_INV_END; i++) {
                assertTrue(restrictedSlots.contains(i));
            }
        }

        @Test
        @DisplayName("Parse 'armor' keyword")
        void parseArmorKeyword() {
            parseAndAddSlots("armor");
            assertEquals(4, restrictedSlots.size());
            assertTrue(restrictedSlots.contains(BOOTS_SLOT));
            assertTrue(restrictedSlots.contains(LEGGINGS_SLOT));
            assertTrue(restrictedSlots.contains(CHESTPLATE_SLOT));
            assertTrue(restrictedSlots.contains(HELMET_SLOT));
        }

        @Test
        @DisplayName("Parse 'offhand' keyword")
        void parseOffhandKeyword() {
            parseAndAddSlots("offhand");
            assertEquals(1, restrictedSlots.size());
            assertTrue(restrictedSlots.contains(OFFHAND_SLOT));
        }

        @Test
        @DisplayName("Parse multiple entries")
        void parseMultipleEntries() {
            parseAndAddSlots("0-2");
            parseAndAddSlots("offhand");
            parseAndAddSlots("36");

            assertEquals(5, restrictedSlots.size());
            assertTrue(restrictedSlots.contains(0));
            assertTrue(restrictedSlots.contains(1));
            assertTrue(restrictedSlots.contains(2));
            assertTrue(restrictedSlots.contains(OFFHAND_SLOT));
            assertTrue(restrictedSlots.contains(36));
        }

        @Test
        @DisplayName("Invalid entry is ignored")
        void parseInvalidEntry() {
            parseAndAddSlots("invalid");
            assertTrue(restrictedSlots.isEmpty());
        }

        @Test
        @DisplayName("Empty entry is ignored")
        void parseEmptyEntry() {
            parseAndAddSlots("");
            parseAndAddSlots("   ");
            assertTrue(restrictedSlots.isEmpty());
        }
    }

    @Nested
    @DisplayName("Slot Restriction Checks")
    class SlotRestrictionChecks {

        @Test
        @DisplayName("isSlotRestricted returns true for restricted slots")
        void restrictedSlotReturnsTrue() {
            restrictedSlots.add(5);
            restrictedSlots.add(10);

            assertTrue(isSlotRestricted(5));
            assertTrue(isSlotRestricted(10));
        }

        @Test
        @DisplayName("isSlotRestricted returns false for unrestricted slots")
        void unrestrictedSlotReturnsFalse() {
            restrictedSlots.add(5);

            assertFalse(isSlotRestricted(0));
            assertFalse(isSlotRestricted(6));
            assertFalse(isSlotRestricted(40));
        }

        @ParameterizedTest
        @DisplayName("Armor slots are correctly identified")
        @ValueSource(ints = { 36, 37, 38, 39 })
        void armorSlotsIdentified(int slot) {
            assertTrue(isArmorSlot(slot));
        }

        @ParameterizedTest
        @DisplayName("Non-armor slots are correctly identified")
        @ValueSource(ints = { 0, 8, 9, 35, 40 })
        void nonArmorSlotsIdentified(int slot) {
            assertFalse(isArmorSlot(slot));
        }
    }

    @Nested
    @DisplayName("Overlay Item Detection")
    class OverlayItemDetection {

        @Test
        @DisplayName("Overlay lore marker is detected")
        void overlayLoreDetected() {
            List<String> lore = List.of("This slot is restricted", "NoArmor-SlotOverlay");
            assertTrue(containsOverlayMarker(lore));
        }

        @Test
        @DisplayName("Alternative overlay marker is detected")
        void alternativeOverlayMarkerDetected() {
            List<String> lore = List.of("Some text", "NoArmor-Overlay");
            assertTrue(containsOverlayMarker(lore));
        }

        @Test
        @DisplayName("Regular item lore is not detected as overlay")
        void regularLoreNotDetected() {
            List<String> lore = List.of("Diamond Sword", "+5 Attack Damage");
            assertFalse(containsOverlayMarker(lore));
        }

        @Test
        @DisplayName("Null lore returns false")
        void nullLoreReturnsFalse() {
            assertFalse(containsOverlayMarker(null));
        }

        @Test
        @DisplayName("Empty lore returns false")
        void emptyLoreReturnsFalse() {
            assertFalse(containsOverlayMarker(List.of()));
        }
    }

    @Nested
    @DisplayName("Slot Range Calculations")
    class SlotRangeCalculations {

        @Test
        @DisplayName("Total inventory slots is 41 (0-40)")
        void totalInventorySlots() {
            assertEquals(41, OFFHAND_SLOT + 1);
        }

        @Test
        @DisplayName("Hotbar has 9 slots")
        void hotbarSlotCount() {
            assertEquals(9, HOTBAR_END - HOTBAR_START + 1);
        }

        @Test
        @DisplayName("Main inventory has 27 slots")
        void mainInventorySlotCount() {
            assertEquals(27, MAIN_INV_END - MAIN_INV_START + 1);
        }

        @Test
        @DisplayName("Armor has 4 slots")
        void armorSlotCount() {
            assertEquals(4, HELMET_SLOT - BOOTS_SLOT + 1);
        }
    }

    // Helper methods matching plugin logic

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
            // Invalid entry - ignore
        }
    }

    private boolean isSlotRestricted(int slot) {
        return restrictedSlots.contains(slot);
    }

    private boolean isArmorSlot(int slot) {
        return slot >= BOOTS_SLOT && slot <= HELMET_SLOT;
    }

    private boolean containsOverlayMarker(List<String> lore) {
        if (lore == null)
            return false;
        return lore.stream().anyMatch(line -> line.contains("NoArmor-SlotOverlay") || line.contains("NoArmor-Overlay"));
    }
}
