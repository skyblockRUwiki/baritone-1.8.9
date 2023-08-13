package baritone.gradle.task;

import org.gradle.api.DefaultTask;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class BaritoneGradleTask extends DefaultTask {

    protected static final String
            PROGUARD_ZIP                    = "proguard.zip",
            PROGUARD_JAR                    = "proguard.jar",
            PROGUARD_CONFIG_TEMPLATE        = "scripts/proguard.pro",
            PROGUARD_CONFIG_DEST            = "template.pro",
            PROGUARD_API_CONFIG             = "api.pro",
            PROGUARD_STANDALONE_CONFIG      = "standalone.pro",
            PROGUARD_EXPORT_PATH            = "proguard_out.jar",

    TEMP_LIBRARY_DIR = "tempLibraries/",

    ARTIFACT_STANDARD         = "%s-%s.jar",
            ARTIFACT_UNOPTIMIZED      = "%s-unoptimized-%s.jar",
            ARTIFACT_API              = "%s-api-%s.jar",
            ARTIFACT_STANDALONE       = "%s-standalone-%s.jar",
            ARTIFACT_FORGE_API        = "%s-api-forge-%s.jar",
            ARTIFACT_FORGE_STANDALONE = "%s-standalone-forge-%s.jar";

    protected String artifactName, artifactVersion;
    protected Path artifactPath, artifactUnoptimizedPath, artifactApiPath, artifactStandalonePath, artifactForgeApiPath, artifactForgeStandalonePath, proguardOut;

    protected void verifyArtifacts() throws IllegalStateException {
        this.artifactName = getProject().getName();
        this.artifactVersion = getProject().getVersion().toString();

        this.artifactPath                = this.getBuildFile(formatVersion(ARTIFACT_STANDARD));
        this.artifactUnoptimizedPath     = this.getBuildFile(formatVersion(ARTIFACT_UNOPTIMIZED));
        this.artifactApiPath             = this.getBuildFile(formatVersion(ARTIFACT_API));
        this.artifactStandalonePath      = this.getBuildFile(formatVersion(ARTIFACT_STANDALONE));
        this.artifactForgeApiPath        = this.getBuildFile(formatVersion(ARTIFACT_FORGE_API));
        this.artifactForgeStandalonePath = this.getBuildFile(formatVersion(ARTIFACT_FORGE_STANDALONE));

        this.proguardOut = this.getTemporaryFile(PROGUARD_EXPORT_PATH);

        if (!Files.exists(this.artifactPath)) {
            throw new IllegalStateException("Artifact not found! Run build first!");
        }
    }

    protected void write(InputStream stream, Path file) throws Exception {
        if (Files.exists(file)) {
            Files.delete(file);
        }
        Files.copy(stream, file);
    }

    protected String formatVersion(String string) {
        return String.format(string, this.artifactName, this.artifactVersion);
    }

    protected Path getRelativeFile(String file) {
        return Paths.get(new File(file).getAbsolutePath());
    }

    protected Path getTemporaryFile(String file) {
        return Paths.get(new File(getTemporaryDir(), file).getAbsolutePath());
    }

    protected Path getBuildFile(String file) {
        return getRelativeFile("build/libs/" + file);
    }
}
