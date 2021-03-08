package cn.topologycraft.uhc;

import cn.topologycraft.uhc.GamePlayer.EnumStat;
import cn.topologycraft.uhc.options.Options;
import cn.topologycraft.uhc.task.*;
import cn.topologycraft.uhc.util.BookNBT;
import cn.topologycraft.uhc.util.LastWinnerList;
import cn.topologycraft.uhc.util.SpawnPlatform;
import cn.topologycraft.uhc.util.TitleUtil;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Optional;
import java.util.Random;

public class GameManager extends Taskable {

	public static final Logger LOG = LogManager.getLogger();
	public static GameManager instance;
	public static final Random rand = new Random();
	
	private DedicatedServer mcServer;
	private DedicatedPlayerList playerList;
	
	private PlayerManager playerManager;
	private ConfigManager configManager = new ConfigManager();
	private Options uhcOptions;
	
	private boolean isGamePlaying;
	private boolean isGameEnded;
	
	private boolean isPregenerating;
	private static boolean preloaded;
	
	public LastWinnerList winnerList;
	private Optional<BossInfoServer> bossInfo = Optional.empty();
	
	public GameManager(DedicatedServer server, DedicatedPlayerList players) {
		instance = this;
		mcServer = server;
		playerList = players;
		uhcOptions = new Options(new File("uhc.properties"));
		playerManager = new PlayerManager(this);
		winnerList = new LastWinnerList(new File("lastwinners.txt"));
	}
	
	public MinecraftServer getMinecraftServer() { return mcServer; }
	public PlayerList getPlayerList() { return playerList; }
	public PlayerManager getPlayerManager() { return playerManager; }
	public ConfigManager getConfigManager() { return configManager; }
	public Options getOptions() { return uhcOptions; }
	public boolean isGamePlaying() { return isGamePlaying; }
	public boolean isConfiguring() { return configManager.isConfiguring(); }
	public boolean hasGameEnded() { return isGameEnded; }
	
