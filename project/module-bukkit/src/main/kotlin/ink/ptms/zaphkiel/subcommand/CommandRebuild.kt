package ink.ptms.zaphkiel.subcommand

import ink.ptms.zaphkiel.ZaphkielCommand.notify
import ink.ptms.zaphkiel.impl.item.toItemStream
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.isAir

// zl rebuild
val CommandRebuild = subCommand {
    execute<Player> { sender, _, _ ->
        if (sender.inventory.itemInMainHand.isAir()) {
            notify(sender, "请手持物品.")
            return@execute
        }
        val itemStream = sender.inventory.itemInMainHand.toItemStream()
        if (itemStream.isExtension()) {
            sender.inventory.setItemInMainHand(itemStream.rebuildToItemStack(sender))
            notify(sender, "成功.")
        } else {
            notify(sender, "不是 Zaphkiel 物品.")
        }
    }
}