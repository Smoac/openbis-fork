package ch.ethz.sis.rdf.main;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Utils {

    public static final String XLSX_EXTENSION = ".xlsx";
    public static final String PREFIX = "UID";
    private static final int TIMEOUT = 300000;

    public static Path createTemporaryFile() {
        Path tempFile = null;
        try
        {
            tempFile = Files.createTempFile(PREFIX, XLSX_EXTENSION);
        } catch (IOException e)
        {
            throw new RuntimeException("Could not create temporary file: ", e);
        }
        return tempFile;
    }

    public static String extractLabel(String uri) {
        int hashIndex = uri.indexOf('#');
        if (hashIndex != -1)
        {
            return uri.substring(hashIndex + 1);
        } else
        {
            return uri;
        }
    }

    public static void autosizeColumns(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public static void connectAndExport(String openbisASURL, String openBISDSSURL, String username, String password, Path tempFile) {

        OpenBIS openBIS = openBISDSSURL == null ? new OpenBIS(openbisASURL, TIMEOUT) : new OpenBIS(openbisASURL, openBISDSSURL, null, TIMEOUT);

        // 4. Call openBIS V3 API executeImport with the Excel file
        //IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, serviceURL + IApplicationServerApi.SERVICE_URL, TIMEOUT);
        String sessionToken = openBIS.login(username, password);
        System.out.println("Retrived sessionToken: " + sessionToken);

        String uploadId = openBIS.uploadToSessionWorkspace(tempFile);
        System.out.println("Retrived uploadId: " + uploadId);
        // Call excel import
        ImportData importData = new ImportData();
        importData.setFormat(ImportFormat.EXCEL);
        importData.setSessionWorkspaceFiles(List.of("output.xlsx").toArray(new String[0])); //List.of(tempFile.toFile().getName()).toArray(new String[0]));

        ImportOptions importOptions = new ImportOptions();
        importOptions.setMode(ImportMode.UPDATE_IF_EXISTS);

        System.out.println("Starting import...");

        openBIS.executeImport(importData, importOptions);

        System.out.println("Import complete.");
    }
}
