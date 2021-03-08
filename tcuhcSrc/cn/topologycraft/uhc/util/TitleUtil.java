package cn.topologycraft.uhc.util;

import cn.topologycraft.uhc.GameManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.text.TextComponentString;

public class TitleUtil {

	public static void sendTitleToPlayer(String title, String subtitle, EntityPlayerMP player) {
		SPacketTitle titlePacket = new SPacketTitle(SPacketTitle.Type.TITLE, new TextComponentString(title));
		player.connection.sendPacket(titlePacket);
		if (subtitle != null) {
			SPacketTitle subtitlePacket = new SPacketTitle(SPacketTitle.Type.SUBTITLE, new TextComponentString(subtitle));
			player.connection.sendPacket(subtitlePacket);
		}
	}

	public static void sendTitleToAllPlayers(String title, String subtitle) {
		for (EntityPlayerMP player : GameManager.instance.getPlayerList().getPlayers())
			sendTitleToPlayer(title, subtitle, player);
	}

}
