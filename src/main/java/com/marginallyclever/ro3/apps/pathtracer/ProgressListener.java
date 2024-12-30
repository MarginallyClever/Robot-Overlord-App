package com.marginallyclever.ro3.apps.pathtracer;

import java.util.EventListener;

public interface ProgressListener extends EventListener {
    void onProgressUpdate(int progress);
}
