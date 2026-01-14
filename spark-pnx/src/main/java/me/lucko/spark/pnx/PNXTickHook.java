package me.lucko.spark.pnx;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.TaskHandler;
import me.lucko.spark.common.tick.AbstractTickHook;
import me.lucko.spark.common.tick.TickHook;

public class PNXTickHook extends AbstractTickHook implements TickHook, Runnable {
    private final Plugin plugin;
    private TaskHandler task;

    public PNXTickHook(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        onTick();
    }

    @Override
    public void start() {
        this.task = this.plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(this.plugin, this, 1,1);
    }

    @Override
    public void close() {
        this.task.cancel();
    }

}