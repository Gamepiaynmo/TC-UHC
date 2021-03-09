package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.GamePlayer.EnumStat;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import com.google.common.collect.Lists;
import net.minecraft.world.GameType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TaskBroadcastData extends TaskTimer {
	private int round = 0;
	
	public TaskBroadcastData(int delay)  {
		super(delay, 100);
	}
	
	private String getGraph(int len) {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < len; i++)
			res.append("*");
		return res.toString();
	}
	
	@Override
	public void onTimer() {
		GameManager gameManager = GameManager.instance;
		if (round == 0) {
			for (GamePlayer player : gameManager.getPlayerManager().getAllPlayers())
				player.getRealPlayer().ifPresent(playermp -> playermp.setGameType(GameType.SPECTATOR));
		}

		while (true) {
			float max = 0;
			EnumStat stat = EnumStat.values()[round];
			List<Pair<String, Float>> stats = Lists.newArrayList();
			for (GamePlayer player : gameManager.getPlayerManager().getCombatPlayers()) {
				float value = player.getStat().getFloatStat(stat);
				max = Math.max(max, value);
				stats.add(Pair.of(player.getTeam().getTeamColor().chatColor + player.getName(), value));
			}

			stats.sort((A, B) -> B.getValue().compareTo(A.getValue()));
			for (int i = 0; i < stats.size(); i++) {
				Pair<String, Float> pair = stats.get(i);
				if (pair.getValue() == 0 || (i >= 6 && pair.getValue() < stats.get(i - 1).getValue())) {
					stats.subList(i, stats.size()).clear();
					break;
				}
			}

			if (!stats.isEmpty()) {
				gameManager.broadcastMessage(stat.name + ":");
				for (Pair<String, Float> pair : stats) {
					gameManager.broadcastMessage(String.format("  %-40s %7.2f %s",
							pair.getLeft(), pair.getValue(), getGraph((int) (40 * pair.getValue() / max))));
				}
			}

			if (++round == EnumStat.values().length)
				setCanceled();
			else if (stats.isEmpty())
				continue;

			break;
		}
	}

}
