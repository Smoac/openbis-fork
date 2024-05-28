package ch.ethz.sis.rdf.main;

import ch.ethz.sis.openbis.generic.OpenBISExtended;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static void connectAndExport(String openbisASURL, String openBISDSSURL, String username, String password, Path tempFile) {

        String serviceURL = openBISDSSURL == null ? openbisASURL + "/openbis/openbis" : openbisASURL;

        // 4. Call openBIS V3 API executeImport with the Excel file
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, serviceURL + IApplicationServerApi.SERVICE_URL, TIMEOUT);
        String sessionToken = v3.login(username, password);
        System.out.println("Retrived sessionToken: " + sessionToken);

        // Call excel import
        UncompressedImportData uncompressedImportData = new UncompressedImportData();
        uncompressedImportData.setFormat(ImportFormat.XLS);
        try {
            uncompressedImportData.setFile(Files.readAllBytes(tempFile));
            Files.deleteIfExists(tempFile);
        } catch (IOException e)
        {
            throw new RuntimeException("Could not read or delete tempFile: " + tempFile, e);
        }

        ImportOptions importOptions = new ImportOptions();
        importOptions.setMode(ImportMode.UPDATE_IF_EXISTS);

        OpenBISExtended openBIS = openBISDSSURL == null ? new OpenBISExtended(openbisASURL, TIMEOUT) : new OpenBISExtended(openbisASURL, openBISDSSURL, TIMEOUT);
        System.out.println("Starting import...");
        openBIS.executeImport(uncompressedImportData, importOptions, sessionToken);
        System.out.println("Import complete.");
    }
}
