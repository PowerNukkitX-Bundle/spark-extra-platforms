/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.pnx;

import org.powernukkitx.Server;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.EntityID;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.format.Chunk;
import org.powernukkitx.level.format.IChunk;
import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.ChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PNXWorldInfoProvider implements WorldInfoProvider {
    private final Server server;

    public PNXWorldInfoProvider(Server server) {
        this.server = server;
    }

    @Override
    public CountsResult pollCounts() {
        int players = this.server.getOnlinePlayers().size();
        int entities = 0;
        int tileEntities = 0;
        int chunks = 0;

        for (Level world : this.server.getLevels().values()) {
            entities += world.getEntities().length;
            tileEntities += world.getBlockEntities().size();
            chunks += world.getChunks().size();
        }

        return new CountsResult(players, entities, tileEntities, chunks);
    }

    @Override
    public ChunksResult<?> pollChunks() {
        ChunksResult<PNXChunkInfo> data = new ChunksResult<>();
        for (Level world : this.server.getLevels().values()) {
            Collection<IChunk> chunks = world.getChunks().values();

            List<PNXChunkInfo> list = new ArrayList<>(chunks.size());
            for (IChunk chunk : chunks) {
                if (chunk != null) {
                    list.add(new PNXChunkInfo(chunk));
                }
            }

            data.put(world.getName(), list);
        }
        return data;
    }

    @Override
    public GameRulesResult pollGameRules() {
        return new GameRulesResult();
    }

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
        return List.of();
    }

    static final class PNXChunkInfo extends AbstractChunkInfo<String> {
        private final CountMap<String> entityCounts;

        PNXChunkInfo(IChunk chunk) {
            super(chunk.getX(), chunk.getZ());

            this.entityCounts = new CountMap.Simple<String>(new HashMap<String, AtomicInteger>());
            for (Entity entity : chunk.getEntities().values()) {
                if (entity != null) {
                    this.entityCounts.increment(entity.getIdentifier());
                }
            }
        }

        @Override
        public CountMap<String> getEntityCounts() {
            return this.entityCounts;
        }

        @Override
        public String entityTypeName(String s) {
            return s;
        }

    }
}