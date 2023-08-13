package baritone.gradle.task;

import org.gradle.api.tasks.TaskAction;

import javax.xml.bind.DatatypeConverter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CreateDistTask extends BaritoneGradleTask {

    private static MessageDigest SHA1_DIGEST;

    @TaskAction
    protected void exec() throws Exception {
        super.verifyArtifacts();

        Path api             = getRelativeFile("dist/" + formatVersion(ARTIFACT_API));
        Path standalone      = getRelativeFile("dist/" + formatVersion(ARTIFACT_STANDALONE));
        Path unoptimized     = getRelativeFile("dist/" + formatVersion(ARTIFACT_UNOPTIMIZED));
        Path forgeApi        = getRelativeFile("dist/" + formatVersion(ARTIFACT_FORGE_API));
        Path forgeStandalone = getRelativeFile("dist/" + formatVersion(ARTIFACT_FORGE_STANDALONE));

        Path dir = getRelativeFile("dist/");
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }

        Files.copy(this.artifactApiPath,             api,             REPLACE_EXISTING);
        Files.copy(this.artifactStandalonePath,      standalone,      REPLACE_EXISTING);
        Files.copy(this.artifactUnoptimizedPath,     unoptimized,     REPLACE_EXISTING);
        Files.copy(this.artifactForgeApiPath,        forgeApi,        REPLACE_EXISTING);
        Files.copy(this.artifactForgeStandalonePath, forgeStandalone, REPLACE_EXISTING);

        List<String> shasum = Stream.of(api, forgeApi, standalone, forgeStandalone, unoptimized)
                .map(path -> sha1(path) + "  " + path.getFileName().toString())
                .collect(Collectors.toList());

        shasum.forEach(System.out::println);

        Files.write(getRelativeFile("dist/checksums.txt"), shasum);
    }

    private static synchronized String sha1(Path path) {
        try {
            if (SHA1_DIGEST == null) {
                SHA1_DIGEST = MessageDigest.getInstance("SHA-1");
            }
            return DatatypeConverter.printHexBinary(SHA1_DIGEST.digest(Files.readAllBytes(path))).toLowerCase();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
