/*
 * Copyright 2014 ETH Zuerich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertySearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;

/**
 * A test database that returns a search result that was stored in the parameters map under a key equal to the searched sequence snippet.
 * 
 * @author pkupczyk
 */
public class EchoDatabase extends AbstractSearchDomainService
{
    public EchoDatabase(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public boolean isAvailable()
    {
        return true;
    }

    @Override
    public List<SearchDomainSearchResult> search(String sequenceSnippet, Map<String, String> optionalParametersOrNull)
    {
        String resultStr = optionalParametersOrNull.get(sequenceSnippet);
        if (resultStr != null)
        {
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                HelperBean bean = mapper.readValue(resultStr, HelperBean.class);
                ISearchDomainResultLocation resultLocation = bean.getResultLocation();
                SearchDomainSearchResult result = new SearchDomainSearchResult();
                result.setSearchDomain(bean.getSearchDomain());
                result.setResultLocation(resultLocation);
                return Collections.singletonList(result);
            } catch (Exception e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            }
        } else
        {
            return Collections.emptyList();
        }
    }

    public static final class HelperBean
    {
        private SearchDomain searchDomain = new SearchDomain();

        private DataSetFileSearchResultLocation dataSetFileResultLocation;

        private EntityPropertySearchResultLocation entityPropertyResultLocation;

        public ISearchDomainResultLocation getResultLocation()
        {
            return dataSetFileResultLocation == null ? entityPropertyResultLocation : dataSetFileResultLocation;
        }

        public SearchDomain getSearchDomain()
        {
            return searchDomain;
        }

        public void setSearchDomain(String searchDomain)
        {
            this.searchDomain.setName(searchDomain);
        }

        public void setDataSetCode(String dataSetCode)
        {
            getDataSetFileResultLocation().setCode(dataSetCode);
            getDataSetFileResultLocation().setPermId(dataSetCode);
        }

        public void setPathInDataSet(String path)
        {
            getDataSetFileResultLocation().setPathInDataSet(path);
        }

        public void setSequenceIdentifier(String identifier)
        {
            getDataSetFileResultLocation().setIdentifier(identifier);
        }

        public void setPositionInSequence(int position)
        {
            getDataSetFileResultLocation().setPosition(position);
        }

        public void setEntityKind(EntityKind entityKind)
        {
            getEntityPropertyResultLocation().setEntityKind(entityKind);
        }

        public void setEntityType(String entityType)
        {
            getEntityPropertyResultLocation().setEntityType(entityType);
        }

        public void setPermId(String permId)
        {
            getEntityPropertyResultLocation().setPermId(permId);
        }

        public void setCode(String code)
        {
            getEntityPropertyResultLocation().setCode(code);
        }

        public void setPropertyType(String propertyType)
        {
            getEntityPropertyResultLocation().setPropertyType(propertyType);
        }

        private DataSetFileSearchResultLocation getDataSetFileResultLocation()
        {
            if (dataSetFileResultLocation == null)
            {
                dataSetFileResultLocation = new DataSetFileSearchResultLocation();
            }
            return dataSetFileResultLocation;
        }

        private EntityPropertySearchResultLocation getEntityPropertyResultLocation()
        {
            if (entityPropertyResultLocation == null)
            {
                entityPropertyResultLocation = new EntityPropertySearchResultLocation();
            }
            return entityPropertyResultLocation;
        }
    }
}
