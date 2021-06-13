package cn.topologycraft.uhc;

import cn.topologycraft.uhc.options.Options;
import cn.topologycraft.uhc.task.Taskable;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GamePlayer extends Taskable {
	
	private static final PlayerManager playerManager = GameManager.instance.getPlayerManager();
	
	private UUID playerUUID;
	private String playerName;
	
	protected boolean isAlive;
	private WeakReference<EntityPlayerMP> realPlayer;
	private GameTeam team;
	private Optional<GameColor> colorSelected = Optional.empty();
	
	protected int deathTime;
	private BlockPos deathPos = BlockPos.ORIGIN;
	private PlayerStatistics statistics = new PlayerStatistics();
	
	private int borderReminder;
	
	public GamePlayer(EntityPlayerMP realPlayer) {
		playerUUID = realPlayer.getUniqueID();
		playerName = realPlayer.getName();
		isAlive = true;
		this.realPlayer = new WeakReference(realPlayer);
	}
	
	public GameTeam getTeam() { return team; }
	protected void setTeam(GameTeam team) { this.team = team; }
	public int getDeathTime() { return deathTime; }
	public BlockPos getDeathPos() { return deathPos; }
	public void setColorSelected(GameColor color) { colorSelected = Optional.ofNullable(color); }
	public Optional<GameColor> getColorSelected() { return colorSelected; }
	public String getName() { return playerName; }
	public boolean isAlive() { return isAlive; }
	public PlayerStatistics getStat() { return statistics; }
	
	public boolean isSamePlayer(EntityPlayer player) {
		return player != null && playerUUID.equals(player.getUniqueID());
	}

	public boolean isKing() {
		return this.getTeam() != null && this.getTeam().getKing() == this;
	}
	
	public void setDead(int curTime) {
		if (isAlive) {
			isAlive = false;
			deathTime = curTime;
			deathPos = getRealPlayer().map(player -> player.getPosition()).orElse(BlockPos.ORIGIN);
			statistics.setStat(EnumStat.ALIVE_TIME, Options.instance.getIntegerOptionValue("gameTime") - deathTime);
		}
	}
	
	public void tick() {
		this.updateTasks();
		if (borderReminder > 0) borderReminder--;
	}
	
	public boolean borderRemindCooldown() {
		if (borderReminder == 0) {
			borderReminder = 200;
			return true;
		} else return false;
	}
	
	public Optional<EntityPlayerMP> getRealPlayer() {
		return playerManager.getPlayerByUUID(playerUUID);
	}

	public void addGhostModeEffect()
	{
		this.getRealPlayer().ifPresent(player -> player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false)));
	}

	public static enum EnumStat {
		PLAYER_KILLED("Player Killed"),
		ENTITY_KILLED("Entity Killed"),
		DAMAGE_TAKEN("Damage Taken"),
		DAMAGE_DEALT("Damage Dealt"),
		FRIENDLY_FIRE("Friendly Fire"),
		CHEST_FOUND("Chest Found"),
		EMPTY_CHEST_FOUND("Empty Chest Found"),
		DIAMOND_FOUND("Diamond Found"),
		HEALTH_HEALED("Health Healed"),
		ALIVE_TIME("Alive Time");
		
		public final String name;
		
		private EnumStat(String name) {
			this.name = name;
		}
	}
	
	public static class PlayerStatistics {
		
		private Map<EnumStat, Float> stats = Maps.newEnumMap(EnumStat.class);
		
		public PlayerStatistics() {
			clear();
		}
		
		public void clear() {
			for (EnumStat stat : EnumStat.values()) {
				stats.put(stat, 0.0f);
			}
		}
		
		public void addStat(EnumStat stat, float value) {
			if (GameManager.instance.isGamePlaying())
				stats.put(stat, stats.get(stat) + value);
		}

		public void setStat(EnumStat stat, float value) {
			if (GameManager.instance.isGamePlaying())
				stats.put(stat, value);
		}
		
		public float getFloatStat(EnumStat stat) {
			return stats.get(stat);
		}
	}

}
