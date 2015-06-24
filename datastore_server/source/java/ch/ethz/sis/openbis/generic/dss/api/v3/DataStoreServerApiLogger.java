package ch.ethz.sis.openbis.generic.dss.api.v3;

import java.util.List;

import ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.search.FileSearchCriterion;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;

public class DataStoreServerApiLogger extends AbstractServerLogger implements
        IDataStoreServerApi
{
    DataStoreServerApiLogger(IInvocationLoggerContext context)
    {
        super(null, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public List<DataSetFile> searchFiles(String sessionToken, FileSearchCriterion searchCriterion)
    {
        logAccess(sessionToken, "search-files", "SEARCH_CRITERION:\n%s\n", searchCriterion);
        return null;
    }

}