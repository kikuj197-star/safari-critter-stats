# Safari Critter Stats

Fabric 客户端模组，适用于 Minecraft 26.2，需要安装官方 Hypixel Mod API 1.0.2 或更高版本，不依赖 Skyblocker。它从 Hypixel SkyBlock 聊天消息中统计当前 Critter Safari 实例的抓捕数、Shard 总数及尚未抓到的 Critter 种类，并在 HUD 中实时显示当前区域的缺失列表。

模组直接订阅 Hypixel 官方 Mod API 的 `ClientboundLocationPacket`，并以官方 `mode=safari` 数据判断位置。只有官方接口确认玩家位于 Critter Safari 时才会统计或响应；每次重新进入 Critter Safari 都会自动清空本轮数据，重新连接服务器也会清除旧数据。

## 使用

- `/pc !unique`、`/pc !uniq`、`/pc !missing` 或 `/pc !miss`：发送剩余数量和名单中的前 5 个英文名称。
- `/pc !unique i`、`/pc !missing ice`、`/pc !miss icy`：只发送 Icy 区域缺少的数量和前 5 个名称，不区分大小写，例如 `Icy Missing 9: ...`。
- 其他区域也可使用：Cavern 为 `c/cave/cavern`，Forest 为 `f/forest`，Haunted 为 `h/haunt/haunted`。
- `/pc !total`：发送本轮 Critter 抓捕总数和 Shard 总量。
- `/pc !progress` 或 `/pc !prog`：发送四个区域的完成进度；已完成区域会显示从进入本轮 Safari 起计算的耗时。

模组会实时按 Critter 所属区域记录唯一种类。一个区域的所有 Critter 都首次抓到后，只会自动向队伍聊天发送一次完成消息，例如 `Icy done (6m 12s)`。重复抓捕（包括 Sparkling）仍会正常增加总抓捕数和 Shard 总数，但不会重复触发区域完成消息。

队友也可以直接在 Party Chat 发送 `!unique`、`!uniq`、`!missing`、`!miss`、`!total`、`!progress` 及其区域参数来查询。开启队友指令响应后，自己的 `/pc !...` 查询也会正常发进队聊，让其他成员收到；关闭时自己的查询会仅在本地处理。所有自动回复共用安全队列：消息至少间隔 1 秒发送；相同查询在排队期间会合并，发送后 1 秒内不会重复响应，防止多人同时查询造成瞬间刷屏。

在 Critter Safari 外，模组不会拦截这些命令，也不会统计聊天消息。

## 实时 HUD

进入 Safari 后，模组读取服务器下发的 `hypixel:cavern`、`hypixel:forest`、`hypixel:haunted`、`hypixel:icy` 和 `hypixel:icy_caves` 生物群系，自动判断当前区域。左上角 HUD 会显示区域名称、唯一抓捕进度和该区域全部尚未抓到的 Critter；回到入口中央时会提示进入一个区域。

HUD 数据会在抓捕或 Loot Share 消息到达后立即更新。Hideyho 的特殊 `You found the Hideyho` 抓捕消息及 `finding the Hideyho` Loot Share 消息也会正确统计。

在 Mod Menu 配置中点击 `Edit HUD position & size` 可打开 HUD 编辑器：按住鼠标左键拖动预览调整位置，将鼠标放在预览上滚动滚轮可在 50%–250% 之间调整大小。位置和缩放会自动保存，`Reset` 可恢复左上角 100%。

## 配置

安装 Mod Menu 后，在模组列表选择 Safari Critter Stats 并点击配置按钮：

- `Mod enabled`：模组功能总开关。关闭后停止统计、指令拦截与自动回复；在 Safari 内重新开启时会从开启时开始一轮新统计。
- `Respond to party commands`：控制是否响应队友直接发在 Party Chat 中的查询指令；不会影响你自己输入 `/pc !...`。
- `Show missing critters HUD`：控制是否显示自动识别区域的实时缺失列表。
- `Edit HUD position & size`：打开可拖动、可用滚轮缩放的 HUD 预览编辑器。

修改会立即保存到 `config/safari-critter-stats.json`。Mod Menu 是可选依赖；未安装时模组仍会使用配置文件中已保存的设置。

示例输出：

```text
Missing 12: Cavernfish, Flitter, Shyworm, Driftling, Chuckwalla
Missing 5: Billygoat, Mantis Shrimp, Nozzlenose, Troodon, Wumpa
Icy Missing 9: Strongarm, Tepid, Polaris, Shuddersquid, Billygoat
All 37 critters caught!
Total: 28 critters | 67 shards
Progress: Cavern done (2m 5s) | Forest 4/9 | Haunted 0/10 | Icy 1/9
Icy done (6m 12s)
```

普通 `CAPTURE!`、`LOOT SHARE!` 与 Sparkling 抓捕消息均会计入；地面拾取的 Shard 不会误计为 Critter 抓捕。

## 安装

需要 Minecraft 26.2、Fabric Loader 0.19.3 或更高版本，以及官方 Hypixel Mod API 1.0.2 或更高版本；不需要 Skyblocker。将构建出的 JAR 放入实例的 `mods` 文件夹即可。

## 构建

安装 Java 25 后，在项目目录执行：

```shell
./gradlew build
```

Windows 可执行 `gradlew.bat build`。生成文件位于 `build/libs/`。
