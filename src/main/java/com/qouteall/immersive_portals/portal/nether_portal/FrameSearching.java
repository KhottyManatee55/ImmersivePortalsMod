package com.qouteall.immersive_portals.portal.nether_portal;

import com.qouteall.immersive_portals.McHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FrameSearching {
    public static void startSearchingPortalFrameAsync(
        ChunkRegion region,
        int regionRadius,
        BlockPos centerPoint,
        BlockPortalShape templateShape,
        Predicate<BlockState> areaPredicate,
        Predicate<BlockState> framePredicate,
        Predicate<BlockPortalShape> resultPredicate,
        Consumer<BlockPortalShape> onFound,
        Runnable onNotFound
    ) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(
            () -> {
                try {
                    BlockPortalShape result = searchPortalFrame(
                        region, regionRadius,
                        centerPoint, templateShape,
                        areaPredicate, framePredicate,
                        resultPredicate
                    );
                    McHelper.getServer().execute(() -> {
                        if (result != null) {
                            onFound.accept(result);
                        }
                        else {
                            onNotFound.run();
                        }
                    });
                }
                catch (Throwable oops) {
                    oops.printStackTrace();
                    onNotFound.run();
                }
            },
            Util.getServerWorkerExecutor()
        );
        
    }
    
    // Return null for not found
    // After removing the usage of stream API, it becomes 100 times faster!!!
    private static BlockPortalShape searchPortalFrame(
        ChunkRegion region,
        int regionRadius,
        BlockPos centerPoint,
        BlockPortalShape templateShape,
        Predicate<BlockState> areaPredicate,
        Predicate<BlockState> framePredicate,
        Predicate<BlockPortalShape> resultPredicate
    ) {
        ArrayList<Chunk> chunks = getChunksFromNearToFar(
            region, centerPoint, regionRadius
        );
        
        BlockPos.Mutable temp = new BlockPos.Mutable();
        BlockPos.Mutable temp1 = new BlockPos.Mutable();
        
        // avoid using stream api and maintain cache locality
        for (int chunkIndex = 0; chunkIndex < chunks.size(); chunkIndex++) {
            Chunk chunk = chunks.get(chunkIndex);
            ChunkSection[] sectionArray = chunk.getSectionArray();
            for (int sectionY = 0; sectionY < sectionArray.length; sectionY++) {
                ChunkSection chunkSection = sectionArray[sectionY];
                if (chunkSection != null) {
                    for (int localY = 0; localY < 16; localY++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            for (int localX = 0; localX < 16; localX++) {
                                BlockState blockState = chunkSection.getBlockState(
                                    localX, localY, localZ
                                );
                                if (framePredicate.test(blockState)) {
                                    int worldX = localX + chunk.getPos().getStartX();
                                    int worldY = localY + sectionY * 16;
                                    int worldZ = localZ + chunk.getPos().getStartZ();
                                    temp.set(worldX, worldY, worldZ);
                                    BlockPortalShape result = templateShape.matchShapeWithMovedFirstFramePos(
                                        pos -> areaPredicate.test(region.getBlockState(pos)),
                                        pos -> framePredicate.test(region.getBlockState(pos)),
                                        temp,
                                        temp1
                                    );
                                    if (result != null) {
                                        if (resultPredicate.test(result)) {
                                            return result;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    private static ArrayList<Chunk> getChunksFromNearToFar(
        ChunkRegion region,
        BlockPos centerPoint,
        int regionRadius
    ) {
        ArrayList<Chunk> chunks = new ArrayList<>();
        
        int searchedRadius = regionRadius - 1;
        int centerX = region.getCenterChunkX();
        int centerZ = region.getCenterChunkZ();
        for (int x = centerX - searchedRadius; x <= centerX + searchedRadius; x++) {
            for (int z = centerZ - searchedRadius; z <= centerZ + searchedRadius; z++) {
                chunks.add(region.getChunk(x, z));
            }
        }
        
        chunks.sort(Comparator.comparingDouble(
            chunk -> chunk.getPos().getCenterBlockPos().getSquaredDistance(centerPoint)
        ));
        return chunks;
    }
}
