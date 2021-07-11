package cn.topologycraft.uhc.util;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

import java.util.Map;
import java.util.function.Consumer;

public class PlayerItems {

	private static final Map<String, ItemStack> items = Maps.newLinkedHashMap();
	private static final ItemStack DEFAULT_MORAL = new ItemStack(Items.PAPER);

	private static void setSingleLore(ItemStack itemStack, String lore)
	{
		NBTTagList loreList = new NBTTagList();
		loreList.appendTag(new NBTTagString(lore));
		itemStack.getOrCreateSubCompound("display").setTag("Lore", loreList);
	}

	public static ItemStack getPlayerItem(String playerName, boolean onFire) {
		ItemStack stack = items.getOrDefault(playerName, DEFAULT_MORAL).copy();
		// drop smelted item if possible when the player is on fire
		if (onFire) {
			ItemStack smeltResult = FurnaceRecipes.instance().getSmeltingResult(stack).copy();
			if (!smeltResult.isEmpty()) {
				if (stack.hasDisplayName()) {
					smeltResult.setStackDisplayName(stack.getDisplayName());
				}
				stack = smeltResult;
			}
		}
		// apply name
		String moralDescription = playerName + "'s moral(jie) integrity(cao)";
		if (!stack.hasDisplayName()) stack.setStackDisplayName(moralDescription);
		else setSingleLore(stack, moralDescription);
		// apply sharpness I if no enchantment
		if (stack.getEnchantmentTagList().tagCount() == 0)
			stack.addEnchantment(Enchantments.SHARPNESS, 1);
		return stack;
	}

	public static ItemStack getPlayerItem(String playerName) {
		return getPlayerItem(playerName, false);
	}

	// for command /uhc givemorals [<targetName>]
	public static void dumpMoralsToPlayer(EntityPlayer player, String targetName) {
		if (targetName == null) {
			items.keySet().forEach(name -> player.inventory.addItemStackToInventory(getPlayerItem(name)));
		}
		else {
			player.inventory.addItemStackToInventory(getPlayerItem(targetName));
		}
	}

	static {
		items.put("hungryartist_", Builder.create(Items.POTIONITEM).potion(PotionTypes.STRONG_POISON).named("hungryartist_'s holy water").get());
		items.put("_Flag_E_", new ItemStack(Blocks.RED_FLOWER));
		items.put("Spring0809", new ItemStack(Blocks.TNT));
		items.put("fire_duang_duang", new ItemStack(Items.COOKED_FISH).setStackDisplayName("fire_duang_duang's salted fish"));
		items.put("Keviince", Builder.create(Items.WOODEN_SWORD, 59).enchant(Enchantments.SHARPNESS, 10).expensive().get());
		items.put("Dazo66", new ItemStack(Blocks.YELLOW_FLOWER));
		items.put("Gamepiaynmo", new ItemStack(Items.COAL));
		items.put("CCS_Covenant", new ItemStack(Items.COOKIE).setStackDisplayName("Crispy Crispy Shark!"));
		items.put("Lancet_Corgi", Builder.create(Items.IRON_SWORD, 250).named("Lancet").enchant(Enchantments.SHARPNESS, 5).expensive().get());
		items.put("ajisai_iii", Builder.create(Items.CHICKEN).named("rua aji").get());  // rua!
		items.put("Aschin", Builder.create(Items.WHEAT).named("comymy").get());
		items.put("Dou_Bi_Long", Builder.create(Blocks.DRAGON_EGG).named("longbao no egg").enchant(Enchantments.UNBREAKING, 10).enchant(Enchantments.MENDING, 1).get());
		items.put("minamotosan", Builder.create(Items.ROTTEN_FLESH).named("spicy strip").get());
		items.put("zi_nv", Builder.create(Blocks.DOUBLE_PLANT, 2).named("mY lIVe").enchant(Enchantments.EFFICIENCY, 11).get());
		items.put("CallMeLecten", Builder.create(Items.GLASS_BOTTLE).named("oxygen").get());
		items.put("HG_Fei", Builder.create(Items.POTIONITEM).potion(PotionTypes.WATER).named("hydrofluoric acid").get());
		items.put("hai_dan", Builder.create(Items.EGG).named("sea egg").get());
		items.put("Fallen_Breath", Builder.create(Items.LEATHER_CHESTPLATE).mani(s -> Items.LEATHER_CHESTPLATE.setColor(s, 16742436)).named("fox fur coat").get());
		items.put("Sanluli36li", Builder.create(Items.TNT_MINECART).named("36li's self-destruct-car").enchant(Enchantments.FORTUNE, 3).get());
		items.put("shamreltuim", Builder.create(Items.FISH, 3).enchant(Enchantments.BINDING_CURSE, 1).get());  // puffer fish
		items.put("YtonE", Builder.create(Items.POTIONITEM).potion(PotionTypes.EMPTY).named("liquid ketone").get());  // uncraftable potion
		items.put("DawNemo", Builder.create(Blocks.DOUBLE_PLANT, 0).named("Real Man Never Look Round").enchant(Enchantments.THORNS, 1).get());  // sunflower
		items.put("Van_Nya", Builder.create(Items.RABBIT_STEW).named("Van Nya Stew").get());  // Nya? Nya!
		items.put("youngdao", Builder.create(Items.STONE_SWORD, 131).named("Murasame").enchant(Enchantments.SHARPNESS, 10).expensive().get());
		items.put("ql_Lwi", Builder.create(Items.COOKED_FISH).named("Dinner in the belly of a penguin").get());
		items.put("Azulene0907", Builder.create(Items.SPLASH_POTION).potion(PotionTypes.EMPTY).named("ArBQ").get());  // uncraftable potion
		items.put("LUZaLID", Builder.create(Items.DYE, 6).named("cyanLu").get());  // cyan dye
		items.put("U_ruby", Builder.create(Items.FEATHER).named("double u").get());
		items.put("Do1phin_jump", Builder.create(Items.FISH, 2).named("do1phin's food").enchant(Enchantments.UNBREAKING, 3).get());
		items.put("kuri_laan", Builder.create(Items.CAKE).named("XiangSuLiRon cake").enchant(Enchantments.LUCK_OF_THE_SEA, 1).get());
	}

	private static class Builder
	{
		private final ItemStack itemStack;

		private Builder(ItemStack itemStack) {
			this.itemStack = itemStack;
		}

		private static Builder create(Item item, int meta) {
			return new Builder(new ItemStack(item, 1, meta));
		}

		private static Builder create(Item item) {
			return new Builder(new ItemStack(item));
		}

		public static Builder create(Block block, int meta)
		{
			return create(Item.getItemFromBlock(block), meta);
		}

		public static Builder create(Block block)
		{
			return create(Item.getItemFromBlock(block));
		}

		private Builder mani(Consumer<ItemStack> consumer) {
			consumer.accept(this.itemStack);
			return this;
		}

		private Builder named(String name) {
			return this.mani(s -> s.setStackDisplayName(name));
		}

		private Builder potion(PotionType potionType) {
			return this.mani(s -> PotionUtils.addPotionToItemStack(s, potionType));
		}

		private Builder enchant(Enchantment ench, int level) {
			return this.mani(s -> s.addEnchantment(ench, level));
		}

		private Builder expensive() {
			return this.mani(stack -> stack.setRepairCost(100));
		}

		private ItemStack get() {
			return this.itemStack;
		}
	}
}
