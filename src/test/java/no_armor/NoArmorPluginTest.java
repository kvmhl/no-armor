package no_armor;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NoArmorPlugin core functionality.
 */
@ExtendWith(MockitoExtension.class)
class NoArmorPluginTest {

    @Mock
    private FileConfiguration mockConfig;

    @Mock
    private NoArmorPlugin mockPlugin;

    @BeforeEach
    void setUp() {
        when(mockPlugin.getConfig()).thenReturn(mockConfig);
    }

    @Test
    @DisplayName("isItemAllowed returns true for allowed items")
    void testIsItemAllowed_Allowed() {
        when(mockConfig.getBoolean("allowed-items.diamond_sword", true)).thenReturn(true);
        when(mockPlugin.isItemAllowed("diamond_sword")).thenCallRealMethod();

        assertTrue(mockPlugin.isItemAllowed("diamond_sword"));
    }

    @Test
    @DisplayName("isItemAllowed returns false for blocked items")
    void testIsItemAllowed_Blocked() {
        when(mockConfig.getBoolean("allowed-items.netherite_helmet", true)).thenReturn(false);
        when(mockPlugin.isItemAllowed("netherite_helmet")).thenCallRealMethod();

        assertFalse(mockPlugin.isItemAllowed("netherite_helmet"));
    }

    @Test
    @DisplayName("isItemAllowed defaults to true for missing items")
    void testIsItemAllowed_MissingDefaultsTrue() {
        when(mockConfig.getBoolean("allowed-items.unknown_item", true)).thenReturn(true);
        when(mockPlugin.isItemAllowed("unknown_item")).thenCallRealMethod();

        assertTrue(mockPlugin.isItemAllowed("unknown_item"));
    }
}
