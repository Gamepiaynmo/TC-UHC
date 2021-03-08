package cn.topologycraft.uhc;

import cn.topologycraft.uhc.options.Options;
import cn.topologycraft.uhc.task.TaskOnce;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

public class GameCommand extends CommandBase {

	@Override
	public String getName() {
		return "uhc";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/uhc <command> [param]";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	private EntityPlayerMP assertConsoleSender(ICommandSender sender) throws WrongUsageException {
		if (!(sender instanceof EntityPlayerMP))
			throw new WrongUsageException("This command cannot be passed by console.");
		return (EntityPlayerMP) sender;
	}
	
	private void assertSenderPermission(ICommandSender sender) throws CommandException {
		if (!sender.canUseCommand(3, getName()))
			throw new CommandException("commands.generic.permission");
	}
	
	private void sendVersionInfo(ICommandSender sender) {
		sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "== UHC Plugin for " + TextFormatting.RED + "T" + TextFormatting.BLUE + "opology" + TextFormatting.RED + "C" + TextFormatting.BLUE + "raft" + TextFormatting.GOLD + " =="));
		sender.sendMessage(new TextComponentString("          " + TextFormatting.GREEN + "Plugin Version " + TextFormatting.GOLD + "1.4.5"));
		sender.sendMessage(new TextComponentString("     " + TextFormatting.GREEN + "Minecraft Version " + TextFormatting.GOLD + "1.12-pre7"));
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, final String[] args) throws CommandException {
		GameManager gameManager = GameManager.instance;
		try {
			if (args.length < 1) {
				sendVersionInfo(sender);
				return;
			}
			switch (args[0]) {
				case "version": {
					sendVersionInfo(sender);
					break;
				}
				case "config": {
					assertSenderPermission(sender);
					gameManager.startConfiguration(assertConsoleSender(sender));
					gameManager.getOptions().savePropertiesFile();
					gameManager.addTask(new TaskOnce(Options.instance.taskReselectTeam));
					break;
				}
				case "regen": {
					assertSenderPermission(sender);
					GameManager.regenerateTerrain();
					break;
				}
				case "start": {
					assertSenderPermission(sender);
					gameManager.startGame(assertConsoleSender(sender));
					break;
				}
				case "stop": {
					assertSenderPermission(sender);
					gameManager.endGame();
					break;
				}
				case "option": {
					assertSenderPermission(sender);
					gameManager.getOptions().getOption(args[2]).ifPresent(opt -> {
						if ("sub".equals(args[1])) opt.decValue();
						if ("add".equals(args[1])) opt.incValue();
						if ("set".equals(args[1])) {
							gameManager.getConfigManager().inputOptionValue(opt);
							notifyCommandListener(sender, this, "Input the value for %s:", opt.getName());
						}
						gameManager.getPlayerManager().refreshConfigBook();
					});
					break;
				}
				case "select": {
					GameColor color = GameColor.getColor(Integer.parseInt(args[1]));
					EntityPlayerMP player = assertConsoleSender(sender);
					gameManager.getPlayerManager().getGamePlayer(player).setColorSelected(color);
					gameManager.getPlayerManager().regiveConfigItems(player);
					break;
				}
				case "adjust": {
					assertSenderPermission(sender);
					EntityPlayerMP player = assertConsoleSender(sender);
					if (gameManager.isGamePlaying()) {
						if (args.length == 1) {
							gameManager.getPlayerManager().regiveAdjustBook(player, true);
						} else if (args.length == 2 && args[1].equals("end")) {
							gameManager.getPlayerManager().removeAdjustBook(player);
						} else if (args.length == 3) {
							if (args[1].equals("kill")) {
								gameManager.getPlayerManager().killPlayer(args[2]);
							} else if (args[1].equals("resu")) {
								gameManager.getPlayerManager().resurrentPlayer(args[2]);
							}
							gameManager.getPlayerManager().regiveAdjustBook(player, false);
						}
					}
					break;
				}
				case "deathpos": {
					EntityPlayerMP player = assertConsoleSender(sender);
					GamePlayer gamePlayer = gameManager.getPlayerManager().getGamePlayer(player);
					BlockPos pos = gamePlayer.getDeathPos();
					if (pos.equals(BlockPos.ORIGIN)) {
						sender.sendMessage(new TextComponentString("You are still alive."));
					} else {
						sender.sendMessage(new TextComponentString(String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ())));
					}
				}
				default:
					throw new CommandException(TextFormatting.RED + "Unknown command %s.", args[0]);
			}
		} catch (Exception e) {
			if (e instanceof CommandException)
				throw e;
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		try {
			assertSenderPermission(sender);
			return getListOfStringsMatchingLastWord(args, new String[] { "version", "config", "regen", "start", "stop", "adjust", "deathpos" });
		} catch (CommandException e) {
			return getListOfStringsMatchingLastWord(args, new String[] { "version", "deathpos" });
		}
	}

}
