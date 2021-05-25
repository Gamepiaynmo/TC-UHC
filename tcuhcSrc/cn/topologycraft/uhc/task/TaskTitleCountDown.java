package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import cn.topologycraft.uhc.util.TitleUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

import java.util.Collections;

public class TaskTitleCountDown extends TaskTimer {
	
	private int count;

	public TaskTitleCountDown(int init, int delay, int interval) {
		super(delay, interval);
		count = init;
	}
	
	@Override
	public void onTimer() {
		TitleUtil.sendTitleToAllPlayers(TextFormatting.GOLD + String.valueOf(--count), null);
		if (count == 0) this.setCanceled();
	}
	
	@Override
	public void onFinish() {
		TitleUtil.sendTitleToAllPlayers("Game Started !", "Enjoy Yourself !");
		GameManager.instance.getPlayerManager().getCombatPlayers().forEach(player -> player.addTask(new TaskFindPlayer(player) {
			@Override
			public void onFindPlayer(EntityPlayerMP player) {
				player.setGameType(GameType.SURVIVAL);
				player.setEntityInvulnerable(false);
				player.clearActivePotions();
				GameManager.instance.getPlayerManager().resetHealthAndFood(player);
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 200, 4));  // 10s Resistance V

				// give invisibility and shiny potion to player for ghost mode
				switch (GameManager.getGameMode()) {
					case GHOST:
						this.getGamePlayer().addGhostModeEffect();
						ItemStack shinyPotion = new ItemStack(Items.SPLASH_POTION).setStackDisplayName("Splash Shiny Potion");
						PotionUtils.appendEffects(shinyPotion, Collections.singleton(new PotionEffect(MobEffects.GLOWING, 200, 0)));
						player.inventory.addItemStackToInventory(shinyPotion);
						break;
					case KING:
						if (this.getGamePlayer().isKing()) {
							EnumDyeColor dyeColor = this.getGamePlayer().getTeam().getTeamColor().dyeColor;
							ItemStack kingsHelmet = new ItemStack(Items.LEATHER_HELMET).setStackDisplayName(String.format("%s crown", dyeColor.getName()));
							kingsHelmet.setTagInfo("KingsCrown", new NBTTagByte((byte)1));
							kingsHelmet.setTagInfo("Unbreakable", new NBTTagByte((byte)1));
							Items.LEATHER_HELMET.setColor(kingsHelmet, dyeColor.getColorValue());
							kingsHelmet.addEnchantment(Enchantments.PROTECTION, 6);
							kingsHelmet.addEnchantment(Enchantments.BINDING_CURSE, 1);
							kingsHelmet.addEnchantment(Enchantments.VANISHING_CURSE, 1);
							player.setItemStackToSlot(EntityEquipmentSlot.HEAD, kingsHelmet);
						}
						break;
				}
			}
		}));
		if (GameManager.getGameMode() == GameManager.EnumMode.KING) {
			GameManager.instance.addTask(new TaskKingEffectField());
		}
		GameManager.instance.addTask(new TaskScoreboard());
	}

}
