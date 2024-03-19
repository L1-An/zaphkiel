package ink.ptms.zaphkiel

import ink.ptms.zaphkiel.impl.feature.openGroupMenu
import ink.ptms.zaphkiel.impl.item.toItemStream
import ink.ptms.zaphkiel.subcommand.*
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.io.zip
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.isAir

/**
 * @author sky
 * @since 2019-12-15 22:39
 */
@CommandHeader(name = "Zaphkiel", aliases = ["zl", "item"], permission = "*")
object ZaphkielCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val list = CommandList

    @CommandBody
    val give = CommandGive

    @CommandBody
    val serialize = CommandSerialize

    @CommandBody
    val rebuild = CommandRebuild

    @CommandBody
    val reload = CommandReload

    fun notify(sender: CommandSender, value: String) {
        sender.sendMessage("§c[Zaphkiel] §7${value.colored()}")
        if (sender is Player) {
            sender.playSound(sender.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
        }
    }
}