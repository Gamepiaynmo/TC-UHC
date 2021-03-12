package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.options.Options;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

public class TaskScoreboard extends TaskTimer {
	
	private final int borderStart, borderEnd;
	private final int gameTime, startTime, endTime, netherTime, caveTime;
	
	public static final String scoreName = "time";
	public static final String displayName = "UHC Game";
	public static final String[] lines = { "Time Remaining:", "Border Radius:", "Nether Close:", "Cave Close:" };
	
	private Scoreboard scoreboard;
	private ScoreObjective objective;
	
	public TaskScoreboard() {
		super(0, 20);
		
		Options options = GameManager.instance.getOptions();
		borderStart = options.getIntegerOptionValue("borderStart");
		borderEnd = options.getIntegerOptionValue("borderEnd");
		gameTime = options.getIntegerOptionValue("gameTime");
		startTime = options.getIntegerOptionValue("borderStartTime");
		endTime = options.getIntegerOptionValue("borderEndTime");
		netherTime = options.getIntegerOptionValue("netherCloseTime");
		caveTime = options.getIntegerOptionValue("caveCloseTime");
		
		scoreboard = GameManager.instance.getMainScoreboard();
		if ((objective = scoreboard.getObjective(scoreName)) == null) {
			objective = scoreboard.addScoreObjective(scoreName, IScoreCriteria.DUMMY);
		}
		objective.setDisplayName(displayName);
		scoreboard.setObjectiveInDisplaySlot(1, objective);
		scoreboard.getOrCreateScore(lines[0], objective).setScorePoints(gameTime);
		scoreboard.getOrCreateScore(lines[1], objective).setScorePoints(borderStart / 2);
		scoreboard.getOrCreateScore(lines[2], objective).setScorePoints(netherTime);
		scoreboard.getOrCreateScore(lines[3], objective).setScorePoints(caveTime);
		
		GameManager.instance.addTask(new TaskNetherCave());
	}
	
	private int getBorderPosition() {
		return (int) GameManager.instance.getMinecraftServer().worlds[0].getWorldBorder().getDiameter();
	}
	
	@Override
	public void onTimer() {
		if (this.hasFinished() || !GameManager.instance.isGamePlaying()) this.setCanceled();
		Score score = scoreboard.getOrCreateScore(lines[0], objective);
		int timeRemaining = score.getScorePoints();
		score.setScorePoints(timeRemaining - 1);
		if (timeRemaining == gameTime - startTime) {
			GameManager.instance.addTask(new TaskBorderReminder());
		}
		scoreboard.getOrCreateScore(lines[1], objective).setScorePoints(getBorderPosition() / 2);

		score = scoreboard.getOrCreateScore(lines[2], objective);
		if (score.getScorePoints() > 0)
			score.setScorePoints(Math.max(0, score.getScorePoints() - 1));
		else scoreboard.removeObjectiveFromEntity(lines[2], objective);

		score = scoreboard.getOrCreateScore(lines[3], objective);
		if (score.getScorePoints() > 0)
			score.setScorePoints(Math.max(0, score.getScorePoints() - 1));
		else scoreboard.removeObjectiveFromEntity(lines[3], objective);

		if (timeRemaining % 60 == 0) updateCompassRotation();
	}
	
	private void updateCompassRotation() {
		for (GamePlayer player : GameManager.instance.getPlayerManager().getCombatPlayers()) {
			if (player.isAlive()) {
				if (player.getRealPlayer().isPresent()) {
					EntityPlayerMP playermp = player.getRealPlayer().get();
					EntityPlayerMP target = null;
					for (GamePlayer tmpTarget : GameManager.instance.getPlayerManager().getCombatPlayers()) {
						if (tmpTarget.isAlive() && player.getTeam() != tmpTarget.getTeam() && tmpTarget.getRealPlayer().isPresent()) {
							if (target == null || playermp.getDistanceSq(target) > playermp.getDistanceSq(tmpTarget.getRealPlayer().get()))
								target = tmpTarget.getRealPlayer().get();
						}
					}
					if (target != null)
						playermp.connection.sendPacket(new SPacketSpawnPosition(target.getPosition()));
				}
			}
		}
	}
	
	public static void hideScoreboard() {
		GameManager.instance.getMainScoreboard().setObjectiveInDisplaySlot(1, null);
	}

}
