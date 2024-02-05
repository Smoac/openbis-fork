package ch.ethz.sis.openbis.generic.typescript.dto;

import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.typescript.TypeScriptMethod;
import ch.ethz.sis.openbis.generic.typescript.TypeScriptObject;

@TypeScriptObject
public class OpenBISJavaScriptDSSFacade implements IDataStoreServerApi
{

    private OpenBISJavaScriptDSSFacade()
    {
    }

    @TypeScriptMethod
    @Override public SearchResult<DataSetFile> searchFiles(final String sessionToken, final DataSetFileSearchCriteria searchCriteria,
            final DataSetFileFetchOptions fetchOptions)
    {
        return null;
    }

    @TypeScriptMethod(ignore = true)
    @Override public InputStream downloadFiles(final String sessionToken, final List<? extends IDataSetFileId> fileIds,
            final DataSetFileDownloadOptions downloadOptions)
    {
        return null;
    }

    @TypeScriptMethod(ignore = true)
    @Override public FastDownloadSession createFastDownloadSession(final String sessionToken, final List<? extends IDataSetFileId> fileIds,
            final FastDownloadSessionOptions options)
    {
        return null;
    }

    @TypeScriptMethod
    @Override public DataSetPermId createUploadedDataSet(final String sessionToken, final UploadedDataSetCreation newDataSet)
    {
        return null;
    }

    @TypeScriptMethod
    @Override public List<DataSetPermId> createDataSets(final String sessionToken, final List<FullDataSetCreation> newDataSets)
    {
        return null;
    }

    @TypeScriptMethod
    @Override public Object executeCustomDSSService(final String sessionToken, final ICustomDSSServiceId serviceId,
            final CustomDSSServiceExecutionOptions options)
    {
        return null;
    }

    @TypeScriptMethod(sessionToken = false)
    public String uploadFilesWorkspaceDSS(List<Object> files)
    {
        return null;
    }

    @TypeScriptMethod(sessionToken = false)
    public CreateDataSetUploadResult createDataSetUpload(String dataSetType)
    {
        return null;
    }

    @TypeScriptMethod(ignore = true)
    @Override public int getMajorVersion()
    {
        return 0;
    }

    @TypeScriptMethod(ignore = true)
    @Override public int getMinorVersion()
    {
        return 0;
    }

    @TypeScriptObject
    public static class CreateDataSetUploadResult
    {
        public String getId()
        {
            return null;
        }

        public String getUrl(String folderPath, boolean ignoreFilePath)
        {
            return null;
        }

        public String getDataSetType()
        {
            return null;
        }
    }
}
