package com.marginallyclever.ro3.node;

import java.util.EventListener;

public interface NodeRenameListener extends EventListener {
    void nodeRenamed(Node node);
}
