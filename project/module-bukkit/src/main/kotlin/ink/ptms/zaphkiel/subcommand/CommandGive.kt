package ink.ptms.zaphkiel.subcommand

import ink.ptms.zaphkiel.Zaphkiel
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.int
import taboolib.common.platform.command.player
import taboolib.common.platform.command.subCommand

// zl give <item> [player] [amount]
val CommandGive = subCommand {
    dynamic("item") {
        suggestion<CommandSender> { _, _ ->
            Zaphkiel.api().getItemManager().getItemMap().keys.toList()
        }
        execute<Player> { sender, ctx, _ ->
            Zaphkiel.api().getItemManager().giveItem(sender, ctx["item"])
        }
        player {
            execute<CommandSender> { _, ctx, _ ->
                val player = Bukkit.getPlayerExact(ctx["player"])!!
                Zaphkiel.api().getItemManager().giveItem(player, ctx["item"])
            }
            int("amount") {
                execute<CommandSender> { _, ctx, _ ->
                    val player = Bukkit.getPlayerExact(ctx["player"])!!
                    val amount = ctx.int("amount")
                    Zaphkiel.api().getItemManager().giveItem(player, ctx["item"], amount)
                }
            }
        }
    }
}