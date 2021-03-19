package cn.topologycraft.uhc.task;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class TaskHUDInfo extends Task.TaskTimer
{
	private final MinecraftServer mcServer;

	public TaskHUDInfo(MinecraftServer mcServer)
	{
		super(0, 5);
		this.mcServer = mcServer;
	}

	private ITextComponent getHUDTexts(EntityPlayerMP player)
	{
		ITextComponent text = new TextComponentString("");
		double mspt = MathHelper.average(this.mcServer.tickTimeArray) * 1.0E-6D;
		double tps = 1000.0D / Math.max(mspt, 50.0D);
		text.appendSibling(new TextComponentString(String.format("TPS: %.1f MSPT: %.1f Ping: %dms", tps, mspt, player.ping)));
		return text;
	}

	@Override
	public void onTimer()
	{
		this.mcServer.getPlayerList().getPlayers().forEach(player -> {
			SPacketPlayerListHeaderFooter packet = new SPacketPlayerListHeaderFooter();
			packet.setHeader(new TextComponentString(""));
			packet.setFooter(this.getHUDTexts(player));
			player.connection.sendPacket(packet);
		});
	}
}
