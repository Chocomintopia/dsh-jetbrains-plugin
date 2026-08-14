import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipBuilder {
  public static void main(String[] args) throws Exception {
    Path src = Paths.get(args[0]);
    Path out = Paths.get(args[1]);
    try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out.toFile())))) {
      zos.setLevel(Deflater.DEFAULT_COMPRESSION);
      try (var files = Files.walk(src)) {
        files.filter(Files::isRegularFile).sorted().forEach(p -> {
          String name = src.relativize(p).toString().replace('\\', '/');
          try {
            zos.putNextEntry(new ZipEntry(name));
            Files.copy(p, zos);
            zos.closeEntry();
          } catch (IOException e) { throw new UncheckedIOException(e); }
        });
      }
    }
    System.out.println("ZIP BUILT: " + out);
  }
}
