package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompatibleIds extends ItemStat<StringListData, StringListData> {
    public CompatibleIds() {
        super("COMPATIBLE_IDS", Material.COMMAND_BLOCK, "兼容ID",
                new String[]{"该皮肤兼容", "的物品 ID(指定单一物品可以使用)"}, new String[]{"skin"});
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "必须指定一个字符串列表");
        return new StringListData((List<String>) object);
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.COMPATIBLE_IDS).enable("在聊天中输入您要添加的物品 ID");

        if (event.getAction() != InventoryAction.PICKUP_HALF || !inv.getEditedSection().contains("compatible-ids"))
            return;
        List<String> lore = inv.getEditedSection().getStringList("compatible-ids");
        if (lore.size() < 1)
            return;

        String last = lore.get(lore.size() - 1);
        lore.remove(last);
        inv.getEditedSection().set("compatible-ids", lore);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功删除 '" + last + "'");
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> lore = inv.getEditedSection().contains("compatible-ids") ? inv.getEditedSection().getStringList("compatible-ids")
                : new ArrayList<>();
        lore.add(message.toUpperCase());
        inv.getEditedSection().set("compatible-ids", lore);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功添加兼容 ID");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {
        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "Current Value:");
            statData.get().getList().forEach(str -> lore.add(ChatColor.GRAY + str));
        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + " 与任何物品兼容");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加新 ID");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击以删除最后一个 ID");
    }

    @NotNull
    @Override
    public StringListData getClearStatData() {
        return new StringListData();
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        // Copy Array, for lore
        List<String> compatibleIds = new ArrayList<>(data.getList());
        item.getLore().insert("compatible-ids", compatibleIds);

        // Add data
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringListData data) {
        // Build Json Array
        JsonArray array = new JsonArray();

        // For each string in the ids of the data
        for (String sts : data.getList()) {
            array.add(sts);
        }

        // Make returning array
        ArrayList<ItemTag> tags = new ArrayList<>();

        // Add Json Array
        tags.add(new ItemTag(getNBTPath(), array.toString()));

        return tags;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        // FInd relvant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Generate data
        StatData data = getLoadedNBT(relevantTags);

        if (data != null)
            mmoitem.setData(this, data);
    }

    @Nullable
    @Override
    public StringListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        // Find relevant tag
        ItemTag rTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (rTag == null)
            // Nope
            return null;

        try {
            // Parse onto Json Array
            JsonArray array = new JsonParser().parse((String) rTag.getValue()).getAsJsonArray();
            // Make and return list
            return new StringListData(array);
        } catch (JsonSyntaxException | IllegalStateException exception) {
            /*
             * OLD ITEM WHICH MUST BE UPDATED.
             */
        }
        return null;
    }
}
