/*
 * Copyright 2024 Benjamin Martin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.lapismc.lapismine.scheduler;

import net.lapismc.lapismine.LapisMine;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class LapisMineRunnable implements Runnable {

    private final double maxMillisPerTick = 5;
    private final Deque<MineTask> taskDeque = new ArrayDeque<>();
    private final TickTimeStorage tickTimeStorage;
    private double currentMillisPerTick = maxMillisPerTick;

    public LapisMineRunnable(LapisMine plugin) {
        tickTimeStorage = new TickTimeStorage(25);
        Bukkit.getScheduler().runTaskTimer(plugin, this, 1, 1);
        //Start delayed a bit so that the values have time to come in
        Bukkit.getScheduler().runTaskTimer(plugin, () -> currentMillisPerTick += calculateAdjustment(), 20 * 10, 20);
    }

    public void addTask(MineTask task) {
        taskDeque.add(task);
    }

    @Override
    public void run() {
        long systemNanos = System.nanoTime();
        tickTimeStorage.add(systemNanos);
        int nanosThisTick = (int) (currentMillisPerTick * 1E6);
        long stopTime = systemNanos + nanosThisTick;

        MineTask nextTask;
        while (System.nanoTime() <= stopTime && (nextTask = taskDeque.poll()) != null) {
            nextTask.run();
        }
    }

    private double calculateAdjustment() {
        //Calculate if we need to decrease our millis per tick, by checking current TPS
        List<Long> tickTimes = tickTimeStorage.getEntries(20);
        long endTime = (long) (tickTimes.get(tickTimes.size() - 1) / 1E6);
        long startTime = (long) (tickTimes.get(0) / 1E6);
        double ticksPerSecond = 1000.0 / ((double) (endTime - startTime) / tickTimes.size());
        double limitedTicksPerSecond = Math.min(ticksPerSecond, 20);
        double adjustment = maxMillisPerTick - currentMillisPerTick;
        if (limitedTicksPerSecond < 10) {
            adjustment -= 4;
        } else if (limitedTicksPerSecond < 15) {
            adjustment -= 3;
        } else if (limitedTicksPerSecond < 18) {
            adjustment -= 2;
        } else if (limitedTicksPerSecond < 19) {
            adjustment -= 1;
        }
        return adjustment;
    }
}
