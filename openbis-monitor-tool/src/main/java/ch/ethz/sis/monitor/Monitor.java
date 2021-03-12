package ch.ethz.sis.monitor;
import java.util.Arrays;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class Monitor
{
    private static IApplicationServerApi v3 = null;
    private static IDataStoreServerApi dssv3 = null;

    public static void main(String... args) {
        String sessionToken  = null;
        try {
            if(args.length != 5) {
                System.out.println("Usage example: java -jar openbis-monitor-tool.jar URL TIMEOUT userId password datasetPermId");
                return;
            }
            // get a reference to API
            if(v3 == null) {
                if (args[0].startsWith("https")) {
                    SslCertificateHelper.trustAnyCertificate(args[0]);
                }
                v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, args[0] + "openbis/openbis" + IApplicationServerApi.SERVICE_URL, Integer.parseInt(args[1]));
            }
            if(dssv3 == null) {
                if (args[0].startsWith("https")) {
                    SslCertificateHelper.trustAnyCertificate(args[0]);
                }
                dssv3 = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, args[0] + "datastore_server" + IDataStoreServerApi.SERVICE_URL, Integer.parseInt(args[1]));
            }
            // login to obtain a session token
            sessionToken = v3.login(args[2], args[3]);
            if(sessionToken == null) {
                System.out.println(Boolean.FALSE);
                return;
            }
            boolean isSessionActive = v3.isSessionActive(sessionToken);

            System.out.println("Login [AS]: " + isSessionActive);

            // Search for the dataset
            Map<IDataSetId, DataSet> asSearchResults = v3.getDataSets(sessionToken, Arrays.asList(new DataSetPermId(args[4])), new DataSetFetchOptions());
            System.out.println("Dataset Search [AS]: " + (asSearchResults.size() == 1));

            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet().withCode().thatEquals(args[4]);
            SearchResult<DataSetFile> dssFileListing = dssv3.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());

            System.out.println("File Search [DSS]: " + (dssFileListing.getTotalCount() > 0));
            v3.logout(sessionToken);
            System.out.println("ALL OK: " + (isSessionActive && (asSearchResults.size() == 1) && (dssFileListing.getTotalCount() > 0)));
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    } 
}
