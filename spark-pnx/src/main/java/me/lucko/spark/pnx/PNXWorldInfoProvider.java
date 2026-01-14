package me.lucko.spark.pnx;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.Chunk;
import cn.nukkit.level.format.IChunk;
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