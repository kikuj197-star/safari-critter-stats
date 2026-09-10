# Safari Critter Stats

Fabric 客户端模组，适用于 Minecraft 26.1.x，需要安装 Skyblocker 6.9.2 或更高版本。它从 Hypixel SkyBlock 聊天消息中统计当前 Critter Safari 实例的抓捕数、Shard 总数及尚未抓到的 Critter 种类。

模组直接使用 Skyblocker 由 Hypixel Mod API 驱动的 `isInSafari()` 地点接口。只有接口确认玩家位于 Critter Safari 时才会统计或响应；每次重新进入 Critter Safari 都会自动清空本轮数据，重新连接服务器也会清除旧数据。

## 使用

- `/pc !unique` 或 `/pc !uniq`：发送剩余数量和名单中的前 5 个英文名称。
- `/pc !unique i`、`/pc !uniq ice`、`/pc !unique icy`：只发送 Icy 区域缺少的数量和前 5 个名称，不区分大小写。
- 其他区域也可使用：Cavern 为 `c/cave/cavern`，Forest 为 `f/forest`，Haunted 为 `h/haunt/haunted`。
- `/pc !total`：发送本轮 Critter 抓捕总数和 Shard 总量。
- `/pc !progress` 或 `/pc !prog`：发送四个区域的完成进度；已完成区域会显示从进入本轮 Safari 起计算的耗时。

模组会实时按 Critter 所属区域记录唯一种类。一个区域的所有 Critter 都首次抓到后，只会自动向队伍聊天发送一次完成消息，例如 `Icy done (6m 12s)`。重复抓捕（包括 Sparkling）仍会正常增加总抓捕数和 Shard 总数，但不会重复触发区域完成消息。

在 Critter Safari 外，模组不会拦截这两个命令，也不会统计聊天消息。

示例输出：

```text
Missing 12: Cavernfish, Flitter, Shyworm, Driftling, Chuckwalla
Missing 5: Billygoat, Mantis Shrimp, Nozzlenose, Troodon, Wumpa
Missing 9: Strongarm, Tepid, Polaris, Shuddersquid, Billygoat
All 37 critters caught!
Total: 28 critters | 67 shards
Progress: Cavern done (2m 5s) | Forest 4/9 | Haunted 0/10 | Icy 1/9
Icy done (6m 12s)
```

普通 `CAPTURE!`、`LOOT SHARE!` 与 Sparkling 抓捕消息均会计入；地面拾取的 Shard 不会误计为 Critter 抓捕。

## 安装

需要 Minecraft 26.1.x、Fabric Loader 0.19.3 或更高版本，以及 Skyblocker 6.9.2 或更高版本。将构建出的 JAR 放入实例的 `mods` 文件夹即可。

## 构建

安装 Java 25 后，在项目目录执行：

```shell
./gradlew build
```

Windows 可执行 `gradlew.bat build`。生成文件位于 `build/libs/`。
