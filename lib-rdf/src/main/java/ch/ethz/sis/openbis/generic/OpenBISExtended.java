package ch.ethz.sis.openbis.generic;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.IImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class OpenBISExtended extends OpenBIS {

    private final IApplicationServerApi asFacadeNoTransactions;


    public OpenBISExtended(final String asURL, final String dssURL, final int timeout)
    {
        super(asURL, dssURL, timeout);

        this.asFacadeNoTransactions =
                HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, asURL + IApplicationServerApi.SERVICE_URL, timeout);
    }

    //this.getSessionToken(): Exception in thread "main" ch.systemsx.cisd.common.exceptions.UserFailureException: Hash cannot be empty.
    public void executeImport(IImportData importData, ImportOptions importOptions, String sessionToken) {
        asFacadeNoTransactions.executeImport(sessionToken, importData, importOptions);
    }

}
