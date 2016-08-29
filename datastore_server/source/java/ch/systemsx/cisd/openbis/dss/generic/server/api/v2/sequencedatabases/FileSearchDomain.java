package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;

public class FileSearchDomain extends AbstractSearchDomainService
{

    public FileSearchDomain(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public boolean isAvailable()
    {
        return PathInfoDataSourceProvider.isDataSourceDefined();
    }

    @Override
    public List<SearchDomainSearchResult> search(String searchString, Map<String, String> optionalParametersOrNull)
    {
        IDataSetPathInfoProvider dataSetPathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();
        Map<String, List<DataSetPathInfo>> allPathInfos = dataSetPathInfoProvider.listPathInfosBySearchString(searchString);

        List<SearchDomainSearchResult> results = new ArrayList<SearchDomainSearchResult>();

        for (String datasetCode : allPathInfos.keySet())
        {
            List<DataSetPathInfo> dataSetPathInfos = allPathInfos.get(datasetCode);

            for (DataSetPathInfo dataSetPathInfo : dataSetPathInfos)
            {
                SearchDomainSearchResult searchDomainSearchResult = new SearchDomainSearchResult();
                DataSetFileSearchResultLocation dfsrl = new DataSetFileSearchResultLocation();
                dfsrl.setPermId(datasetCode);
                dfsrl.setCode(datasetCode);
                dfsrl.setPathInDataSet(dataSetPathInfo.getRelativePath());
                dfsrl.setEntityKind(EntityKind.DATA_SET);

                searchDomainSearchResult.setResultLocation(dfsrl);
                results.add(searchDomainSearchResult);
            }
        }

        return results;
    }

}