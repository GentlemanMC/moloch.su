package net.spartanb312.base.core.concurrent.blocking;

import net.spartanb312.base.core.concurrent.ConcurrentTaskManager;
import net.spartanb312.base.core.concurrent.task.VoidTask;
import net.spartanb312.base.core.concurrent.utils.Syncer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingContent {

    private final List<BlockingUnit> tasks = new ArrayList<>();
    private final AtomicInteger finished = new AtomicInteger(0);
    private Syncer syncer;

    public void launch(VoidTask task) {
        if (ConcurrentTaskManager.instance.executor == null) {
            return;
        }

        BlockingUnit unit = new BlockingUnit(task, this);
        ConcurrentTaskManager.instance.executor.execute(unit);
        tasks.add(unit);
    }

    //Invoke this synchronized method to count.If the blocking thread invoked await(),then the child tasks will start counting down syncer
    public synchronized void count() {
        //Double lock to make sure we will wait for correct count
        synchronized (finished) {
            finished.incrementAndGet();
        }
    }

    public void await() {
        if (tasks.size() == 0) return;
        //Lock the count first to create a syncer
        synchronized (finished) {
            syncer = new Syncer(tasks.size() - finished.get());
        }
        syncer.await();
    }

    public synchronized void countDown() {
        if (syncer != null) syncer.countDown();
    }

}
