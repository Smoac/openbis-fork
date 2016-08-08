package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.DatabaseBasedDataSetPathInfoProvider.ExtendedDataSetFileRecord;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
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
        // TODO Auto-generated method stub
        return true;
    }

    
    @Override
    public List<SearchDomainSearchResult> search(String searchString, Map<String, String> optionalParametersOrNull)
    {   	
    	IDataSetPathInfoProvider dataSetPathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();
    	List<ExtendedDataSetFileRecord> dataSetPathInfos = dataSetPathInfoProvider.listPathInfosByRegularExpression(searchString);
        
    	List<SearchDomainSearchResult> results = new ArrayList<SearchDomainSearchResult>();
    	
    	for(ExtendedDataSetFileRecord dataSetPathInfo : dataSetPathInfos){
        	
        	SearchDomainSearchResult searchDomainSearchResult = new SearchDomainSearchResult();
        	
        	SearchDomain searchDomain = new SearchDomain();
        	searchDomain.setName("File");
        	searchDomain.setLabel("File");

        	searchDomainSearchResult.setSearchDomain(searchDomain);
        	        	
        	DataSetFileSearchResultLocation dfsrl = new DataSetFileSearchResultLocation();
        	dfsrl.setPermId(dataSetPathInfo.code);
        	dfsrl.setCode(dataSetPathInfo.code);
        	dfsrl.setPathInDataSet(dataSetPathInfo.relative_path);
        	dfsrl.setIdentifier(dataSetPathInfo.code);
        	dfsrl.setEntityKind(EntityKind.DATA_SET);
        	
        	searchDomainSearchResult.setResultLocation(dfsrl);
        	results.add(searchDomainSearchResult);
    	}
    	
    	return results;
    }
    

}