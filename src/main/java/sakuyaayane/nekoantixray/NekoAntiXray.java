package sakuyaayane.nekoantixray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sakuyaayane.nekoantixray.command.NekoCommandManager;
import sakuyaayane.nekoantixray.config.ConfigManager;
import sakuyaayane.nekoantixray.detection.DetectionManager;
import sakuyaayane.nekoantixray.util.FakeOreManager;

/**
 * NekoAntiXray Fabric版本主类
 */
public class NekoAntiXray implements ModInitializer {
    // 创建日志记录器
    public static final Logger LOGGER = LoggerFactory.getLogger("nekoantixray");
    
    // 实例
    private static NekoAntiXray instance;
    
    // 管理器
    private ConfigManager configManager;
    private DetectionManager detectionManager;
    private FakeOreManager fakeOreManager;
    private NekoCommandManager commandManager;
    
    // 服务器实例
    private MinecraftServer server;

    @Override
    public void onInitialize() {
        // 设置实例
        instance = this;
        
        // 初始化日志
        LOGGER.info("NekoAntiXray Fabric版本正在初始化...");
        
        // 初始化配置
        configManager = new ConfigManager();
        
        // 初始化管理器
        detectionManager = new DetectionManager(this);
        fakeOreManager = new FakeOreManager(this);
        commandManager = new NekoCommandManager(this, configManager, detectionManager);
        
        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            commandManager.registerCommands(dispatcher);
        });
        
        // 注册服务器启动事件
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            LOGGER.info("NekoAntiXray已连接到服务器");
        });
        
        // 注册服务器关闭事件
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("NekoAntiXray正在关闭...");
        });
        
        LOGGER.info("NekoAntiXray Fabric版本初始化完成!");
    }
    
    /**
     * 获取实例
     */
    public static NekoAntiXray getInstance() {
        return instance;
    }
    
    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取检测管理器
     */
    public DetectionManager getDetectionManager() {
        return detectionManager;
    }
    
    /**
     * 获取假矿石管理器
     */
    public FakeOreManager getFakeOreManager() {
        return fakeOreManager;
    }
    
    /**
     * 获取命令管理器
     */
    public NekoCommandManager getCommandManager() {
        return commandManager;
    }
    
    /**
     * 获取服务器实例
     */
    public MinecraftServer getServer() {
        return server;
    }
}
