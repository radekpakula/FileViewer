package org.vaadin.addons.viewer.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViewerSettingsTest {

    @Test
    void whenSettingsAreCreatedWithValidValuesThenKeepsValues() {
        ViewerSettings settings = new ViewerSettings(12, 18, 20);

        assertEquals(12, settings.getFontSize());
        assertEquals(18, settings.getLineHeight());
        assertEquals(20, settings.getRowHeight());
    }

    @Test
    void whenSettingsContainOutOfRangeValuesThenRejectsThem() {
        assertThrows(IllegalArgumentException.class, () -> new ViewerSettings(7, 18, 20));
        assertThrows(IllegalArgumentException.class, () -> new ViewerSettings(12, 31, 20));
        assertThrows(IllegalArgumentException.class, () -> new ViewerSettings(12, 18, 31));

        ViewerSettings settings = ViewerSettings.base();
        assertThrows(IllegalArgumentException.class, () -> settings.setFontSize(31));
        assertThrows(IllegalArgumentException.class, () -> settings.setLineHeight(7));
        assertThrows(IllegalArgumentException.class, () -> settings.setRowHeight(13));
    }

    @Test
    void whenSettingsAreCopiedThenChangesRemainIndependent() {
        ViewerSettings original = ViewerSettings.base();

        ViewerSettings copy = original.getCopy();
        copy.setFontSize(10);

        assertNotSame(original, copy);
        assertEquals(20, original.getFontSize());
        assertEquals(10, copy.getFontSize());
    }
}
