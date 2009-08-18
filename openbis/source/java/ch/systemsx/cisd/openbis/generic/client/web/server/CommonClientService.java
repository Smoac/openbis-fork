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

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListPersonsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService.IDataStoreBaseURLProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.AuthorizationGroupTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.RoleCodeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SearchableEntityTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.TypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TSVRenderer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;

/**
 * The {@link ICommonClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonClientService extends AbstractClientService implements
        ICommonClientService, IDataStoreBaseURLProvider
{
    private final ICommonServer commonServer;

    private String dataStoreBaseURL;

    public CommonClientService(final ICommonServer commonServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.commonServer = commonServer;
    }

    @Override
    public String getDataStoreBaseURL()
    {
        return this.dataStoreBaseURL;
    }

    public final void setDataStoreBaseURL(String dataStoreBaseURL)
    {
        this.dataStoreBaseURL = dataStoreBaseURL + "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME;
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
        getExportManager().removeData(exportDataKey);
        return exportCriteria;
    }

    private final <T> List<T> fetchCachedEntities(final TableExportCriteria<T> exportCriteria)
    {
        final IResultSetManager<String> resultSetManager = getResultSetManager();
        IResultSetConfig<String, T> resultSetConfig = createExportListCriteria(exportCriteria);
        IOriginalDataProvider<T> dummyDataProvider = createDummyDataProvider();
        final IResultSet<String, T> result =
                resultSetManager.getResultSet(resultSetConfig, dummyDataProvider);
        final ResultSet<T> entities = ResultSetTranslator.translate(result);
        return entities.getList();
    }

    private static <T> IOriginalDataProvider<T> createDummyDataProvider()
    {
        return new IOriginalDataProvider<T>()
            {
                public List<T> getOriginalData() throws UserFailureException
                {
                    throw new IllegalStateException("Data not found in the cache");
                }
            };
    }

    private static <T> IResultSetConfig<String, T> createExportListCriteria(
            final TableExportCriteria<T> exportCriteria)
    {
        final DefaultResultSetConfig<String, T> criteria = DefaultResultSetConfig.createFetchAll();
        criteria.setSortInfo(exportCriteria.getSortInfo());
        criteria.setFilterInfos(exportCriteria.getFilterInfos());
        criteria.setResultSetKey(exportCriteria.getResultSetKey());
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
            final List<T> entities = fetchCachedEntities(exportCriteria);
            return TSVRenderer.createTable(entities, exportCriteria.getColumnDefs(), lineSeparator);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void removeResultSet(final String resultSetKey)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
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
            commonServer.registerGroup(sessionToken, groupCode, descriptionOrNull);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void updateGroup(final IGroupUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";

        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updateGroup(sessionToken, updates);
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

    public final void registerGroupRole(final RoleSetCode roleSetCode, final String group,
            final Grantee grantee)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            commonServer.registerGroupRole(sessionToken, RoleCodeTranslator.translate(roleSetCode),
                    groupIdentifier, grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerInstanceRole(final RoleSetCode roleSetCode, final Grantee grantee)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerInstanceRole(sessionToken, RoleCodeTranslator
                    .translate(roleSetCode), grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void deleteGroupRole(final RoleSetCode roleSetCode, final String group,
            final Grantee grantee)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            commonServer.deleteGroupRole(sessionToken, RoleCodeTranslator.translate(roleSetCode),
                    groupIdentifier, grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final void deleteInstanceRole(final RoleSetCode roleSetCode, final Grantee grantee)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteInstanceRole(sessionToken,
                    RoleCodeTranslator.translate(roleSetCode), grantee);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final List<SampleType> listSampleTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<SampleTypePE> sampleTypes = commonServer.listSampleTypes(sessionToken);
            final List<SampleType> result = new ArrayList<SampleType>();
            for (final SampleTypePE sampleTypePE : sampleTypes)
            {
                result.add(SampleTypeTranslator.translate(sampleTypePE));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // --------- methods preparing exported content. Note: GWT does not support
    // generic methods :(

    public final String prepareExportSamples(final TableExportCriteria<Sample> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public final String prepareExportExperiments(final TableExportCriteria<Experiment> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public final String prepareExportMatchingEntities(
            final TableExportCriteria<MatchingEntity> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPropertyTypes(TableExportCriteria<PropertyType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPropertyTypeAssignments(
            TableExportCriteria<EntityTypePropertyType<?>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportProjects(TableExportCriteria<Project> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportVocabularies(final TableExportCriteria<Vocabulary> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportVocabularyTerms(TableExportCriteria<VocabularyTermWithStats> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportMaterialTypes(final TableExportCriteria<MaterialType> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportExperimentTypes(final TableExportCriteria<ExperimentType> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportSampleTypes(final TableExportCriteria<SampleType> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportDataSetTypes(final TableExportCriteria<DataSetType> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportFileTypes(TableExportCriteria<FileFormatType> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportAttachmentVersions(TableExportCriteria<AttachmentVersions> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportGroups(TableExportCriteria<Group> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportPersons(TableExportCriteria<Person> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportRoleAssignments(TableExportCriteria<RoleAssignment> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    // ---------------- methods which list entities using cache

    public final ResultSet<Sample> listSamples(final ListSampleCriteria listCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(listCriteria, new ListSamplesOriginalDataProvider(commonServer,
                sessionToken, listCriteria));
    }

    public ResultSet<ExternalData> searchForDataSets(final String baseIndexURL,
            DataSetSearchCriteria criteria,
            final IResultSetConfig<String, ExternalData> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(resultSetConfig, new ListDataSetsOriginalDataProvider(commonServer,
                sessionToken, criteria, getDataStoreBaseURL(), baseIndexURL));
    }

    public final ResultSet<Experiment> listExperiments(final ListExperimentsCriteria listCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(listCriteria, new ListExperimentsOriginalDataProvider(commonServer,
                listCriteria, sessionToken));
    }

    public ResultSet<PropertyType> listPropertyTypes(
            DefaultResultSetConfig<String, PropertyType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<PropertyType>()
            {
                public List<PropertyType> getOriginalData() throws UserFailureException
                {
                    return listPropertyTypes(true);
                }
            });
    }

    public final ResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final IResultSetConfig<String, MatchingEntity> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] matchingEntities =
                SearchableEntityTranslator.translate(searchableEntityOrNull);
        return listEntities(resultSetConfig, new ListMatchingEntitiesOriginalDataProvider(
                commonServer, sessionToken, matchingEntities, queryText));
    }

    public ResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignments(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> criteria)
    {
        return listEntities(criteria, new IOriginalDataProvider<EntityTypePropertyType<?>>()
            {
                public List<EntityTypePropertyType<?>> getOriginalData()
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

    public ResultSet<Group> listGroups(DefaultResultSetConfig<String, Group> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<Group>()
            {
                public List<Group> getOriginalData() throws UserFailureException
                {
                    return listGroups();
                }
            });
    }

    private List<Group> listGroups()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final DatabaseInstanceIdentifier identifier = new DatabaseInstanceIdentifier(null);
            final List<GroupPE> groups = commonServer.listGroups(sessionToken, identifier);
            return GroupTranslator.translate(groups);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<AuthorizationGroup> listAuthorizationGroups()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<AuthorizationGroupPE> groups =
                    commonServer.listAuthorizationGroups(sessionToken);
            return AuthorizationGroupTranslator.translate(groups);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<Person> listPersons(final ListPersonsCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<Person>()
            {
                public List<Person> getOriginalData() throws UserFailureException
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
            final List<PersonPE> persons = commonServer.listPersons(sessionToken);
            return PersonTranslator.translate(persons);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<RoleAssignment> listRoleAssignments(
            DefaultResultSetConfig<String, RoleAssignment> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<RoleAssignment>()
            {
                public List<RoleAssignment> getOriginalData() throws UserFailureException
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
            final List<RoleAssignmentPE> roles = commonServer.listRoleAssignments(sessionToken);
            return RoleAssignmentTranslator.translate(roles);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<Project> listProjects(DefaultResultSetConfig<String, Project> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<Project>()
            {
                public List<Project> getOriginalData() throws UserFailureException
                {
                    return listProjects();
                }
            });
    }

    private List<Project> listProjects()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<ProjectPE> projects = commonServer.listProjects(sessionToken);
            return ProjectTranslator.translate(projects);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<Vocabulary> listVocabularies(final boolean withTerms,
            final boolean excludeInternal, DefaultResultSetConfig<String, Vocabulary> criteria)
            throws UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<Vocabulary>()
            {
                public List<Vocabulary> getOriginalData() throws UserFailureException
                {
                    return listVocabularies(withTerms, excludeInternal);
                }
            });
    }

    private List<Vocabulary> listVocabularies(final boolean withTerms, boolean excludeInternal)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final List<VocabularyPE> vocabularies =
                    commonServer.listVocabularies(sessionToken, withTerms, excludeInternal);
            return BeanUtils.createBeanList(Vocabulary.class, vocabularies, DtoConverters
                    .getVocabularyConverter());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<VocabularyTermWithStats> listVocabularyTerms(final Vocabulary vocabulary,
            DefaultResultSetConfig<String, VocabularyTermWithStats> criteria)
    {
        return listEntities(criteria, new IOriginalDataProvider<VocabularyTermWithStats>()
            {
                public List<VocabularyTermWithStats> getOriginalData() throws UserFailureException
                {
                    List<ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats> terms =
                            commonServer.listVocabularyTermsWithStatistics(getSessionToken(),
                                    vocabulary);
                    return VocabularyTermTranslator.translate(terms);
                }
            });
    }

    public ResultSet<MaterialType> listMaterialTypes(
            DefaultResultSetConfig<String, MaterialType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<MaterialType>()
            {
                public List<MaterialType> getOriginalData() throws UserFailureException
                {
                    return listMaterialTypes();
                }
            });
    }

    public ResultSet<SampleType> listSampleTypes(DefaultResultSetConfig<String, SampleType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<SampleType>()
            {
                public List<SampleType> getOriginalData() throws UserFailureException
                {
                    return listSampleTypes();
                }
            });
    }

    public ResultSet<ExperimentType> listExperimentTypes(
            DefaultResultSetConfig<String, ExperimentType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<ExperimentType>()
            {
                public List<ExperimentType> getOriginalData() throws UserFailureException
                {
                    return listExperimentTypes();
                }
            });
    }

    public ResultSet<DataSetType> listDataSetTypes(
            DefaultResultSetConfig<String, DataSetType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<DataSetType>()
            {
                public List<DataSetType> getOriginalData() throws UserFailureException
                {
                    return listDataSetTypes();
                }
            });
    }

    public ResultSet<FileFormatType> listFileTypes(
            DefaultResultSetConfig<String, FileFormatType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<FileFormatType>()
            {
                public List<FileFormatType> getOriginalData() throws UserFailureException
                {
                    return listFileTypes();
                }
            });
    }

    public ResultSet<ExternalData> listSampleDataSets(final TechId sampleId,
            final String baseIndexURL, DefaultResultSetConfig<String, ExternalData> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<ExternalData>()
            {
                public List<ExternalData> getOriginalData() throws UserFailureException
                {
                    final String sessionToken = getSessionToken();
                    final List<ExternalDataPE> externalData =
                            commonServer.listSampleExternalData(sessionToken, sampleId);
                    return ExternalDataTranslator.translate(externalData, getDataStoreBaseURL(),
                            baseIndexURL);
                }
            });
    }

    public ResultSet<ExternalData> listExperimentDataSets(final TechId experimentId,
            final String baseIndexURL, DefaultResultSetConfig<String, ExternalData> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(criteria, new IOriginalDataProvider<ExternalData>()
            {

                public List<ExternalData> getOriginalData() throws UserFailureException
                {
                    final String sessionToken = getSessionToken();
                    final List<ExternalDataPE> externalData =
                            commonServer.listExperimentExternalData(sessionToken, experimentId);
                    return ExternalDataTranslator.translate(externalData, getDataStoreBaseURL(),
                            baseIndexURL);
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
            Collections.sort(searchableEntities);
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
            final List<ExperimentType> result = new ArrayList<ExperimentType>();
            final List<ExperimentTypePE> experiments =
                    commonServer.listExperimentTypes(sessionToken);
            for (final ExperimentTypePE expType : experiments)
            {
                result.add(ExperimentTranslator.translate(expType));
            }
            return result;
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
            final List<PropertyTypePE> propertyTypes =
                    commonServer.listPropertyTypes(sessionToken, withRelations);
            return PropertyTypeTranslator.translate(propertyTypes);
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
            final List<DataTypePE> dataTypes = commonServer.listDataTypes(sessionToken);
            return BeanUtils.createBeanList(DataType.class, dataTypes, DtoConverters
                    .getDataTypeConverter());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String assignPropertyType(final EntityKind entityKind, final String propertyTypeCode,
            final String entityTypeCode, final boolean isMandatory, final String defaultValue)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return commonServer.assignPropertyType(sessionToken, DtoConverters
                    .convertEntityKind(entityKind), propertyTypeCode, entityTypeCode, isMandatory,
                    defaultValue);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void updatePropertyTypeAssignment(final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.updatePropertyTypeAssignment(sessionToken, DtoConverters
                    .convertEntityKind(entityKind), propertyTypeCode, entityTypeCode, isMandatory,
                    defaultValue);
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
            commonServer.unassignPropertyType(sessionToken, DtoConverters
                    .convertEntityKind(entityKind), propertyTypeCode, entityTypeCode);
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
            return commonServer.countPropertyTypedEntities(sessionToken, DtoConverters
                    .convertEntityKind(entityKind), propertyTypeCode, entityTypeCode);
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
                extendVocabularyWithUploadedData(vocabulary, termsSessionKey);
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

    private final void extendVocabularyWithUploadedData(NewVocabulary vocabulary, String sessionKey)
    {
        VocabularyTermsExtractor extractor =
                new VocabularyTermsExtractor().prepareTerms(sessionKey);
        vocabulary.setTerms(extractor.getTerms());
    }

    private class VocabularyTermsExtractor
    {
        private List<VocabularyTerm> terms;

        private VocabularyTermsExtractor()
        {
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
                                        return new VocabularyTermObjectFactory(propertyMapper);
                                    }
                                });
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

        private List<VocabularyTerm> loadTermsFromFiles(UploadedFilesBean uploadedFiles,
                final BisTabFileLoader<VocabularyTerm> tabFileLoader)
        {
            final List<VocabularyTerm> results = new ArrayList<VocabularyTerm>();
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                final StringReader stringReader =
                        new StringReader(new String(multipartFile.getBytes()));
                final List<VocabularyTerm> loadedTerms =
                        tabFileLoader.load(new DelegatedReader(stringReader, multipartFile
                                .getOriginalFilename()));
                results.addAll(loadedTerms);
            }
            return results;
        }

    }

    public void addVocabularyTerms(TechId vocabularyId, List<String> vocabularyTerms)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary id.";

        if (vocabularyTerms != null && vocabularyTerms.isEmpty() == false)
        {
            try
            {
                final String sessionToken = getSessionToken();
                commonServer.addVocabularyTerms(sessionToken, vocabularyId, vocabularyTerms);
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
            Set<VocabularyTermPE> terms =
                    commonServer.listVocabularyTerms(sessionToken, vocabulary);
            return VocabularyTermTranslator.translateTerms(terms);
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
                public void register(List<AttachmentPE> attachments)
                {
                    final ProjectIdentifier projectIdentifier =
                            new ProjectIdentifierFactory(project.getIdentifier())
                                    .createIdentifier();
                    Person leader = project.getProjectLeader();
                    final String leaderId =
                            leader == null ? null : project.getProjectLeader().getUserId();
                    commonServer.registerProject(sessionToken, projectIdentifier, project
                            .getDescription(), leaderId, attachments);
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
            final List<MaterialType> result = new ArrayList<MaterialType>();
            final List<MaterialTypePE> projects = commonServer.listMaterialTypes(sessionToken);
            for (final MaterialTypePE expType : projects)
            {
                result.add(MaterialTypeTranslator.translate(expType));
            }
            return result;
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
            final List<DataSetType> result = new ArrayList<DataSetType>();
            final List<DataSetTypePE> types = commonServer.listDataSetTypes(sessionToken);
            for (final DataSetTypePE type : types)
            {
                result.add(DataSetTypeTranslator.translate(type));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<Material> listMaterials(ListMaterialCriteria criteria)
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

    public void deleteExperiments(List<TechId> experimentIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
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
            commonServer.deleteGroups(sessionToken, groupIds, reason);
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
        return listEntities(criteria, new IOriginalDataProvider<AttachmentVersions>()
            {
                public List<AttachmentVersions> getOriginalData() throws UserFailureException
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
            List<AttachmentPE> attachments = null;
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
            return AttachmentTranslator.translate(attachments);
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

    public Project getProjectInfo(TechId projectId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {

        try
        {
            final String sessionToken = getSessionToken();
            final ProjectPE project = commonServer.getProjectInfo(sessionToken, projectId);
            return ProjectTranslator.translate(project);
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
                public void register(List<AttachmentPE> attachments)
                {
                    ProjectUpdatesDTO updatesDTO = translate(updates);
                    updatesDTO.setAttachments(attachments);
                    Date date = commonServer.updateProject(sessionToken, updatesDTO);
                    modificationDate.setTime(date.getTime());
                }
            }
                .process(updates.getAttachmentSessionKey(), getHttpSession(), updates
                        .getAttachments());
        return modificationDate;
    }

    private static ProjectUpdatesDTO translate(ProjectUpdates updates)
    {
        ProjectUpdatesDTO updatesDTO = new ProjectUpdatesDTO();
        updatesDTO.setDescription(updates.getDescription());
        updatesDTO.setVersion(updates.getVersion());
        final ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(updates.getIdentifier()).createIdentifier();
        updatesDTO.setIdentifier(projectIdentifier);
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

    public IEntityInformationHolder getEntityInformationHolder(EntityKind entityKind, String permId)
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

    public String getTemplate(EntityKind entityKind, String type, boolean autoGenerate,
            boolean withExperiments)
    {
        try
        {
            String sessionToken = getSessionToken();
            ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind kind =
                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind
                            .valueOf(entityKind.name());
            return commonServer.getTemplateColumns(sessionToken, kind, type, autoGenerate,
                    withExperiments);
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
            final List<FileFormatType> result = new ArrayList<FileFormatType>();
            final List<FileFormatTypePE> types = commonServer.listFileFormatTypes(sessionToken);
            for (final FileFormatTypePE type : types)
            {
                result.add(TypeTranslator.translate(type));
            }
            return result;
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
            String resultSetKey = saveInCache(tableModel.getRows());
            return new TableModelReference(resultSetKey, tableModel.getHeader());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // Saves the specified rows in the cache.
    // Returns a key in the cache where the data were saved.
    private String saveInCache(final List<TableModelRow> tableModelRows)
    {
        DefaultResultSetConfig<String, TableModelRow> criteria =
                new DefaultResultSetConfig<String, TableModelRow>();
        criteria.setLimit(0); // we do not need any data now, just a key
        ResultSet<TableModelRow> resultSet =
                listEntities(criteria, new IOriginalDataProvider<TableModelRow>()
                    {
                        public List<TableModelRow> getOriginalData() throws UserFailureException
                        {
                            return tableModelRows;
                        }
                    });
        return resultSet.getResultSetKey();
    }

    public ResultSet<TableModelRow> listDatasetReport(
            DefaultResultSetConfig<String, TableModelRow> resultSetConfig)
    {
        IOriginalDataProvider<TableModelRow> dummyDataProvider = createDummyDataProvider();
        return listEntities(resultSetConfig, dummyDataProvider);
    }

    public String prepareExportDatasetReport(TableExportCriteria<TableModelRow> criteria)
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
            List<ExternalData> datasets = fetchCachedEntities(displayedItemsCriteria);
            if (serviceDescriptionOrNull != null)
            {
                datasets = filterDatasets(datasets, serviceDescriptionOrNull);
            }
            return ExternalData.extractCodes(datasets);
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

        return listEntities(resultSetConfig, new IOriginalDataProvider<AuthorizationGroup>()
            {
                public List<AuthorizationGroup> getOriginalData() throws UserFailureException
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

    public List<Person> listPersonsInAuthorizationGroup(TechId group)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return PersonTranslator.translate(commonServer.listPersonInAuthorizationGroup(
                    sessionToken, group));
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
}
