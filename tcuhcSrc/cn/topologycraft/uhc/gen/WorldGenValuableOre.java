package cn.topologycraft.uhc.gen;

import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;

public class WorldGenValuableOre extends WorldGenMinable {

	public WorldGenValuableOre(IBlockState state, int blockCount) {
		super(state, blockCount);
	}

	public WorldGenValuableOre(IBlockState state, int blockCount, Predicate<IBlockState> predicate) {
		super(state, blockCount, predicate);
	}

	@Override
	protected boolean isValidPosition(World world, BlockPos pos) {
		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++)
					if (world.isAirBlock(pos.add(x, y, z)))
						return true;
		return false;
	}

}
