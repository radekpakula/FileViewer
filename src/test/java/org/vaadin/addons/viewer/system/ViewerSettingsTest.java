package org.vaadin.addons.viewer.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViewerSettingsTest {

    @Test
    void whenConstructedWithValuesInRangeThenStoresThem() {
        ViewerSettings settings = new ViewerSettings(16, 20, 18);

        assertEquals(16, settings.getFontSize());
        assertEquals(20, settings.getLineHeight());
        assertEquals(18, settings.getRowHeight());
    }

    @Test
    void whenConstructedWithValuesOutsideSupportedRangesThenRejectsThem() {
        assertThrows(IllegalArgumentException.class, () -> new ViewerSettings(7, 20, 18));
        assertThrows(IllegalArgumentException.class, () -> new ViewerSettings(16, 31, 18));
        assertThrows(IllegalArgumentException.class, () -> new ViewerSettings(16, 20, 31));
    }

    @Test
    void whenCopyingSettingsThenReturnsIndependentCopy() {
        ViewerSettings original = new ViewerSettings(16, 20, 18);
        ViewerSettings copy = original.getCopy();
        copy.setFontSize(18);

        assertEquals(16, original.getFontSize());
        assertEquals(18, copy.getFontSize());
    }
}
