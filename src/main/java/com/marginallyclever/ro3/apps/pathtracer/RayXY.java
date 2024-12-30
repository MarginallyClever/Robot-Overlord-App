package com.marginallyclever.ro3.apps.pathtracer;

public class RayXY {
    public int x;
    public int y;
    public int samples = 0;
    public final ColorDouble sum = new ColorDouble(0, 0, 0);
    public final ColorDouble average = new ColorDouble(0, 0, 0);

    public RayXY(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
