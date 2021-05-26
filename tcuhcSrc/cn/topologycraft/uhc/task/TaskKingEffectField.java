package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class TaskKingEffectField extends Task.TaskTimer
{
	public TaskKingEffectField()
	{
		super(0, 10);
	}

	// give 2 seconds speed I to any player near its king
	@Override
	public void onTimer()
	{
		if (GameManager.instance.isGamePlaying() && GameManager.getGameMode() == GameManager.EnumMode.KING) {
			GameManager.instance.getPlayerManager().getCombatPlayers().forEach(gamePlayer -> {
				// only non-king players are able to have the boost
				if (!gamePlayer.isKing() && gamePlayer.isAlive() && gamePlayer.getRealPlayer().isPresent()) {
					GamePlayer king = gamePlayer.getTeam().getKing();
					if (king.isAlive() && king.getRealPlayer().isPresent()) {
						double distanceToKing = gamePlayer.getRealPlayer().get().getDistance(king.getRealPlayer().get());
						if (distanceToKing <= 5) {
							gamePlayer.getRealPlayer().get().addPotionEffect(new PotionEffect(MobEffects.SPEED, 0, 40));
						}
					}
				}
			});
		}
		else {
			this.setCanceled();
		}
	}
}
