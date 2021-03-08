package cn.topologycraft.uhc.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import cn.topologycraft.uhc.GameManager;
import cn.topologycraft.uhc.GameManager.EnumMode;
import cn.topologycraft.uhc.GamePlayer;
import cn.topologycraft.uhc.task.Task;
import net.minecraft.world.EnumDifficulty;

public class Options {
	private static final Logger LOGGER = LogManager.getLogger();
	public static Options instance;
	
	private Map<String, Option> configOptions = Maps.newHashMap();
	private Properties uhcProperties = new Properties();
	private File uhcOptionsFile;
	private final GameManager gameManager = GameManager.instance;
	
	public final Task taskSaveProperties = new Task() {
		@Override
		public void onUpdate() {
			Options.this.savePropertiesFile();
		}
		@Override
		public boolean hasFinished() { return false; }
	};
	
	public final Task taskReselectTeam = new Task() {
		@Override
		public void onUpdate() {
			for (GamePlayer player : gameManager.getPlayerManager().getAllPlayers()) {
				player.setColorSelected(null);
				player.getRealPlayer().ifPresent(playermp -> {
					gameManager.getPlayerManager().regiveConfigItems(playermp);
					playermp.setEntityInvulnerable(true);
				});
			}
		}
		@Override
		public boolean hasFinished() { return false; }
	};
	
	public Options(File optionsFile) {
		instance = this;
		uhcOptionsFile = optionsFile;
		
		addOption(new Option("teamCount", "Team Count", new OptionType.IntegerType(2, 8, 1), 4).addTask(taskReselectTeam).setDescription("Count of different teams, only works on normal mode."));
		addOption(new Option("gameMode", "Game Mode", new OptionType.EnumType(EnumMode.class), EnumMode.NORMAL).addTask(taskReselectTeam).setDescription("UHC Game Mode, normal for original rules, solo for one player one team, boss for hungryartist_."));
		addOption(new Option("difficulty", "Difficulty", new OptionType.EnumType(EnumDifficulty.class), EnumDifficulty.HARD).setDescription("Difficulty of game"));
		addOption(new Option("borderStart", "Border Start", new OptionType.IntegerType(100, 2000000, 100), 2000).setNeedToSave().setDescription("The initial size of world border."));
		addOption(new Option("borderEnd", "Border End", new OptionType.IntegerType(10, 2000000, 10), 200).setNeedToSave().setDescription("The end size of world border."));
		addOption(new Option("borderFinal", "Border Final", new OptionType.IntegerType(10, 2000000, 10), 50).setNeedToSave().setDescription("The final size of world border."));
		addOption(new Option("borderStartTime", "Start Time", new OptionType.IntegerType(0, 1000000, 100), 1000).setNeedToSave().setDescription("The time that border starts to shrink."));
		addOption(new Option("borderEndTime", "End Time", new OptionType.IntegerType(0, 1000000, 100), 3000).setNeedToSave().setDescription("The time that border stops shrinking."));
		addOption(new Option("gameTime", "Game Time", new OptionType.IntegerType(0, 1000000, 100), 4000).setNeedToSave().setDescription("The total time of the game."));
		addOption(new Option("netherCloseTime", "Nether Time", new OptionType.IntegerType(0, 1000000, 100), 3000).setNeedToSave().setDescription("When does the nether and the end become forbidden."));
		addOption(new Option("caveCloseTime", "Cave Time", new OptionType.IntegerType(0, 1000000, 100), 3700).setNeedToSave().setDescription("When does the caves become forbidden."));
		addOption(new Option("randomTeams", "Random Teams", new OptionType.BooleanType(), true).addTask(taskReselectTeam).setDescription("Form teams randomly or manually, doesn't work on solo mode."));
		addOption(new Option("daylightCycle", "Have Nights", new OptionType.BooleanType(), true).setDescription("Gamerule doDaylightCycle."));
		addOption(new Option("friendlyFire", "Team Fire", new OptionType.BooleanType(), false).setDescription("Can team members attack each other."));
		addOption(new Option("teamCollision", "Team Coll", new OptionType.BooleanType(), true).setDescription("Can team members collide with each other."));
		addOption(new Option("greenhandProtect", "Greenhand", new OptionType.BooleanType(), true).setNeedToSave().setDescription("Reduce damage in the first few minutes."));
		addOption(new Option("greenhandTime", "GH Time", new OptionType.IntegerType(0, 1000000, 100), 3000).setNeedToSave().setDescription("Length of greenhand protection."));
		addOption(new Option("forceViewport", "Force View", new OptionType.BooleanType(), true).setDescription("Force viewport on team members after death."));
		addOption(new Option("deathBonus", "Death Bonus", new OptionType.BooleanType(), true).setDescription("Few potion effects on other members after death."));
		addOption(new Option("merchantFrequency", "Merchants", new OptionType.FloatType(0.0f, 1.0f, 0.05f), 1.0f).setNeedToSave().setDescription("Frequency that merchants appears."));
		addOption(new Option("oreFrequency", "Ores", new OptionType.IntegerType(0, 100, 1), 4).setNeedToSave().setDescription("Frequency of variable ores include diamonds, lapis and gold."));
		addOption(new Option("chestFrequency", "Bonus Chests", new OptionType.FloatType(0.0f, 10.0f, 0.1f), 1.0f).setNeedToSave().setDescription("Frequency of bonus chests."));
		addOption(new Option("trappedChestFrequency", "Empty Chests", new OptionType.FloatType(0.0f, 1.0f, 0.05f), 0.2f).setNeedToSave().setDescription("Frequency of empty bonus chests."));
		addOption(new Option("chestItemFrequency", "Chest Loots", new OptionType.FloatType(0.0f, 10.0f, 0.1f), 1.0f).setNeedToSave().setDescription("Frequency of variable items in bonus chests."));
		addOption(new Option("mobCount", "Mob Count", new OptionType.IntegerType(10, 300, 10), 70).setNeedToSave().setDescription("Adjust number of monsters in the world."));

		loadPropertiesFile();
		savePropertiesFile();
	}
	
