package ink.ptms.zaphkiel.subcommand

import ink.ptms.zaphkiel.Zaphkiel
import ink.ptms.zaphkiel.ZaphkielCommand.notify
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.subCommand

// zl reload
val CommandReload = subCommand {
    execute<CommandSender> { sender, _, _ ->
        Zaphkiel.api().reload()
        notify(sender, "成功.")
    }
}