package sakuyaayane.nekoantixray.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import sakuyaayane.nekoantixray.NekoAntiXray;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages configuration for NekoAntiXray
 */
public class ConfigManager {
    private final Path configDir;
    private final File configFile;
    private final Gson gson;
    
    private ConfigData configData;
    
    public ConfigManager() {
        this.configDir = FabricLoader.getInstance().getConfigDir().resolve(NekoAntiXray.MOD_ID);
        this.configFile = configDir.resolve("config.json").toFile();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.configData = new ConfigData();
    }
    
    public void loadConfig() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    configData = gson.fromJson(reader, ConfigData.class);
                    NekoAntiXray.LOGGER.info("Configuration loaded successfully");
                }
            } else {
                // Create default config
                saveConfig();
                NekoAntiXray.LOGGER.info("Created default configuration");
            }
        } catch (IOException e) {
            NekoAntiXray.LOGGER.error("Failed to load configuration", e);
        }
    }
    
    public void saveConfig() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(configData, writer);
                NekoAntiXray.LOGGER.info("Configuration saved successfully");
            }
        } catch (IOException e) {
            NekoAntiXray.LOGGER.error("Failed to save configuration", e);
        }
    }
    
    public ConfigData getConfig() {
        return configData;
    }
    
    /**
     * Attempts to migrate from Bukkit YAML config to Fabric JSON config
     * @param yamlConfigPath Path to the original YAML config file
     * @return true if migration was successful
     */
    public boolean migrateFromBukkitConfig(String yamlConfigPath) {
        // This would require a YAML parser library
        // For now, we'll just create a placeholder for this functionality
        NekoAntiXray.LOGGER.info("Config migration from Bukkit not implemented yet");
        return false;
    }
    
    /**
     * Configuration data class
     */
    public static class ConfigData {
        // Detection settings
        public boolean enableXrayDetection = true;
        public boolean enableAlgoAntiXray = false;
        public boolean enableSameChunkDetection = true;
        
        // Fake ore generation settings
        public int fakeOreCount = 15;
        public int fakeOreLifetime = 100;
        public int fakeOreDistanceLimit = 12;
        public int fakeOrePlayerDistanceLimit = 12;
        public int fakeOreGenerationCooldown = 100;
        
        // X-change range
        public int xChangeMin = -15;
        public int xChangeMax = 20;
        
        // Z-change range
        public int zChangeMin = -15;
        public int zChangeMax = 20;
        
        // Y-change range
        public int yChangeMin = -10;
        public int yChangeMax = 10;
        
        // Amount range
        public int amountMin = 5;
        public int amountMax = 7;
        
        // Violation settings
        public double vlAddBreakSelf = 1.0;
        public double vlAddBreakOther = 2.0;
        public boolean vlBanEnable = true;
        public double vlBanThreshold = 10.0;
        public String vlBanCommand = "ban %player% %duration% 使用X-Ray外挂";
        
        // Continuous increase settings
        public int continuousIncreaseCount = 3;
        public int continuousIncreaseInterval = 60;
        
        // Ban settings
        public String banAnnouncement = "§c玩家 %player% 因使用作弊被封禁！";
        public String banDetail = "§7原因: §f%reason% ";
        public String banReason = "使用作弊客户端 (VL: %vl%)";
        public int banDuration = 30;
        public String customBanMessage = "&c你已被此服务器封禁！\n&7原因: &f%s\n&7封禁时长: &f%s\n&7如果你认为这是一个错误，请联系服务器管理员。\n&d[NekoAntiXray] &6高效且轻量反Xray插件 By SakuyaAyane";
        
        // Other settings
        public boolean showLocationInConsole = false;
        public boolean enableBanAnnouncement = true;
        public int violationCooldown = 10;
        public int violationDecreaseInterval = 300;
        public double violationDecreaseAmount = 1.0;
        
        // Vertical distribution settings
        public boolean verticalDistributionEnabled = true;
        public int verticalDistributionRange = 5;
        public int verticalDistributionMinDifference = 2;
        public boolean verticalDistributionDebug = false;
        
        // Replay settings
        public boolean replayEnabled = true;
        public int replayRecordDuration = 300;
        
        // FakeSeed settings
        public boolean enableFakeSeed = true;
        public boolean showRealSeedToOp = false;
        
        // AntiSeedCracker settings
        public boolean enableAntiSeedCracker = true;
        public boolean modifyHashedSeed = true;
        public boolean modifyEndStructures = true;
        public List<String> endStructureWorlds = new ArrayList<>();
        public boolean modifyEndSpikes = true;
        public String endSpikesMode = "swap";
        public boolean modifyEndCities = true;
        
        // Blacklist settings
        public List<String> blacklistOres = new ArrayList<>();
        public List<String> blacklistWorlds = new ArrayList<>();
        
        public ConfigData() {
            // Initialize default blacklists
            blacklistOres.add("COAL_ORE");
            blacklistWorlds.add("world_the_end");
            
            // Initialize default end structure worlds
            endStructureWorlds.add("world_the_end");
        }
    }
}
