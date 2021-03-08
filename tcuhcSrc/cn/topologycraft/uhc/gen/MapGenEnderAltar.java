package cn.topologycraft.uhc.gen;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.options.Options;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.WorldGenerator;

public class MapGenEnderAltar extends WorldGenerator {

	static List<ChunkPos> altarPoses = Lists.newArrayList();

	public MapGenEnderAltar() {
		calcAltarPosition();
	}

	private void calcAltarPosition() {
		if (altarPoses.isEmpty()) {
			int border = Options.instance.getIntegerOptionValue("borderStart");
			border *= 0.45;
			for (int i = 0; i < 4; i++) {
				int x = i / 2 == 1 ? 1 : -1;
				int z = i % 2 == 1 ? 1 : -1;
				x *= GameManager.instance.rand.nextInt(border);
				z *= GameManager.instance.rand.nextInt(border);
				altarPoses.add(new ChunkPos(new BlockPos(x, 0, z)));
			}
		}
	}

	@Override
	public boolean generate(World worldIn, Random rand, BlockPos position) {
		Chunk chunk = worldIn.getChunk(position);
		if (altarPoses.stream().anyMatch(pos -> pos.equals(new ChunkPos(position)))) {
			BlockPos top = worldIn.getTopSolidOrLiquidBlock(position.add(rand.nextInt(16), 0, rand.nextInt(16))).down();
			while (top.getY() > 0) {
				for (int x = -3; x <= 3; x++)
					for (int z = -3; z <= 3; z++)
						if (!worldIn.getBlockState(top.add(x, 0, z)).isNormalCube())
							continue;
				break;
			}
			IBlockState floor = Blocks.OBSIDIAN.getDefaultState();
			IBlockState banner = Blocks.STANDING_BANNER.getDefaultState();
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					worldIn.setBlockState(top.add(x, 0, z), floor);
					for (int y = 1; y <= 5; y++)
						worldIn.setBlockToAir(top.add(x, y, z));
				}
			}
			for (int i = -1; i <= 1; i++) {
				worldIn.setBlockState(top.add(3, 0, i), floor);
				worldIn.setBlockState(top.add(-3, 0, i), floor);
				worldIn.setBlockState(top.add(i, 0, 3), floor);
				worldIn.setBlockState(top.add(i, 0, -3), floor);
				for (int y = 1; y <= 5; y++) {
					worldIn.setBlockToAir(top.add(3, y, i));
					worldIn.setBlockToAir(top.add(-3, y, i));
					worldIn.setBlockToAir(top.add(i, y, 3));
					worldIn.setBlockToAir(top.add(i, y, -3));
				}
			}
			worldIn.setBlockState(top.add(-3, 1, 1), floor);
			worldIn.setBlockState(top.add(-3, 1, -1), floor);
			worldIn.setBlockState(top.add(3, 1, 1), floor);
			worldIn.setBlockState(top.add(3, 1, -1), floor);
			worldIn.setBlockState(top.add(1, 1, 3), banner);
			worldIn.setBlockState(top.add(-1, 1, 3), banner);
			worldIn.setBlockState(top.add(1, 1, -3), banner.withProperty(BlockBanner.ROTATION, 8));
			worldIn.setBlockState(top.add(-1, 1, -3), banner.withProperty(BlockBanner.ROTATION, 8));
			int radius = 16;
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = top.add(x, 0, z);
					pos = worldIn.getTopSolidOrLiquidBlock(pos).down();
					if (!worldIn.getBlockState(pos).isNormalCube())
						continue;
					float chance = 64.0f / (x * x + z * z);
					if (rand.nextFloat() < chance)
						worldIn.setBlockState(pos, floor);
				}
			}
			EntityEnderCrystal crystal = new EntityEnderCrystal(worldIn, top.getX() + 0.5, top.getY() + 2, top.getZ() + 0.5);
			crystal.setShowBottom(false);
			worldIn.spawnEntity(crystal);
			for (int x = -1; x <= 1; x++)
				for (int z = -1; z <= 1; z++)
					for (int y = -2; y <= -1; y++)
						worldIn.setBlockState(top.add(x, y, z), floor);
			worldIn.setBlockState(top.down(), Blocks.GOLD_BLOCK.getDefaultState());
			worldIn.setBlockState(top.add(0, 1, 0), Blocks.END_STONE.getDefaultState());
			worldIn.setBlockState(top.add(0, 0, 0), Blocks.END_STONE.getDefaultState());
			return true;
		}
		return false;
	}
}
