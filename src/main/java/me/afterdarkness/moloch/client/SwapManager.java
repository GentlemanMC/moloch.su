package me.afterdarkness.moloch.client;

import me.afterdarkness.moloch.module.modules.client.GlobalManagers;
import net.spartanb312.base.core.concurrent.task.VoidTask;
import net.spartanb312.base.utils.Timer;

public class SwapManager {
    private static final Timer swapTimeout = new Timer();
    private static String currentTaskName;

    public static void swapInvoke(String moduleName, boolean noDelayCondition, boolean shouldResetTimeout, VoidTask task) {
        if (noDelayCondition
                || swapTimeout.passed(GlobalManagers.INSTANCE.swapTimeout.getValue())
                || (currentTaskName != null && currentTaskName.equals(moduleName))) {
            currentTaskName = moduleName;
            task.invoke();
            if (!noDelayCondition && shouldResetTimeout)
                swapTimeout.reset();
        }
    }
}
