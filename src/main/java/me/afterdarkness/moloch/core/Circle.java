package me.afterdarkness.moloch.core;

public class Circle implements Shape {
    public float x, y, radius;

    public Circle(float x, float y, float radius) {
        set(x, y, radius);
    }

    public void set(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        float x = mouseX - this.x;
        float y = mouseY - this.y;
        return Math.sqrt((x * x) + (y * y)) < radius;
    }
}
