package cn.topologycraft.uhc;

import cn.topologycraft.uhc.GameManager.EnumMode;
import cn.topologycraft.uhc.GamePlayer.EnumStat;
import cn.topologycraft.uhc.task.TaskFindPlayer;
import cn.topologycraft.uhc.task.TaskKeepSpectate;
import cn.topologycraft.uhc.util.*;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team.CollisionRule;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerManager {
	
	private GameManager gameManager;
	private PlayerList playerList;
	
	private List<GamePlayer> allPlayerList = Lists.newArrayList();
	private List<GamePlayer> combatPlayerList = Lists.newArrayList();
	private List<GamePlayer> observePlayerList = Lists.newArrayList();
	private List<GameTeam> teams = Lists.newArrayList();
	
	private int playersPerTeam;
	
	public PlayerManager(GameManager manager) {
		gameManager = manager;
		playerList = manager.getPlayerList();
	}
	
	public Optional<EntityPlayerMP> getPlayerByUUID(UUID id) {
		return Optional.ofNullable(playerList.getPlayerByUUID(id));
	}
	
	public boolean forceFriendlyView(GamePlayer player) {
		if (player.isAlive()) return false;
		if (!gameManager.getOptions().getBooleanOptionValue("forceViewport")) return false;
		if (player.getTeam().getAliveCount() == 0) return false;
		return true;
	}
	
	public GamePlayer getGamePlayer(EntityPlayer player) {
		for (GamePlayer gamePlayer : allPlayerList) {
			if (gamePlayer.isSamePlayer(player))
				return gamePlayer;
		}
		return null;
	}

	public void onPlayerJoin(EntityPlayerMP player) {
		GamePlayer gamePlayer = getGamePlayer(player);
		if (gamePlayer == null)
			allPlayerList.add(gamePlayer = new GamePlayer(player));
		if (gameManager.isGamePlaying()) {
			if (combatPlayerList.contains(gamePlayer)) {
				if (gamePlayer.isAlive()) player.setGameType(GameType.SURVIVAL);
				else player.setGameType(GameType.SPECTATOR);
			} else {
				player.setGameType(GameType.SPECTATOR);
				if (!observePlayerList.contains(gamePlayer))
					observePlayerList.add(gamePlayer);
			}
		} else {
			randomSpawnPosition(player);
			resetHealthAndFood(player);
			if (gameManager.hasGameEnded())
				player.setGameType(GameType.SPECTATOR);
			else {
				player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
				player.setGameType(GameType.ADVENTURE);
				regiveConfigItems(player);
				if (gameManager.getConfigManager().isConfiguring())
					player.setEntityInvulnerable(true);
			}
		}
	}
	
	public void regiveConfigItems(EntityPlayerMP player) {
		if (!gameManager.isGamePlaying()) 
			player.inventory.clear();
		if (gameManager.getConfigManager().isConfiguring()) {
			this.getGamePlayer(player).getColorSelected().ifPresent(color -> {
				ItemStack teamItem = getTeamItem(color);
				EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(teamItem);
				player.setItemStackToSlot(slot, teamItem);
			});
			if (gameManager.getConfigManager().isOperator(player))
				player.inventory.addItemStackToInventory(BookNBT.getConfigBook(gameManager));
			player.inventory.addItemStackToInventory(BookNBT.getPlayerBook(gameManager));
		}
	}
	
	public void regiveAdjustBook(EntityPlayerMP player, boolean force) {
		Item current = player.inventory.getCurrentItem().getItem();
		ItemStack book = BookNBT.getAdjustBook(gameManager);
		if (current == Items.WRITTEN_BOOK)
			player.inventory.setInventorySlotContents(player.inventory.currentItem, book);
		else if (force) player.inventory.addItemStackToInventory(book);
	}
	
	public void removeAdjustBook(EntityPlayerMP player) {
		Item current = player.inventory.getCurrentItem().getItem();
		if (current == Items.WRITTEN_BOOK)
			player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
	}
	
	public void refreshConfigBook() {
		gameManager.getConfigManager().getOperator().getRealPlayer().ifPresent(player -> {
			regiveConfigItems(player);
		});
	}
	
	private ItemStack getTeamItem(GameColor color) {
		ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
		float[] rgb = color.dyeColor.getColorComponentValues();
		int r = (int) (rgb[0] * 255);
		int g = (int) (rgb[1] * 255);
		int b = (int) (rgb[2] * 255);
		Items.LEATHER_CHESTPLATE.setColor(stack, (((r << 8) | g) << 8) | b);
		stack.setStackDisplayName(color.dyeColor.toString());
		return stack;
	}
	
	public void randomSpawnPosition(EntityPlayerMP player) {
		BlockPos pos = SpawnPlatform.getRandomSpawnPosition(gameManager.rand);
		player.setPosition(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5);
		player.fallDistance = 0.0f;
	}
	
	public void resetHealthAndFood(EntityPlayerMP player) {
		player.setHealth(player.getMaxHealth());
		player.getFoodStats().setFoodLevel(20);
		player.getFoodStats().setSaturationLevel(20);
	}
	
	public Iterable<GamePlayer> getAllPlayers() {
		return allPlayerList;
	}
	
	public Iterable<GamePlayer> getCombatPlayers() {
		return combatPlayerList;
	}
	
	public Iterable<GamePlayer> getObservePlayers() {
		return observePlayerList;
	}
	
	public Iterable<GameTeam> getTeams() {
		return teams;
	}
	
	public boolean isObserver(GamePlayer player) {
		return observePlayerList.contains(player);
	}
	
	public void onPlayerChat(EntityPlayerMP player, String msg) {
		if (msg == null) return;
		if (!gameManager.isGamePlaying()) {
			gameManager.boardcastMessage(chatMessage(player, msg, false));
			return;
		}
		if (msg.startsWith("p ")) {
			gameManager.boardcastMessage(chatMessage(player, msg.substring(2), false));
			return;
		}
		GamePlayer gamePlayer = getGamePlayer(player);
		if (gamePlayer.getTeam() == null || gamePlayer.getTeam().getAliveCount() == 0) {
			gameManager.boardcastMessage(chatMessage(player, msg, false));
			return;
		}
		String message = chatMessage(player, msg, true);
		gamePlayer.getTeam().getPlayers().forEach(other -> other.getRealPlayer().ifPresent(playermp -> {
			playermp.sendMessage(new TextComponentString(message));
		}));
	}
	
	private String chatMessage(EntityPlayer player, String msg, boolean secret) {
		GamePlayer gamePlayer = getGamePlayer(player);
		TextFormatting color = gamePlayer.getTeam() == null ? TextFormatting.WHITE : gamePlayer.getTeam().getTeamColor().chatColor;
		return TextFormatting.AQUA.toString() + "[" + TextFormatting.GOLD + (secret ? "To Team" : "To All") + TextFormatting.AQUA.toString() + "]" +
				color + player.getName() + TextFormatting.YELLOW + ": " + TextFormatting.WHITE + msg;
	}
	
	public void onPlayerDeath(EntityPlayerMP player, DamageSource cause) {
		if (gameManager.isGamePlaying()) {
			GamePlayer gamePlayer = getGamePlayer(player);
			if (combatPlayerList.contains(gamePlayer) && gamePlayer.isAlive()) {
				gamePlayer.setDead(gameManager.getGameTimeRemaining());
				player.setGameType(GameType.SPECTATOR);
				if (gameManager.getOptions().getBooleanOptionValue("forceViewport")) {
					gameManager.addTask(new TaskKeepSpectate(gamePlayer));
				}
				if (gamePlayer.getTeam().getAliveCount() == 0) {
					gameManager.checkWinner();
				} else {
					this.deadPotionEffects(gamePlayer.getTeam());
				}
			}
		}
		EntityItem entityitem = new EntityItem(player.world, player.posX, player.posY, player.posZ, PlayerItems.getPlayerItem(player.getName()));
        entityitem.setPickupDelay(40);
        player.world.spawnEntity(entityitem);
	}
	
	private void deadPotionEffects(GameTeam team) {
		if (gameManager.getOptions().getBooleanOptionValue("deathBonus")) {
			for (GamePlayer player : team.getPlayers()) {
				if (player.isAlive()) {
					gameManager.addTask(new TaskFindPlayer(player) {
						@Override
						public void onFindPlayer(EntityPlayerMP player) {
							player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 300, 1));
							player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 1));
							player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 1));
							player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 300, 1));
						}
					});
				}
			}
		}
	}
	
	public void onPlayerRespawn(EntityPlayerMP player) {
		if (gameManager.isGamePlaying()) {
			GamePlayer gamePlayer = getGamePlayer(player);
			if (!gamePlayer.isAlive()) {
				player.setSpectatingEntity(player);
			}
		} else this.randomSpawnPosition(player);
	}
	
	public void onPlayerDamaged(EntityPlayerMP player, DamageSource source, float amount) {
		if (gameManager.isGamePlaying()) {
			String msg = String.format("You got %.2f damage from ", amount);
			if (source == DamageSource.IN_FIRE) msg += "fire";
			else if (source == DamageSource.LIGHTNING_BOLT) msg += "lightning blot";
			else if (source == DamageSource.ON_FIRE) msg += "fire";
			else if (source == DamageSource.LAVA) msg += "lava";
			else if (source == DamageSource.HOT_FLOOR) msg += "hot floor";
			else if (source == DamageSource.IN_WALL) msg += "suffocating";
			else if (source == DamageSource.CRAMMING) msg += "cramming";
			else if (source == DamageSource.DROWN) msg += "drown";
			else if (source == DamageSource.STARVE) msg += "starve";
			else if (source == DamageSource.CACTUS) msg += "cactus";
			else if (source == DamageSource.FALL) msg += "falling";
			else if (source == DamageSource.FLY_INTO_WALL) msg += "flying into wall";
			else if (source == DamageSource.OUT_OF_WORLD) msg += "out of world";
			else if (source == DamageSource.GENERIC) msg += "unknown";
			else if (source == DamageSource.MAGIC) msg += "magic";
			else if (source == DamageSource.WITHER) msg += "wither";
			else if (source == DamageSource.ANVIL) msg += "anvil";
			else if (source == DamageSource.FALLING_BLOCK) msg += "falling block";
			else if (source == DamageSource.DRAGON_BREATH) msg += "dragon breath";
			else if (source == DamageSource.FIREWORKS) msg += "fireworks";
			else if (source.damageType.startsWith("explosion")) msg += "explosion" + (source.getTrueSource() != null ? " by " + source.getTrueSource().getName() : "");
			else if (source instanceof EntityDamageSourceIndirect) msg += source.getImmediateSource().getName() + " by " + source.getTrueSource().getName();
			else if (source instanceof EntityDamageSource) msg += source.getTrueSource().getName();
			else msg += source.getDamageType() + (source.getTrueSource() != null ? " by " + source.getTrueSource().getName() : "");
			player.sendMessage(new TextComponentString(TextFormatting.RED + msg));
		}
	}
	
	public Entity onPlayerSpectate(EntityPlayerMP player, Entity target, Entity origin) {
		GamePlayer gamePlayer = getGamePlayer(player);
		if (gamePlayer.isAlive()) return target;
		if (!SpectateTargetUtil.isCapableTarget(gamePlayer, target)) {
			return SpectateTargetUtil.getCapableTarget(gamePlayer, origin);
		}
		return target;
	}
	
	private Optional<GamePlayer> getPlayerByName(String name) {
		return allPlayerList.stream().filter(player -> player.getName().equals(name)).findFirst();
	}
	
	public void killPlayer(String playerName) {
		getPlayerByName(playerName).ifPresent(player -> {
			player.setDead(gameManager.getGameTimeRemaining());
			player.getRealPlayer().ifPresent(playermp -> playermp.setGameType(GameType.SPECTATOR));
			if (gameManager.getOptions().getBooleanOptionValue("forceViewport"))
				gameManager.addTask(new TaskKeepSpectate(player));
			if (player.getTeam().getAliveCount() == 0) {
				gameManager.checkWinner();
			} else this.deadPotionEffects(player.getTeam());
			gameManager.boardcastMessage(player.getTeam().getTeamColor().chatColor + player.getName() + TextFormatting.WHITE + " got -1s.");
		});
	}
	
	public void resurrentPlayer(String playerName) {
		getPlayerByName(playerName).ifPresent(player -> {
			player.deathTime = 0;
			player.isAlive = true;
			player.getStat().setStat(EnumStat.ALIVE_TIME, 0);
			player.getRealPlayer().ifPresent(playermp -> playermp.setGameType(GameType.SURVIVAL));
			gameManager.boardcastMessage(player.getTeam().getTeamColor().chatColor + player.getName() + TextFormatting.WHITE + " got +1s.");
		});
	}
	
	public boolean formTeams(boolean auto) {
		this.refreshOnlinePlayers();
		return auto ? this.automaticFormTeams() : this.manuallyFormTeams();
	}
	
	private boolean automaticFormTeams() {
		Optional<EntityPlayerMP> operator = gameManager.getConfigManager().getOperator().getRealPlayer();
		boolean alright = true;
		for (GamePlayer gamePlayer : getAllPlayers()) {
			GameColor color = gamePlayer.getColorSelected().orElse(null);
			if (color == null) {
				gamePlayer.getRealPlayer().ifPresent(player -> player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Please select a team to join, others are waiting for you !")));
				operator.ifPresent(player -> player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + gamePlayer.getName())));
				alright = false;
			} else {
				if (color == GameColor.WHITE) observePlayerList.add(gamePlayer);
				else combatPlayerList.add(gamePlayer);
			}
		}
		
		if (!alright) {
			operator.ifPresent(player -> player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Some players has not made a choice.")));
			return false;
		}
		
		teams.clear();
		switch ((EnumMode) gameManager.getOptions().getOptionValue("gameMode")) {
			case NORMAL: {
				int playerCount = combatPlayerList.size();
				int teamCount = gameManager.getOptions().getIntegerOptionValue("teamCount");
				playersPerTeam = playerCount / teamCount + (playerCount % teamCount == 0 ? 0 : 1);
				int morePlayers = playerCount % teamCount;
				int[] randomTeam = new int[playerCount];
				int posCnt = 0;
				for (int i = 0; i < teamCount; i++) {
					for (int j = (morePlayers > 0 && i >= morePlayers ? 1 : 0); j < playersPerTeam; j++)
						randomTeam[posCnt++] = i;
					teams.add(new GameTeam().setColorTeam(GameColor.getColor(i)));
				}
				for (int i = 0; i < playerCount; i++) {
					int pos = gameManager.rand.nextInt(playerCount - i) + i;
					int temp = randomTeam[i];
					randomTeam[i] = randomTeam[pos];
					randomTeam[pos] = temp;
					teams.get(randomTeam[i]).addPlayer(combatPlayerList.get(i));
				}
				break;
			}
			case SOLO: {
				combatPlayerList.stream().map(player -> new GameTeam().setPlayerTeam(player)).forEach(teams::add);
				playersPerTeam = 1;
				break;
			}
			case BOSS: {
				GamePlayer boss = combatPlayerList.get(gameManager.rand.nextInt(combatPlayerList.size()));
				teams.add(new GameTeam().setColorTeam(GameColor.RED).addPlayer(boss));
				GameTeam team = new GameTeam().setColorTeam(GameColor.BLUE);
				combatPlayerList.stream().filter(player -> player != boss).forEach(team::addPlayer);
				teams.add(team);
				playersPerTeam = combatPlayerList.size() - 1;
				break;
			}
		}
		return true;
	}
	
	private boolean manuallyFormTeams() {
		Optional<EntityPlayerMP> operator = gameManager.getConfigManager().getOperator().getRealPlayer();
		boolean alright = true;
		for (GamePlayer gamePlayer : getAllPlayers()) {
			GameColor color = gamePlayer.getColorSelected().orElse(null);
			if (color == null) {
				gamePlayer.getRealPlayer().ifPresent(player -> player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Please select a team to join, others are waiting for you !")));
				operator.ifPresent(player -> player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + gamePlayer.getName())));
				alright = false;
			} else {
				if (color == GameColor.WHITE) observePlayerList.add(gamePlayer);
				else combatPlayerList.add(gamePlayer);
			}
		}
		
		if (!alright) {
			operator.ifPresent(player -> player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Some players has not made a choice.")));
			return false;
		}
		
		teams.clear();
		switch ((EnumMode) gameManager.getOptions().getOptionValue("gameMode")) {
			case NORMAL: {
				int playerCount = combatPlayerList.size();
				int teamCount = gameManager.getOptions().getIntegerOptionValue("teamCount");
				for (int i = 0; i < teamCount; i++) {
					teams.add(new GameTeam().setColorTeam(GameColor.getColor(i)));
				}

				List<GamePlayer> randomPlayers = Lists.newArrayList();
				combatPlayerList.forEach(player -> {
					GameColor color = player.getColorSelected().orElse(GameColor.WHITE);
					if (color != GameColor.BLACK) {
						teams.get(color.getId()).addPlayer(player);
					} else {
						randomPlayers.add(player);
					}
				});

				for (int i = 0; i < teamCount; i++) {
					int pos = gameManager.rand.nextInt(teamCount);
					GameTeam temp = teams.get(i);
					teams.set(i, teams.get(pos));
					teams.set(pos, temp);
				}

				for (int i = 0; i < randomPlayers.size(); i++) {
					int pos = gameManager.rand.nextInt(randomPlayers.size());
					GamePlayer temp = randomPlayers.get(i);
					randomPlayers.set(i, randomPlayers.get(pos));
					randomPlayers.set(pos, temp);
				}

				randomPlayers.forEach(player -> {
					GameTeam team = teams.get(0);
					for (int i = 1; i < teamCount; i++) {
						if (teams.get(i).getPlayerCount() < team.getPlayerCount())
							team = teams.get(i);
					}

					team.addPlayer(player);
				});

				playersPerTeam = teams.stream().mapToInt(team -> team.getPlayerCount()).max().orElse(0);
				break;
			}
			case SOLO: {
				combatPlayerList.stream().map(player -> new GameTeam().setPlayerTeam(player)).forEach(teams::add);
				playersPerTeam = 1;
				break;
			}
			case BOSS: {
				GamePlayer boss = null;
				for (GamePlayer player : combatPlayerList) {
					if (player.getColorSelected().orElse(GameColor.BLUE) == GameColor.RED) {
						if (boss == null) boss = player;
						else {
							player.getRealPlayer().ifPresent(playermp -> playermp.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "There cannot be more than one boss.")));
							alright = false;
						}
					}
				}
				if (!alright) {
					operator.ifPresent(player -> player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "There are more than one boss.")));
					return false;
				}
				teams.add(new GameTeam().setColorTeam(GameColor.RED).addPlayer(boss));
				GameTeam team = new GameTeam().setColorTeam(GameColor.BLUE);
				final GamePlayer playerBoss = boss;
				combatPlayerList.stream().filter(player -> player != playerBoss).forEach(team::addPlayer);
				teams.add(team);
				playersPerTeam = combatPlayerList.size() - 1;
				break;
			}
		}
		
		return true;
	}
	
	protected GamePlayer getBossPlayer() {
		if ((EnumMode) gameManager.getOptions().getOptionValue("gameMode") == EnumMode.BOSS) {
			return teams.get(0).getPlayers().iterator().next();
		}
		return null;
	}
	
	public void setupIngameTeams() {
		Scoreboard scoreboard = gameManager.getMainScoreboard();
		for (Object team : scoreboard.getTeams().toArray())
			scoreboard.removeTeam((ScorePlayerTeam) team);
		boolean teamFire = gameManager.getOptions().getBooleanOptionValue("friendlyFire");
		boolean teamColl = gameManager.getOptions().getBooleanOptionValue("teamCollision");
		for (GameTeam team : teams) {
			ScorePlayerTeam spTeam = scoreboard.createTeam(team.getTeamName());
			spTeam.setPrefix(team.getTeamColor().chatColor.toString());
			spTeam.setSuffix(TextFormatting.RESET.toString());
			spTeam.setAllowFriendlyFire(teamFire);
			spTeam.setCollisionRule(teamColl ? CollisionRule.ALWAYS : CollisionRule.HIDE_FOR_OWN_TEAM);
			gameManager.boardcastMessage(team.getColorfulTeamName() + " Members:");
			for (GamePlayer player : team.getPlayers()) {
				scoreboard.addPlayerToTeam(player.getName(), team.getTeamName());
				gameManager.boardcastMessage("    " + team.getTeamColor().chatColor + player.getName());
			}
		}
	}
	
	private void addInitialEquipments(BlockPos pos, int playerCnt) {
		World world = gameManager.getMinecraftServer().worlds[0];
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof TileEntityChest)) return;
		TileEntityChest chest = (TileEntityChest) te;
		chest.setInventorySlotContents(0, new ItemStack(Items.WOODEN_AXE));
		chest.setInventorySlotContents(1, new ItemStack(Items.WOODEN_SWORD));
		if (gameManager.getOptions().getBooleanOptionValue("greenhandProtect"))
			chest.setInventorySlotContents(2, new ItemStack(Items.GOLDEN_APPLE, playerCnt));
	}
	
	public void spreadPlayers() {
		
		class TaskInitPlayer extends TaskFindPlayer {
			private BlockPos homePos;
			private double health;
			public TaskInitPlayer(GamePlayer player, BlockPos pos, double health) {
				super(player);
				homePos = pos;
				this.health = health;
			}
			@Override
			public void onFindPlayer(EntityPlayerMP player) {
				BlockPos newpos = homePos.add(gameManager.rand.nextInt(5) - 2, 0, gameManager.rand.nextInt(5) - 2);
				player.setPositionAndUpdate(newpos.getX() + 0.5, newpos.getY() + 0.5, newpos.getZ() + 0.5);
				player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
				player.fallDistance = 0.0f;
				player.inventory.clear();
			}
		}
		
		World world = gameManager.getMinecraftServer().worlds[0];
		int borderStart = gameManager.getOptions().getIntegerOptionValue("borderStart");
		switch ((EnumMode) gameManager.getOptions().getOptionValue("gameMode")) {
			case NORMAL: {
				SpawnPosition spawnPosition = new SpawnPosition(teams.size(), borderStart);
				for (GameTeam team : teams) {
					final BlockPos pos = gameManager.buildSmallHouse(spawnPosition.nextPos(), team.getTeamColor().dyeColor);
					this.addInitialEquipments(pos, team.getPlayerCount());
					double teamHealth = 20.0 * this.playersPerTeam / team.getPlayerCount();
					team.getPlayers().forEach(player -> gameManager.addTask(new TaskInitPlayer(player, pos, teamHealth)));
				}
				break;
			}
			case SOLO:
			case BOSS: {
				SpawnPosition spawnPosition = new SpawnPosition(combatPlayerList.size(), borderStart);
				double maxHealth = 20.0 * playersPerTeam;
				for (GamePlayer player : combatPlayerList) {
					final BlockPos pos = gameManager.buildSmallHouse(spawnPosition.nextPos(), player.getTeam().getTeamColor().dyeColor);
					this.addInitialEquipments(pos, 1);
					gameManager.addTask(new TaskInitPlayer(player, pos, player.getTeam().getPlayerCount() == 1 ? maxHealth : 20));
				}
				break;
			}
		}
		
		for (GamePlayer player : observePlayerList) {
			player.getRealPlayer().ifPresent(playermp -> playermp.setGameType(GameType.SPECTATOR));
		}
	}
	
	public void refreshOnlinePlayers() {
		List<GamePlayer> toRemove = Lists.newArrayList();
		allPlayerList.stream().filter(player -> !player.getRealPlayer().isPresent()).forEach(toRemove::add);
		allPlayerList.removeAll(toRemove);
		combatPlayerList.clear();
		observePlayerList.clear();
		teams.forEach(team -> team.clearTeam());
		teams.clear();
	}

}
