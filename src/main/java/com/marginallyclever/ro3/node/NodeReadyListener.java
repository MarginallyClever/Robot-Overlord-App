package com.marginallyclever.ro3.node;

import java.util.EventListener;

public interface NodeReadyListener extends EventListener {
    void nodeReady(Node source);
}
