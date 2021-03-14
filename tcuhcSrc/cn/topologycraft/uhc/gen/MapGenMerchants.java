package cn.topologycraft.uhc.gen;

import cn.topologycraft.uhc.GameManager;
import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

public class MapGenMerchants extends WorldGenerator {

	private static List<UHCRecipe> randRecipeList, staticRecipeList;
	private static float merchantChance;

	@Override
	public boolean generate(World worldIn, Random rand, BlockPos position) {
		Biome biome = worldIn.getBiome(position);
		if (biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN || biome == Biomes.FROZEN_OCEAN)
			return false;
		int chunkX = position.getX() >> 4;
		int chunkZ = position.getZ() >> 4;
		if (Math.abs(chunkX) < 2 || Math.abs(chunkZ) < 2)
			return false;
		if (chunkX % 4 == 0 && chunkZ % 4 == 0 && rand.nextFloat() < 0.3 * merchantChance) {
			BlockPos pos = worldIn.getTopSolidOrLiquidBlock(position.add(rand.nextInt(16) - 8, 0, rand.nextInt(16) - 8)).down();
			if (worldIn.getBlockState(pos).getBlock() == Blocks.WATER)
				return false;
			EntityVillager villager = new EntityVillager(worldIn);
			villager.setNoAI(true);
			villager.setEntityInvulnerable(true);
			villager.setPosition(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5);
			for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++)
				for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
					worldIn.setBlockState(new BlockPos(x, pos.getY(), z), Blocks.STONEBRICK.getDefaultState());
					if (x != pos.getX() || z != pos.getZ())
						worldIn.setBlockState(new BlockPos(x, pos.getY() + 1, z), Blocks.IRON_BARS.getDefaultState());
					worldIn.setBlockState(new BlockPos(x, pos.getY() + 3, z), Blocks.STONEBRICK.getDefaultState());
				}
			worldIn.checkLight(pos.up(2));
			worldIn.setBlockState(pos.up(4), Blocks.STONE_SLAB.getDefaultState());
			int recipeCnt = rand.nextInt(3) + 2;
			MerchantRecipeList recipes = new MerchantRecipeList();
			for (int i = 0; i < recipeCnt; i++)
				recipes.add(getRandomRecipe(rand));
			addStaticRecipes(recipes, rand);
			villager.setRecipes(recipes);
			worldIn.spawnEntity(villager);
			return true;
		}
		return false;
	}

	private MerchantRecipe getRandomRecipe(Random rand) {
		return randRecipeList.get(rand.nextInt(randRecipeList.size())).getRecipe(rand);
	}

	private void addStaticRecipes(MerchantRecipeList list, Random rand) {
		for (UHCRecipe recipe : staticRecipeList) {
			list.add(recipe.getRecipe(rand));
		}
	}

	static {
		staticRecipeList = Lists.newArrayList();
		staticRecipeList.add(new UHCRecipe(Items.GOLDEN_APPLE, Items.APPLE, 1, 1, 18, 30, true));
		staticRecipeList.add(new UHCRecipe(Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, 1, 1, 36, 48, true));
		staticRecipeList.add(new UHCRecipe(Items.DIAMOND_LEGGINGS, Items.IRON_LEGGINGS, 1, 1, 30, 42, true));
		staticRecipeList.add(new UHCRecipe(Items.DIAMOND_HELMET, Items.IRON_HELMET, 1, 1, 22, 30, true));
		staticRecipeList.add(new UHCRecipe(Items.DIAMOND_BOOTS, Items.IRON_BOOTS, 1, 1, 18, 24, true));
		randRecipeList = Lists.newArrayList();
		randRecipeList.add(new UHCRecipe(Items.EXPERIENCE_BOTTLE, 2, 4, 1, 1, true));
		randRecipeList.add(new UHCRecipe(Items.COAL, 3, 6, 1, 1, false));
		randRecipeList.add(new UHCRecipe(Items.REDSTONE, 3, 6, 1, 1, false));
		randRecipeList.add(new UHCRecipe(Items.IRON_INGOT, 1, 2, 1, 1, false));
		randRecipeList.add(new UHCRecipe(Items.GOLD_INGOT, 1, 1, 1, 2, false));
		randRecipeList.add(new UHCRecipe(Items.ENDER_PEARL, 1, 1, 10, 20, false));
		randRecipeList.add(new UHCRecipe(Items.EMERALD, 1, 1, 10, 20, false));
		randRecipeList.add(new UHCRecipe(Items.DIAMOND, 1, 1, 2, 4, false));
		merchantChance = GameManager.instance.getOptions().getFloatOptionValue("merchantFrequency");
	}

	public static class UHCRecipe {
		Item item1, item2;
		int itemMin, itemMax;
		int moneyMin, moneyMax;
		boolean sell;

		public UHCRecipe(Item item1, Item item2, int imin, int imax, int mmin, int mmax, boolean sell) {
			this.item1 = item1;
			this.item2 = item2;
			itemMin = imin;
			itemMax = imax;
			moneyMin = mmin;
			moneyMax = mmax;
			this.sell = sell;
		}

		public UHCRecipe(Item item1, int imin, int imax, int mmin, int mmax, boolean sell) {
			this(item1, null, imin, imax, mmin, mmax, sell);
		}

		public MerchantRecipe getRecipe(Random rand) {
			ItemStack stack = itemMax == itemMin ? new ItemStack(item1, itemMax) : new ItemStack(item1, rand.nextInt(itemMax - itemMin + 1) + itemMin);
			ItemStack money = moneyMax == moneyMin ? new ItemStack(Items.QUARTZ, moneyMax) : new ItemStack(Items.QUARTZ, rand.nextInt(moneyMax - moneyMin + 1) + moneyMin);
			if (sell) return new MerchantRecipe(money, item2 == null ? ItemStack.EMPTY : new ItemStack(item2), stack, 0, 10000);
			else return new MerchantRecipe(stack, ItemStack.EMPTY, money, 0, 10000);
		}
	}
}
