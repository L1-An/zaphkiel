package ink.ptms.zaphkiel.subcommand

import ink.ptms.zaphkiel.Zaphkiel
import ink.ptms.zaphkiel.impl.feature.openGroupMenu
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand

// zl list [group]
val CommandList = subCommand {
    dynamic("group") {
        suggestion<Player> { _, _ ->
            Zaphkiel.api().getItemManager().getGroupMap().keys.toList()
        }
        execute<Player> { sender, ctx, _ ->
            sender.openGroupMenu(Zaphkiel.api().getItemManager().getGroup(ctx["group"])!!)
        }
    }
    execute<Player> { sender, _, _ -> sender.openGroupMenu() }
}