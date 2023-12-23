package com.marginallyclever.ro3.node;

import java.util.EventListener;

public interface NodeDetachListener extends EventListener {
    void nodeDetached(Node source);
}
