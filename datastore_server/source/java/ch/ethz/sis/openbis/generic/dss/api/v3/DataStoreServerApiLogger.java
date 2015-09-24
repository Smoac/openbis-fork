package ch.ethz.sis.openbis.generic.dss.api.v3;

import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.openbis.generic.dss.api.v3.dto.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.id.datasetfile.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.search.DataSetFileSearchCriteria;
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
    public List<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-files", "SEARCH_CRITERION:\n%s\n", searchCriteria);
        return null;
    }

    @Override
    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds,
            DataSetFileDownloadOptions downloadOptions)
    {
        logAccess(sessionToken, "download-files", "FILE_IDS:\n%s\n%s", fileIds, downloadOptions);
        return null;
    }

}