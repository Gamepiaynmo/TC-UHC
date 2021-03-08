package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import cn.topologycraft.uhc.util.TitleUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

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
				GameManager.instance.getPlayerManager().resetHealthAndFood(player);
			}
		}));
		GameManager.instance.addTask(new TaskScoreboard());
	}

}
