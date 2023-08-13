package baritone.utils.accessor;

import baritone.cache.WorldProvider;

import java.io.File;

/**
 * @author Brady
 * @see WorldProvider
 * @since 8/4/2018
 */
public interface IAnvilChunkLoader {

    File getChunkSaveLocation();
}
