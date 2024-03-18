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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TickTimeStorage {

    private final Queue<Long> tickTimes;
    private final int size;

    public TickTimeStorage(int size) {
        tickTimes = new ArrayDeque<>();
        this.size = size;
    }

    public void add(long time) {
        tickTimes.add(time);
        while (tickTimes.size() > size) {
            tickTimes.remove();
        }
    }

    public List<Long> getEntries(int size) {
        List<Long> entries = new ArrayList<>();
        for (Long entry : tickTimes) {
            entries.add(entry);
            if (entries.size() == size)
                break;
        }
        return entries;
    }

}
