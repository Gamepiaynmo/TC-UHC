package cn.topologycraft.uhc.task;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.options.Options;
import cn.topologycraft.uhc.task.Task.TaskTimer;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TaskNetherCave extends TaskTimer {
	
	private final int netherCloseTime, caveCloseTime, gameTime, borderFinal;

	private int finalX, finalZ, finalTime, finalMinY, finalMaxY;

	public static final String[] lines = { "Border Min Y:", "Border Max Y:", "Border Center X:", "Border Center Z:" };

	public TaskNetherCave() {
		super(0, 20);
		Options options = Options.instance;
		netherCloseTime = options.getIntegerOptionValue("netherCloseTime");
		caveCloseTime = options.getIntegerOptionValue("caveCloseTime");
		gameTime = options.getIntegerOptionValue("gameTime");
		borderFinal = GameManager.instance.getOptions().getIntegerOptionValue("borderFinal");
	}
	
	@Override
	public void onTimer() {
		if (this.hasFinished()) return;
		if (!GameManager.instance.isGamePlaying() || GameManager.instance.hasGameEnded()) this.setCanceled();
		int timeRemaining = GameManager.instance.getGameTimeRemaining();
		int timePast = gameTime - timeRemaining;
//		if (timeRemaining == 0) this.setCanceled();
		Iterable<GamePlayer> combatPlayers = GameManager.instance.getPlayerManager().getCombatPlayers();
		
		int netherTime = netherCloseTime - timePast;
		if (netherTime > 0 && netherTime <= 120 && netherTime % 30 == 0) {
			GameManager.instance.broadcastMessage(TextFormatting.DARK_RED + "Nether will be closed in " + netherTime + " seconds.");
		} else if (netherTime == 0) {
			GameManager.instance.broadcastMessage(TextFormatting.DARK_RED + "Nether closed.");
		} else if (netherTime < 0) {
			for (GamePlayer player : combatPlayers) {
				player.getRealPlayer().ifPresent(playermp -> {
					if (playermp.dimension != 0)
						playermp.attackEntityFrom(DamageSource.IN_WALL, 1.0f);
				});
			}
		}
		
		int caveTime = caveCloseTime - timePast;
		if (caveTime > 0 && caveTime <= 120 && caveTime % 30 == 0) {
			GameManager.instance.broadcastMessage(TextFormatting.DARK_RED + "Caves will be closed in " + caveTime + " seconds.");
		} else if (caveTime == 0) {
			GameManager.instance.broadcastMessage(TextFormatting.DARK_RED + "Caves closed.");
			WorldBorder border = GameManager.instance.getMinecraftServer().worlds[0].getWorldBorder();
			int finalSize = Math.max((int) border.getDiameter() / 2, 1);
			Random random = new Random();
			while (finalX * finalX + finalZ * finalZ < finalSize * finalSize / 4) {
				finalX = random.nextInt(finalSize * 2) - finalSize;
				finalZ = random.nextInt(finalSize * 2) - finalSize;
			}

			finalTime = Math.max(gameTime - caveCloseTime, finalSize * 2 - borderFinal);
			border.setTransition(border.getDiameter(), borderFinal, finalTime * 1000L);

			WorldServer world = GameManager.instance.getMinecraftServer().worlds[0];
			List<Integer> heights = Lists.newArrayList();
			int sampleSize = borderFinal / 2;
			int step = Math.max(1, sampleSize / 4);
			for (int x = finalX - sampleSize; x <= finalX + sampleSize; x += step) {
				for (int z = finalZ - sampleSize; z <= finalZ + sampleSize; z += step) {
					for (int y = 255; y > 0; y--) {
						IBlockState state = world.getBlockState(new BlockPos(x, y, z));
						if (state.getBlock() == Blocks.STONE && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) {
							heights.add(y);
							break;
						}
						if (state.getBlock() == Blocks.WATER) {
							heights.add(Math.max(1, y - 4));
							break;
						}
					}
				}
			}

			Collections.sort(heights);
			finalMinY = heights.get(4);
			finalMaxY = heights.get(heights.size() - 4) + 12;

			WorldInfo worldinfo = world.getWorldInfo();
			worldinfo.setCleanWeatherTime(gameTime * 20);
			worldinfo.setRainTime(0);
			worldinfo.setThunderTime(0);
			worldinfo.setRaining(false);
			worldinfo.setThundering(false);

			GameRules gameRules = world.getGameRules();
			gameRules.setOrCreateGameRule("daylightCycle", "false");
			world.setWorldTime(1000);
		} else if (caveTime < 0) {
			caveTime = -caveTime;
			boolean glow = caveTime % 60 == 0;
			float partial = caveTime <= finalTime ? (float) caveTime / finalTime : 1;
			float minY = finalMinY * partial;
			float maxY = 255 - partial * (255 - finalMaxY);

			Scoreboard scoreboard = GameManager.instance.getMainScoreboard();
			ScoreObjective objective = scoreboard.getObjective(TaskScoreboard.scoreName);
			scoreboard.getOrCreateScore(lines[0], objective).setScorePoints((int) Math.ceil(minY));
			scoreboard.getOrCreateScore(lines[1], objective).setScorePoints((int) Math.floor(maxY));
			scoreboard.getOrCreateScore(lines[2], objective).setScorePoints(Math.round(partial * finalX));
			scoreboard.getOrCreateScore(lines[3], objective).setScorePoints(Math.round(partial * finalZ));

			for (GamePlayer player : combatPlayers) {
				player.getRealPlayer().ifPresent(playermp -> {
					if (glow) playermp.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 1, false, false));
					if (playermp.dimension == 0) {
						if (playermp.posY < minY || playermp.posY > maxY)
							playermp.attackEntityFrom(DamageSource.IN_WALL, 1.0f);

						float particleY = -1;
						if (playermp.posY < minY + 5)
							particleY = minY + 3;
						if (playermp.posY > maxY - 5)
							particleY = maxY - 2;
						if (particleY > 0) {
							GameManager.instance.getMinecraftServer().worlds[0].spawnParticle(playermp, EnumParticleTypes.PORTAL, false,
									playermp.posX, particleY, playermp.posZ, 100, 2, 0, 2, 0, 0);
						}
					}
				});
			}

			if (caveTime <= finalTime) {
				WorldBorder border = GameManager.instance.getMinecraftServer().worlds[0].getWorldBorder();
				border.setCenter(partial * finalX, partial * finalZ);
			}
		}
	}

}
