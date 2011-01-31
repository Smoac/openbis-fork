/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.UnicodeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ArchivingResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListPersonsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListScriptsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.ITableDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DataProviderAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.FileFormatTypesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ITableModelProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.MatchingEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ProjectsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.SampleProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ScriptProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.SpacesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.TableDataProviderFactory;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.VocabulariesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.VocabularyTermsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator.Escape;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SearchableEntityTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TSVRenderer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DynamicPropertyEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;

/**
 * The {@link ICommonClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonClientService extends AbstractClientService implements
        ICommonClientService
{
    private final ICommonServer commonServer;

    public CommonClientService(final ICommonServer commonServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.commonServer = commonServer;
    }

    @Override
    protected final IServer getServer()
    {
        return commonServer;
    }

    // ----------- export and listing with cache generic functionality

    /**
     * Returns and removes cached export criteria.
     */
    private final <T> TableExportCriteria<T> getAndRemoveExportCriteria(final String exportDataKey)
    {
        final CacheManager<String, TableExportCriteria<T>> exportManager = getExportManager();
        final TableExportCriteria<T> exportCriteria = exportManager.tryGetData(exportDataKey);
        assert exportCriteria != null : "No export criteria found at key " + exportDataKey;
        exportManager.removeData(exportDataKey);
        return exportCriteria;
    }

    protected final <T> GridRowModels<T> fetchCachedEntities(
            final TableExportCriteria<T> exportCriteria)
    {
        return fetchCachedEntities(exportCriteria, Escape.NO);
    }

    protected final <T> GridRowModels<T> fetchCachedEntities(
            final TableExportCriteria<T> exportCriteria, Escape escape)
    {
        IResultSetConfig<String, T> resultSetConfig = createExportListCriteria(exportCriteria);
        IOriginalDataProvider<T> dummyDataProvider = createDummyDataProvider();
        final IResultSet<String, T> result = getResultSet(resultSetConfig, dummyDataProvider);
        final ResultSet<T> entities = ResultSetTranslator.translate(result, escape);
        return entities.getList();
    }

    private static <T> IResultSetConfig<String, T> createExportListCriteria(
            final TableExportCriteria<T> exportCriteria)
    {
        final DefaultResultSetConfig<String, T> criteria = DefaultResultSetConfig.createFetchAll();
        criteria.setSortInfo(exportCriteria.getSortInfo());
        criteria.setFilters(exportCriteria.getFilters());
        criteria.setCacheConfig(ResultSetFetchConfig.createFetchFromCache(exportCriteria
                .getResultSetKey()));
        criteria.setAvailableColumns(exportCriteria.getAvailableColumns());
        criteria.setPresentedColumns(exportCriteria.getColumnDefs());
        criteria.setGridDisplayId(exportCriteria.getGridDisplayId());
        return criteria;
    }

    /**
     * Assumes that preparation of the export ( {@link #prepareExportSamples(TableExportCriteria)}
     * has been invoked before and returned with an exportDataKey passed here as a parameter.
     */
    public final String getExportTable(final String exportDataKey, final String lineSeparator)
    {
        // NOTE: no generics in GWT
        return getGenericExportTable(exportDataKey, lineSeparator);
    }

    private final <T> String getGenericExportTable(final String exportDataKey,
            final String lineSeparator)
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            final TableExportCriteria<T> exportCriteria = getAndRemoveExportCriteria(exportDataKey);
            final GridRowModels<T> entities = fetchCachedEntities(exportCriteria);
            ITableDataProvider dataProvider =
                    TableDataProviderFactory.createDataProvider(entities,
                            exportCriteria.getColumnDefs());
            return TSVRenderer.createTable(dataProvider, lineSeparator);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void removeResultSet(final String resultSetKey)
    {
        try
        {
            try
            {
                // Not directly needed but this refreshes the session.
                getSessionToken();
                getResultSetManager().removeResultSet(resultSetKey);
            } catch (ch.systemsx.cisd.common.exceptions.InvalidSessionException e)
            {
                return; // there is no session, so nothing has to be removed from it
            } catch (InvalidSessionException e)
            {
                return; // there is no session, so nothing has to be removed from it
            }
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // --------------- end export & listing

    //
    // IGenericClientService
    //

    public final void registerGroup(final String groupCode, final String descriptionOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerSpace(sessionToken, groupCode, descriptionOrNull);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void updateGroup(final ISpaceUpdates updates)
    {
        assert updates != null : "Unspecified updates.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updateSpace(sessionToken, updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void updateScript(final IScriptUpdates updates)
    {
        assert updates != null : "Unspecified updates.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updateScript(sessionToken, updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerPerson(final String code)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerPerson(sessionToken, code);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroupRole(final RoleWithHierarchy role, final String group,
            final Grantee grantee)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            commonServer.registerSpaceRole(sessionToken, role.getRoleCode(), groupIdentifier,
                    grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerInstanceRole(final RoleWithHierarchy role, final Grantee grantee)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerInstanceRole(sessionToken, role.getRoleCode(), grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void deleteGroupRole(final RoleWithHierarchy role, final String group,
            final Grantee grantee)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            commonServer
                    .deleteSpaceRole(sessionToken, role.getRoleCode(), groupIdentifier, grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final void deleteInstanceRole(final RoleWithHierarchy role, final Grantee grantee)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteInstanceRole(sessionToken, role.getRoleCode(), grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final List<SampleType> listSampleTypes()
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<SampleType> sampleTypes = commonServer.listSampleTypes(sessionToken);
            return sampleTypes;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // --------- methods preparing exported content. Note: GWT does not support
    // generic methods :(

    public String prepareExportSamples(TableExportCriteria<TableModelRowWithObject<Sample>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public final String prepareExportExperiments(final TableExportCriteria<Experiment> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public final String prepareExportMatchingEntities(
            final TableExportCriteria<TableModelRowWithObject<MatchingEntity>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPropertyTypes(TableExportCriteria<PropertyType> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPropertyTypeAssignments(
            TableExportCriteria<EntityTypePropertyType<?>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportProjects(
            TableExportCriteria<TableModelRowWithObject<Project>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportVocabularies(
            final TableExportCriteria<TableModelRowWithObject<Vocabulary>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportVocabularyTerms(
            TableExportCriteria<TableModelRowWithObject<VocabularyTermWithStats>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportMaterialTypes(final TableExportCriteria<MaterialType> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportExperimentTypes(final TableExportCriteria<ExperimentType> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportSampleTypes(final TableExportCriteria<SampleType> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportDataSetTypes(final TableExportCriteria<DataSetType> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportFileTypes(TableExportCriteria<FileFormatType> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportAttachmentVersions(TableExportCriteria<AttachmentVersions> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportScripts(TableExportCriteria<TableModelRowWithObject<Script>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportGroups(TableExportCriteria<TableModelRowWithObject<Space>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPersons(TableExportCriteria<Person> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportRoleAssignments(TableExportCriteria<RoleAssignment> criteria)
    {
        return prepareExportEntities(criteria);
    }

    // ---------------- methods which list entities using cache

    public final ResultSetWithEntityTypes<Sample> listSamples(
            final ListSampleDisplayCriteria listCriteria)
    {
        final String sessionToken = getSessionToken();
        return listEntitiesWithTypes(listCriteria, new ListSamplesOriginalDataProvider(
                commonServer, sessionToken, listCriteria));
    }

    public TypedTableResultSet<Sample> listSamples2(ListSampleDisplayCriteria2 criteria)
    {
        SampleProvider provider = new SampleProvider(commonServer, getSessionToken(), criteria);
        return listEntities(provider, criteria);
    }

    public ResultSetWithEntityTypes<ExternalData> searchForDataSets(
            DetailedSearchCriteria criteria,
            final IResultSetConfig<String, ExternalData> resultSetConfig)
    {
        final String sessionToken = getSessionToken();
        return listEntitiesWithTypes(resultSetConfig, new ListDataSetSearchOriginalDataProvider(
                commonServer, sessionToken, criteria));
    }

    public ResultSetWithEntityTypes<ExternalData> searchForDataSets(
            RelatedDataSetCriteria<? extends IEntityInformationHolder> criteria,
            final IResultSetConfig<String, ExternalData> resultSetConfig)
    {
        final String sessionToken = getSessionToken();
        DataSetRelatedEntities entities = extractRelatedEntities(criteria);
        return listEntitiesWithTypes(resultSetConfig, new ListRelatedDataSetOriginalDataProvider(
                commonServer, sessionToken, entities));
    }

    private <E extends IEntityInformationHolder> DataSetRelatedEntities extractRelatedEntities(
            RelatedDataSetCriteria<E> criteria)
    {
        List<TableModelRowWithObject<E>> rows = criteria.tryGetSelectedEntities();
        if (rows == null)
        {
            TableExportCriteria<TableModelRowWithObject<E>> displayedEntitiesCriteria =
                    criteria.tryGetDisplayedEntities();
            assert displayedEntitiesCriteria != null : "displayedEntitiesCriteria is null";
            rows = fetchCachedEntities(displayedEntitiesCriteria).extractOriginalObjects();
        }
        List<E> entities = new ArrayList<E>();
        for (TableModelRowWithObject<E> row : rows)
        {
            entities.add(row.getObjectOrNull());
        }
        return new DataSetRelatedEntities(entities);
    }

    public final ResultSet<Experiment> listExperiments(final ListExperimentsCriteria listCriteria)
    {
        final String sessionToken = getSessionToken();
        return listEntities(listCriteria, new ListExperimentsOriginalDataProvider(commonServer,
                listCriteria, sessionToken));
    }

    public ResultSet<PropertyType> listPropertyTypes(
            DefaultResultSetConfig<String, PropertyType> criteria)
    {
        return listEntities(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<PropertyType>()
                    {
                        @Override
                        public List<PropertyType> getFullOriginalData() throws UserFailureException
                        {
                            return listPropertyTypes(true);
                        }
                    });
    }

    public final TypedTableResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final boolean useWildcardSearchMode,
            final IResultSetConfig<String, TableModelRowWithObject<MatchingEntity>> resultSetConfig)
    {
        ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] matchingEntities =
                SearchableEntityTranslator.translate(searchableEntityOrNull);
        MatchingEntitiesProvider provider =
                new MatchingEntitiesProvider(commonServer, getSessionToken(), matchingEntities,
                        queryText, useWildcardSearchMode);
        return listEntities(provider, resultSetConfig);
    }

    public ResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignments(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> criteria)
    {
        return listEntities(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<EntityTypePropertyType<?>>()
                    {
                        @Override
                        public List<EntityTypePropertyType<?>> getFullOriginalData()
                                throws UserFailureException
                        {
                            return extractAssignments(listPropertyTypes(true));
                        }
                    });
    }

    private static List<EntityTypePropertyType<?>> extractAssignments(
            List<PropertyType> listPropertyTypes)
    {
        List<EntityTypePropertyType<?>> result = new ArrayList<EntityTypePropertyType<?>>();
        for (PropertyType propertyType : listPropertyTypes)
        {
            extractAssignments(result, propertyType);
        }
        Collections.sort(result);
        return result;
    }

    private static void extractAssignments(List<EntityTypePropertyType<?>> result,
            final PropertyType propertyType)
    {
        for (ExperimentTypePropertyType etpt : propertyType.getExperimentTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (SampleTypePropertyType etpt : propertyType.getSampleTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (MaterialTypePropertyType etpt : propertyType.getMaterialTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (DataSetTypePropertyType etpt : propertyType.getDataSetTypePropertyTypes())
        {
            result.add(etpt);
        }
    }

    private <T extends ISerializable> TypedTableResultSet<T> listEntities(
            ITableModelProvider<T> provider,
            IResultSetConfig<String, TableModelRowWithObject<T>> criteria)
    {
        try
        {
            DataProviderAdapter<T> dataProvider = new DataProviderAdapter<T>(provider);
            ResultSet<TableModelRowWithObject<T>> resultSet = listEntities(criteria, dataProvider);
            return new TypedTableResultSet<T>(resultSet);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public TypedTableResultSet<Space> listGroups(
            DefaultResultSetConfig<String, TableModelRowWithObject<Space>> criteria)
    {
        SpacesProvider spacesProvider = new SpacesProvider(commonServer, getSessionToken());
        return listEntities(spacesProvider, criteria);
    }

    public TypedTableResultSet<Script> listScripts(final ListScriptsCriteria criteria)
    {
        ScriptProvider scriptProvider =
                new ScriptProvider(commonServer, getSessionToken(), criteria.tryGetScriptType(),
                        criteria.tryGetEntityKind());
        return listEntities(scriptProvider, criteria);
    }

    public List<AuthorizationGroup> listAuthorizationGroups()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<AuthorizationGroup> authGroups =
                    commonServer.listAuthorizationGroups(sessionToken);
            return authGroups;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<Person> listPersons(final ListPersonsCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new AbstractOriginalDataProviderWithoutHeaders<Person>()
            {
                @Override
                public List<Person> getFullOriginalData() throws UserFailureException
                {
                    if (criteria.getAuthorizationGroupId() == null)
                        return listPersons();
                    else
                        return listPersonsInAuthorizationGroup(criteria.getAuthorizationGroupId());
                }
            });
    }

    public final List<Person> listPersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<Person> persons = commonServer.listPersons(sessionToken);
            return persons;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<RoleAssignment> listRoleAssignments(
            DefaultResultSetConfig<String, RoleAssignment> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<RoleAssignment>()
                    {
                        @Override
                        public List<RoleAssignment> getFullOriginalData()
                                throws UserFailureException
                        {
                            return listRoleAssignments();
                        }
                    });
    }

    public final List<RoleAssignment> listRoleAssignments()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<RoleAssignment> roles = commonServer.listRoleAssignments(sessionToken);
            return roles;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public TypedTableResultSet<Project> listProjects(
            DefaultResultSetConfig<String, TableModelRowWithObject<Project>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        ProjectsProvider projectsProvider = new ProjectsProvider(commonServer, getSessionToken());
        return listEntities(projectsProvider, criteria);
    }

    public TypedTableResultSet<Vocabulary> listVocabularies(boolean withTerms,
            boolean excludeInternal,
            DefaultResultSetConfig<String, TableModelRowWithObject<Vocabulary>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        VocabulariesProvider provider =
                new VocabulariesProvider(commonServer, getSessionToken(), withTerms,
                        excludeInternal);
        return listEntities(provider, criteria);
    }

    public TypedTableResultSet<VocabularyTermWithStats> listVocabularyTerms(
            final Vocabulary vocabulary,
            DefaultResultSetConfig<String, TableModelRowWithObject<VocabularyTermWithStats>> criteria)
    {
        VocabularyTermsProvider vocabularyTermsProvider =
                new VocabularyTermsProvider(commonServer, getSessionToken(), vocabulary);
        return listEntities(vocabularyTermsProvider, criteria);
    }

    public ResultSet<MaterialType> listMaterialTypes(
            DefaultResultSetConfig<String, MaterialType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<MaterialType>()
                    {
                        @Override
                        public List<MaterialType> getFullOriginalData() throws UserFailureException
                        {
                            return listMaterialTypes();
                        }
                    });
    }

    public ResultSet<SampleType> listSampleTypes(DefaultResultSetConfig<String, SampleType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new AbstractOriginalDataProviderWithoutHeaders<SampleType>()
            {
                @Override
                public List<SampleType> getFullOriginalData() throws UserFailureException
                {
                    return listSampleTypes();
                }
            });
    }

    public ResultSet<ExperimentType> listExperimentTypes(
            DefaultResultSetConfig<String, ExperimentType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<ExperimentType>()
                    {
                        @Override
                        public List<ExperimentType> getFullOriginalData()
                                throws UserFailureException
                        {
                            return listExperimentTypes();
                        }
                    });
    }

    public ResultSet<DataSetType> listDataSetTypes(
            DefaultResultSetConfig<String, DataSetType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new AbstractOriginalDataProviderWithoutHeaders<DataSetType>()
            {
                @Override
                public List<DataSetType> getFullOriginalData() throws UserFailureException
                {
                    return listDataSetTypes();
                }
            });
    }

    public TypedTableResultSet<FileFormatType> listFileTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        FileFormatTypesProvider provider =
                new FileFormatTypesProvider(commonServer, getSessionToken());
        return listEntities(provider, criteria);
    }

    public ResultSetWithEntityTypes<ExternalData> listSampleDataSets(final TechId sampleId,
            DefaultResultSetConfig<String, ExternalData> criteria,
            final boolean showOnlyDirectlyConnected)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntitiesWithTypes(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<ExternalData>()
                    {
                        @Override
                        public List<ExternalData> getFullOriginalData() throws UserFailureException
                        {
                            final String sessionToken = getSessionToken();
                            final List<ExternalData> externalData =
                                    commonServer.listSampleExternalData(sessionToken, sampleId,
                                            showOnlyDirectlyConnected);
                            return externalData;
                        }
                    });
    }

    public ResultSetWithEntityTypes<ExternalData> listExperimentDataSets(final TechId experimentId,
            DefaultResultSetConfig<String, ExternalData> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntitiesWithTypes(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<ExternalData>()
                    {

                        @Override
                        public List<ExternalData> getFullOriginalData() throws UserFailureException
                        {
                            final String sessionToken = getSessionToken();
                            final List<ExternalData> externalData =
                                    commonServer.listExperimentExternalData(sessionToken,
                                            experimentId);
                            return externalData;
                        }

                    });
    }

    public ResultSetWithEntityTypes<ExternalData> listDataSetRelationships(final TechId datasetId,
            final DataSetRelationshipRole role,
            final DefaultResultSetConfig<String, ExternalData> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntitiesWithTypes(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<ExternalData>()
                    {
                        @Override
                        public List<ExternalData> getFullOriginalData() throws UserFailureException
                        {
                            final String sessionToken = getSessionToken();
                            final List<ExternalData> externalData =
                                    commonServer.listDataSetRelationships(sessionToken, datasetId,
                                            role);
                            return externalData;
                        }
                    });
    }

    // ---------------- end list using cache ----------

    public final List<SearchableEntity> listSearchableEntities()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            final List<SearchableEntity> searchableEntities =
                    BeanUtils.createBeanList(SearchableEntity.class, Arrays
                            .asList(ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity
                                    .values()));
            return searchableEntities;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<ExperimentType> listExperimentTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<ExperimentType> experimentTypes =
                    commonServer.listExperimentTypes(sessionToken);
            return experimentTypes;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<PropertyType> listPropertyTypes(boolean withRelations)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<PropertyType> propertyTypes =
                    commonServer.listPropertyTypes(sessionToken, withRelations);
            return propertyTypes;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<DataType> listDataTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<DataType> dataTypes = commonServer.listDataTypes(sessionToken);
            return dataTypes;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String assignPropertyType(NewETPTAssignment assignment)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.assignPropertyType(sessionToken, assignment);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void updatePropertyTypeAssignment(NewETPTAssignment assignmentUpdates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updatePropertyTypeAssignment(sessionToken, assignmentUpdates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.unassignPropertyType(sessionToken, entityKind, propertyTypeCode,
                    entityTypeCode);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public int countPropertyTypedEntities(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.countPropertyTypedEntities(sessionToken, entityKind,
                    propertyTypeCode, entityTypeCode);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerPropertyType(final PropertyType propertyType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert propertyType != null : "Unspecified property type.";
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerPropertyType(sessionToken, propertyType);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void updatePropertyType(final IPropertyTypeUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updatePropertyType(sessionToken, updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void updateVocabularyTerm(final IVocabularyTermUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updateVocabularyTerm(sessionToken, updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerVocabulary(final String termsSessionKey,
            final NewVocabulary vocabulary)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabulary != null : "Unspecified vocabulary.";
        try
        {
            final String sessionToken = getSessionToken();
            if (vocabulary.isUploadedFromFile())
            {
                List<VocabularyTerm> extractedTerms =
                        extractVocabularyTermsFromUploadedData(termsSessionKey,
                                BatchOperationKind.REGISTRATION);
                vocabulary.setTerms(extractedTerms);
            }
            commonServer.registerVocabulary(sessionToken, vocabulary);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void updateVocabulary(final IVocabularyUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updateVocabulary(sessionToken, updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private final List<VocabularyTerm> extractVocabularyTermsFromUploadedData(String sessionKey,
            BatchOperationKind operationKind)
    {
        VocabularyTermsExtractor extractor =
                new VocabularyTermsExtractor(operationKind).prepareTerms(sessionKey);
        return extractor.getTerms();
    }

    private class VocabularyTermsExtractor
    {
        private List<VocabularyTerm> terms;

        private final BatchOperationKind operationKind;

        private VocabularyTermsExtractor(BatchOperationKind operationKind)
        {
            this.operationKind = operationKind;
        }

        public List<VocabularyTerm> getTerms()
        {
            return terms;
        }

        public VocabularyTermsExtractor prepareTerms(final String sessionKey)
        {
            HttpSession session = null;
            UploadedFilesBean uploadedFiles = null;
            try
            {
                session = getHttpSession();
                assert session.getAttribute(sessionKey) != null
                        && session.getAttribute(sessionKey) instanceof UploadedFilesBean : String
                        .format("No UploadedFilesBean object as session attribute '%s' found.",
                                sessionKey);
                uploadedFiles = (UploadedFilesBean) session.getAttribute(sessionKey);
                final BisTabFileLoader<VocabularyTerm> tabFileLoader = createTermsLoader();
                terms = loadTermsFromFiles(uploadedFiles, tabFileLoader);
                return this;
            } catch (final UserFailureException e)
            {
                throw UserFailureExceptionTranslator.translate(e);
            } finally
            {
                if (uploadedFiles != null)
                {
                    uploadedFiles.deleteTransferredFiles();
                }
                if (session != null)
                {
                    session.removeAttribute(sessionKey);
                }
            }
        }

        private BisTabFileLoader<VocabularyTerm> createTermsLoader()
        {
            final BisTabFileLoader<VocabularyTerm> tabFileLoader =
                    new BisTabFileLoader<VocabularyTerm>(
                            new IParserObjectFactoryFactory<VocabularyTerm>()
                                {
                                    public final IParserObjectFactory<VocabularyTerm> createFactory(
                                            final IPropertyMapper propertyMapper)
                                            throws ParserException
                                    {
                                        switch (operationKind)
                                        {
                                            case REGISTRATION:
                                                return new VocabularyTermObjectFactory(
                                                        propertyMapper);
                                            case UPDATE:
                                                return new UpdatedVocabularyTermObjectFactory(
                                                        propertyMapper);
                                        }
                                        throw new UnsupportedOperationException(operationKind
                                                + " is not supported");
                                    }
                                }, false);
            return tabFileLoader;
        }

        private class VocabularyTermObjectFactory extends
                AbstractParserObjectFactory<VocabularyTerm>
        {
            protected VocabularyTermObjectFactory(IPropertyMapper propertyMapper)
            {
                super(VocabularyTerm.class, propertyMapper);
            }

        }

        private class UpdatedVocabularyTermObjectFactory extends
                AbstractParserObjectFactory<VocabularyTerm>
        {

            private final VocabularyTermBatchUpdateDetails basicBatchUpdateDetails;

            protected UpdatedVocabularyTermObjectFactory(IPropertyMapper propertyMapper)
            {
                super(VocabularyTerm.class, propertyMapper);
                this.basicBatchUpdateDetails = createBasicBatchUpdateDetails();
            }

            /**
             * Prepares details about which values should be updated in general taking into account
             * only the information about availability of columns in the file.
             */
            private VocabularyTermBatchUpdateDetails createBasicBatchUpdateDetails()
            {
                boolean updateLabel = isColumnAvailable(UpdatedVocabularyTerm.LABEL);
                boolean updateDescription = isColumnAvailable(UpdatedVocabularyTerm.DESCRIPTION);
                return new VocabularyTermBatchUpdateDetails(updateLabel, updateDescription);
            }

            @Override
            public VocabularyTerm createObject(String[] lineTokens) throws ParserException
            {
                final VocabularyTerm term = super.createObject(lineTokens);
                final VocabularyTermBatchUpdateDetails updateDetails =
                        createBatchUpdateDetails(term);
                cleanUp(term);
                return new UpdatedVocabularyTerm(term, updateDetails);
            }

            //
            // handle empty values and deletion
            //

            /**
             * Returns details about which values should be updated for the specified term. If a
             * cell was left empty in the file the corresponding value will not be modified.
             */
            private VocabularyTermBatchUpdateDetails createBatchUpdateDetails(VocabularyTerm term)
            {
                final boolean updateLabel =
                        basicBatchUpdateDetails.isLabelUpdateRequested()
                                && isNotEmpty(term.getLabel());
                final boolean updateDescription =
                        basicBatchUpdateDetails.isDescriptionUpdateRequested()
                                && isNotEmpty(term.getDescription());
                return new VocabularyTermBatchUpdateDetails(updateLabel, updateDescription);
            }

            /** Cleans properties of the specified term that are marked for deletion. */
            private void cleanUp(VocabularyTerm term)
            {
                if (isDeletionMark(term.getLabel()))
                {
                    term.setLabel(null);
                }
                if (isDeletionMark(term.getDescription()))
                {
                    term.setDescription(null);
                }
            }
        }

        private List<VocabularyTerm> loadTermsFromFiles(UploadedFilesBean uploadedFiles,
                final BisTabFileLoader<VocabularyTerm> tabFileLoader)
        {
            final List<VocabularyTerm> results = new ArrayList<VocabularyTerm>();
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                Reader reader = UnicodeUtils.createReader(multipartFile.getInputStream());
                final List<VocabularyTerm> loadedTerms =
                        tabFileLoader.load(new DelegatedReader(reader, multipartFile
                                .getOriginalFilename()));
                results.addAll(loadedTerms);
            }
            // set initial order equivalent with order from the file
            Long order = 1L;
            for (VocabularyTerm term : results)
            {
                term.setOrdinal(order++);
            }
            return results;
        }
    }

    public void addVocabularyTerms(TechId vocabularyId, List<String> vocabularyTerms,
            Long previousTermOrdinal)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary id.";

        if (vocabularyTerms != null && vocabularyTerms.isEmpty() == false)
        {
            try
            {
                final String sessionToken = getSessionToken();
                commonServer.addVocabularyTerms(sessionToken, vocabularyId, vocabularyTerms,
                        previousTermOrdinal);
            } catch (final UserFailureException e)
            {
                throw UserFailureExceptionTranslator.translate(e);
            }
        }
    }

    public void deleteVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary id.";
        assert termsToBeDeleted != null : "Unspecified term to be deleted.";
        assert termsToBeReplaced != null : "Unspecified term to be replaced.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteVocabularyTerms(sessionToken, vocabularyId, termsToBeDeleted,
                    termsToBeReplaced);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<VocabularyTerm> listVocabularyTerms(Vocabulary vocabulary)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final Set<VocabularyTerm> terms =
                    commonServer.listVocabularyTerms(sessionToken, vocabulary);
            return new ArrayList<VocabularyTerm>(terms);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void registerProject(String sessionKey, final Project project)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert project != null : "Unspecified project.";
        final String sessionToken = getSessionToken();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    final ProjectIdentifier projectIdentifier =
                            new ProjectIdentifierFactory(project.getIdentifier())
                                    .createIdentifier();
                    Person leader = project.getProjectLeader();
                    final String leaderId =
                            leader == null ? null : project.getProjectLeader().getUserId();
                    commonServer.registerProject(sessionToken, projectIdentifier,
                            project.getDescription(), leaderId, attachments);
                }
            }.process(sessionKey, getHttpSession(), project.getNewAttachments());

    }

    public String prepareExportDataSetSearchHits(TableExportCriteria<ExternalData> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }

    public List<MaterialType> listMaterialTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<MaterialType> materialTypes = commonServer.listMaterialTypes(sessionToken);
            return materialTypes;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<DataSetType> listDataSetTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<DataSetType> types = commonServer.listDataSetTypes(sessionToken);
            return types;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<Material> listMaterials(ListMaterialDisplayCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListMaterialOriginalDataProvider(commonServer,
                sessionToken, criteria));
    }

    public String prepareExportMaterials(TableExportCriteria<Material> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public void registerMaterialType(MaterialType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerMaterialType(sessionToken, entityType);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void registerExperimentType(ExperimentType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerExperimentType(sessionToken, entityType);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void registerSampleType(SampleType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerSampleType(sessionToken, entityType);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void registerDataSetType(DataSetType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerDataSetType(sessionToken, entityType);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void registerFileType(FileFormatType type)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerFileFormatType(sessionToken, type);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void updateEntityType(EntityKind entityKind, EntityType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            switch (entityKind)
            {
                case MATERIAL:
                    commonServer.updateMaterialType(sessionToken, entityType);
                    break;
                case SAMPLE:
                    commonServer.updateSampleType(sessionToken, entityType);
                    break;
                case EXPERIMENT:
                    commonServer.updateExperimentType(sessionToken, entityType);
                    break;
                case DATA_SET:
                    commonServer.updateDataSetType(sessionToken, entityType);
                    break;
            }
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String uploadDataSets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria,
            DataSetUploadParameters uploadParameters)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            List<String> datasetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);
            return uploadDataSets(datasetCodes, uploadParameters);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private String uploadDataSets(List<String> dataSetCodes,
            DataSetUploadParameters uploadParameters)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            DataSetUploadContext uploadContext = new DataSetUploadContext();
            uploadContext.setCifexURL(uploadParameters.getCifexURL());
            uploadContext.setComment(uploadParameters.getComment());
            uploadContext.setFileName(uploadParameters.getFileName());
            uploadContext.setUserID(uploadParameters.getUserID());
            uploadContext.setPassword(uploadParameters.getPassword());
            return commonServer.uploadDataSets(sessionToken, dataSetCodes, uploadContext);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteDataSet(String singleData, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> dataSetCodes = Collections.singletonList(singleData);
            commonServer.deleteDataSets(sessionToken, dataSetCodes, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteDataSets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> dataSetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);
            commonServer.deleteDataSets(sessionToken, dataSetCodes, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteSamples(List<TechId> sampleIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteSamples(sessionToken, sampleIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteSample(TechId sampleId, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteSamples(sessionToken, Collections.singletonList(sampleId), reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteSamples(DisplayedOrSelectedIdHolderCriteria<? extends IIdHolder> criteria,
            String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<TechId> sampleIds = extractTechIds(criteria);
            commonServer.deleteSamples(sessionToken, sampleIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteExperiment(TechId experimentId, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteExperiments(sessionToken, Collections.singletonList(experimentId),
                    reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteExperiments(DisplayedOrSelectedIdHolderCriteria<Experiment> criteria,
            String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<TechId> experimentIds = extractTechIds(criteria);
            commonServer.deleteExperiments(sessionToken, experimentIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteVocabularies(List<TechId> vocabularyIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteVocabularies(sessionToken, vocabularyIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deletePropertyTypes(List<TechId> propertyTypeIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deletePropertyTypes(sessionToken, propertyTypeIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteProjects(List<TechId> projectIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteProjects(sessionToken, projectIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteGroups(List<TechId> groupIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteSpaces(sessionToken, groupIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteScripts(List<TechId> scriptIds)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteScripts(sessionToken, scriptIds);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteAttachments(TechId holderId, AttachmentHolderKind holderKind,
            List<String> fileNames, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            switch (holderKind)
            {
                case EXPERIMENT:
                    commonServer.deleteExperimentAttachments(sessionToken, holderId, fileNames,
                            reason);
                    break;
                case SAMPLE:
                    commonServer.deleteSampleAttachments(sessionToken, holderId, fileNames, reason);
                    break;
                case PROJECT:
                    commonServer
                            .deleteProjectAttachments(sessionToken, holderId, fileNames, reason);
                    break;
            }
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<AttachmentVersions> listAttachmentVersions(final TechId holderId,
            final AttachmentHolderKind holderKind,
            final DefaultResultSetConfig<String, AttachmentVersions> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria,
                new AbstractOriginalDataProviderWithoutHeaders<AttachmentVersions>()
                    {
                        @Override
                        public List<AttachmentVersions> getFullOriginalData()
                                throws UserFailureException
                        {
                            return listAttachmentVersions(holderId, holderKind);
                        }
                    });
    }

    private List<AttachmentVersions> listAttachmentVersions(TechId holderId,
            AttachmentHolderKind holderKind)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        List<Attachment> attachments = listAttachments(holderId, holderKind);
        List<AttachmentVersions> result = convert(attachments);
        return result;
    }

    private List<Attachment> listAttachments(TechId holderId, AttachmentHolderKind holderKind)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            List<Attachment> attachments = null;
            switch (holderKind)
            {
                case EXPERIMENT:
                    attachments = commonServer.listExperimentAttachments(sessionToken, holderId);
                    break;
                case SAMPLE:
                    attachments = commonServer.listSampleAttachments(sessionToken, holderId);
                    break;
                case PROJECT:
                    attachments = commonServer.listProjectAttachments(sessionToken, holderId);
                    break;
            }
            return attachments;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private List<AttachmentVersions> convert(final List<Attachment> attachments)
    {
        Map<String, List<Attachment>> map = new HashMap<String, List<Attachment>>();
        for (Attachment a : attachments)
        {
            if (false == map.containsKey(a.getFileName()))
            {
                map.put(a.getFileName(), new ArrayList<Attachment>());
            }
            map.get(a.getFileName()).add(a);
        }
        final List<AttachmentVersions> result = new ArrayList<AttachmentVersions>(map.size());
        for (List<Attachment> versions : map.values())
        {
            result.add(new AttachmentVersions(versions));
        }
        return result;
    }

    public LastModificationState getLastModificationState()
    {
        try
        {
            return commonServer.getLastModificationState(getSessionToken());
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Experiment getExperimentInfo(final String experimentIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experimentIdentifier).createIdentifier();
            final Experiment experiment = commonServer.getExperimentInfo(sessionToken, identifier);
            transformXML(experiment);
            return experiment;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Experiment getExperimentInfo(final TechId experimentId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final Experiment experiment =
                    commonServer.getExperimentInfo(sessionToken, experimentId);
            transformXML(experiment);
            return experiment;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public Project getProjectInfo(TechId projectId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final Project project = commonServer.getProjectInfo(sessionToken, projectId);
            return project;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public Project getProjectInfo(BasicProjectIdentifier projectIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final Project project =
                    commonServer.getProjectInfo(sessionToken, new ProjectIdentifier(
                            projectIdentifier));
            return project;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String generateCode(String prefix)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.generateCode(sessionToken, prefix);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public Date updateProject(final ProjectUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Date modificationDate = new Date();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    ProjectUpdatesDTO updatesDTO = translate(updates);
                    updatesDTO.setAttachments(attachments);
                    Date date = commonServer.updateProject(sessionToken, updatesDTO);
                    modificationDate.setTime(date.getTime());
                }
            }.process(updates.getAttachmentSessionKey(), getHttpSession(), updates.getAttachments());
        return modificationDate;
    }

    private static ProjectUpdatesDTO translate(ProjectUpdates updates)
    {
        ProjectUpdatesDTO updatesDTO = new ProjectUpdatesDTO();
        updatesDTO.setDescription(updates.getDescription());
        updatesDTO.setVersion(updates.getVersion());
        updatesDTO.setTechId(updates.getTechId());
        updatesDTO.setGroupCode(updates.getGroupCode());
        return updatesDTO;
    }

    public void deleteEntityTypes(EntityKind entityKind, List<String> entityTypesCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            switch (entityKind)
            {
                case DATA_SET:
                    commonServer.deleteDataSetTypes(sessionToken, entityTypesCodes);
                    break;
                case SAMPLE:
                    commonServer.deleteSampleTypes(sessionToken, entityTypesCodes);
                    break;
                case EXPERIMENT:
                    commonServer.deleteExperimentTypes(sessionToken, entityTypesCodes);
                    break;
                case MATERIAL:
                    commonServer.deleteMaterialTypes(sessionToken, entityTypesCodes);
                    break;
            }
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public IEntityInformationHolderWithPermId getEntityInformationHolder(EntityKind entityKind,
            String permId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            return commonServer.getEntityInformationHolder(sessionToken, entityKind, permId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public IEntityInformationHolderWithPermId getMaterialInformationHolder(
            MaterialIdentifier identifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            return commonServer.getMaterialInformationHolder(sessionToken, identifier);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String getTemplate(EntityKind entityKind, String type, boolean autoGenerate,
            boolean withExperiments, BatchOperationKind operationKind)
    {
        try
        {
            String sessionToken = getSessionToken();
            return commonServer.getTemplateColumns(sessionToken, entityKind, type, autoGenerate,
                    withExperiments, operationKind);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<FileFormatType> listFileTypes()
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<FileFormatType> types = commonServer.listFileFormatTypes(sessionToken);
            return types;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteFileFormatTypes(List<String> fileFormatTypeCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteFileFormatTypes(sessionToken, fileFormatTypeCodes);
        } catch (UserFailureException ex)
        {
            throw UserFailureExceptionTranslator.translate(ex);
        }
    }

    public void updateFileFormatType(AbstractType type)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updateFileFormatType(sessionToken, type);
        } catch (UserFailureException ex)
        {
            throw UserFailureExceptionTranslator.translate(ex);
        }
    }

    public void updateAttachment(TechId holderId, AttachmentHolderKind holderKind,
            Attachment attachment)
    {
        final String sessionToken = getSessionToken();
        try
        {
            switch (holderKind)
            {
                case EXPERIMENT:
                    commonServer.updateExperimentAttachments(sessionToken, holderId, attachment);
                    break;
                case SAMPLE:
                    commonServer.updateSampleAttachments(sessionToken, holderId, attachment);
                    break;
                case PROJECT:
                    commonServer.updateProjectAttachments(sessionToken, holderId, attachment);
                    break;
            }
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void addAttachment(final TechId holderId, String sessionKey,
            final AttachmentHolderKind holderKind, final NewAttachment attachment)
    {
        final String sessionToken = getSessionToken();

        AttachmentRegistrationHelper helper = new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    switch (holderKind)
                    {
                        case EXPERIMENT:
                            commonServer
                                    .addExperimentAttachment(sessionToken, holderId, attachment);
                            break;
                        case SAMPLE:
                            commonServer.addSampleAttachments(sessionToken, holderId, attachment);
                            break;
                        case PROJECT:
                            commonServer.addProjectAttachments(sessionToken, holderId, attachment);
                            break;
                    }
                }
            };
        try
        {
            helper.process(sessionKey, getHttpSession(), Collections.singletonList(attachment));
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private static final String MANAGED_PROPERTY_UPDATE_ERROR_MSG =
            "Problem occured when updating managed property. "
                    + "Contact instance admin about a possible bug in script definition.";

    public void updateManagedProperty(TechId entityId, EntityKind entityKind,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            switch (entityKind)
            {
                case EXPERIMENT:
                    commonServer.updateManagedPropertyOnExperiment(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
                case SAMPLE:
                    commonServer.updateManagedPropertyOnSample(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
                case DATA_SET:
                    commonServer.updateManagedPropertyOnDataSet(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
                case MATERIAL:
                    commonServer.updateManagedPropertyOnMaterial(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
            }
        } catch (final UserFailureException e)
        {
            // TODO
            // Throwable cause = e.getCause();
            // if (cause instanceof ValidationException)
            // {
            // throw new UserFailureException("Error in row " + (i + 1) + ": "
            // + cause.getMessage());
            // }
            if (operationLog.isInfoEnabled())
            {
                // we do not log this as an error, because it can be only user's fault
                operationLog.info(
                        MANAGED_PROPERTY_UPDATE_ERROR_MSG + " DETAILS: " + e.getMessage(), e);
            }
            throw UserFailureExceptionTranslator.translate(e, MANAGED_PROPERTY_UPDATE_ERROR_MSG);
        }
    }

    public List<DatastoreServiceDescription> listDataStoreServices(
            DataStoreServiceKind dataStoreServiceKind)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.listDataStoreServices(sessionToken, dataStoreServiceKind);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public TableModelReference createReportFromDatasets(
            DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> datasetCodes =
                    extractDatasetCodes(displayedOrSelectedDatasetCriteria, serviceDescription);
            final TableModel tableModel =
                    commonServer.createReportFromDatasets(sessionToken, serviceDescription,
                            datasetCodes);
            String resultSetKey = saveReportInCache(tableModel);
            return new TableModelReference(resultSetKey, tableModel.getHeader());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public TableModelReference createReportFromTableModel(TableModel tableModel)
    {
        try
        {
            String resultSetKey = saveReportInCache(tableModel);
            return new TableModelReference(resultSetKey, tableModel.getHeader());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public TypedTableResultSet<ReportRowModel> listReport(
            IResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> resultSetConfig)
    {
        IOriginalDataProvider<TableModelRowWithObject<ReportRowModel>> dataProvider =
                new IOriginalDataProvider<TableModelRowWithObject<ReportRowModel>>()
                    {
                        public List<TableModelColumnHeader> getHeaders()
                        {
                            return null;
                        }

                        public List<TableModelRowWithObject<ReportRowModel>> getOriginalData(
                                int maxSize) throws UserFailureException
                        {
                            throw new IllegalStateException("Data not found in the cache");
                        }

                    };
        return new TypedTableResultSet<ReportRowModel>(listEntities(resultSetConfig, dataProvider));
    }

    public String prepareExportReport(
            TableExportCriteria<TableModelRowWithObject<ReportRowModel>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    private List<String> extractDatasetCodes(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        return extractDatasetCodes(displayedOrSelectedDatasetCriteria, null);
    }

    private List<String> extractDatasetCodes(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria,
            DatastoreServiceDescription serviceDescriptionOrNull)
    {
        if (displayedOrSelectedDatasetCriteria.tryGetSelectedItems() != null)
        {
            return displayedOrSelectedDatasetCriteria.tryGetSelectedItems();
        } else
        {
            TableExportCriteria<ExternalData> displayedItemsCriteria =
                    displayedOrSelectedDatasetCriteria.tryGetDisplayedItems();
            assert displayedItemsCriteria != null : "displayedItemsCriteria is null";
            List<ExternalData> datasets =
                    fetchCachedEntities(displayedItemsCriteria).extractOriginalObjects();
            if (serviceDescriptionOrNull != null)
            {
                datasets = filterDatasets(datasets, serviceDescriptionOrNull);
            }
            return Code.extractCodes(datasets);
        }
    }

    // returns datasets which have type code belonging to the specified set and belong to the same
    // dataset store as the plugin
    private static List<ExternalData> filterDatasets(List<ExternalData> datasets,
            DatastoreServiceDescription serviceDescription)
    {
        String[] datasetTypeCodes = serviceDescription.getDatasetTypeCodes();
        Set<String> datasetTypeCodesMap = new HashSet<String>(Arrays.asList(datasetTypeCodes));
        List<ExternalData> result = new ArrayList<ExternalData>();
        String serviceDatastoreCode = serviceDescription.getDatastoreCode();
        for (ExternalData dataset : datasets)
        {
            String datasetTypeCode = dataset.getDataSetType().getCode();
            if (datasetTypeCodesMap.contains(datasetTypeCode))
            {
                String datasetDatastoreCode = dataset.getDataStore().getCode();
                if (datasetDatastoreCode.equals(serviceDatastoreCode))
                {
                    result.add(dataset);
                }
            }
        }
        return result;
    }

    public void processDatasets(DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> datasetCodes =
                    extractDatasetCodes(displayedOrSelectedDatasetCriteria, serviceDescription);
            commonServer.processDatasets(sessionToken, serviceDescription, datasetCodes);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ArchivingResult archiveDatasets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> datasetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);
            int result = commonServer.archiveDatasets(sessionToken, datasetCodes);
            return new ArchivingResult(datasetCodes.size(), result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ArchivingResult unarchiveDatasets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> datasetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);
            int result = commonServer.unarchiveDatasets(sessionToken, datasetCodes);
            return new ArchivingResult(datasetCodes.size(), result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteAuthorizationGroups(List<TechId> groupIds, String reason)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteAuthorizationGroups(sessionToken, groupIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<AuthorizationGroup> listAuthorizationGroups(
            DefaultResultSetConfig<String, AuthorizationGroup> resultSetConfig)
    {

        return listEntities(resultSetConfig,
                new AbstractOriginalDataProviderWithoutHeaders<AuthorizationGroup>()
                    {
                        @Override
                        public List<AuthorizationGroup> getFullOriginalData()
                                throws UserFailureException
                        {
                            return listAuthorizationGroups();
                        }
                    });
    }

    public String prepareExportAuthorizationGroups(
            TableExportCriteria<AuthorizationGroup> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    public void registerAuthorizationGroup(NewAuthorizationGroup newAuthorizationGroup)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerAuthorizationGroup(sessionToken, newAuthorizationGroup);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public void registerScript(Script script)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerScript(sessionToken, script);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public List<Person> listPersonsInAuthorizationGroup(TechId group)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.listPersonInAuthorizationGroup(sessionToken, group);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void updateAuthorizationGroup(AuthorizationGroupUpdates updates)
    {
        assert updates != null : "Unspecified updates.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updateAuthorizationGroup(sessionToken, updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public void addPersonsToAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.addPersonsToAuthorizationGroup(sessionToken, authorizationGroupId,
                    personsCodes);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void removePersonsFromAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.removePersonsFromAuthorizationGroup(sessionToken, authorizationGroupId,
                    personsCodes);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    private <T extends IIdHolder> List<TechId> extractTechIds(
            DisplayedOrSelectedIdHolderCriteria<T> displayedOrSelectedEntitiesCriteria)
    {
        if (displayedOrSelectedEntitiesCriteria.tryGetSelectedItems() != null)
        {
            return displayedOrSelectedEntitiesCriteria.tryGetSelectedItems();
        } else
        {
            TableExportCriteria<T> displayedItemsCriteria =
                    displayedOrSelectedEntitiesCriteria.tryGetDisplayedItems();
            assert displayedItemsCriteria != null : "displayedItemsCriteria is null";
            List<T> entities = fetchCachedEntities(displayedItemsCriteria).extractOriginalObjects();

            List<TechId> ids = new ArrayList<TechId>();
            for (T entity : entities)
            {
                if (entity instanceof TableModelRowWithObject<?>)
                {
                    TableModelRowWithObject<?> row = (TableModelRowWithObject<?>) entity;
                    Object object = row.getObjectOrNull();
                    if (object instanceof IIdHolder)
                    {
                        ids.add(TechId.create((IIdHolder) object));
                    }
                }
            }
            ids = TechId.createList(entities);
            return ids;
        }
    }

    // -- custom grid filters

    public List<GridCustomFilter> listFilters(String gridId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            return commonServer.listFilters(getSessionToken(), gridId);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<GridCustomFilter> listFilters(final String gridId,
            DefaultResultSetConfig<String, GridCustomFilter> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(resultSetConfig,
                new AbstractOriginalDataProviderWithoutHeaders<GridCustomFilter>()
                    {
                        @Override
                        public List<GridCustomFilter> getFullOriginalData()
                                throws UserFailureException
                        {
                            return listFilters(gridId);
                        }
                    });
    }

    public String prepareExportFilters(TableExportCriteria<GridCustomFilter> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public void registerFilter(NewColumnOrFilter filter)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert filter != null : "Unspecified filter.";
        try
        {
            commonServer.registerFilter(getSessionToken(), filter);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteFilters(List<TechId> filterIds)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            commonServer.deleteFilters(getSessionToken(), filterIds);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void updateFilter(final IExpressionUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";
        try
        {
            commonServer.updateFilter(getSessionToken(), updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // -- grid custom columns

    public List<GridCustomColumn> listGridCustomColumns(String gridId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            return commonServer.listGridCustomColumns(getSessionToken(), gridId);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<GridCustomColumn> listGridCustomColumns(final String gridId,
            DefaultResultSetConfig<String, GridCustomColumn> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(resultSetConfig,
                new AbstractOriginalDataProviderWithoutHeaders<GridCustomColumn>()
                    {
                        @Override
                        public List<GridCustomColumn> getFullOriginalData()
                                throws UserFailureException
                        {
                            return listGridCustomColumns(gridId);
                        }
                    });
    }

    public String prepareExportColumns(TableExportCriteria<GridCustomColumn> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public void registerColumn(NewColumnOrFilter newColumn)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert newColumn != null : "Unspecified grid custom column.";
        try
        {
            commonServer.registerGridCustomColumn(getSessionToken(), newColumn);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public void deleteColumns(List<TechId> columnIds)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            commonServer.deleteGridCustomColumns(getSessionToken(), columnIds);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public void updateColumn(IExpressionUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified grid custom updates.";
        try
        {
            commonServer.updateGridCustomColumn(getSessionToken(), updates);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // --

    public Boolean keepSessionAlive()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            commonServer.keepSessionAlive(getSessionToken());
            return true;
        } catch (final InvalidSessionException e)
        {
            // most probable cause - user logged out
            return false;
        } catch (final UserFailureException e)
        {
            // should not happen
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void updateVocabularyTerms(String termsSessionKey, TechId vocabularyId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary.";
        try
        {
            final String sessionToken = getSessionToken();
            List<VocabularyTerm> extractedTerms =
                    extractVocabularyTermsFromUploadedData(termsSessionKey,
                            BatchOperationKind.UPDATE);
            commonServer.updateVocabularyTerms(sessionToken, vocabularyId, extractedTerms);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void deleteMaterials(DisplayedOrSelectedIdHolderCriteria<Material> criteria,
            String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<TechId> materialIds = extractTechIds(criteria);
            commonServer.deleteMaterials(sessionToken, materialIds, reason);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ArchivingResult lockDatasets(DisplayedOrSelectedDatasetCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> datasetCodes = extractDatasetCodes(criteria);
            int result = commonServer.lockDatasets(sessionToken, datasetCodes);
            return new ArchivingResult(datasetCodes.size(), result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ArchivingResult unlockDatasets(DisplayedOrSelectedDatasetCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            List<String> datasetCodes = extractDatasetCodes(criteria);
            int result = commonServer.unlockDatasets(sessionToken, datasetCodes);
            return new ArchivingResult(datasetCodes.size(), result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public LinkModel retrieveLinkFromDataSet(DatastoreServiceDescription serviceDescription,
            String dataSetCode)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final LinkModel url =
                    commonServer.retrieveLinkFromDataSet(sessionToken, serviceDescription,
                            dataSetCode);
            return url;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public Script getScriptInfo(TechId scriptId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final Script script = commonServer.getScriptInfo(sessionToken, scriptId);
            return script;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String evaluate(DynamicPropertyEvaluationInfo info)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.evaluate(sessionToken, info);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public IEntityInformationHolderWithPermId getEntityInformationHolder(BasicEntityDescription info)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            return commonServer.getEntityInformationHolder(sessionToken, info);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

}
