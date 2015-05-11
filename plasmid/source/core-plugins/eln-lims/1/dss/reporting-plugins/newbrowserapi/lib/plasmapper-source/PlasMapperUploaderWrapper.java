package ch.ethz.ssdm.eln;
import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;


public class PlasMapperUploaderWrapper
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PlasMapperUploaderWrapper.class);

    private static final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            PlasMapperUploaderWrapper.class);
    
    private static final String HTML_FILE_TEMPLATE =
            "<html><head>\n"
                    + "<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"><title>PlasMapper - Graphic Map</title></head>\n"
                    + "<body>\n"
                    + "<embed src=\"%%FILE_NAME%%\" type=\"image/svg+xml\" pluginspage=\"http://www.adobe.com/svg/viewer/install/\" id=\"Panel\" height=\"1010\" width=\"1010\">\n"
                    + "<br>\n" + "<a href=\"%%FILE_NAME%%\" target=\"_blank\">Download Link</a>"
                    + "</body></html>";
    

    public static void uploadAndCopyGeneratedFile(
            final String baseURL,
            final String serverRootDir,
            final String seqFilePath,
            final String destinationFilePath)
    {
        final File seqFile = new File(seqFilePath);
        final File destinationFile = new File(destinationFilePath);
        final PlasMapperUploader.PlasMapperService service = PlasMapperUploader.PlasMapperService.values()[0];
        final PlasMapperUploader uploader = new PlasMapperUploader(baseURL);
        
        String outputFilePath = uploader.upload(seqFile, service);
        if (StringUtils.isBlank(outputFilePath))
        {
            notifyLog.error("Cannot upload file '" + seqFile.getName()
                    + "', see jetty.out for details.");
            throw new IllegalStateException("Cannot upload file '" + seqFile.getName()
                    + "', see jetty.out for details.");
        }
        File outputFile = new File(serverRootDir + outputFilePath);
        if (outputFile.isFile())
        {
            operationLog.info("Renaming and copying file '" + outputFile.getName() + "' from '"
                    + outputFile + "' to " + destinationFile);
            FileOperations.getInstance().copyFile(outputFile, destinationFile);

            if (destinationFile.getName().endsWith("svg"))
            {
                String htmlFileName = destinationFile.getName().replaceAll(".svg", ".html");
                File htmlFile = new File(destinationFile.getParentFile(), htmlFileName);
                operationLog.info("Generating html file '" + htmlFile + "'");
                FileUtilities.writeToFile(htmlFile,
                        HTML_FILE_TEMPLATE.replaceAll("%%FILE_NAME%%", destinationFile.getName()));
            }
        } else
        {
            throw new EnvironmentFailureException("'" + outputFile
                    + "' doesn't exist or is not a file.");
        }
    }
}
