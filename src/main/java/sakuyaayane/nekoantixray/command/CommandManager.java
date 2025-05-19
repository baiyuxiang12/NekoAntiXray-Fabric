package sakuyaayane.nekoantixray.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.config.ConfigManager;
import sakuyaayane.nekoantixray.util.PermissionUtil;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Manages commands for NekoAntiXray
 */
public class CommandManager {

    /**
     * Register all commands
     * @param dispatcher Command dispatcher
     * @param registryAccess Registry access
     * @param environment Registration environment
     */
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, 
                                CommandRegistryAccess registryAccess, 
                                CommandManager.RegistrationEnvironment environment) {
        // Main command: /nax
        dispatcher.register(literal("nax")
            .requires(source -> hasPermission(source, "nekoantixray.admin"))
            .then(literal("reload")
                .requires(source -> hasPermission(source, "nekoantixray.reload"))
                .executes(this::executeReload))
            .then(literal("ban")
                .requires(source -> hasPermission(source, "nekoantixray.ban"))
                .then(argument("player", StringArgumentType.word())
                    .executes(context -> executeBan(context, 30)) // Default 30 days
                    .then(argument("days", IntegerArgumentType.integer(1, 999))
                        .executes(context -> executeBan(context, IntegerArgumentType.getInteger(context, "days"))))))
            .then(literal("resetviolation")
                .requires(source -> hasPermission(source, "nekoantixray.resetviolation"))
                .then(argument("player", StringArgumentType.word())
                    .executes(this::executeResetViolation)))
            .then(literal("showhwid")
                .requires(source -> hasPermission(source, "nekoantixray.showhwid"))
                .then(argument("player", StringArgumentType.word())
                    .executes(this::executeShowHWID)))
            .then(literal("replay")
                .requires(source -> hasPermission(source, "nekoantixray.replay"))
                .then(argument("player", StringArgumentType.word())
                    .executes(this::executeReplay)))
            .executes(this::executeHelp));
        
        // Update command: /nekoantixrayupdate
        dispatcher.register(literal("nekoantixrayupdate")
            .requires(source -> hasPermission(source, "nekoantixray.update"))
            .executes(this::executeUpdate));
        
        // Fake seed command: /seed
        dispatcher.register(literal("seed")
            .executes(this::executeFakeSeed));
        
        // Fake seed reload command: /fakeseedreload
        dispatcher.register(literal("fakeseedreload")
            .requires(source -> hasPermission(source, "fakeseed.reload"))
            .executes(this::executeFakeSeedReload));
    }
    
    /**
     * Check if a command source has a permission
     * @param source Command source
     * @param permission Permission to check
     * @return true if the source has the permission
     */
    private boolean hasPermission(ServerCommandSource source, String permission) {
        // Console always has permission
        if (source.getEntity() == null) {
            return true;
        }
        
        // Check if the entity is a player
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            return PermissionUtil.hasPermission(player, permission);
        }
        
        // Default to permission level check
        return source.hasPermissionLevel(4); // OP level
    }
    
    /**
     * Execute the help command
     * @param context Command context
     * @return Command result
     */
    private int executeHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        source.sendFeedback(() -> Text.of("§6===== NekoAntiXray 命令帮助 ====="), false);
        source.sendFeedback(() -> Text.of("§e/nax reload §7- 重新加载配置"), false);
        source.sendFeedback(() -> Text.of("§e/nax ban <玩家> [天数] §7- 封禁玩家"), false);
        source.sendFeedback(() -> Text.of("§e/nax resetviolation <玩家> §7- 重置玩家违规记录"), false);
        source.sendFeedback(() -> Text.of("§e/nax showhwid <玩家> §7- 显示玩家HWID"), false);
        source.sendFeedback(() -> Text.of("§e/nax replay <玩家> §7- 回放玩家挖矿记录"), false);
        source.sendFeedback(() -> Text.of("§e/nekoantixrayupdate §7- 更新插件"), false);
        source.sendFeedback(() -> Text.of("§e/fakeseedreload §7- 重新加载假种子配置"), false);
        
        return 1;
    }
    
    /**
     * Execute the reload command
     * @param context Command context
     * @return Command result
     */
    private int executeReload(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // Reload config
        NekoAntiXray.getInstance().getConfigManager().loadConfig();
        
        source.sendFeedback(() -> Text.of("§a[NekoAntiXray] 配置已重新加载"), true);
        return 1;
    }
    
    /**
     * Execute the ban command
     * @param context Command context
     * @param days Ban duration in days
     * @return Command result
     */
    private int executeBan(CommandContext<ServerCommandSource> context, int days) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // Get player from server
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        
        if (player == null) {
            source.sendFeedback(() -> Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // Get config
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        
        // Execute ban command
        String banCommand = config.vlBanCommand
                .replace("%player%", player.getName().getString())
                .replace("%duration%", String.valueOf(days));
        
        context.getSource().getServer().getCommandManager().executeWithPrefix(
                context.getSource().getServer().getCommandSource(), banCommand);
        
        // Announce ban if enabled
        if (config.enableBanAnnouncement) {
            String announcement = config.banAnnouncement
                    .replace("%player%", player.getName().getString());
            
            // Broadcast to all players
            for (ServerPlayerEntity serverPlayer : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(Text.of(announcement), false);
            }
        }
        
        source.sendFeedback(() -> Text.of("§a[NekoAntiXray] 已封禁玩家 " + playerName + " " + days + " 天"), true);
        return 1;
    }
    
    /**
     * Execute the reset violation command
     * @param context Command context
     * @return Command result
     */
    private int executeResetViolation(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // Get player from server
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        
        if (player == null) {
            source.sendFeedback(() -> Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // Reset violation
        NekoAntiXray.getInstance().getDetectionManager().resetPlayerViolation(player.getUuid());
        
        source.sendFeedback(() -> Text.of("§a[NekoAntiXray] 已重置玩家 " + playerName + " 的违规记录"), true);
        return 1;
    }
    
    /**
     * Execute the show HWID command (placeholder implementation)
     * @param context Command context
     * @return Command result
     */
    private int executeShowHWID(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // Get player from server
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        
        if (player == null) {
            source.sendFeedback(() -> Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // In Fabric version, we don't implement HWID tracking yet
        source.sendFeedback(() -> Text.of("§e[NekoAntiXray] 玩家 " + playerName + " 的HWID功能在Fabric版本中暂未实现"), false);
        return 1;
    }
    
    /**
     * Execute the replay command (placeholder implementation)
     * @param context Command context
     * @return Command result
     */
    private int executeReplay(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // Get player from server
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        
        if (player == null) {
            source.sendFeedback(() -> Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // In Fabric version, we don't implement replay yet
        source.sendFeedback(() -> Text.of("§e[NekoAntiXray] 回放功能在Fabric版本中暂未实现"), false);
        return 1;
    }
    
    /**
     * Execute the update command (placeholder implementation)
     * @param context Command context
     * @return Command result
     */
    private int executeUpdate(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // In Fabric version, we don't implement auto-update
        source.sendFeedback(() -> Text.of("§e[NekoAntiXray] 自动更新功能在Fabric版本中暂未实现，请手动更新"), false);
        return 1;
    }
    
    /**
     * Execute the fake seed command
     * @param context Command context
     * @return Command result
     */
    private int executeFakeSeed(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        
        // Check if fake seed is enabled
        if (!config.enableFakeSeed) {
            // Just use the vanilla seed command behavior
            return 0; // Let vanilla handle it
        }
        
        // Check if player is OP and should see real seed
        if (source.getEntity() instanceof ServerPlayerEntity player && 
                player.hasPermissionLevel(4) && config.showRealSeedToOp) {
            // Show real seed to OP
            long realSeed = context.getSource().getWorld().getSeed();
            source.sendFeedback(() -> Text.of(config.messages.seedMessage.replace("%seed%", String.valueOf(realSeed))), false);
            return 1;
        }
        
        // Show fake seed
        long fakeSeed = generateFakeSeed(context.getSource().getWorld().getSeed());
        source.sendFeedback(() -> Text.of(config.messages.seedMessage.replace("%seed%", String.valueOf(fakeSeed))), false);
        return 1;
    }
    
    /**
     * Execute the fake seed reload command
     * @param context Command context
     * @return Command result
     */
    private int executeFakeSeedReload(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // Reload config
        NekoAntiXray.getInstance().getConfigManager().loadConfig();
        
        source.sendFeedback(() -> Text.of("§a[NekoAntiXray] 假种子配置已重新加载"), true);
        return 1;
    }
    
    /**
     * Generate a fake seed from a real seed
     * @param realSeed The real seed
     * @return A fake seed
     */
    private long generateFakeSeed(long realSeed) {
        // Simple algorithm to generate a different but consistent fake seed
        return realSeed ^ 0x5DEECE66DL + 11L;
    }
}
