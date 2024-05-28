package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import ch.ethz.sis.openbis.generic.server.FileServiceServlet;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class FileServerUtils
{

    /** These files are used for testing purposes and should be treated separately. */
    private static final Set<String> TEST_FILE_PATHS = Set.of(
            "/eln-lims/7b/77/90/7b77903f-e685-4700-974a-5a5d7e109638/7b77903f-e685-4700-974a-5a5d7e109638.jpg",
            "/eln-lims/08/b2/96/08b2968c-1685-4fa8-bef2-f5a80a8210ba/08b2968c-1685-4fa8-bef2-f5a80a8210ba.jpg",
            "/eln-lims/46/63/05/466305f0-4842-441f-b21c-777ea82079b4/466305f0-4842-441f-b21c-777ea82079b4.jpg",
            "/eln-lims/c0/1b/2e/c01b2e1f-8212-4562-ae8a-9072bf92e687/c01b2e1f-8212-4562-ae8a-9072bf92e687.jpg",
            "/eln-lims/c1/b2/91/c1b2912a-2ed6-40d6-8d9f-8c3ec2b29c5c/c1b2912a-2ed6-40d6-8d9f-8c3ec2b29c5c.jpg",
            "/eln-lims/f3/e4/0c/f3e40c2e-109c-4191-bed0-2cf931de185a/f3e40c2e-109c-4191-bed0-2cf931de185a.jpg");

    private static Path getFilePath(String filePath) throws IOException
    {
        if (!TEST_FILE_PATHS.contains(filePath))
        {
            // Runtime mode.
            String repositoryPathAsString = CommonServiceProvider.tryToGetProperty(
                    FileServiceServlet.REPO_PATH_KEY);
            String repositoryFilePathAsString = repositoryPathAsString + filePath;
            Path repositoryPath = Path.of(new File(repositoryPathAsString).getCanonicalPath());
            Path repositoryFilePath = Path.of(new File(repositoryFilePathAsString).getCanonicalPath());
            //Security Test that repositoryFilePath is indeed inside repositoryPath
            if (!repositoryFilePath.startsWith(repositoryPath))
            {
                throw new IllegalArgumentException("File Path is not inside the Repository Path");
            }

            return repositoryFilePath;
        } else
        {
            // Testing mode.

            // Return the default testing path.
            return Path.of(new File("./sourceTest/java/ch/ethz/sis/openbis/generic/server/xls/importer/utils/" + filePath).getCanonicalPath());
        }
    }

    public static InputStream read(String src) throws IOException
    {
        return Files.newInputStream(getFilePath(src));
    }

    public static byte[] readAllBytes(final String src) throws IOException
    {
        final Path filePathAsPath = getFilePath(src);
        return Files.readAllBytes(filePathAsPath);
    }

    public static long write(final InputStream src, final String dst) throws IOException
    {
        final Path filePathAsPath = getFilePath(dst);
        return Files.copy(src, filePathAsPath);
    }

    public static OutputStream newOutputStream(String dst) throws IOException
    {
        final Path filePathAsPath = getFilePath(dst);
        return Files.newOutputStream(filePathAsPath);
    }

}
