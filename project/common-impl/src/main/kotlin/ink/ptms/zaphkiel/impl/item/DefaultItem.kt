package ink.ptms.zaphkiel.impl.item

import ink.ptms.zaphkiel.Zaphkiel
import ink.ptms.zaphkiel.api.*
import ink.ptms.zaphkiel.api.event.ItemBuildEvent
import ink.ptms.zaphkiel.api.event.ItemGiveEvent
import ink.ptms.zaphkiel.impl.Translator
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.MetadataValue
import org.bukkit.plugin.Plugin
import taboolib.common.io.digest
import taboolib.common.platform.function.console
import taboolib.common.platform.function.severe
import taboolib.common.util.asList
import taboolib.common.util.unsafeLazy
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.configuration.util.getMap
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.hasItem
import taboolib.platform.util.isAir
import taboolib.platform.util.takeItem
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.impl.item.DefaultItem
 *
 * @author 坏黑
 * @since 2022/7/23 16:51
 */
class DefaultItem(override val config: ConfigurationSection, override val group: Group?) : Item() {

    override val id = config.name

    override val display = config.getString("display") ?: "null"

    override val displayInstance = Zaphkiel.api().getItemManager().getDisplay(display)

    override val icon = parseIcon(config)

    override val iconLocked = config.contains("icon!!")

    override val name = parseName(config)

    override val nameLocked = config.contains("name!!")

    override val lore = parseLore(config)

    override val loreLocked = config.contains("lore!!")

    override val data = config.getConfigurationSection("data") ?: config.createSection("data")

    override val dataMapper = config.getMap<Any, Any>("data-mapper").map { it.key.toString() to it.value.asList().joinToString("\n") }.toMap(HashMap())

    override val model = config.getString("event.from")?.split(",")?.map { it.trim() }?.toMutableList() ?: arrayListOf()

    override val lockedData = getLockedData(HashMap(), data)

    private val originVars = config.getConfigurationSection("event.data")?.getValues(false) ?: emptyMap()
    override val eventVars = mergeModelData(originVars)

    /**
     * 将model中的data合并到eventVars
     */
    private fun mergeModelData(originVars: Map<String, Any?>) : Map<String, Any?> {
        val vars = originVars.toMutableMap()
        if (model.isNotEmpty()) {
            // 遍历model，将model中的data合并到vars最后返回
            model.forEach {
                val model = Zaphkiel.api().getItemManager().getModel(it)
                if (model != null) {
                    // 获取model中的data
                    val modelData = model.config.getConfigurationSection("event.data")?.getValues(false) ?: emptyMap()
                    // 合并到并返回eventVars
                    vars.putAll(modelData)
                } else {
                    severe("Model $it not found.")
                }
            }
            return vars
        } else {
            // 若model为空则直接返回原vars
            return vars
        }
    }

    override val eventMap by unsafeLazy {
        val field = HashMap<String, ItemEvent>()
        if (model.isNotEmpty()) {
            model.forEach {
                val model = Zaphkiel.api().getItemManager().getModel(it)
                if (model != null) {
                    field.putAll(parseEvent(this, model.config))
                } else {
                    severe("Model $it not found.")
                }
            }
        } else {
            field.putAll(parseEvent(this, config))
        }
        field
    }

    override val meta = Zaphkiel.api().getItemLoader().loadMetaFromSection(config).toMutableList().also {
        it.addAll(displayInstance?.meta ?: emptyList())
    }

    override val version = Configuration.empty(Type.YAML).run {
        set("value", config)
        if (displayInstance != null) {
            set("display.name", displayInstance.name)
            set("display.lore", displayInstance.lore)
        }
        saveToString().digest("sha-1")
    }

    val metadataList = ConcurrentHashMap<String, MutableMap<String, MetadataValue>>()

    override fun buildItemStack(player: Player?): ItemStack {
        return build(player).toItemStack(player)
    }

    override fun build(player: Player?, args: Map<String, Any?>): ItemStream {
        return build(player, args) {}
    }

    override fun build(player: Player?, args: Map<String, Any?>, prepareCallback: Consumer<ItemStream>): ItemStream {
        val itemStream = DefaultItemStreamGenerated(icon.clone(), name.toMutableMap(), lore.toMutableMap())
        val compound = itemStream.sourceCompound.computeIfAbsent("zaphkiel") { ItemTag() }.asCompound()
        compound[ItemKey.ID.key] = ItemTagData(id)
        compound[ItemKey.DATA.key] = Translator.toItemTag(ItemTag(), data)
        prepareCallback.accept(itemStream)
        return build(player, itemStream, args)
    }

