package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GamePlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class TaskFindPlayer extends Task {
	
	private GamePlayer player;
	private EntityPlayerMP realPlayer;
	
	public TaskFindPlayer(GamePlayer player) {
		this.player = player;
	}

	public GamePlayer getGamePlayer() {
		return player;
	}

	public void onFindPlayer(EntityPlayerMP player) { }
	
	@Override
	public final void onUpdate() {
		player.getRealPlayer().ifPresent(playermp -> {
			realPlayer = playermp;
			onFindPlayer(realPlayer);
		});
	}
	
	@Override
	public final boolean hasFinished() {
		return realPlayer != null;
	}

}
