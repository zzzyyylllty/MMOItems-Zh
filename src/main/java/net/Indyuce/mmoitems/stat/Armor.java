package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

public class Armor extends AttributeStat {
	public Armor() {
		super("ARMOR", new ItemStack(VersionMaterial.GOLDEN_CHESTPLATE.toMaterial()), "Armor", new String[] { "The armor given to the holder." },
				Attribute.GENERIC_ARMOR);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).getValue();
		// for (String slot : item.getMMOItem().getType().getSlots())
		// item.addItemAttribute(new Attribute("armor", value, slot));
		item.addItemTag(new ItemTag(getNBTPath(), value));
		item.getLore().insert(getPath(), formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}
}
