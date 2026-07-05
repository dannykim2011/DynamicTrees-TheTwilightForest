package com.dtteam.dttwilightforest.worldgen;

import com.dtteam.dynamictrees.api.worldgen.GroundFinder;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class TwilightForestGroundFinder implements GroundFinder {

    private static final Block HARDENED_DARK_LEAVES = BuiltInRegistries.BLOCK.get(
            ResourceLocation.fromNamespaceAndPath("twilightforest", "hardened_dark_leaves")
    );

    @Override
    public List<BlockPos> findGround(LevelAccessor level, BlockPos start, @Nullable Heightmap.Types heightmap) {
        BlockPos.MutableBlockPos groundPos = CoordUtils.findWorldSurface(
                level,
                start,
                heightmap == null ? Heightmap.Types.WORLD_SURFACE_WG : heightmap
        ).mutable();

        if (!level.getBlockState(groundPos).is(HARDENED_DARK_LEAVES)) {
            return Collections.singletonList(groundPos.immutable());
        }

        while (groundPos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(groundPos);
            if (!state.is(HARDENED_DARK_LEAVES) && !state.canBeReplaced()) {
                break;
            }
            groundPos.move(0, -1, 0);
        }

        return Collections.singletonList(groundPos.immutable());
    }
}
