package ch.ethz.sis.openbis.generic.server.xls.importer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationHelper;

public class MainImportXlsTest
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MainImportXlsTest.class);

    private static final int TIMEOUT = 10000;

    private static final String URL = "http://localhost:8888/openbis/openbis" + IApplicationServerApi.SERVICE_URL;

    private static final File ELN_MASTER_DATA_PATH = new File("openbis_standard_technologies/dist/core-plugins/eln-lims/1/as");

    private static final File LIFE_SCIENCES_MASTER_DATA_PATH =
            new File("../openbis_standard_technologies/dist/core-plugins/eln-lims-life-sciences/1/as");

    // used only for development!
    public static void main(String[] args) throws IOException
    {
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);
        String sessionToken = v3.login("admin", "changeit");

        Map<String, String> scripts = getListScripts(ELN_MASTER_DATA_PATH.getPath());
        ImportOptions options = new ImportOptions();

        XLSImport importXls = new XLSImport(sessionToken, v3, scripts, ImportModes.UPDATE_IF_EXISTS, options,
                "ELN-LIMS");

        final File zip = new File("/home/viktor/Work/test.2022-11-08-16-41-32-831.zip");
        final XLSImport.ImportZipResult importZipResult = importXls.importZip(FileUtilities.loadToByteArray(zip));

        System.out.printf("Imported IDS: %s%n", importZipResult.getIds());
        System.out.printf("Imported Scripts: %s%n", importZipResult.getScripts());

//        Collection<IObjectId> importedIds = new ArrayList<>();

//        File commonDataModel = new File(ELN_MASTER_DATA_PATH, "/master-data/common-data-model.xls");
//        importedIds.addAll(importXls.importXLS(FileUtilities.loadToByteArray(commonDataModel)));
//        File singleGroupDataModel = new File(ELN_MASTER_DATA_PATH, "/master-data/single-group-data-model.xls");
//        importedIds.addAll(importXls.importXLS(FileUtilities.loadToByteArray(singleGroupDataModel)));

//        Map<String, String> lifeSciencesImporterScripts = getListScripts(LIFE_SCIENCES_MASTER_DATA_PATH.getPath());
//        XLSImport lifeSciencesImporter =
//                new XLSImport(sessionToken, v3, lifeSciencesImporterScripts, ImportModes.UPDATE_IF_EXISTS, options, "ELN-LIMS-LIFE-SCIENCES");
//        File lifeSciencesDataModel = new File(LIFE_SCIENCES_MASTER_DATA_PATH, "/master-data/data-model.xls");
//        importedIds.addAll(lifeSciencesImporter.importXLS(FileUtilities.loadToByteArray(lifeSciencesDataModel)));
    }

    private static Map<String, String> getListScripts(String path)
    {
        MasterDataRegistrationHelper helper = new MasterDataRegistrationHelper(Arrays.asList(path));
        return helper.getAllScripts();
    }
}
