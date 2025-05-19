package sakuyaayane.nekoantixray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sakuyaayane.nekoantixray.command.CommandManager;
import sakuyaayane.nekoantixray.config.ConfigManager;
import sakuyaayane.nekoantixray.detection.DetectionManager;
import sakuyaayane.nekoantixray.util.FakeOreManager;

/**
 * NekoAntiXray main class - Fabric adaptation
 */
public class NekoAntiXray implements ModInitializer {
    public static final String MOD_ID = "nekoantixray";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static NekoAntiXray instance;
    private ConfigManager configManager;
    private DetectionManager detectionManager;
    private FakeOreManager fakeOreManager;
    private CommandManager commandManager;
    
    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Initializing NekoAntiXray for Fabric");
        
        // Initialize config
        configManager = new ConfigManager();
        configManager.loadConfig();
        
        // Initialize managers
        fakeOreManager = new FakeOreManager();
        detectionManager = new DetectionManager();
        commandManager = new CommandManager();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register(commandManager::registerCommands);
        
        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("NekoAntiXray is starting up...");
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("NekoAntiXray is shutting down...");
            // Save any data if needed
        });
        
        LOGGER.info("NekoAntiXray has been initialized");
    }
    
    public static NekoAntiXray getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DetectionManager getDetectionManager() {
        return detectionManager;
    }
    
    public FakeOreManager getFakeOreManager() {
        return fakeOreManager;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
}