	public void onPlayerJoin(EntityPlayerMP player) {
		try {
			playerManager.onPlayerJoin(player);
			bossInfo.ifPresent(info -> info.addPlayer(player));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean onPlayerChat(EntityPlayerMP player, String msg) {
		try {
			if (configManager.onPlayerChat(player, msg))
				playerManager.onPlayerChat(player, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void onPlayerDeath(EntityPlayerMP player, DamageSource cause) {
		try {
			playerManager.onPlayerDeath(player, cause);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onPlayerRespawn(EntityPlayerMP player) {
		try {
			playerManager.onPlayerRespawn(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onPlayerDamaged(EntityPlayerMP player, DamageSource cause, float amount) {
		try {
			playerManager.onPlayerDamaged(player, cause, amount);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Entity onPlayerSpectate(EntityPlayerMP player, Entity target, Entity origin) {
		try {
			return playerManager.onPlayerSpectate(player, target, origin);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return target;
	}
	
	public void onServerInited() {
		SpawnPlatform.generatePlatform(this, mcServer.worlds[0]);
	}
	
	public void postInit() {
		this.displayHealth();
		TaskScoreboard.hideScoreboard();
		if (!preloaded) {
			int borderStart = uhcOptions.getIntegerOptionValue("borderStart");
			borderStart = (borderStart + 80) / 32 + 1;
			this.addTask(new TaskPregenerate(borderStart, mcServer.worlds[0]));
			this.addTask(new TaskPregenerate(borderStart, mcServer.worlds[1]));
			isPregenerating = true;
		}
	}
	
	public void setPregenerateComplete() {
		isPregenerating = false;
	}
	
	public boolean isPregenerating() {
		return isPregenerating;
	}
	
	public float modifyPlayerDamage(float amount) {
		if (isGamePlaying) {
			boolean greenHand = uhcOptions.getBooleanOptionValue("greenhandProtect");
			int time = uhcOptions.getIntegerOptionValue("greenhandTime");
			int gameTime = uhcOptions.getIntegerOptionValue("gameTime") - this.getGameTimeRemaining();
			if (greenHand && gameTime < time) return amount / 2;
		}
		return amount;
	}
	
	public static void tryUpdateSaveFolder(String folder) {
		if (!new File(folder + "/preload").exists()) {
			deleteFolder(new File(folder));
		} else {
			preloaded = true;
		}
	}

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files == null) return;
		for (File file : files) {
			if (file.isDirectory()) deleteFolder(file);
			else file.delete();
		}
	}
	
	public static void regenerateTerrain() {
		File preload = new File(instance.mcServer.getFolderName() + "/preload");
		if (preload.exists()) preload.delete();
		instance.mcServer.initiateShutdown();
	}
	
	public void startGame(EntityPlayerMP operator) {
		if (isGamePlaying || !configManager.isConfiguring()) {
			operator.sendMessage(new TextComponentString("It's not time to start."));
			return;
		}
		boolean autoTeams = uhcOptions.getBooleanOptionValue("randomTeams");
		if (!playerManager.formTeams(autoTeams)) return;
		if ((EnumMode) uhcOptions.getOptionValue("gameMode") == EnumMode.BOSS) {
			bossInfo = Optional.of(new BossInfoServer(new TextComponentString(playerManager.getBossPlayer().getName()), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS));
			playerList.getPlayers().forEach(player -> bossInfo.ifPresent(info -> info.addPlayer(player)));
			bossInfo.ifPresent(info -> info.setVisible(true));
		}
		isGamePlaying = true;
		this.initWorlds();
		configManager.stopConfiguring();
		playerManager.setupIngameTeams();
		playerManager.spreadPlayers();
		this.destroySpawnPlatform();
		this.addTask(new TaskTitleCountDown(10, 80, 20));
	}
	
	public void endGame() {
		if (isGameEnded) return;
		isGameEnded = true;
		removeWorldBorder();
		TaskScoreboard.hideScoreboard();
		bossInfo.ifPresent(info -> info.setVisible(false));
		bossInfo = Optional.empty();
	}
	
	public void checkWinner() {
		if (isGameEnded || !isGamePlaying) return;
		int remainTeamCnt = 0;
		GameTeam winner = null;
		for (GameTeam team : playerManager.getTeams()) {
			if (team.getAliveCount() > 0) {
				remainTeamCnt++;
				winner = team;
			}
		}
		if (remainTeamCnt == 1)
			this.onTeamWin(winner);
	}
	
	private void onTeamWin(GameTeam team) {
		TitleUtil.sendTitleToAllPlayers(team.getColorfulTeamName() + " Wins !", "Congratulations !");
		this.boardcastMessage(team.getColorfulTeamName() + " is the winner !");
		for (GamePlayer player : playerManager.getCombatPlayers()) {
			if (player.getStat().getFloatStat(EnumStat.ALIVE_TIME) < 1)
				player.getStat().setStat(EnumStat.ALIVE_TIME, uhcOptions.getIntegerOptionValue("gameTime") - this.getGameTimeRemaining());
		}
		winnerList.setWinner(team.getPlayers());
		this.endGame();
		this.addTask(new TaskBoardcastData(160));
	}
	
	private void initWorlds() {
		boolean daylightCycle = uhcOptions.getBooleanOptionValue("daylightCycle");
		EnumDifficulty difficulty = (EnumDifficulty) uhcOptions.getOptionValue("difficulty");
		int borderStart = uhcOptions.getIntegerOptionValue("borderStart");
		for (WorldServer world : mcServer.worlds) {
			world.getGameRules().setOrCreateGameRule("naturalRegeneration", "false");
			world.getGameRules().setOrCreateGameRule("doDaylightCycle", daylightCycle ? "true" : "false");
			world.setWorldTime(0);
			world.getWorldBorder().setTransition(borderStart);
		}
		mcServer.setDifficultyForAllWorlds(difficulty);
	}
	
	private void removeWorldBorder() {
		for (WorldServer world : mcServer.worlds) {
			world.getWorldBorder().setTransition(2999984);
		}
	}
	
	public void displayHealth() {
		Scoreboard scoreboard = getMainScoreboard();
		String name = "Health";
		ScoreObjective objective;
		if ((objective = scoreboard.getObjective(name)) == null) {
			objective = scoreboard.addScoreObjective(name, IScoreCriteria.HEALTH);
		}
		scoreboard.setObjectiveInDisplaySlot(0, objective);
		scoreboard.setObjectiveInDisplaySlot(2, objective);
	}
	
	public Scoreboard getMainScoreboard() {
		return mcServer.worlds[0].getScoreboard();
	}
	
	public void tick() {
		try {
			this.updateTasks();
			if (!this.isGamePlaying)
				this.winnerParticles();
			for (GamePlayer player : playerManager.getAllPlayers()) {
				player.tick();
			}
			bossInfo.ifPresent(info -> playerManager.getBossPlayer().getRealPlayer().ifPresent(player -> info.setPercent(player.getHealth() / player.getMaxHealth())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void winnerParticles() {
		for (EntityPlayerMP player : playerList.getPlayers()) {
			if (player.ticksExisted % 2 == 0 && winnerList.isWinner(player.getName())) {
				double angle = (player.ticksExisted % 360) * 9 * Math.PI / 180;
				double dx = Math.cos(angle) * 0.6;
				double dz = Math.sin(angle) * 0.6;
				double dy = Math.cos(angle) * 0.4;
				((WorldServer) player.world).spawnParticle(EnumParticleTypes.FLAME, player.posX + dx, player.posY + dy + player.getEyeHeight() / 2, player.posZ + dz, 1, 0, 0, 0, 0, new int[0]);
				((WorldServer) player.world).spawnParticle(EnumParticleTypes.FLAME, player.posX - dx, player.posY + dy + player.getEyeHeight() / 2, player.posZ - dz, 1, 0, 0, 0, 0, new int[0]);
			}
		}
	}
	
	public void generateSpawnPlatform() { SpawnPlatform.generatePlatform(this, mcServer.worlds[0]); }
	public void destroySpawnPlatform() { SpawnPlatform.destroyPlatform(mcServer.worlds[0]); }
	
	public void startConfiguration(EntityPlayerMP operator) {
		configManager.startConfiguring(playerManager.getGamePlayer(operator));
		operator.inventory.addItemStackToInventory(BookNBT.getConfigBook(this));
		SpawnPlatform.generateSafePlatform(mcServer.worlds[0]);
	}
	
	public void boardcastMessage(String msg) {
		ITextComponent text = new TextComponentString(msg);
		playerList.getPlayers().forEach(player -> player.sendMessage(text));
		LOG.info(msg);
	}
	
	public BlockPos buildSmallHouse(BlockPos pos, EnumDyeColor color) {
		World world = mcServer.worlds[0];
		pos = world.getTopSolidOrLiquidBlock(pos).down();
		IBlockState floor = Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, color);
		IBlockState wall = Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR, color);
		IBlockState ceiling = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, color);
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				world.setBlockState(pos.add(x, 0, z), floor);
				world.setBlockState(pos.add(x, 4, z), ceiling);
				if (x == -3 || x == 3 || z == -3 || z == 3) {
					for (int y = 1; y <= 3; y++) {
						world.setBlockState(pos.add(x, y, z), wall);
					}
				} else {
					for (int y = 1; y <= 3; y++) {
						world.setBlockState(pos.add(x, y, z), Blocks.AIR.getDefaultState());
					}
				}
			}
		}
		world.setBlockState(pos.up(), Blocks.CHEST.getDefaultState());
		return pos.up();
	}
	
	public int getGameTimeRemaining() {
		Scoreboard scoreboard = getMainScoreboard();
		ScoreObjective objective = scoreboard.getObjective(TaskScoreboard.scoreName);
		return scoreboard.getOrCreateScore(TaskScoreboard.lines[0], objective).getScorePoints();
	}
	
	public static enum EnumMode {
		NORMAL,
		SOLO,
		BOSS;
	}

}
