## 此处是面对ShapeShifterCurse-UnofficialPort的移植，本Fork仅进行高版本移植，所有功能版权归属原作者Xu233333所有

> ⚠️ **注意**：下方「原作者说明」一节是原作者 Xu233333 在 **1.20.1 / Forge** 时期写的，其中的**平台名（Forge）、Gradle 配置、版本号均已过时**，请以本节的当前状态为准。原文保留未改，仅作设计意图参考。

---

## 当前状态（Fork，1.21.1）

**目标平台**：`1.21.1 × Fabric + NeoForge`（Architectury 多平台）—— **不是 Forge**。

| 依赖 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| 平台 | `enabled_platforms=fabric,neoforge` |
| Architectury | 13.0.11 |
| NeoForge | 21.1.248 |
| Fabric Loader / API | 0.19.3 / 0.116.15+1.21.1 |
| CCA | 6.1.3 |
| mod_version | `1.0.0`（原作者文档里写的 `1.0.0-alpha` 已过时） |

### 模块分层

| 模块 | 内容 |
|---|---|
| `common/` | 平台无关 API：`ComponentAPI`、`ComponentType`、`ComponentProvider`、`SerializableComponent`、`ComponentInitializer`；平台差异经 `Platform`（`@ExpectPlatform` 三方法：`registerComponent` / `getComponent` / `syncComponent`）下沉 |
| `fabric/` | `ComponentSystemImpl_CCA`（挂在 CCA 的 `cardinal-components-entity` entrypoint 上）、`PlayerComponentBase` / `EntityComponentBase` |
| `neoforge/` | `capability/`（`EntityCapability.createVoid` + `RegisterCapabilitiesEvent.registerEntity`）、`network/`（`ComponentSyncPacket` + `PayloadRegistrar.playToClient`） |

### 与原作者文档（1.20.1 / Forge）的差异

| 原文 | 现状 |
|---|---|
| `@Mod` + `MinecraftForge.EVENT_BUS` + `FMLCommonSetupEvent` | NeoForge：`@Mod` 构造器注入 `IEventBus`，`modBus.addListener(...)`；Capability 走 `RegisterCapabilitiesEvent` |
| Forge Capability（`CapabilityManager` / `ICapabilityProvider`） | **NeoForge 已把 Capability 拆改**：用 `EntityCapability.createVoid(id, clazz)`，一个组件 id 一个 capability |
| Gradle `fg.deobf(...)` + flatDir「本地导入」 | Architectury 多平台工程；`settings.gradle` 已移除原作者时期的 `mavenCent` |
| 版本号 `1.0.0-alpha` | `1.0.0` |

### API 骨架未变（原文示例的写法基本仍适用）

- 注册：`ComponentAPI.registerComponent_Provider(ComponentAPI.PLAYER, id, PlayerData::new)`
- 取用：`ComponentProvider.getComponent(owner)` / `syncComponent(owner)`
- 实现：`SerializableComponent<T>` 的四个方法 —— `save` / `load`（各带 `boolean fromSync`）、`init(T)`、`onRespawn(old, new)`

**原文示例里对不上的两处**：
- `@Override public static void init()` —— 现接口**没有**这个方法
- `PlayerData.initComponent()` —— 现 API 中不存在（注册直接走 `ComponentAPI.registerComponent_Provider`）

### 使用者的注册流程（Fabric）

1. 实现 `ComponentInitializer`，在 `registerComponent()` 里调 `ComponentAPI.registerComponent_Provider(...)`
2. 在**自己的** `fabric.mod.json` 声明 `"xu_component_lib": ["your.RegPlayerData"]` entrypoint

> 这个 entrypoint 名仍是 `xu_component_lib`：库侧 `ComponentSystemImpl_CCA` 会扫它并逐个调 `registerComponent()`
> （`ComponentSystemImpl_CCA.KEY = "xu_component_lib"`），所以原文档的这套流程**依然有效**。

### 已知未完成

- **NeoForge 侧暂无与 Fabric `xu_component_lib` entrypoint 等价的自动扫描**：`XuComponentLib.init()` 目前是空实现，
  组件注册需在 `CapabilityRegistry.registerAll()`（能力注册）**之前**自行完成，否则 capability 注册时拿不到组件表。

---

<details>
<summary><b>以下为原作者 Xu233333 的原始 README（1.20.1 / Forge 时期，保留未改）</b></summary>

算是SSC双端版本开发的第1步 如果失败了SSC双端版本的开发就基本宣布失败了
我之前只写过单端Mod(Port加载器只手动Port) 所以需要测试一下技术可行性 最坏的可能性就是没法做双端 那么只能Fabric和Forge二选一了(我个人倾向使用Forge 只需手搓一个类Apoli 就能享受到Forge的高级API 后续开发会十分舒服)

目前仅准备实现挂载到LivingEntity Player两个类 一个给能力引擎 另一个给SSC

~~CCA 需要挂Custom 我之后试试能不能用Mixin在它读取前注入对应的Custom~~ 没法整 还是挂Custom吧
Forge端没写过Component 之后研究一下(之前都是Mixin进对应函数附加NBT 没用过Forge的API)

