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
                    println(text)
                    val itemEvent = itemEvent<Event>()
                    if (itemEvent is Editable) {
                        if (text is List<*>) {
                            itemEvent.addLore(value, text.map { it.toString() })
                            println("text is list")
                        } else {
                            itemEvent.addLore(value, text)
                            println("text is single line")
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