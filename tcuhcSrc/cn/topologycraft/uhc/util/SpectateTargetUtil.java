package cn.topologycraft.uhc.util;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.GameTeam;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;

public class SpectateTargetUtil {
	
	public static boolean isCapableTarget(GamePlayer player, Entity target) {
		GameManager gameManager = GameManager.instance;
		if (!gameManager.isGamePlaying()) return true;
		if (gameManager.getPlayerManager().isObserver(player)) return true;
		if (!gameManager.getOptions().getBooleanOptionValue("forceViewport")) return true;
		GameTeam team = player.getTeam();
		if (team.getAliveCount() == 0) return true;
		if (target instanceof EntityArmorStand) return true;
		if (!(target instanceof EntityPlayerMP)) return false;
		if (GameManager.instance.getPlayerList().getPlayerByUUID(target.getUniqueID()) != target) return false;
		GamePlayer targetPlayer = gameManager.getPlayerManager().getGamePlayer((EntityPlayerMP) target);
		if (player == targetPlayer) return false;
		return player.getTeam() == targetPlayer.getTeam();
	}
	
	public static Entity getCapableTarget(GamePlayer player, Entity origin) {
		if (isCapableTarget(player, origin)) return origin;
		for (GamePlayer target : player.getTeam().getPlayers()) {
			if (target.isAlive() && target.getRealPlayer().isPresent()) {
				if (origin instanceof EntityArmorStand) origin.setDead();
				return target.getRealPlayer().get();
			}
		}
		for (GamePlayer target : player.getTeam().getPlayers()) {
			if (target.isAlive()) {
				EntityPlayerMP playermp = player.getRealPlayer().get();
				Vec3d pos = playermp.getPositionVector();
				EntityArmorStand armorStand = new EntityArmorStand(playermp.world);
				armorStand.setInvisible(true);
				armorStand.setEntityInvulnerable(true);
				armorStand.setNoGravity(true);
				armorStand.setPositionAndUpdate(pos.x, pos.y, pos.z);
				playermp.world.spawnEntity(armorStand);
				return armorStand;
			}
		}
		return origin;
	}

}
