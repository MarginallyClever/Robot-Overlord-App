package com.marginallyclever.ro3.mesh;

import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.Test;

public class MeshChooserDialogTest {
    @Test
    public void test() {
        Registry.start();
        var dialog = new MeshChooserDialog();
        assert(dialog.getSelectedItem()==null);
    }
}
