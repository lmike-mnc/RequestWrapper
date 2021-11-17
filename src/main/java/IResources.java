import javassist.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public interface IResources {
    org.slf4j.Logger LOG = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    URL jar = IResources.class.getProtectionDomain().getCodeSource().getLocation();
    Path jarFile = Paths.get(jar.toString().substring("file:".length()+1));

    static List<File> getFilesByDir(Path path, String ext) {
        return Arrays.stream(Objects.requireNonNull(path.resolve("").toFile().listFiles((dir, name) -> name.endsWith(ext)))).collect(Collectors.toList());
    }
    static List<File> getResourcesAll(String dirPath, String ext) throws IOException, URISyntaxException {
        List<File> res = new ArrayList<>();
        ClassLoader classLoader = Loader.class.getClassLoader();
        URI uri = Loader.class.getResource(dirPath).toURI();
/** i want to know if i am inside the jar or working on the IDE*/
        if (uri.getScheme().contains("jar")) {
            /** jar case */
            LOG.info("Jar resources processing...");
            LOG.info("jar contents:"+jar.toString());
            FileSystem fs = FileSystems.newFileSystem(jarFile, (ClassLoader) null);
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath(dirPath));
            for (Path p : directoryStream) {
                InputStream is = IResources.class.getResourceAsStream(p.toString());
                save2temp(res, p, is);
            }
        } else {
            /** IDE case */
            Path path = Paths.get(uri);
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
            for (Path p : directoryStream) {
                InputStream is = new FileInputStream(p.toFile());
                save2temp(res, p, is);
                //performFooOverInputStream(is);
            }
        }
        return res;
    }

    static void save2temp(List<File> res, Path p, InputStream is) throws IOException {
        final File tempFile = File.createTempFile("tmp_", ".pdf");
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(is, out);
            res.add(tempFile);
            LOG.info(p.toString() + "\n->" +tempFile);
        }
    }
}
