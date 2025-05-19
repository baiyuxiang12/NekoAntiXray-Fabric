# NekoAntiXray Fabric版本使用说明

## 简介

NekoAntiXray Fabric版本是原Bukkit/Spigot插件NekoAntiXray-AllPlatform-1.5的Fabric平台适配版本。该mod保留了原插件的核心功能，包括假矿石生成、玩家挖矿行为监控、违规检测和自动封禁等反作弊机制。

## 安装要求

- Minecraft 1.19.4
- Fabric Loader 0.14.19+
- Fabric API 0.78.0+
- Java 17+

## 安装步骤

1. 安装Fabric Loader（如果尚未安装）
2. 将nekoantixray-1.0.0.jar放入服务器的mods文件夹
3. 将Fabric API放入服务器的mods文件夹
4. 启动服务器

## 配置文件

首次启动后，mod会在`config/nekoantixray`目录下生成`config.json`配置文件。您可以根据需要修改配置选项。

主要配置项包括：

- 检测设置（enableXrayDetection, enableAlgoAntiXray等）
- 假矿石生成设置（fakeOreCount, fakeOreLifetime等）
- 违规值设置（vlAddBreakSelf, vlAddBreakOther等）
- 封禁设置（vlBanEnable, vlBanThreshold等）
- 假种子设置（enableFakeSeed, showRealSeedToOp等）

## 命令

NekoAntiXray Fabric版本支持以下命令：

- `/nax` - 主命令
  - `/nax reload` - 重新加载配置
  - `/nax ban <玩家> [天数]` - 封禁玩家
  - `/nax resetviolation <玩家>` - 重置玩家违规记录
  - `/nax showhwid <玩家>` - 显示玩家HWID（暂未实现）
  - `/nax replay <玩家>` - 回放玩家挖矿记录（暂未实现）
- `/nekoantixrayupdate` - 更新插件（暂未实现）
- `/seed` - 显示假种子
- `/fakeseedreload` - 重新加载假种子配置

## 权限

NekoAntiXray Fabric版本支持以下权限：

- `nekoantixray.admin` - 允许使用所有NekoAntiXray管理命令
- `nekoantixray.ban` - 允许使用ban子命令
- `nekoantixray.reload` - 允许使用reload子命令
- `nekoantixray.resetviolation` - 允许重置玩家违规记录
- `nekoantixray.update` - 允许更新NekoAntiXray插件
- `nekoantixray.bypass` - 允许玩家绕过XRay检查
- `fakeseed.reload` - 允许重新加载假种子配置
- `fakeseed.viewreal` - 允许查看真实世界种子

注意：由于Fabric原版没有内置权限系统，默认情况下只有OP（权限等级4）拥有这些权限。如需更细粒度的权限控制，请安装第三方权限mod。

## 功能说明

### 假矿石生成

mod会在玩家周围生成假矿石，这些矿石只有服务器能看到，客户端无法通过常规方式探测到。当玩家使用X-Ray等作弊手段挖掘这些假矿石时，系统会记录违规行为并增加违规值。

### 违规检测

当玩家挖掘假矿石时，系统会根据配置增加玩家的违规值。违规值会随时间自动降低，但连续违规会导致额外惩罚。当违规值达到阈值时，系统会自动执行封禁命令。

### 假种子保护

mod可以向玩家显示假的世界种子，防止通过种子计算器等工具预测矿物分布。管理员可以配置是否允许OP查看真实种子。

## 与原版区别

Fabric版本与原Bukkit/Spigot版本的主要区别：

1. 配置文件格式从YAML改为JSON
2. 部分高级功能（如HWID检测、回放系统）暂未实现
3. 权限系统简化，默认只支持OP权限检查
4. 命令系统使用Brigadier重新实现

## 常见问题

**Q: 配置文件在哪里？**  
A: 在服务器的`config/nekoantixray`目录下。

**Q: 如何重新加载配置？**  
A: 使用命令`/nax reload`。

**Q: 如何查看玩家的违规值？**  
A: 目前暂未实现查看违规值的命令，将在后续版本添加。

**Q: 为什么某些命令显示"暂未实现"？**  
A: 部分高级功能在Fabric版本中尚未完全实现，将在后续版本中添加。

## 技术支持

如有问题或建议，请联系原作者或提交issue。

---

感谢使用NekoAntiXray Fabric版本！