注册方法: 目前仅支持LivingEntity Player两个类
双端 写一个ComponentInitializer 在registerComponent函数里注册SerializableComponent
Fabric 使用EntryPoint挂载ComponentInitializer 然后给CCA的Custom里添加对应的ID
Forge 在FMLCommonSetupEvent之前调用ComponentInitializer里的registerComponent函数 或者直接注册 不写ComponentInitializer

用了大量泛型和强转 写对应代码记得填对参数 否则可能会在未知地方触发ClassCastException

示例:
```java
// 共用
public class PlayerData implements SerializableComponent<Player> {
    public static final ComponentProvider<Player, PlayerData> PlayerDataProvider = ComponentAPI.registerComponent_Provider(ComponentAPI.PLAYER, new ResourceLocation("xu_mod", "player_data"), PlayerData::new);

    public Player Owner = null;
    public int tick = 0;

    public PlayerData(Player player) {
        // 不用在这里init 由引擎专门调用init
    }

    @Override
    public void save(CompoundTag compoundTag, boolean fromSync) {
        // 当使用Sync时fromSync为True 可以实现轻量同步
        compoundTag.putInt("tick", tick);
    }

    @Override
    public void load(CompoundTag compoundTag, boolean fromSync) {
        // 当使用Sync时fromSync为True 可以实现轻量同步
        tick = compoundTag.getInt("tick");
    }

    @Override
    public void init(Player componentOwner) {
        // 初始化时由引擎自动调用 不用在代码里手动调用
        this.Owner = componentOwner;
    }

    @Override
    public static void init() {
        // 注册组件 由Static自动调用 不过Static得加载类时执行 所以需要外部触发加载
    }

    @Override
    public void onRespawn(T oldObject, T newObject) {
        // 当玩家重生时调用 顺序在 load(fromSync == false) 之后
    }
}
```
```java
// Forge 端
@Mod(Test_Forge.MODID)
public class Test_Forge {
    public static final String MODID = "xu_mod";

    public Test_Forge() {
        // 必须在 FMLCommonSetupEvent 之前注册
        PlayerData.initComponent();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        for (Player player : event.getServer().getPlayerList().getPlayers()) {
            PlayerData playerData = PlayerData.PlayerDataProvider.getComponent(player);
            playerData.tick++;
            PlayerData.PlayerDataProvider.syncComponent(player);
        }
    }
}
```
```java
// Fabric 端
public class Test_Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::serverTick);
    }

    public void serverTick(MinecraftServer server) {
        for (Player player : server.getPlayerList().getPlayers()) {
            PlayerData playerData = PlayerData.PlayerDataProvider.getComponent(player);
            if (playerData != null) {
                playerData.tick++;
            }
            PlayerData.PlayerDataProvider.syncComponent(player);
        }
    }
}

public class RegPlayerData implements ComponentInitializer {
    @Override
    public void registerComponent() {
        PlayerData.initComponent();
    }
}
```
Fabric端的`fabric.mod.json`
```json
{
  "schemaVersion": 1,
  "id": "test-fabric",
  "version": "${version}",
  "name": "Test-Fabric",
  "description": "",
  "authors": [
    {
      "name": "XuHaoNan",
      "contact": {
        "github": "https://github.com/xu233333",
        "email": "18510478058@163.com"
      }
    }
  ],
  "contact": {},
  "license": "MIT",
  "icon": "assets/test-fabric/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": [
      "xu_mod.testfabric.Test_Fabric"
    ],
    "xu_component_lib": [
      "xu_mod.testfabric.RegPlayerData"
    ]
  },
  "custom": {
    "cardinal-components": [
      "xu_mod:player_data"
    ]
  },
  "mixins": [
    "test-fabric.mixins.json"
  ],
  "depends": {
    "fabricloader": ">=${loader_version}",
    "fabric": "*",
    "minecraft": "${minecraft_version}"
  }
}
```

Gradle 配置(没有maven仓库 需要本地导入 等开发的差不多再发布) 把Mod jar放进/libs文件夹里:  
Forge端直接导入就行
```
repositories {
    flatDir {
        dir 'libs'
    }
}

dependencies {
    implementation fg.deobf('xu_mod.xu_component_lib:Xu_Component_Lib-1.0.0-alpha:1.0.0-alpha')
}
```
Fabric需要额外依赖CCA(不知道为什么内嵌jar无法加载)
```
repositories {
    maven {
        name = 'Ladysnake Mods'
        url = 'https://maven.ladysnake.org/releases'
    }
    flatDir {
        dir 'libs'
    }
}

dependencies {
    modImplementation 'xu_mod.xu_component_lib:Xu_Component_Lib-1.0.0-alpha:1.0.0-alpha'
    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${cca_version}") {
        exclude(group: "net.fabricmc.fabric-api")
    }
    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${cca_version}") {
        exclude(group: "net.fabricmc.fabric-api")
    }
}
```

</details>
