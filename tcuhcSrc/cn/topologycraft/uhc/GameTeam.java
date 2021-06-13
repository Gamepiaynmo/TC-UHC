package cn.topologycraft.uhc;

import cn.topologycraft.uhc.GamePlayer.EnumStat;
import com.google.common.collect.Lists;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GameTeam {
	
	private List<GamePlayer> players = Lists.newArrayList();
	private TeamType type = new TeamType();
	
	public GameTeam setColorTeam(GameColor color) { type.setColor(color); players.clear(); return this; }
	public GameTeam setPlayerTeam(GamePlayer player) { type.setPlayer(player); players.clear(); addPlayer(player); return this; }
	public String getTeamName() { return type.teamName; }
	public String getColorfulTeamName() { return type.getColorfulName(); }
	public GameColor getTeamColor() { return type.color; }
	
	public GameTeam addPlayer(GamePlayer player) { players.add(player); player.setTeam(this); return this; }
	public GameTeam removePlayer(GamePlayer player) { players.remove(player); return this; }
	public Iterable<GamePlayer> getPlayers() { return players; }

	// The first player of the team is always the king
	public GamePlayer getKing() { return GameManager.getGameMode() == GameManager.EnumMode.KING ? players.get(0) : null; }

	public void clearTeam() {
		players.clear();
	}
	
	public int getAliveCount() {
		return (int) players.stream().filter(player -> player.isAlive()).count();
	}
	
	public int getKillCount() {
		return (int) (players.stream().mapToDouble(player -> player.getStat().getFloatStat(EnumStat.PLAYER_KILLED)).sum() + 0.5);
	}
	
	public int getPlayerCount() {
		return players.size();
	}
	
	static class TeamType {
		private GameColor color = GameColor.WHITE;
		private GamePlayer player;
		private String name = "Team Empty", teamName = "Team Empty";
		
		public void setColor(GameColor color) {
			this.color = color;
			player = null;
			teamName = name = "Team " + color.name;
		}
		
		public void setPlayer(GamePlayer player) {
			this.player = player;
			color = GameColor.randomColor();
			teamName = player.getName();
			name = "Team " + teamName;
		}
		
		public String getColorfulName() {
			return color.chatColor + name + TextFormatting.RESET;
		}
	}

}
