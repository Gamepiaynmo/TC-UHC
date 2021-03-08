package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.options.Options;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.border.WorldBorder;

public class TaskBorderReminder extends TaskTimer {
	
	private final int borderStart, borderEnd;
	private final int borderStartTime, borderEndTime, gameTime;
	private final WorldBorder border;

	public TaskBorderReminder() {
		super(0, 20);
		Options options = Options.instance;
		borderStart = options.getIntegerOptionValue("borderStart");
		borderEnd = options.getIntegerOptionValue("borderEnd");
		borderStartTime = options.getIntegerOptionValue("borderStartTime");
		borderEndTime = options.getIntegerOptionValue("borderEndTime");
		gameTime = options.getIntegerOptionValue("gameTime");
		border = GameManager.instance.getMinecraftServer().worlds[0].getWorldBorder();
		border.setTransition(borderStart, borderEnd, (borderEndTime - borderStartTime) * 1000);
		GameManager.instance.boardcastMessage(TextFormatting.DARK_RED + "World border started to shrink.");
	}
	
	@Override
	public void onTimer() {
		if (this.hasFinished()) return;
		if (gameTime - GameManager.instance.getGameTimeRemaining() >= borderEndTime) {
			GameManager.instance.boardcastMessage(TextFormatting.DARK_RED + "World border stopped shrinking.");
			this.setCanceled();
		}
		for (EntityPlayerMP player : GameManager.instance.getPlayerList().getPlayers()) {
			if (border.getClosestDistance(player) < 5 && !(Math.abs(player.posX) < borderEnd / 2 && Math.abs(player.posZ) < borderEnd / 2) 
					&& !player.isCreative() && !player.isSpectator() && GameManager.instance.getPlayerManager().getGamePlayer(player).borderRemindCooldown()) {
				player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "You will fall behind the world border!"));
			}
		}
	}

}
