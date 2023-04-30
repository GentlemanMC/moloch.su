package me.afterdarkness.moloch.core;

import net.spartanb312.base.utils.graphics.RenderUtils2D;

public class Rect implements Shape {
    public float startX, startY, endX, endY;

    public Rect(float startX, float startY, float endX, float endY) {
        set(startX, startY, endX, endY);
    }

    public void set(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return RenderUtils2D.isMouseInRect(mouseX, mouseY, startX, startY, endX, endY);
    }
}
