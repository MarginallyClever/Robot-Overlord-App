package com.marginallyclever.ro3.node;

import java.util.EventListener;

public interface NodeAttachListener extends EventListener {
    void nodeAttached(Node source);
}
