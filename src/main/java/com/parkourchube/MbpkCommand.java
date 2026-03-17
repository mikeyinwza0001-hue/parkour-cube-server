package com.parkourchube;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MbpkCommand implements CommandExecutor, TabCompleter {

    private final CheckpointCommands cpCmds;
    private final FunCommands funCmds;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "back", "cp", "restart",
            "goto60", "trapweb", "fly", "tnt"
    );

    public MbpkCommand(CheckpointCommands cpCmds, FunCommands funCmds) {
        this.cpCmds = cpCmds;
        this.funCmds = funCmds;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6[ParkourCube] §7Available subcommands:");
            sender.sendMessage("§e /mbpk cp §7- Teleport to current checkpoint");
            sender.sendMessage("§e /mbpk back [number] §7- Go back to a previous checkpoint");
            sender.sendMessage("§e /mbpk restart §7- Reset to checkpoint 0");
            sender.sendMessage("§e /mbpk fly <seconds> §7- Temporary flight");
            sender.sendMessage("§e /mbpk tnt <amount> §7- TNT launch");
            sender.sendMessage("§e /mbpk trapweb §7- Trap players in cobwebs");
            sender.sendMessage("§e /mbpk goto60 §7- Dragon ride to CP 60");
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        return switch (sub) {
            // Checkpoint commands
            case "registercp" -> cpCmds.registerCp(sender, subArgs);
            case "setfinalcp" -> cpCmds.setFinalCp(sender, subArgs);
            case "back" -> cpCmds.back(subArgs);
            case "cp" -> cpCmds.cp();
            case "restart" -> cpCmds.restart();
            case "exportcp" -> cpCmds.exportCp(sender);
            // Fun commands
            case "goto60" -> funCmds.goto60();
            case "trapweb" -> funCmds.trapWeb();
            case "fly" -> funCmds.fly(subArgs);
            case "tnt" -> funCmds.tntz(sender, subArgs);
            case "chickenride" -> funCmds.chickenRide();
            default -> {
                sender.sendMessage("§c[ParkourCube] Unknown subcommand: " + sub);
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
