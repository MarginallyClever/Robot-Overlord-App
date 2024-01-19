package com.marginallyclever.ro3.texture;

import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.Test;

public class TextureChooserDialogTest {
    @Test
    public void test() {
        Registry.start();
        var dialog = new TextureChooserDialog();
        assert(dialog.getSelectedItem()==null);
    }
}
