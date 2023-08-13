package baritone.utils.schematic.format;

import baritone.api.schematic.IStaticSchematic;
import baritone.api.schematic.format.ISchematicFormat;
import baritone.utils.schematic.format.defaults.MCEditSchematic;
import baritone.utils.schematic.format.defaults.SpongeSchematic;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Default implementations of {@link ISchematicFormat}
 *
 * @author Brady
 * @since 12/13/2019
 */
public enum DefaultSchematicFormats implements ISchematicFormat {

    /**
     * The MCEdit schematic specification. Commonly denoted by the ".schematic" file extension.
     */
    MCEDIT("schematic") {
        @Override
        public IStaticSchematic parse(InputStream input) throws IOException {
            return new MCEditSchematic(CompressedStreamTools.readCompressed(input));
        }
    },

    /**
     * The SpongePowered Schematic Specification. Commonly denoted by the ".schem" file extension.
     *
     * @see <a href="https://github.com/SpongePowered/Schematic-Specification">Sponge Schematic Specification</a>
     */
    SPONGE("schem") {
        @Override
        public IStaticSchematic parse(InputStream input) throws IOException {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(input);
            int version = nbt.getInteger("Version");
            switch (version) {
                case 1:
                case 2:
                    return new SpongeSchematic(nbt);
                default:
                    throw new UnsupportedOperationException("Unsupported Version of a Sponge Schematic");
            }
        }
    };

    private final String extension;

    DefaultSchematicFormats(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean isFileType(File file) {
        return this.extension.equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()));
    }
}