    override fun build(player: Player?, itemStream: ItemStream, args: Map<String, Any?>): ItemStream {
        val pre = if (itemStream is DefaultItemStreamGenerated) {
            ItemBuildEvent.Pre(player, itemStream, itemStream.name, itemStream.lore, args.toMutableMap())
        } else {
            ItemBuildEvent.Pre(player, itemStream, name.toMutableMap(), lore.toMutableMap(), args.toMutableMap())
        }
        if (pre.call()) {
            // 设置数据
            lockedData.forEach { (k, v) -> itemStream.getZaphkielData().putDeep(k, v) }
            // 设置版本
            pre.itemStream.sourceCompound["zaphkiel"]!!.asCompound()[ItemKey.VERSION.key] = ItemTagData(version)
            // 替换 PlaceholderAPI 变量
            val placeholderReplaced = if (player != null) {
                val map = HashMap<String, MutableList<String>>()
                pre.lore.forEach { (key, lore) -> map[key] = lore.replacePlaceholder(player).toMutableList() }
                map
            } else null
            // 回调事件
            val post = ItemBuildEvent.Post(player, pre.itemStream, pre.name, placeholderReplaced ?: pre.lore)
            post.call()
            return post.itemStream
        }
        return itemStream
    }

    override fun isSimilar(itemStack: ItemStack): Boolean {
        if (itemStack.isAir()) return false
        return kotlin.runCatching { Zaphkiel.api().getItemHandler().getItemId(itemStack) == id }.getOrElse { false }
    }

    override fun hasItem(player: Player, amount: Int): Boolean {
        return player.inventory.hasItem(amount) { isSimilar(it) }
    }

    override fun takeItem(player: Player, amount: Int): Boolean {
        if (!hasItem(player, amount)) return false
        return player.inventory.takeItem(amount) { isSimilar(it) }
    }

    override fun giveItem(player: Player, amount: Int, overflow: Consumer<List<ItemStack>>) {
        val event = ItemGiveEvent(player, build(player), amount).also { it.call() }
        if (!event.isCancelled) {
            val item = event.itemStream.rebuildToItemStack(player)
            item.amount = event.amount
            overflow.accept(player.inventory.addItem(item).values.toList())
        }
    }

    override fun giveItemOrDrop(player: Player, amount: Int) {
        giveItem(player, amount) { it.forEach { item -> player.world.dropItem(player.location, item) } }
    }

    override fun invokeScript(key: List<String>, event: PlayerEvent, itemStream: ItemStream, namespace: String): CompletableFuture<ItemEvent.ItemResult?>? {
        val itemEvent = eventMap.entries.firstOrNull { it.key in key }?.value ?: return null
        if (itemEvent.isCancelled && event is Cancellable) {
            event.isCancelled = true
        }
        return itemEvent.invoke(event.player, event, itemStream, eventVars, namespace)
    }

    override fun invokeScript(key: List<String>, player: Player?, event: Event, itemStream: ItemStream, namespace: String): CompletableFuture<ItemEvent.ItemResult?>? {
        val itemEvent = eventMap.entries.firstOrNull { it.key in key }?.value ?: return null
        if (itemEvent.isCancelled && event is Cancellable) {
            event.isCancelled = true
        }
        return itemEvent.invoke(player, event, itemStream, eventVars, namespace)
    }

    fun getLockedData(map: MutableMap<String, ItemTagData?>, section: ConfigurationSection, path: String = ""): MutableMap<String, ItemTagData?> {
        section.getKeys(false).forEach { key ->
            if (key.endsWith("!!")) {
                map[path + key.substring(0, key.length - 2)] = Translator.toItemTag(config["data.$path$key"])
            } else if (section.isConfigurationSection(key)) {
                getLockedData(map, section.getConfigurationSection(key)!!, "$path$key.")
            }
        }
        return map
    }

    override fun setMetadata(key: String, value: MetadataValue) {
        metadataList.computeIfAbsent(key) { ConcurrentHashMap() }[value.owningPlugin?.name ?: "null"] = value
    }

    override fun getMetadata(key: String): MutableList<MetadataValue> {
        return metadataList[key]?.values?.toMutableList() ?: mutableListOf()
    }

    override fun hasMetadata(key: String): Boolean {
        return metadataList.containsKey(key) && metadataList[key]?.isNotEmpty() == true
    }

    override fun removeMetadata(key: String, plugin: Plugin) {
        metadataList[key]?.remove(plugin.name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultItem) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "DefaultItem(config=$config, group=$group, id='$id', display='$display', version='$version')"
    }
}