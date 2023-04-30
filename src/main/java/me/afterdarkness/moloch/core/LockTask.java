package me.afterdarkness.moloch.core;

import net.spartanb312.base.core.concurrent.task.VoidTask;

public class LockTask {
    private boolean locked;
    private final VoidTask task;

    public LockTask(VoidTask task) {
        this.task = task;
    }

    public void invokeLock() {
        if (locked) return;
        task.invoke();
        locked = true;
    }

    public void reverseInvokeLock() {
        if (!locked) return;
        task.invoke();
        locked = false;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean getLocked() {
        return locked;
    }
}
