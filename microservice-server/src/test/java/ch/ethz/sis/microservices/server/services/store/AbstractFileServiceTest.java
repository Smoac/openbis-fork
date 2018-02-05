package ch.ethz.sis.microservices.server.services.store;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.microservices.server.logging.LogManager;
import ch.ethz.sis.microservices.server.logging.log4j.Log4J2LogFactory;
import ch.ethz.sis.microservices.server.startup.HttpClient;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class AbstractFileServiceTest
{
    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    public static void test(String openbisURL, String serviceURL, String user, String pass) throws Exception
    {
        // Service
        String externalDMSCode = "ADMIN-BS-MBPR28.D.ETHZ.CH-E96954A7";
        String contentCopyPath = "/Users/localadmin/obis_data/data";
        String datasetPermId = "20180131090547942-3";
        String datasetPathToFile = "file";

        // Obtain session token from openBIS
        int timeout = 10000;
        IApplicationServerApi v3As = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openbisURL, timeout);
        String sessionToken = v3As.login(user, pass);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sessionToken", sessionToken);
        parameters.put("datasetPermId", datasetPermId);
        parameters.put("externalDMSCode", externalDMSCode);
        parameters.put("contentCopyPath", contentCopyPath);
        parameters.put("datasetPathToFile", datasetPathToFile);

        long start = System.currentTimeMillis();
        byte[] response = HttpClient.doGet(serviceURL, parameters);
        long end = System.currentTimeMillis();
        System.out.println(new String(response));
        System.out.println("Time: " + (end - start));
    }
}
