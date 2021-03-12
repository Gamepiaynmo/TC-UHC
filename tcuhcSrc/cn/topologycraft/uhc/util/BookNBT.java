package cn.topologycraft.uhc.util;

import cn.topologycraft.uhc.GameColor;
import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.GameTeam;
import cn.topologycraft.uhc.options.Option;
import cn.topologycraft.uhc.options.Options;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.Optional;

public class BookNBT {
	
	public static NBTTagList appendPageText(NBTTagList nbt, ITextComponent text) {
		nbt.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(text)));
		return nbt;
	}
	
	public static ITextComponent createTextEvent(String text, String cmd, String hover, TextFormatting color) {
		ITextComponent res = new TextComponentString(text);
		if (color != null) res.getStyle().setColor(color);
		if (cmd != null) res.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
		if (hover != null) res.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hover)));
		return res;
	}
	
	public static ITextComponent createOptionText(Optional<Option> opt) {
		return opt.map(option -> createTextEvent(option.getName(), null, option.getDescription(), TextFormatting.BLUE)
				.appendSibling(createTextEvent(" < ", "/uhc option sub " + option.getId(), option.getDecString(), TextFormatting.RED))
				.appendSibling(createTextEvent(option.getStringValue(), "/uhc option set " + option.getId(), "Click to input value", TextFormatting.GOLD))
				.appendSibling(createTextEvent(" >", "/uhc option add " + option.getId(), option.getIncString(), TextFormatting.GREEN))
				.appendText("\n")).orElse(new TextComponentString("Unknown Option"));
	}
	
	public static ItemStack createWrittenBook(String author, String title, NBTBase pages) {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		book.setTagInfo("author", new NBTTagString(author));
		book.setTagInfo("title", new NBTTagString(title));
		book.setTagInfo("pages", pages);
		return book;
	}
	
	public static ITextComponent createReturn() {
		return new TextComponentString("\n");
	}
	
	public static ItemStack getConfigBook(GameManager gameManager) {
		Options options = gameManager.getOptions();
		NBTTagList pages = new NBTTagList();
		
		appendPageText(pages, new TextComponentString("General Settings\n\n")
				.appendSibling(createOptionText(options.getOption("gameMode")))
				.appendSibling(createOptionText(options.getOption("randomTeams")))
				.appendSibling(createOptionText(options.getOption("teamCount")))
				.appendSibling(createReturn())
				.appendSibling(createOptionText(options.getOption("difficulty")))
				.appendSibling(createOptionText(options.getOption("daylightCycle")))
				.appendSibling(createOptionText(options.getOption("friendlyFire")))
				.appendSibling(createOptionText(options.getOption("teamCollision")))
				.appendSibling(createOptionText(options.getOption("greenhandProtect")))
				.appendSibling(createOptionText(options.getOption("forceViewport")))
				.appendSibling(createOptionText(options.getOption("deathBonus")))
				);
		
		appendPageText(pages, new TextComponentString("Time Settings\n\n")
				.appendSibling(createOptionText(options.getOption("borderStart")))
				.appendSibling(createOptionText(options.getOption("borderEnd")))
				.appendSibling(createOptionText(options.getOption("borderFinal")))
				.appendSibling(createReturn())
				.appendSibling(createOptionText(options.getOption("gameTime")))
				.appendSibling(createOptionText(options.getOption("borderStartTime")))
				.appendSibling(createOptionText(options.getOption("borderEndTime")))
				.appendSibling(createOptionText(options.getOption("netherCloseTime")))
				.appendSibling(createOptionText(options.getOption("caveCloseTime")))
				.appendSibling(createOptionText(options.getOption("greenhandTime")))
				);
		
		appendPageText(pages, new TextComponentString("World Settings\n\n")
				.appendSibling(createOptionText(options.getOption("merchantFrequency")))
				.appendSibling(createOptionText(options.getOption("oreFrequency")))
				.appendSibling(createOptionText(options.getOption("chestFrequency")))
				.appendSibling(createOptionText(options.getOption("trappedChestFrequency")))
				.appendSibling(createOptionText(options.getOption("chestItemFrequency")))
				.appendSibling(createOptionText(options.getOption("mobCount")))
				.appendSibling(createReturn())
				.appendSibling(createTextEvent("     Reset Gameplay\n", "/uhc reset 0", "Reset Gameplay Settings", TextFormatting.GOLD))
				.appendSibling(createTextEvent("    Reset Generation\n", "/uhc reset 1", "Reset Generation Settings", TextFormatting.GOLD))
				.appendSibling(createTextEvent("       Regenerate\n", "/uhc regen", "Regenerate Terrain", TextFormatting.LIGHT_PURPLE))
				.appendSibling(createTextEvent("          Start !\n", "/uhc start", "Start the UHC game !", TextFormatting.LIGHT_PURPLE))
				);
		
		return createWrittenBook("sbGP", "UHC Game Configuration", pages);
	}
	
	public static ItemStack getPlayerBook(GameManager gameManager) {
		Options options = gameManager.getOptions();
		int teamCount = options.getIntegerOptionValue("teamCount");
		boolean randomTeams = options.getBooleanOptionValue("randomTeams");
		ITextComponent text = new TextComponentString("Select Teams\n\n");
		String line = "***********************\n";
		text.appendSibling(createTextEvent(line, "/uhc select 8", "Select to observe", TextFormatting.GRAY));
		if (randomTeams)
			text.appendSibling(createTextEvent(line, "/uhc select 9", "Select to fight", TextFormatting.BLACK));
		else {
			switch ((GameManager.EnumMode) options.getOptionValue("gameMode")) {
				case NORMAL: {
					text.appendSibling(createTextEvent(line, "/uhc select 9", "Select to join random team", TextFormatting.BLACK));
					for (int i = 0; i < teamCount; i++) {
						GameColor color = GameColor.getColor(i);
						text.appendSibling(createTextEvent(line, "/uhc select " + color.getId(), "Select to join " + color.dyeColor, color.chatColor));
					}
					break;
				}
				case SOLO: 
					text.appendSibling(createTextEvent(line, "/uhc select 9", "Select to fight", TextFormatting.BLACK));
					break;
				case BOSS: {
					text.appendSibling(createTextEvent(line, "/uhc select 0", "Select to become a bully", TextFormatting.RED));
					text.appendSibling(createTextEvent(line, "/uhc select 1", "Select to become a vegetable chicken", TextFormatting.BLUE));
				}
			}
		}
		NBTTagList pages = new NBTTagList();
		appendPageText(pages, text);
		
		return createWrittenBook("sbGP", "UHC Team Selection", pages);
	}
	
	public static ITextComponent createPlayerText(GamePlayer player) {
		ITextComponent text = createTextEvent(player.getName(), null, player.getName(), player.getTeam().getTeamColor().chatColor);
		if (player.isAlive())
			text.appendSibling(createTextEvent(" alive\n", "/uhc adjust kill " + player.getName(), "Click to kill " + player.getName(), TextFormatting.DARK_GREEN));
		else text.appendSibling(createTextEvent(" dead\n", "/uhc adjust resu " + player.getName(), "Click to resurrent " + player.getName(), TextFormatting.DARK_RED));
		return text;
	}
	
	public static ItemStack getAdjustBook(GameManager gameManager) {
		Options options = gameManager.getOptions();
		NBTTagList pages = new NBTTagList();
		
		switch ((GameManager.EnumMode) options.getOptionValue("gameMode")) {
			case BOSS:
			case NORMAL: {
				for (GameTeam team : gameManager.getPlayerManager().getTeams()) {
					ITextComponent text = new TextComponentString(team.getColorfulTeamName() + "\n\n");
					for (GamePlayer player : team.getPlayers()) {
						text.appendSibling(createPlayerText(player));
					}
					appendPageText(pages, text);
				}
				break;
			}
			case SOLO: {
				ITextComponent text = new TextComponentString(TextFormatting.LIGHT_PURPLE + "All Players\n\n");
				for (GamePlayer player : gameManager.getPlayerManager().getCombatPlayers()) {
					text.appendSibling(createPlayerText(player));
				}
				appendPageText(pages, text);
			}
		}
		
		ITextComponent text = new TextComponentString("End\n\n");
		text.appendSibling(createTextEvent("Stop Adjusting", "/uhc adjust end", "Click to remove this book", TextFormatting.LIGHT_PURPLE));
		appendPageText(pages, text);
		return createWrittenBook("sbGP", "UHC Game Adjustion", pages);
	}

}
