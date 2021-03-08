package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketSpectate;

public class TaskKeepSpectate extends TaskTimer {
	
	private GamePlayer gamePlayer;
	
	public TaskKeepSpectate(GamePlayer gamePlayer) {
		super(0, 10);
		this.gamePlayer = gamePlayer;
	}
	
	@Override
	public void onTimer() {
		if (!GameManager.instance.isGamePlaying() || gamePlayer.getTeam().getAliveCount() == 0 || gamePlayer.isAlive())
			this.setCanceled();
		gamePlayer.getRealPlayer().ifPresent(player -> {
			Entity target = player.getSpectatingEntity();
//			if (!SpectateTargetUtil.isCapableTarget(gamePlayer, target))
//				target = SpectateTargetUtil.getCapableTarget(gamePlayer, target);
			if (target != null) player.setSpectatingEntity(target);
			if (player.world != target.world) {
				player.connection.handleSpectate(new CPacketSpectate(target.getUniqueID()));
			}
		});
	}
	
	@Override
	public void onFinish() {
		gamePlayer.getRealPlayer().ifPresent(player -> player.setSpectatingEntity(player));
	}

}
