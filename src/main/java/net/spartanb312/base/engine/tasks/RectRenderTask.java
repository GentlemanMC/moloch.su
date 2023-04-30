package net.spartanb312.base.engine.tasks;

import net.spartanb312.base.utils.graphics.RenderUtils2D;
import net.spartanb312.base.engine.RenderTask;

public class RectRenderTask implements RenderTask {

    float x, y, endX, endY;
    int colorTopRight, colorTopLeft, colorDownLeft, colorDownRight;

    public RectRenderTask(float x, float y, float endX, float endY, int colorTopRight, int colorTopLeft, int colorDownLeft, int colorDownRight) {
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
        this.colorTopRight = colorTopRight;
        this.colorTopLeft = colorTopLeft;
        this.colorDownLeft = colorDownLeft;
        this.colorDownRight = colorDownRight;
    }

    public RectRenderTask(float x, float y, float endX, float endY, int color) {
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
        this.colorTopRight = color;
        this.colorTopLeft = color;
        this.colorDownLeft = color;
        this.colorDownRight = color;
    }

    @Override
    public void onRender() {
        RenderUtils2D.drawCustomRect(x, y, endX, endY, colorTopRight, colorTopLeft, colorDownLeft, colorDownRight);
    }

}
