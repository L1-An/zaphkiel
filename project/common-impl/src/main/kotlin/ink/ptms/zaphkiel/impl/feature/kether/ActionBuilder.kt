package ink.ptms.zaphkiel.impl.feature.kether

import ink.ptms.zaphkiel.api.event.Editable
import ink.ptms.zaphkiel.api.event.ItemReleaseEvent
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.platform.function.console
import taboolib.common5.cint
import taboolib.library.xseries.parseToMaterial
import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionNow
import taboolib.module.kether.combinationParser
import taboolib.module.kether.scriptParser

@KetherParser(["cancel"], namespace = "zaphkiel")
private fun parserCancel() = scriptParser {
    actionNow {
        val e = itemEvent<Event>()
        if (e is Cancellable) {
            e.isCancelled = true
        }
    }
}

@KetherParser(["preset", "build"], namespace = "zaphkiel-build")
private fun parserPreset() = combinationParser {
    it.group(
        symbol(),
        text(),
        command("to", then = any()).option()
    ).apply(it) { action, value, text ->
        now {
            when (action) {
                // 名称
                "name" -> {
                    text ?: error("missing value for preset name $value")
                    val itemEvent = itemEvent<Event>()
                    if (itemEvent is Editable) {
                        itemEvent.addName(value, text)
                    } else {
                        error("It cannot be modified in this event")
                    }
                }
                // 描述
                "lore" -> {
                    text ?: error("missing value for preset name $value")
                    val itemEvent = itemEvent<Event>()
                    if (itemEvent is Editable) {
                        // 如果是列表则分行添加
                        if (text is List<*>) {
                            itemEvent.addLore(value, text.map { it.toString() })
                        } else { // 反之则直接添加
                            itemEvent.addLore(value, text)
                        }
                    } else {
                        error("It cannot be modified in this event")
                    }
                }
                // 图标（材质）
                "icon", "material" -> itemEvent<ItemReleaseEvent>().icon = value.parseToMaterial()
                // 附加值
                "data", "damage" -> itemEvent<ItemReleaseEvent>().data = value.cint
                // 其他
                else -> error("unknown preset action $action")
            }
        }
    }
}