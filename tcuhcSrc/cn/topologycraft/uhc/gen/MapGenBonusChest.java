package cn.topologycraft.uhc.gen;

import cn.topologycraft.uhc.GameManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class MapGenBonusChest extends WorldGenerator {

	private static Map<Biome, Double> POSSIBILITY_MAP = Maps.newHashMap();
	private static Enchantment[] POSSIBLE_ENCHANTMENTS = { Enchantments.POWER, Enchantments.SHARPNESS, Enchantments.UNBREAKING, Enchantments.EFFICIENCY, Enchantments.FIRE_ASPECT, Enchantments.PROTECTION, Enchantments.PROJECTILE_PROTECTION };
	private static Random rand = new Random();
	
	private static List<RandomItem> chestItemList = Lists.newArrayList();
	private static List<RandomItem> valuableItemList = Lists.newArrayList();
	private static List<RandomItem> emptyItemList = Lists.newArrayList();
	
	private static double chestChance;
	private static double emptyChestChance;
	private static double itemChance;

	@Override
	public boolean generate(World worldIn, Random rand, BlockPos position) {
		int chunkX = position.getX() >> 4;
		int chunkZ = position.getZ() >> 4;
		if (Math.abs(chunkX) <= 1 || Math.abs(chunkZ) <= 1)
			return false;
		int posX = rand.nextInt(16) + position.getX() - 8;
		int posZ = rand.nextInt(16) + position.getZ() - 8;
		position = new BlockPos(posX, 255, posZ);
		while (!worldIn.getBlockState(position).isNormalCube() && position.getY() > 0)
			position = position.down();
		if (position.getY() == 0)
			return false;
		Biome biome = worldIn.getBiome(position);
		if (!POSSIBILITY_MAP.containsKey(biome))
			return false;
		if (rand.nextFloat() < POSSIBILITY_MAP.get(biome) * chestChance) {
			if (rand.nextDouble() < emptyChestChance) {
				worldIn.setBlockState(position, Blocks.TRAPPED_CHEST.getStateFromMeta((byte) (rand.nextInt(4) + 2)));
				TileEntity tileentity = worldIn.getTileEntity(position);
				if (!(tileentity instanceof TileEntityChest))
					return false;
				TileEntityChest chest = (TileEntityChest) tileentity;
				this.genChestItem(chest, emptyItemList, false);
				return true;
			}
			worldIn.setBlockState(position, Blocks.CHEST.getStateFromMeta((byte) (rand.nextInt(4) + 2)));
			TileEntity tileentity = worldIn.getTileEntity(position);
			if (!(tileentity instanceof TileEntityChest))
				return false;
			TileEntityChest chest = (TileEntityChest) tileentity;
			chest.setCustomName("Bonus Chest");
			this.genChestItem(chest, chestItemList, false);
			this.genChestItem(chest, valuableItemList, true);
		}
		return true;
	}
	
	private void genChestItem(TileEntityChest chest, List<RandomItem> itemList, boolean valuable) {
		for (RandomItem item : itemList) {
			Optional<ItemStack> itemstack = item.getItemStack();
			itemstack.ifPresent(stack -> chest.setInventorySlotContents(rand.nextInt(27), stack));
			if (valuable && itemstack.isPresent()) break;
		}
	}
	
	static class ItemSupplier implements Supplier<ItemStack> {
		Item item;
		public ItemSupplier(Item item) {
			this.item = item;
		}
		public ItemStack get() {
			return new ItemStack(item);
		}
	}
	
	static class MinMaxSupplier implements Supplier<ItemStack> {
		Item item;
		int min, max;
		public MinMaxSupplier(Item item, int min, int max) {
			this.item = item;
			this.min = min;
			this.max = max;
		}
		public ItemStack get() {
			return new ItemStack(item, MapGenBonusChest.rand.nextInt(max - min + 1) + min);
		}
	}
	
	static class RandomItem {
		int chance;
		Supplier<ItemStack> stack;
		public RandomItem(int chance, Supplier<ItemStack> stack) {
			this.chance = chance;
			this.stack = stack;
		}
		public Optional<ItemStack> getItemStack() {
			return MapGenBonusChest.rand.nextInt(chance) == 0 ? Optional.of(stack.get()) : Optional.empty();
		}
	}
	
	static {
		double forestChance = 0.12;
		double oceanChance = 0.0;
		double desertChance = 0.06;
		double exHillsChance = 0.12;
		double plainChance = 0.06;
		double icePlainChance = 0.2;
		double iceMountainChance = 0.2;
		double jungleChance = 0.12;
		double mesaChance = 0.12;
		double mushroomChance = 0.1;
		double rforestChance = 0.12;
		double savannaChance = 0.12;
		double taigaChance = 0.12;
		double riverChance = 0.0;
		double beachChance = 0.0;
		double swamplandChance = 0.1;
		POSSIBILITY_MAP.put(Biomes.BEACH, beachChance);
		POSSIBILITY_MAP.put(Biomes.BIRCH_FOREST, forestChance);
		POSSIBILITY_MAP.put(Biomes.BIRCH_FOREST_HILLS, forestChance);
		POSSIBILITY_MAP.put(Biomes.COLD_BEACH, beachChance);
		POSSIBILITY_MAP.put(Biomes.DEEP_OCEAN, oceanChance);
		POSSIBILITY_MAP.put(Biomes.DESERT, desertChance);
		POSSIBILITY_MAP.put(Biomes.DESERT_HILLS, desertChance);
		POSSIBILITY_MAP.put(Biomes.EXTREME_HILLS, exHillsChance);
		POSSIBILITY_MAP.put(Biomes.EXTREME_HILLS_WITH_TREES, exHillsChance);
		POSSIBILITY_MAP.put(Biomes.FOREST, forestChance);
		POSSIBILITY_MAP.put(Biomes.FOREST_HILLS, forestChance);
		POSSIBILITY_MAP.put(Biomes.FROZEN_OCEAN, oceanChance);
		POSSIBILITY_MAP.put(Biomes.FROZEN_RIVER, riverChance);
		POSSIBILITY_MAP.put(Biomes.HELL, 0.0);
		POSSIBILITY_MAP.put(Biomes.ICE_PLAINS, icePlainChance);
		POSSIBILITY_MAP.put(Biomes.ICE_MOUNTAINS, iceMountainChance);
		POSSIBILITY_MAP.put(Biomes.JUNGLE, jungleChance);
		POSSIBILITY_MAP.put(Biomes.JUNGLE_EDGE, jungleChance);
		POSSIBILITY_MAP.put(Biomes.JUNGLE_HILLS, jungleChance);
		POSSIBILITY_MAP.put(Biomes.MESA, mesaChance);
		POSSIBILITY_MAP.put(Biomes.MESA_CLEAR_ROCK, mesaChance);
		POSSIBILITY_MAP.put(Biomes.MESA_ROCK, mesaChance);
		POSSIBILITY_MAP.put(Biomes.MUSHROOM_ISLAND, mushroomChance);
		POSSIBILITY_MAP.put(Biomes.MUSHROOM_ISLAND_SHORE, mushroomChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_BIRCH_FOREST, forestChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_BIRCH_FOREST_HILLS, forestChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_DESERT, desertChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_EXTREME_HILLS, exHillsChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_EXTREME_HILLS_WITH_TREES, exHillsChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_FOREST, forestChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_ICE_FLATS, icePlainChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_JUNGLE, jungleChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_JUNGLE_EDGE, jungleChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_MESA, mesaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_MESA_CLEAR_ROCK, mesaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_MESA_ROCK, mesaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_PLAINS, plainChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_REDWOOD_TAIGA, taigaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_REDWOOD_TAIGA_HILLS, taigaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_ROOFED_FOREST, rforestChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_SAVANNA, savannaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_SAVANNA_ROCK, savannaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_SWAMPLAND, swamplandChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_TAIGA, taigaChance);
		POSSIBILITY_MAP.put(Biomes.MUTATED_TAIGA_COLD, taigaChance);
		POSSIBILITY_MAP.put(Biomes.OCEAN, oceanChance);
		POSSIBILITY_MAP.put(Biomes.PLAINS, plainChance);
		POSSIBILITY_MAP.put(Biomes.REDWOOD_TAIGA, taigaChance);
		POSSIBILITY_MAP.put(Biomes.REDWOOD_TAIGA_HILLS, taigaChance);
		POSSIBILITY_MAP.put(Biomes.RIVER, riverChance);
		POSSIBILITY_MAP.put(Biomes.ROOFED_FOREST, rforestChance);
		POSSIBILITY_MAP.put(Biomes.SAVANNA, savannaChance);
		POSSIBILITY_MAP.put(Biomes.SAVANNA_PLATEAU, savannaChance);
		POSSIBILITY_MAP.put(Biomes.SKY, 0.0);
		POSSIBILITY_MAP.put(Biomes.EXTREME_HILLS_EDGE, exHillsChance);
		POSSIBILITY_MAP.put(Biomes.STONE_BEACH, beachChance);
		POSSIBILITY_MAP.put(Biomes.SWAMPLAND, swamplandChance);
		POSSIBILITY_MAP.put(Biomes.TAIGA, taigaChance);
		POSSIBILITY_MAP.put(Biomes.COLD_TAIGA, taigaChance);
		POSSIBILITY_MAP.put(Biomes.COLD_TAIGA_HILLS, taigaChance);
		POSSIBILITY_MAP.put(Biomes.TAIGA_HILLS, taigaChance);
		POSSIBILITY_MAP.put(Biomes.VOID, 0.0);

		valuableItemList.add(new RandomItem(16, new ItemSupplier(Items.DIAMOND_SWORD)));
		valuableItemList.add(new RandomItem(24, new ItemSupplier(Items.DIAMOND_PICKAXE)));
		valuableItemList.add(new RandomItem(20, new ItemSupplier(Items.GOLDEN_APPLE)));
		valuableItemList.add(new RandomItem(8, new ItemSupplier(Items.DIAMOND)));
		valuableItemList.add(new RandomItem(16, () -> {
			ItemStack item = new ItemStack(Items.ENCHANTED_BOOK);
			ItemEnchantedBook.addEnchantment(item, new EnchantmentData(POSSIBLE_ENCHANTMENTS[rand.nextInt(POSSIBLE_ENCHANTMENTS.length)], rand.nextInt(4) == 0 ? 2 : 1));
			return item;
		}));
		
		chestItemList.add(new RandomItem(1, new ItemSupplier(Items.STICK)));
		chestItemList.add(new RandomItem(1, new ItemSupplier(Items.BONE)));
		chestItemList.add(new RandomItem(2, new ItemSupplier(Items.STRING)));
		chestItemList.add(new RandomItem(2, new MinMaxSupplier(Items.IRON_INGOT, 1, 2)));
		chestItemList.add(new RandomItem(3, new ItemSupplier(Items.GOLD_INGOT)));
		chestItemList.add(new RandomItem(3, new ItemSupplier(Items.CHORUS_FRUIT)));
		chestItemList.add(new RandomItem(5, new ItemSupplier(Items.LEATHER)));
		chestItemList.add(new RandomItem(5, new MinMaxSupplier(Items.EXPERIENCE_BOTTLE, 2, 4)));
		
		emptyItemList.add(new RandomItem(1, () ->
				new ItemStack(Blocks.TALLGRASS).setStackDisplayName("There should be something here, but ...")));

		chestChance = GameManager.instance.getOptions().getFloatOptionValue("chestFrequency");
		emptyChestChance = GameManager.instance.getOptions().getFloatOptionValue("trappedChestFrequency");
		itemChance = GameManager.instance.getOptions().getFloatOptionValue("chestItemFrequency");
	}
}
