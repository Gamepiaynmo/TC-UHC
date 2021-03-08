package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.util.SpawnPlatform;
import net.minecraft.entity.player.EntityPlayerMP;

public class TaskSpawnPlatformProtect extends Task {
	
	private GameManager gameManager;
	
	public TaskSpawnPlatformProtect(GameManager manager) {
		gameManager = manager;
	}
	
	@Override
	public void onUpdate() {
		for (EntityPlayerMP player : gameManager.getPlayerList().getPlayers()) {
			if (player.isEntityAlive() && !player.isCreative() && !player.isSpectator()) {
				if (player.posY < SpawnPlatform.height - 20 && Math.abs(player.posX) < 64 && Math.abs(player.posZ) < 64) {
					gameManager.getPlayerManager().randomSpawnPosition(player);
				}
			}
		}
	}
	
	@Override
	public boolean hasFinished() {
		return gameManager.isGamePlaying();
	}

}
