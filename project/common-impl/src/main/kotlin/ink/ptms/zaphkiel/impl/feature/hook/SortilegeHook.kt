package ink.ptms.zaphkiel.impl.feature.hook

import ink.ptms.zaphkiel.impl.item.toItemStream
import org.bukkit.entity.Player
import org.yuseries.sortilege.code.api.*
import org.yuseries.sortilege.code.api.event.TabooCodeAttributeLoadEvent
import taboolib.common.platform.Ghost
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Coerce
import taboolib.platform.util.isAir
import java.util.UUID

@Suppress("DuplicatedCode")
internal object SortilegeHook {

    @SubscribeEvent
    @Ghost
    fun onAttributeLoad(e: TabooCodeAttributeLoadEvent.Post) {
        val entity = e.entity
        if (entity is Player) {
            val attrMap = e.newMap
            // 获取所有已注册槽位的物品
            val items = CodeFilter.collectItem(entity).map { it.item }
            items.forEachIndexed { index, item ->
                // 为空气则跳过
                if (item.isAir) return
                val itemStream = item.toItemStream()
                // 为原版物品则跳过
                if (itemStream.isVanilla()) return
                val attribute = itemStream.getZaphkielData()["sortilege"]?.asCompound() ?: return

                // 如果物品上有 ignore 属性则跳过
                val isIgnore = attribute["ignore"]?.asString()?.toBoolean() ?: false
                if (isIgnore) return@forEachIndexed

                // 新建一个属性 map
                val map = AttributeMap(UUID.randomUUID())
                // 将物品上的属性转换为 Modifier 并添加到 map 中
                attribute.forEach { (key, data) ->
                    val args = data.asString().split("-")
                    map.addModifier(
                        key,
                        CodeModifier.of(
                            // 属性值
                            CodeValue.of(Coerce.toDouble(args[0]), Coerce.toDouble(args.getOrElse(1) { args[0] })),
                            namespace = "Zaphkiel.$index"
                        )
                    )
                }
                // 最后将属性合并到原属性中
                attrMap.merge(map)
            }
        }
    }

}