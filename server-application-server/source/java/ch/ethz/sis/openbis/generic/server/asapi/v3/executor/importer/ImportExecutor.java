/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.ethz.sis.openbis.generic.server.xls.importer.utils.FileServerUtils;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.ImportOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.IImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportScript;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ZipImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.xls.importer.XLSImport;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

@Component
public class ImportExecutor implements IImportExecutor
{

    private static final String ZIP_PATH_SEPARATOR = "/";

    private static final String SCRIPTS_FOLDER_NAME = "scripts" + ZIP_PATH_SEPARATOR;

    private static final String DATA_FOLDER_NAME = "data" + ZIP_PATH_SEPARATOR;

    private static final String MISCELLANEOUS_FOLDER_NAME = "miscellaneous" + ZIP_PATH_SEPARATOR;

    private static final String FILE_SERVICES_FOLDER_NAME = MISCELLANEOUS_FOLDER_NAME + "file-service" + ZIP_PATH_SEPARATOR;

    private static final String XLS_EXTENSION = "." + "xls";

    private static final String XLSX_EXTENSION = "." + "xlsx";

    @Override
    public void doImport(final IOperationContext context, final ImportOperation operation)
    {
        final IImportData importData = operation.getImportData();

        try
        {
            if (importData instanceof UncompressedImportData)
            {
                // XLS file

                final UncompressedImportData uncompressedImportData = (UncompressedImportData) importData;

                final Map<String, String> scripts = uncompressedImportData.getScripts() != null
                        ? uncompressedImportData.getScripts().stream().collect(Collectors.toMap(ImportScript::getName, ImportScript::getSource))
                        : null;
                final Map<String, String> importValues = uncompressedImportData.getImportValues() != null
                        ? uncompressedImportData.getImportValues().stream().collect(Collectors.toMap(ImportValue::getName, ImportValue::getValue))
                        : null;
                importXls(context, operation, scripts, importValues, uncompressedImportData.getFile(), null);
            } else if (importData instanceof ZipImportData)
            {
                // ZIP file
                final Map<String, String> scripts = new HashMap<>();
                final Map<String, String> importValues = new HashMap<>();
                byte[] xlsFileContent = null;

                final ZipImportData zipImportData = (ZipImportData) importData;
                try (final ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipImportData.getFile())))
                {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null)
                    {
                        final String entryName = entry.getName();
                        if (entry.isDirectory())
                        {
                            if (!SCRIPTS_FOLDER_NAME.equals(entryName) && !DATA_FOLDER_NAME.equals(entryName) && !MISCELLANEOUS_FOLDER_NAME.equals(entryName))
                            {
                                throw UserFailureException.fromTemplate("Illegal directory '%s' is found inside the imported file.", entryName);
                            }
                        } else
                        {
                            if (!entryName.contains(ZIP_PATH_SEPARATOR) && (entryName.endsWith(XLS_EXTENSION) || entryName.endsWith(XLSX_EXTENSION)))
                            {
                                if (xlsFileContent == null)
                                {
                                    xlsFileContent = zip.readAllBytes();
                                } else
                                {
                                    throw UserFailureException.fromTemplate("More than one XLS file found in the root of the imported ZIP file.");
                                }
                            } else if (entryName.startsWith(SCRIPTS_FOLDER_NAME))
                            {
                                scripts.put(entryName.substring(SCRIPTS_FOLDER_NAME.length()), new String(zip.readAllBytes()));
                            } else if (entryName.startsWith(DATA_FOLDER_NAME))
                            {
                                importValues.put(entryName.substring(DATA_FOLDER_NAME.length()), new String(zip.readAllBytes()));
                            } else
                            {
                                throw UserFailureException.fromTemplate(
                                        "Entry '%s' is not allowed. Only one root XLS file is allowed and files inside the '%s' or '%s' folder",
                                        entryName, SCRIPTS_FOLDER_NAME, DATA_FOLDER_NAME);
                            }
                        }
                    }
                }

                if (xlsFileContent != null)
                {
                    importXls(context, operation, scripts, importValues, xlsFileContent, zipImportData.getFile());
                } else
                {
                    throw UserFailureException.fromTemplate("XLS file not found in the root of the imported ZIP file.");
                }

            } else
            {
                throw UserFailureException.fromTemplate("Unknown instance of import data '%s'.",
                        importData != null ? importData.getClass().getName() : null);
            }
        } catch (final IOException e)
        {
            throw UserFailureException.fromTemplate(e, "IO exception importing.");
        }
    }

    private static void importXls(final IOperationContext context, final ImportOperation operation, final Map<String, String> scripts,
            final Map<String, String> importValues, final byte[] xlsContent, byte[] zipImportDataOrNull)
            throws IOException
    {
        final IApplicationServerInternalApi applicationServerApi = CommonServiceProvider.getApplicationServerApi();
        final ImportOptions importOptions = operation.getImportOptions();

        final ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions importerImportOptions =
                new ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions();

        final boolean projectSamplesEnabled = Boolean.parseBoolean(applicationServerApi.getServerInformation(context.getSession().getSessionToken())
                .get("project-samples-enabled"));
        importerImportOptions.setAllowProjectSamples(projectSamplesEnabled);

        final XLSImport xlsImport = new XLSImport(context.getSession().getSessionToken(), applicationServerApi, scripts,
                importValues, ImportModes.valueOf(importOptions.getMode().name()), importerImportOptions, "DEFAULT");

        xlsImport.importXLS(xlsContent);
        if (zipImportDataOrNull != null)
        {
            importZipData(zipImportDataOrNull);
        }
    }

    public static void importZipData(byte[] zipImportDataOrNull) throws IOException
    {
        try (final ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipImportDataOrNull)))
        {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null)
            {
                final String filePath = entry.getName();
                if (!entry.isDirectory() && filePath.startsWith(FILE_SERVICES_FOLDER_NAME))
                {
                    String fileServicePath = ZIP_PATH_SEPARATOR + filePath.substring(FILE_SERVICES_FOLDER_NAME.length());
                    byte[] fileBytes = zip.readAllBytes();
                    FileServerUtils.write(fileServicePath, fileBytes);
                }
            }
        }
    }
}
