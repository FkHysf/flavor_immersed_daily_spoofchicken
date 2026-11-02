烟火凡人心：更多白切鸡 0.2.2
模组 ID：flavor_immersed_daily_spoofchicken

新增内容
1. 五种食材现在都能无序合成（材料各1，产物1；工作台摆放顺序任意）：
   芋泥：马铃薯＋紫色染料。
   折耳根奶茶：烟火阿萨姆奶茶＋原版蕨＋生鳕鱼。
   王老吉：烟火红茶＋原版蒲公英＋糖。
   藿香正气水：烟火纯净水＋生姜＋原版棕色蘑菇＋烟火三十里红（kaoliangwine）。
   跳跳糖：原版糖＋烟火食用小苏打＋原版红石粉。
   加入配方书解锁：获得每条配方的首项材料后解锁对应配方。
2. 30种白切鸡和5种新增食材均有独立两行文案：黄色短评、灰色吐槽，悬停直接显示。提供中文与英文翻译。
3. 模组ID、Java包、资源命名空间、汤锅配方、模型引用、本地化键和领取指令全部更名。
4. 保留30种白切鸡的30条汤锅配方、90个阶段模型和三口食用；贴图及造型与0.2.0一致。

适用版本
Minecraft 1.21.1 / NeoForge 21.1.244+ 的21.1.x / Java21。
依赖用户提供的 flavor_immersed_daily-1.21.1-neoforge-2026.10.2.jar，内置fidworkblock 1.0.0。

指令（需要作弊或管理员权限）
/function flavor_immersed_daily_spoofchicken:give_all              全部30盘白切鸡
/function flavor_immersed_daily_spoofchicken:give_ingredients      新增5种食材，各16份
/function flavor_immersed_daily_spoofchicken:give_crafting_kit     五种新合成所需原材料，各16份
/function flavor_immersed_daily_spoofchicken:give_recipe_kit       汤锅和全部配方测试原料（较多，物品栏放不下会掉落）
也可在创造栏“烟火凡人心：更多白切鸡”领取全部35种物品。

汤锅说明
汤锅fidworkblock:pot，原料各1份，分别放入前六个食材槽，不需要额外的碗，产出1盘白切鸡。
配方配置120 tick（20TPS约6秒），实际进度由原厨房组件控制。
五种新食材的合成见“新增合成与35种文案.md”，全部白切鸡配方见“30种汤锅配方.md”。
文案仅供趣味展示，未新增buff、药效、跳跃或爆炸效果。

验证
Gradle clean build成功。加载烟火发行JAR及其内置组件，10项自动GameTest通过：
- 五种工作台配方倒序摆放仍正确匹配、得到指定物品，缺少材料时拒绝。
- 全部35物品有两行正确新命名空间的可翻译悬停文案。
- 30条汤锅配方全部实际用原组件服务端加工，产物与材料消耗正确。
- 五种新食材属性、饮用/食用动画。
- 30种白切鸡放置并食用三次。
- 朝向与支撑变化。
- 创造模式食用及状态持久化。
资源核查35配方、5项配方书解锁、90模型、5张16×16食材贴图，中文英文各70条文案键。
未在整合包客户端内检查JEI界面和悬停排版。最小测试环境缺少其他可选模组时，烟火自带的部分跨模组配方会记录解析错误；本附属35条配方均加载成功。

源码
可编辑工程在 flavor_immersed_daily_spoofchicken/；源码ZIP包含Gradle工程、Java、全部资源、90个Blockbench模型源文件和本轮脚本。
将用户提供的烟火原JAR复制为源码目录 libs/flavor_immersed_daily-reference.jar，然后使用Java21执行gradlew.bat build。
Gradle9.2.1，首次开发依赖下载需要网络。测试代码不打入成品。
五种16×16贴图的像素数据另存于work/item_sprite_sources.json，work/generate_item_textures.py可重新生成。

本版更新：第三口吃完后地上掉落1个原版碗；不会直接塞进背包。
减少前两口邻居更新，拒绝过期重复交互。全部10项游戏测试及独立服务器卸载后两次重启验证通过。
详细范围与卸载说明见“优化与卸载验证报告.md”。本次未安装到任何客户端。
