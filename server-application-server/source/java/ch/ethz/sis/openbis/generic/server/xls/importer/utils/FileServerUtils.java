package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import ch.ethz.sis.openbis.generic.server.FileServiceServlet;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileServerUtils
{
    private Path getFilePath(String filePath) throws IOException
    {
        String repositoryPathAsString = CommonServiceProvider.tryToGetProperty(
                FileServiceServlet.REPO_PATH_KEY);
        String repositoryFilePathAsString = repositoryPathAsString + filePath;
        Path repositoryPath = Path.of(new File(repositoryPathAsString).getCanonicalPath());
        Path repositoryFilePath =  Path.of(new File(repositoryFilePathAsString).getCanonicalPath());
        //Security Test that repositoryFilePath is indeed inside repositoryPath
        if(!repositoryFilePath.startsWith(repositoryPath)) {
            throw new IllegalArgumentException("File Path is not inside the Repository Path");
        }
        return repositoryFilePath;
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
