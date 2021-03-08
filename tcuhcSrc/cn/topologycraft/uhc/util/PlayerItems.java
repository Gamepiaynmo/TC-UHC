package cn.topologycraft.uhc.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;

public class PlayerItems {
	
	private static Map<String, ItemStack> items = Maps.newHashMap();
	
	public static ItemStack getPlayerItem(String playerName) {
		ItemStack stack = items.get(playerName);
		if (stack == null) stack = new ItemStack(Items.PAPER);
		else stack = stack.copy();
		if (!stack.hasDisplayName())
			stack.setStackDisplayName(playerName + "'s moral(jie) integrity(cao)");
		if (stack.getEnchantmentTagList().tagCount() == 0)
			stack.addEnchantment(Enchantments.SHARPNESS, 1);
		return stack;
	}
	
	static {
		ItemStack stack = null;
		stack = new ItemStack(Items.POTIONITEM);
		PotionUtils.addPotionToItemStack(stack, PotionTypes.STRONG_POISON);
		items.put("hungryartist_", stack.setStackDisplayName("hungryartist_'s holy water"));
		items.put("_Flag_E_", new ItemStack(Blocks.RED_FLOWER));
		items.put("Spring0809", new ItemStack(Blocks.TNT));
		items.put("fire_duang_duang", new ItemStack(Items.COOKED_FISH).setStackDisplayName("fire_duang_duang's salted fish"));
		stack = new ItemStack(Items.WOODEN_SWORD, 1, 59);
		stack.addEnchantment(Enchantments.SHARPNESS, 10);
		stack.getTagCompound().setInteger("RepairCost", 100);
		items.put("Keviince", stack);
		items.put("Dazo66", new ItemStack(Blocks.YELLOW_FLOWER));
		items.put("Gamepiaynmo", new ItemStack(Items.COAL));
	}

}
