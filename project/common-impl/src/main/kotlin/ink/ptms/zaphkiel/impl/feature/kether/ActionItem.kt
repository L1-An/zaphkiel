package ink.ptms.zaphkiel.impl.feature.kether

import ink.ptms.zaphkiel.api.ItemSignal
import ink.ptms.zaphkiel.impl.Translator
import ink.ptms.zaphkiel.impl.feature.damageItem
import ink.ptms.zaphkiel.impl.feature.getCurrentDurability
import ink.ptms.zaphkiel.impl.feature.getMaxDurability
import ink.ptms.zaphkiel.impl.feature.repairItem
import org.bukkit.entity.Player
import taboolib.module.kether.*
import taboolib.module.nms.ItemTagData

/**
 * 获取物品当前耐久度
 * item durability
 * 获取物品最大耐久度
 * item max-durability
 * 消耗物品
 * item consume
 * 修复一点耐久度
 * item repair 1
 * 损坏一点耐久度
 * item damage 1
 * 更新物品
 * item update
 * 获取物品数据
 * item data key
 * 设置物品数据
 * item data key to 1
 * 移除物品数据
 * item data key to ~
 */
@KetherParser(["item", "zitem"])
private fun parserItem() = scriptParser {
    it.switch {
        case("durability") {
            actionNow { itemStream().getCurrentDurability() }
        }
        case("max-durability", "max_durability") {
            actionNow { itemStream().getMaxDurability() }
        }
        case("consume") {
            actionNow { itemStream().sourceItem.amount-- }
        }
        case("repair") {
            val value = it.nextParsedAction()
            actionTake {
                run(value).int { value -> itemStream().repairItem(value, script().sender?.castSafely<Player>()) }
            }
        }
        case("damage") {
            val value = it.nextParsedAction()
            actionTake {
                run(value).int { value -> itemStream().damageItem(value, script().sender?.castSafely<Player>()) }
            }
        }
        // 更新
        // 下次检查时更新，不是立即更新
        case("update") {
            actionNow { itemStream().signal.add(ItemSignal.UPDATE_CHECKED) }
        }
        // 数据
        case("data") {
            val key = it.nextParsedAction()
            val value = try {
                it.mark()
                expect("to")
                it.nextParsedAction()
            } catch (_: Throwable) {
                it.reset()
                null
            }
            actionFuture { f ->
                run(key).str { key ->
                    // 获取
                    if (value == null) {
                        val unsafeData = itemStream().getZaphkielData().getDeep(key)?.unsafeData()
                        f.complete(if (unsafeData != null) Translator.fromItemTag(unsafeData) else null)
                    }
                    // 设置
                    else if (key != "~") {
                        run(value).str { value -> f.complete(itemStream().getZaphkielData().putDeep(key, ItemTagData.toNBT(value))) }
                    }
                    // 移除
                    else {
                        itemStream().getZaphkielData().removeDeep(key)
                        f.complete(null)
                    }
                }
            }
        }
    }
}