package baritone.api.utils;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BlockUtils {

    private static transient Map<String, Block> resourceCache = new HashMap<>();

    public static String blockToString(Block block) {
        ResourceLocation loc = Block.blockRegistry.getNameForObject(block);
        String name = loc.getResourcePath(); // normally, only write the part after the minecraft:
        if (!loc.getResourceDomain().equals("minecraft")) {
            // Baritone is running on top of forge with mods installed, perhaps?
            name = loc.toString(); // include the namespace with the colon
        }
        return name;
    }

    public static Block stringToBlockRequired(String name) {
        Block block = stringToBlockNullable(name);

        if (block == null) {
            throw new IllegalArgumentException(String.format("Invalid block name %s", name));
        }

        return block;
    }

    public static Block stringToBlockNullable(String name) {
        // do NOT just replace this with a computeWithAbsent, it isn't thread safe
        Block block = resourceCache.get(name); // map is never mutated in place so this is safe
        if (block != null) {
            return block;
        }
        if (resourceCache.containsKey(name)) {
            return null; // cached as null
        }
        block = Block.getBlockFromName(name.contains(":") ? name : "minecraft:" + name);
        Map<String, Block> copy = new HashMap<>(resourceCache); // read only copy is safe, wont throw concurrentmodification
        copy.put(name, block);
        resourceCache = copy;
        return block;
    }

    private BlockUtils() {}
}
