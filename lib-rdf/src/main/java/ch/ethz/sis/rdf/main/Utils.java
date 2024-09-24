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

}
