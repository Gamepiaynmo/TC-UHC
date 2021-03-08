package net.minecraft.item.crafting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class RecipeArmorRepair implements IRecipe {
	
	private ItemStack res;

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemStack armor = null;
		for (int i = 0; i < inv.getWidth(); ++i) {
			for (int j = 0; j < inv.getHeight(); ++j) {
				ItemStack itemstack = inv.getStackInRowAndColumn(i, j);
				if (itemstack.getItem() instanceof ItemArmor) {
					if (armor == null) armor = itemstack;
					else return false;
				}
			}
		}
		if (armor == null) return false;
		ItemArmor armorItem = (ItemArmor) armor.getItem();
		int repairCnt = 0;
		for (int i = 0; i < inv.getWidth(); ++i) {
			for (int j = 0; j < inv.getHeight(); ++j) {
				ItemStack itemstack = inv.getStackInRowAndColumn(i, j);
				if (!itemstack.isEmpty() && !(itemstack.getItem() instanceof ItemArmor)) {
					if (itemstack.getItem() == armorItem.getArmorMaterial().getRepairItem()) repairCnt++;
					else return false;
				}
			}
		}
		if (repairCnt == 0) return false;
		res = armor.copy();
		res.setItemDamage(armor.getItemDamage() - repairCnt * armor.getMaxDamage() / 3);
		return true;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return res;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		return NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

}
