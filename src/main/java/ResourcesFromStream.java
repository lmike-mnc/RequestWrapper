import org.apache.pdfbox.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResourcesFromStream implements IResources {
    private static final String ext=".tmp";

    static List<File> getFiles() throws IOException {
        String[] fnames={
                //setup file names here
        };
        List<File>res=new ArrayList<>();
        for(String s:fnames){
            LOG.info("res file:"+s);
            res.add(writeToFile(ResourcesFromStream.class.getResourceAsStream("/"+s)));
        }
        return res;

    }
    private static File writeToFile(InputStream is) throws IOException {
        Path targetFile = Files.createTempFile("res", ext);// ("src/main/resources/targetFile.tmp");
        OutputStream outStream = new FileOutputStream(targetFile.toFile());

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outStream);
        LOG.info("tmp file:"+targetFile.toFile().getName());
        return targetFile.toFile();
    }

}
