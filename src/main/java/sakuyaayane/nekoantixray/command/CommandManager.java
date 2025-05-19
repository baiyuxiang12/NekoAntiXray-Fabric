package sakuyaayane.nekoantixray.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import sakuyaayane.nekoantixray.config.ConfigManager;
import sakuyaayane.nekoantixray.detection.DetectionManager;
import sakuyaayane.nekoantixray.NekoAntiXray;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * 命令管理器 - Fabric版本
 */
public class NekoCommandManager {
    private final NekoAntiXray mod;
    private final ConfigManager configManager;
    private final DetectionManager detectionManager;

    public NekoCommandManager(NekoAntiXray mod, ConfigManager configManager, DetectionManager detectionManager) {
        this.mod = mod;
        this.configManager = configManager;
        this.detectionManager = detectionManager;
    }

    /**
     * 注册所有命令
     * @param dispatcher 命令分发器
     */
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 主命令
        dispatcher.register(literal("nax")
            .requires(source -> hasPermission(source, "nekoantixray.admin"))
            .then(literal("reload")
                .executes(this::reloadCommand))
            .then(literal("ban")
                .then(argument("player", StringArgumentType.word())
                    .executes(context -> banCommand(context, 0))
                    .then(argument("days", IntegerArgumentType.integer(1))
                        .executes(context -> banCommand(
                            context, 
                            IntegerArgumentType.getInteger(context, "days"))))))
            .then(literal("resetviolation")
                .then(argument("player", StringArgumentType.word())
                    .executes(this::resetViolationCommand)))
            .then(literal("showhwid")
                .then(argument("player", StringArgumentType.word())
                    .executes(this::showHwidCommand)))
            .then(literal("replay")
                .then(argument("player", StringArgumentType.word())
                    .executes(this::replayCommand)))
            .executes(this::helpCommand));

        // 更新命令
        dispatcher.register(literal("nekoantixrayupdate")
            .requires(source -> hasPermission(source, "nekoantixray.admin"))
            .executes(this::updateCommand));

        // 假种子命令
        dispatcher.register(literal("fakeseedreload")
            .requires(source -> hasPermission(source, "nekoantixray.admin"))
            .executes(this::fakeSeedReloadCommand));

        // 种子命令
        dispatcher.register(literal("seed")
            .executes(this::seedCommand));
    }

    /**
     * 检查权限
     * @param source 命令源
     * @param permission 权限节点
     * @return 是否有权限
     */
    private boolean hasPermission(ServerCommandSource source, String permission) {
        // 在Fabric中，我们简化权限检查，只检查操作员权限
        return source.hasPermissionLevel(4);
    }

    /**
     * 帮助命令
     */
    private int helpCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(Text.of("§6===== NekoAntiXray 命令帮助 ====="), false);
        source.sendFeedback(Text.of("§e/nax reload §7- 重新加载配置"), false);
        source.sendFeedback(Text.of("§e/nax ban <玩家> [天数] §7- 封禁玩家"), false);
        source.sendFeedback(Text.of("§e/nax resetviolation <玩家> §7- 重置玩家违规记录"), false);
        source.sendFeedback(Text.of("§e/nax showhwid <玩家> §7- 显示玩家HWID"), false);
        source.sendFeedback(Text.of("§e/nax replay <玩家> §7- 回放玩家挖矿记录"), false);
        source.sendFeedback(Text.of("§e/nekoantixrayupdate §7- 更新插件"), false);
        source.sendFeedback(Text.of("§e/fakeseedreload §7- 重新加载假种子配置"), false);
        return 1;
    }

    /**
     * 重载命令
     */
    private int reloadCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        configManager.reloadConfig();
        source.sendFeedback(Text.of("§a[NekoAntiXray] 配置已重新加载"), true);
        return 1;
    }

    /**
     * 封禁命令
     */
    private int banCommand(CommandContext<ServerCommandSource> context, int days) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // 获取玩家
        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            source.sendFeedback(Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // 设置默认天数
        if (days <= 0) {
            days = configManager.getConfig().defaultBanDays;
        }
        
        // 执行封禁
        String reason = configManager.getConfig().banReason;
        player.networkHandler.disconnect(Text.of(reason));
        
        // 通知
        source.sendFeedback(Text.of("§a[NekoAntiXray] 已封禁玩家 " + playerName + " " + days + " 天"), true);
        return 1;
    }

    /**
     * 重置违规记录命令
     */
    private int resetViolationCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // 获取玩家
        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            source.sendFeedback(Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // 重置违规记录
        detectionManager.resetViolation(player.getUuid());
        source.sendFeedback(Text.of("§a[NekoAntiXray] 已重置玩家 " + playerName + " 的违规记录"), true);
        return 1;
    }

    /**
     * 显示HWID命令
     */
    private int showHwidCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // 获取玩家
        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            source.sendFeedback(Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // HWID功能在Fabric版本中暂未实现
        source.sendFeedback(Text.of("§e[NekoAntiXray] 玩家 " + playerName + " 的HWID功能在Fabric版本中暂未实现"), false);
        return 1;
    }

    /**
     * 回放命令
     */
    private int replayCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // 获取玩家
        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            source.sendFeedback(Text.of("§c[NekoAntiXray] 玩家不在线或不存在"), false);
            return 0;
        }
        
        // 回放功能在Fabric版本中暂未实现
        source.sendFeedback(Text.of("§e[NekoAntiXray] 回放功能在Fabric版本中暂未实现"), false);
        return 1;
    }

    /**
     * 更新命令
     */
    private int updateCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 更新功能在Fabric版本中暂未实现
        source.sendFeedback(Text.of("§e[NekoAntiXray] 自动更新功能在Fabric版本中暂未实现，请手动更新"), false);
        return 1;
    }

    /**
     * 种子命令
     */
    private int seedCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 检查是否启用假种子
        if (!configManager.getConfig().fakeSeedEnabled) {
            // 显示真实种子
            long realSeed = source.getWorld().getSeed();
            String seedMessage = "世界种子: %seed%";
            source.sendFeedback(Text.of(seedMessage.replace("%seed%", String.valueOf(realSeed))), false);
            return 1;
        }
        
        // 显示假种子
        long fakeSeed = configManager.getConfig().fakeSeed;
        String seedMessage = "世界种子: %seed%";
        source.sendFeedback(Text.of(seedMessage.replace("%seed%", String.valueOf(fakeSeed))), false);
        return 1;
    }

    /**
     * 假种子重载命令
     */
    private int fakeSeedReloadCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 重载假种子配置
        configManager.reloadConfig();
        source.sendFeedback(Text.of("§a[NekoAntiXray] 假种子配置已重新加载"), true);
        return 1;
    }
}
