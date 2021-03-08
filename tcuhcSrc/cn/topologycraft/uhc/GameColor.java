package cn.topologycraft.uhc;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.text.TextFormatting;

public enum GameColor {
	
	RED(EnumDyeColor.RED, TextFormatting.RED, "Red", 0),
	BLUE(EnumDyeColor.BLUE, TextFormatting.BLUE, "Blue", 1),
	YELLOW(EnumDyeColor.YELLOW, TextFormatting.YELLOW, "Yellow", 2),
	GREEN(EnumDyeColor.LIME, TextFormatting.GREEN, "Green", 3),
	ORANGE(EnumDyeColor.ORANGE, TextFormatting.GOLD, "Orange", 4),
	PURPLE(EnumDyeColor.PURPLE, TextFormatting.LIGHT_PURPLE, "Purple", 5),
	CYAN(EnumDyeColor.CYAN, TextFormatting.DARK_BLUE, "Cyan", 6),
	BROWN(EnumDyeColor.BROWN, TextFormatting.DARK_RED, "Brown", 7),
	WHITE(EnumDyeColor.WHITE, TextFormatting.WHITE, "White", 8),
	BLACK(EnumDyeColor.BLACK, TextFormatting.BLACK, "Black", 9);
	
	public static final int MAX_TEAM_COLORS = 8;
	private static int rand = 0;
	
	public final EnumDyeColor dyeColor;
	public final TextFormatting chatColor;
	public final String name;
	private final int id;
	
	private GameColor(EnumDyeColor dye, TextFormatting chat, String name, int id) {
		dyeColor = dye;
		chatColor = chat;
		this.name = name;
		this.id = id;
	}
	
	public static GameColor randomColor() {
		return values()[(rand++) % MAX_TEAM_COLORS];
	}
	
	public int getMeta() {
		return dyeColor.getMetadata();
	}
	
	public int getId() {
		return id;
	}
	
	public static GameColor getColor(int id) {
		return id < 10 ? GameColor.values()[id] : null;
	}

}
