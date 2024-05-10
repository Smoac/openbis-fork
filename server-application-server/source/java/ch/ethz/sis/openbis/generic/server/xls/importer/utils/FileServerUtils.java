package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import ch.ethz.sis.openbis.generic.server.FileServiceServlet;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileServerUtils
{
    private Path getFilePath(String filePath) {
        String repositoryPath = CommonServiceProvider.tryToGetProperty(
                FileServiceServlet.REPO_PATH_KEY);
        String repositoryFilePath = repositoryPath + filePath;
        return Path.of(repositoryFilePath);
    }

    public byte[] read(String filePath) throws IOException
    {
        return Files.readAllBytes(getFilePath(filePath));
    }

    public Path write(String filePath, byte[] bytes) throws IOException
    {
        return Files.write(getFilePath(filePath), bytes);
    }
}
