package ch.ethz.sis.microservices.server.services.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import ch.ethz.sis.microservices.server.logging.LogManager;
import ch.ethz.sis.microservices.server.logging.Logger;

public class DownloadHandler extends AbstractFileServiceHandler
{
    private static Logger logger = LogManager.getLogger(DownloadHandler.class);

    @Override
    protected void success(Path pathToFile, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + pathToFile.getFileName().toString());
        response.setHeader("Content-Length", Long.toString(Files.size(pathToFile)));
        Files.copy(pathToFile, response.getOutputStream());
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void failure(Path pathToFile, HttpServletResponse response) throws ServletException, IOException
    {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}