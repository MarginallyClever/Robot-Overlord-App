package com.marginallyclever.ro3.mesh;

import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.Test;

public class MeshFactoryDialogTest {
    @Test
    public void test() {
        Registry.start();
        var dialog = new MeshFactoryDialog();
        assert(dialog.getMesh()==null);
    }
}
