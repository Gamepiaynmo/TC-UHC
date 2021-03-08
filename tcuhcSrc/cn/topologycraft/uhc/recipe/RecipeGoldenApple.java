package cn.topologycraft.uhc.recipe;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class RecipeGoldenApple implements IRecipe {
	private int level;

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		boolean hasApple = false;
		int goldCnt = 0;
		if (inv.getWidth() == 3 && inv.getHeight() == 3) {
			for (int i = 0; i < inv.getWidth(); ++i) {
				for (int j = 0; j < inv.getHeight(); ++j) {
					ItemStack itemstack = inv.getStackInRowAndColumn(i, j);
					if (itemstack.getItem() == Items.APPLE)
						if (!hasApple)
							hasApple = true;
						else return false;
					else if (itemstack.getItem() == Items.GOLD_INGOT)
						goldCnt++;
				}
			}
		}
		if (hasApple && goldCnt > 0 && goldCnt % 2 == 0) {
			level = goldCnt / 2;
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack res = new ItemStack(Items.GOLDEN_APPLE);
		if (level != 4)
			res.setTagInfo("level", new NBTTagInt(level));
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
