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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.AbstractEntitySearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertySearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * @author Franz-Josef Elmer
 */
public class SearchDomainSearcher extends AbstractBusinessObject implements ISearchDomainSearcher
{
    private static final Map<Long, Set<Metaproject>> EMPTY_METAPROJECTS = Collections.<Long, Set<Metaproject>> emptyMap();

    private static final IKeyExtractor<String, IEntityInformationHolderWithPermId> PERM_ID_EXTRACTOR =
            new IKeyExtractor<String, IEntityInformationHolderWithPermId>()
                {
                    @Override
                    public String getKey(IEntityInformationHolderWithPermId e)
                    {
                        return e.getPermId();
                    }
                };

    private final IDataStoreServiceFactory dssFactory;

    public SearchDomainSearcher(IDAOFactory daoFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker, IRelationshipService relationshipService,
            IDataStoreServiceFactory dssFactory)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        this.dssFactory = dssFactory;
    }

    @Override
    public List<SearchDomain> listAvailableSearchDomains()
    {
        List<SearchDomain> result = new ArrayList<SearchDomain>();
        List<DataStorePE> stores = getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : stores)
        {
            IDataStoreService service = tryGetDataStoreService(dataStore, dssFactory);
            if (service != null)
            {
                try
                {
                    result.addAll(service.listAvailableSearchDomains(dataStore.getSessionToken()));
                } catch (RuntimeException e)
                {
                    Throwable causingException = ExceptionUtils.getEndOfChain(e);
                    if (causingException instanceof ConnectException == false)
                    {
                        throw e;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<SearchDomainSearchResultWithFullEntity> searchForEntitiesWithSequences(String preferredSearchDomainOrNull,
            String sequenceSnippet, Map<String, String> optionalParametersOrNull)
    {
        List<SearchDomainSearchResult> searchResults = askAllDataStoreServers(preferredSearchDomainOrNull, sequenceSnippet, optionalParametersOrNull);
        return enrichWithEntities(searchResults);
    }

    private List<SearchDomainSearchResult> askAllDataStoreServers(String preferredSearchDomainOrNull,
            String sequenceSnippet, Map<String, String> optionalParametersOrNull)
    {
        List<SearchDomainSearchResult> result = new ArrayList<SearchDomainSearchResult>();
        List<DataStorePE> stores = getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : stores)
        {
            IDataStoreService service = tryGetDataStoreService(dataStore, dssFactory);
            if (service != null)
            {
                result.addAll(service.searchForEntitiesWithSequences(dataStore.getSessionToken(),
                        preferredSearchDomainOrNull, sequenceSnippet, optionalParametersOrNull));
            }
        }
        Collections.sort(result, Collections.reverseOrder());
        return result;
    }

    private List<SearchDomainSearchResultWithFullEntity> enrichWithEntities(List<SearchDomainSearchResult> searchResults)
    {
        Map<EntityLoader, List<String>> map = separate(searchResults);
        Map<EntityLoader, TableMap<String, IEntityInformationHolderWithPermId>> result = loadEntities(map);
        List<SearchDomainSearchResultWithFullEntity> enrichedResults = new ArrayList<SearchDomainSearchResultWithFullEntity>();
        for (SearchDomainSearchResult searchResult : searchResults)
        {
            ISearchDomainResultLocation location = searchResult.getResultLocation();
            Selector selector = new Selector(location);
            EntityLoader loader = selector.getLoader();
            IEntityInformationHolderWithPermId entity = result.get(loader).tryGet(selector.getPermId());
            SearchDomainSearchResultWithFullEntity searchResultWithEntity = new SearchDomainSearchResultWithFullEntity();
            searchResultWithEntity.setSearchResult(searchResult);
            searchResultWithEntity.setEntity(entity);
            if (entity != null)
            {
                String code = entity.getCode();
                String type = entity.getEntityType().getCode();
                if (location instanceof AbstractEntitySearchResultLocation)
                {
                    AbstractEntitySearchResultLocation entityLocation = (AbstractEntitySearchResultLocation) location;
                    entityLocation.setCode(code);
                    entityLocation.setEntityType(type);
                }
            }
            enrichedResults.add(searchResultWithEntity);
        }
        return enrichedResults;
    }

    private Map<EntityLoader, List<String>> separate(List<SearchDomainSearchResult> searchResults)
    {
        Map<EntityLoader, List<String>> map = new EnumMap<EntityLoader, List<String>>(EntityLoader.class);
        for (SearchDomainSearchResult searchResult : searchResults)
        {
            ISearchDomainResultLocation resultLocation = searchResult.getResultLocation();
            Selector selector = new Selector(resultLocation);
            EntityLoader loader = selector.getLoader();
            List<String> list = map.get(loader);
            if (list == null)
            {
                list = new ArrayList<String>();
                map.put(loader, list);
            }
            list.add(selector.getPermId());
        }
        return map;
    }

    private Map<EntityLoader, TableMap<String, IEntityInformationHolderWithPermId>> loadEntities(Map<EntityLoader, List<String>> map)
    {
        Map<EntityLoader, TableMap<String, IEntityInformationHolderWithPermId>> result =
                new EnumMap<EntityLoader, TableMap<String, IEntityInformationHolderWithPermId>>(EntityLoader.class);
        Set<Entry<EntityLoader, List<String>>> entrySet = map.entrySet();
        for (Entry<EntityLoader, List<String>> entry : entrySet)
        {
            EntityLoader loader = entry.getKey();
            List<String> permIds = entry.getValue();
            List<IEntityInformationHolderWithPermId> entities = loader.loadEntities(this, managedPropertyEvaluatorFactory, permIds);
            result.put(loader, new TableMap<String, IEntityInformationHolderWithPermId>(entities, PERM_ID_EXTRACTOR));
        }
        return result;
    }

    private static enum EntityLoader
    {
        SAMPLE()
        {
            @Override
            public List<? extends IEntityInformationHolderWithPermId> doLoadEntities(IDAOFactory daoFactory,
                    IManagedPropertyEvaluatorFactory evaluatorFactory, List<String> permIds)
            {
                List<SamplePE> samples = daoFactory.getSampleDAO().listByPermID(permIds);
                return SampleTranslator.translate(samples, "", EMPTY_METAPROJECTS, evaluatorFactory);
            }
        },
        DATA_SET()
        {
            @Override
            public List<? extends IEntityInformationHolderWithPermId> doLoadEntities(IDAOFactory daoFactory,
                    IManagedPropertyEvaluatorFactory evaluatorFactory, List<String> permIds)
            {
                List<DataPE> dataSets = daoFactory.getDataDAO().listByCode(new HashSet<String>(permIds));
                return DataSetTranslator.translate(dataSets, "", "", EMPTY_METAPROJECTS, evaluatorFactory);
            }
        },
        EXPERIMENT()
        {
            @Override
            public List<? extends IEntityInformationHolderWithPermId> doLoadEntities(IDAOFactory daoFactory,
                    IManagedPropertyEvaluatorFactory evaluatorFactory, List<String> permIds)
            {
                List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByPermID(permIds);
                return ExperimentTranslator.translate(experiments, "", EMPTY_METAPROJECTS, evaluatorFactory);
            }
        },
        MATERIAL()
        {
            @Override
            public List<? extends IEntityInformationHolderWithPermId> doLoadEntities(IDAOFactory daoFactory,
                    IManagedPropertyEvaluatorFactory evaluatorFactory, List<String> permIds)
            {
                throw new UnsupportedOperationException();
            }
        };

        public List<IEntityInformationHolderWithPermId> loadEntities(IDAOFactory daoFactory,
                IManagedPropertyEvaluatorFactory evaluatorFactory, List<String> permIds)
        {
            List<IEntityInformationHolderWithPermId> result = new ArrayList<IEntityInformationHolderWithPermId>();
            for (IEntityInformationHolderWithPermId entity : doLoadEntities(daoFactory, evaluatorFactory, permIds))
            {
                result.add(entity);
            }
            return result;
        }

        public abstract List<? extends IEntityInformationHolderWithPermId> doLoadEntities(IDAOFactory daoFactory,
                IManagedPropertyEvaluatorFactory evaluatorFactory, List<String> permIds);
    }

    private static final class Selector
    {
        private EntityLoader loader;

        private String permId;

        Selector(ISearchDomainResultLocation resultLocation)
        {
            permId = null;
            loader = EntityLoader.DATA_SET;
            if (resultLocation instanceof DataSetFileSearchResultLocation)
            {
                permId = ((DataSetFileSearchResultLocation) resultLocation).getPermId();
            } else if (resultLocation instanceof EntityPropertySearchResultLocation)
            {
                EntityPropertySearchResultLocation location = (EntityPropertySearchResultLocation) resultLocation;
                permId = location.getPermId();
                loader = EntityLoader.valueOf(location.getEntityKind().toString());
            }
        }

        EntityLoader getLoader()
        {
            return loader;
        }

        String getPermId()
        {
            return permId;
        }

    }

}
