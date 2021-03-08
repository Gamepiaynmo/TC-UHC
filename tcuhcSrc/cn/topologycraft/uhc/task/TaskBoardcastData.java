package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.GamePlayer.EnumStat;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import net.minecraft.world.GameType;

public class TaskBoardcastData extends TaskTimer {
	
	public TaskBoardcastData(int delay)  {
		super(delay, 0);
	}
	
	private String getGraph(int len) {
		String res = "";
		for (int i = 0; i < len; i++)
			res += "█";
		return res;
	}
	
	@Override
	public void onTimer() {
		GameManager gameManager = GameManager.instance;
		for (GamePlayer player : gameManager.getPlayerManager().getAllPlayers())
			player.getRealPlayer().ifPresent(playermp -> playermp.setGameType(GameType.SPECTATOR));
		for (EnumStat stat : EnumStat.values()) {
			float max = 0;
			for (GamePlayer player : gameManager.getPlayerManager().getCombatPlayers()) {
				max = Math.max(max, player.getStat().getFloatStat(stat));
			}
			max = Math.max(max, 1);
			gameManager.boardcastMessage(stat.name + ":");
			for (GamePlayer player : gameManager.getPlayerManager().getCombatPlayers()) {
				float value = player.getStat().getFloatStat(stat);
				gameManager.boardcastMessage(String.format("  %-16.16s: %8.2f %s", player.getTeam().getTeamColor().chatColor + player.getName(), value, getGraph((int) (value * 20 / max))));
			}
		}
	}

}
