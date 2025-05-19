package sakuyaayane.nekoantixray.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置管理器 - Fabric版本
 */
public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("nekoantixray");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configDir = FabricLoader.getInstance().getConfigDir().resolve("nekoantixray");
    private final File configFile;
    private ConfigData config;

    public ConfigManager() {
        // 创建配置目录
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LOGGER.error("创建配置目录失败", e);
        }

        // 设置配置文件
        configFile = configDir.resolve("config.json").toFile();

        // 加载配置
        loadConfig();
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config = gson.fromJson(reader, ConfigData.class);
                LOGGER.info("配置已加载");
            } catch (IOException e) {
                LOGGER.error("加载配置失败", e);
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }

    /**
     * 创建默认配置
     */
    private void createDefaultConfig() {
        config = new ConfigData();
        saveConfig();
        LOGGER.info("已创建默认配置");
    }

    /**
     * 保存配置
     */
    private void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
            LOGGER.info("配置已保存");
        } catch (IOException e) {
            LOGGER.error("保存配置失败", e);
        }
    }

    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * 获取配置
     */
    public ConfigData getConfig() {
        return config;
    }

    /**
     * 配置数据类
     */
    public static class ConfigData {
        // 基本设置
        public boolean enabled = true;
        public int defaultBanDays = 30;
        public String banReason = "§c您因使用矿物透视被封禁";
        
        // 检测设置
        public int maxViolation = 10;
        public int violationDecay = 1;
        public int violationDecayInterval = 60;
        
        // 假矿石设置
        public boolean fakeOreEnabled = true;
        public int fakeOreDistance = 20;
        public int fakeOreAmount = 3;
        public int fakeOreCooldown = 5;
        public List<String> fakeOreTypes = new ArrayList<String>() {{
            add("minecraft:diamond_ore");
            add("minecraft:deepslate_diamond_ore");
            add("minecraft:ancient_debris");
        }};
        
        // 假种子设置
        public boolean fakeSeedEnabled = true;
        public long fakeSeed = 114514;
        
        // 调试设置
        public boolean debug = false;
    }
}
