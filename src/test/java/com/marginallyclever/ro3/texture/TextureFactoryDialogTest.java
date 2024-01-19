package com.marginallyclever.ro3.texture;

import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.Test;

public class TextureFactoryDialogTest {
    @Test
    public void test() {
        Registry.start();
        var dialog = new TextureFactoryDialog();
        assert(dialog.getTexture()==null);
    }
}
