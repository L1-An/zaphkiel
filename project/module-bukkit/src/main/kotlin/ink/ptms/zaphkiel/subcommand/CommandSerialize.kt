package ink.ptms.zaphkiel.subcommand

import ink.ptms.zaphkiel.Zaphkiel
import ink.ptms.zaphkiel.ZaphkielCommand.notify
import org.bukkit.entity.Player
import taboolib.common.io.zip
import taboolib.common.platform.command.subCommand

// zl serialize
val CommandSerialize = subCommand {
    execute<Player> { sender, _, _ ->
        try {
            val serializedItem = Zaphkiel.api().getItemSerializer().serialize(sender.inventory.itemInMainHand)
            val json = serializedItem.toJson().replace('§', '&')
            val zipped = json.toByteArray().zip()
            notify(sender, "序列化: &f$json")
            notify(sender, "明文: &f${json.length} &7字符, &f${json.toByteArray().size} &7字节 &a-> &7压缩后: &f${zipped.size} &7字节")
        } catch (ex: Throwable) {
            notify(sender, "无效的物品: $ex")
        }
    }
}