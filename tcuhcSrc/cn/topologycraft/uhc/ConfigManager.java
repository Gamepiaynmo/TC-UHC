package cn.topologycraft.uhc;

import cn.topologycraft.uhc.options.Option;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public class ConfigManager {
	
	private GamePlayer operator;
	private boolean isConfiguring;
	private boolean isInputting;
	private Option curOption;
	
	public void startConfiguring(GamePlayer op) {
		operator = op;
		isConfiguring = true;
	}
	
	public void stopConfiguring() {
		isConfiguring = false;
	}
	
	public boolean isConfiguring() {
		return isConfiguring;
	}
	
	public boolean isOperator(EntityPlayerMP player) {
		return operator.isSamePlayer(player);
	}
	
	public GamePlayer getOperator() {
		return operator;
	}
	
	public void inputOptionValue(Option option) {
		isInputting = true;
		curOption = option;
	}

	public boolean onPlayerChat(EntityPlayerMP player, String msg) {
		if (isConfiguring && operator.isSamePlayer(player) && isInputting) {
			curOption.setStringValue(msg);
			GameManager.instance.getPlayerManager().refreshConfigBook();
			player.sendMessage(new TextComponentString("Set " + curOption.getName() + " to " + curOption.getStringValue()));
			isInputting = false;
			return false;
		}
		return true;
	}

}