	public void loadPropertiesFile() {
		if (uhcOptionsFile.exists()) {
			try (FileInputStream input = new FileInputStream(uhcOptionsFile)) {
				uhcProperties.load(input);
			} catch (Exception e) {
				LOGGER.warn("Failed to load {}", uhcOptionsFile, e);
			}
		} else {
			LOGGER.warn("{} does not exist", uhcOptionsFile);
		}

		for (Entry<Object, Object> entry : uhcProperties.entrySet()) {
			configOptions.get(entry.getKey()).setInitialValue((String) entry.getValue());
		}
	}
	
	public void savePropertiesFile() {
		try (FileOutputStream output = new FileOutputStream(uhcOptionsFile)) {
			configOptions.values().stream().filter(opt -> opt.needToSave()).forEach(opt -> uhcProperties.setProperty(opt.getId(), opt.getStringValue()));
			uhcProperties.store(output, "UHC Game Properties");
		} catch (Exception e) {
			LOGGER.warn("Failed to save {}", this.uhcOptionsFile, e);
		}
	}
	
	private void addOption(Option option) {
		configOptions.put(option.getId(), option);
	}
	
	public Optional<Option> getOption(String option) {
		return Optional.ofNullable(configOptions.get(option));
	}
	
	public void setOptionValue(String option, Object value) {
		getOption(option).ifPresent(opt -> {
			opt.setValue(value);
		});
	}
	
	public void incOptionValue(String option) {
		getOption(option).ifPresent(opt -> {
			opt.incValue();
		});
	}
	
	public void decOptionValue(String option) {
		getOption(option).ifPresent(opt -> {
			opt.decValue();
		});
	}
	
	public Object getOptionValue(String option) {
		return getOption(option).map(opt -> opt.getValue()).orElse(null);
	}
	
	public int getIntegerOptionValue(String option) {
		return (int) getOptionValue(option);
	}
	
	public float getFloatOptionValue(String option) {
		return (float) getOptionValue(option);
	}
	
	public String getStringOptionValue(String option) {
		return (String) getOptionValue(option);
	}
	
	public boolean getBooleanOptionValue(String option) {
		return (boolean) getOptionValue(option);
	}

}
