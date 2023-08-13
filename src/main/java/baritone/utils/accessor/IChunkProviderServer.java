package baritone.utils.accessor;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;

/**
 * @author Brady
 * @see WorldProvider
 * @since 8/4/2018
 */
public interface IChunkProviderServer {

    IChunkLoader getChunkLoader();
}