package conglin.clrpc.common;

import java.util.Collection;

/**
 * 任务
 */

@FunctionalInterface
public interface Task {
    /**
     * 运行任务
     */
    void run();

    /**
     * 顺序执行任务
     * 
     * @param after
     */
    default void andThen(Task after) {
        run();
        after.run();
    }

    /**
     * 顺序执行任务
     * 
     * @param after
     */
    default void andThen(Collection<Task> after) {
        run();
        for (Task t : after) {
            t.run();
        }
    }
}