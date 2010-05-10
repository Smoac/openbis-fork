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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAttachmentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAuthorizationGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGridCustomFilterOrColumnBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyTermBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.HibernateSearchDataProvider;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.AuthorizationGroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreServiceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.TypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomFilterTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

public final class CommonServer extends AbstractCommonServer<ICommonServer> implements
        ICommonServer
{
    private final LastModificationState lastModificationState;

    public CommonServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final ICommonBusinessObjectFactory businessObjectFactory,
            LastModificationState lastModificationState)
    {
        super(authenticationService, sessionManager, daoFactory, businessObjectFactory);
        this.lastModificationState = lastModificationState;
    }

    ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
    }

    private static UserFailureException createUserFailureException(final DataAccessException ex)
    {
        return new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final ICommonServer createLogger(IInvocationLoggerContext context)
    {
        return new CommonServerLogger(getSessionManager(), context);
    }

    //
    // IGenericServer
    //

    public final List<Space> listSpaces(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final Session session = getSession(sessionToken);
        final DatabaseInstancePE databaseInstance =
                GroupIdentifierHelper.getDatabaseInstance(identifier, getDAOFactory());
        final List<GroupPE> groups = getDAOFactory().getGroupDAO().listGroups(databaseInstance);
        final GroupPE homeGroupOrNull = session.tryGetHomeGroup();
        for (final GroupPE group : groups)
        {
            group.setHome(group.equals(homeGroupOrNull));
        }
        Collections.sort(groups);
        return GroupTranslator.translate(groups);
    }

    public final void registerSpace(final String sessionToken, final String spaceCode,
            final String descriptionOrNull)
    {
        final Session session = getSession(sessionToken);
        final IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
        groupBO.define(spaceCode, descriptionOrNull);
        groupBO.save();
    }

    public final void updateSpace(final String sessionToken, final ISpaceUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
        groupBO.update(updates);
    }

    public final void registerPerson(final String sessionToken, final String userID)
    {
        registerPersons(sessionToken, Arrays.asList(userID));
    }

    public final List<RoleAssignment> listRoleAssignments(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<RoleAssignmentPE> roles =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        return RoleAssignmentTranslator.translate(roles);
    }

    public final void registerSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setGrantee(grantee);
        newRoleAssignment.setSpaceIdentifier(spaceIdentifier);
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setGrantee(grantee);
        newRoleAssignment.setDatabaseInstanceIdentifier(new DatabaseInstanceIdentifier(
                DatabaseInstanceIdentifier.HOME));
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void deleteSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindGroupRoleAssignment(roleCode,
                        spaceIdentifier.getSpaceCode(), grantee);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given space role does not exist.");
        }
        final PersonPE personPE = session.tryGetPerson();
        if (roleAssignment.getPerson() != null && roleAssignment.getPerson().equals(personPE)
                && roleAssignment.getRole().equals(RoleCode.ADMIN))
        {
            boolean isInstanceAdmin = false;
            for (final RoleAssignmentPE roleAssigment : personPE.getRoleAssignments())
            {
                if (roleAssigment.getDatabaseInstance() != null
                        && roleAssigment.getRole().equals(RoleCode.ADMIN))
                {
                    isInstanceAdmin = true;
                }
            }
            if (isInstanceAdmin == false)
            {
                throw new UserFailureException(
                        "For safety reason you cannot give away your own space admin power. "
                                + "Ask instance admin to do that for you.");
            }
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    public final void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        final Session session = getSession(sessionToken);
        final IRoleAssignmentDAO roleAssignmentDAO = getDAOFactory().getRoleAssignmentDAO();
        final RoleAssignmentPE roleAssignment =
                roleAssignmentDAO.tryFindInstanceRoleAssignment(roleCode, grantee);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given database instance role does not exist.");
        }
        if (roleAssignment.getPerson() != null
                && roleAssignment.getPerson().equals(session.tryGetPerson())
                && roleAssignment.getRole().equals(RoleCode.ADMIN)
                && roleAssignment.getDatabaseInstance() != null)
        {
            throw new UserFailureException(
                    "For safety reason you cannot give away your own omnipotence. "
                            + "Ask another instance admin to do that for you.");
        }
        roleAssignmentDAO.deleteRoleAssignment(roleAssignment);
    }

    public final List<Person> listPersons(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();
        Collections.sort(persons);
        return PersonTranslator.translate(persons);
    }

    public final List<Project> listProjects(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return ProjectTranslator.translate(projects);
    }

    public final List<SampleType> listSampleTypes(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<SampleTypePE> sampleTypes = getDAOFactory().getSampleTypeDAO().listSampleTypes();
        Collections.sort(sampleTypes);
        return SampleTypeTranslator.translate(sampleTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    public final List<Sample> listSamples(final String sessionToken,
            final ListSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(new ListOrSearchSampleCriteria(criteria));
    }

    public List<Sample> searchForSamples(String sessionToken, DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        try
        {
            IHibernateSearchDAO searchDAO = getDAOFactory().getHibernateSearchDAO();
            final Collection<Long> sampleIds =
                    searchDAO.searchForEntityIds(criteria, DtoConverters
                            .convertEntityKind(EntityKind.SAMPLE));
            final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
            return sampleLister.list(new ListOrSearchSampleCriteria(sampleIds));
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public final List<ExternalData> listSampleExternalData(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<ExternalData> datasets =
                datasetLister.listBySampleTechId(sampleId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    public final List<ExternalData> listExperimentExternalData(final String sessionToken,
            final TechId experimentId)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<ExternalData> datasets =
                datasetLister.listByExperimentTechIds(Collections.singleton(experimentId));
        Collections.sort(datasets);
        return datasets;
    }

    // 'fast' implementation
    public List<ExternalData> listDataSetRelationships(String sessionToken, TechId datasetId,
            DataSetRelationshipRole role)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        List<ExternalData> datasets = null;
        switch (role)
        {
            case CHILD:
                datasets = datasetLister.listByChildTechId(datasetId);
                break;
            case PARENT:
                datasets = datasetLister.listByParentTechId(datasetId);
                break;
        }
        Collections.sort(datasets);
        return datasets;
    }

    public final List<PropertyType> listPropertyTypes(final String sessionToken,
            boolean withRelations)
    {
        final Session session = getSession(sessionToken);
        final IPropertyTypeTable propertyTypeTable =
                businessObjectFactory.createPropertyTypeTable(session);
        if (withRelations)
        {
            propertyTypeTable.loadWithRelations();
        } else
        {
            propertyTypeTable.load();
        }
        final List<PropertyTypePE> propertyTypes = propertyTypeTable.getPropertyTypes();
        Collections.sort(propertyTypes);
        return PropertyTypeTranslator.translate(propertyTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    public final List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText,
            final boolean useWildcardSearchMode)
    {
        checkSession(sessionToken);
        final List<MatchingEntity> list = new ArrayList<MatchingEntity>();
        try
        {
            for (final SearchableEntity searchableEntity : searchableEntities)
            {
                HibernateSearchDataProvider dataProvider =
                        new HibernateSearchDataProvider(getDAOFactory());
                list.addAll(getDAOFactory().getHibernateSearchDAO().searchEntitiesByTerm(
                        searchableEntity, queryText, dataProvider, useWildcardSearchMode));
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
        return list;
    }

    public final List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IExperimentTable experimentTable =
                businessObjectFactory.createExperimentTable(session);
        experimentTable.load(experimentType.getCode(), projectIdentifier);
        final List<ExperimentPE> experiments = experimentTable.getExperiments();
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL(),
                ExperimentTranslator.LoadableFields.PROPERTIES);
    }

    public final List<ExperimentType> listExperimentTypes(final String sessionToken)
    {
        final List<ExperimentTypePE> experimentTypes =
                listEntityTypes(sessionToken, EntityKind.EXPERIMENT);
        return ExperimentTranslator.translate(experimentTypes);
    }

    public List<MaterialType> listMaterialTypes(String sessionToken)
    {
        final List<MaterialTypePE> materialTypes =
                listEntityTypes(sessionToken, EntityKind.MATERIAL);
        return MaterialTypeTranslator.translate(materialTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    private <T extends EntityTypePE> List<T> listEntityTypes(String sessionToken,
            EntityKind entityKind)
    {
        checkSession(sessionToken);
        final List<T> types =
                getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind))
                        .listEntityTypes();
        Collections.sort(types);
        return types;
    }

    public final List<DataType> listDataTypes(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<DataTypePE> dataTypePEs = getDAOFactory().getPropertyTypeDAO().listDataTypes();
        final List<DataType> dataTypes = DataTypeTranslator.translate(dataTypePEs);
        Collections.sort(dataTypes, new Comparator<DataType>()
            {
                public int compare(DataType o1, DataType o2)
                {
                    return o1.getCode().name().compareTo(o2.getCode().name());
                }
            });
        return dataTypes;
    }

    public List<FileFormatType> listFileFormatTypes(String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<FileFormatTypePE> fileFormatTypePEs =
                getDAOFactory().getFileFormatTypeDAO().listFileFormatTypes();
        final List<FileFormatType> fileFormatTypes = TypeTranslator.translate(fileFormatTypePEs);
        Collections.sort(fileFormatTypes, new Comparator<FileFormatType>()
            {
                public int compare(FileFormatType o1, FileFormatType o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
        return fileFormatTypes;
    }

    public final List<Vocabulary> listVocabularies(final String sessionToken,
            final boolean withTerms, boolean excludeInternal)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<VocabularyPE> vocabularies =
                getDAOFactory().getVocabularyDAO().listVocabularies(excludeInternal);
        if (withTerms)
        {
            for (final VocabularyPE vocabularyPE : vocabularies)
            {
                enrichWithTerms(vocabularyPE);
            }
        }
        Collections.sort(vocabularies);
        return VocabularyTranslator.translate(vocabularies);
    }

    private void enrichWithTerms(final VocabularyPE vocabularyPE)
    {
        HibernateUtils.initialize(vocabularyPE.getTerms());
    }

    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue, final String section, final Long previousETPTOrdinal)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        final ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind kind =
                DtoConverters.convertEntityKind(entityKind);
        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, kind);
        etptBO.createAssignment(propertyTypeCode, entityTypeCode, isMandatory, defaultValue,
                section, previousETPTOrdinal);
        return String.format("%s property type '%s' successfully assigned to %s type '%s'",
                isMandatory ? "Mandatory" : "Optional", propertyTypeCode, kind.getLabel(),
                entityTypeCode);
    }

    public void updatePropertyTypeAssignment(final String sessionToken,
            final EntityKind entityKind, final String propertyTypeCode,
            final String entityTypeCode, final boolean isMandatory, final String defaultValue,
            final String section, final Long previousETPTOrdinal)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, DtoConverters
                        .convertEntityKind(entityKind));
        etptBO.loadAssignment(propertyTypeCode, entityTypeCode);
        etptBO.updateLoadedAssignment(isMandatory, defaultValue, section, previousETPTOrdinal);
    }

    public void unassignPropertyType(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, DtoConverters
                        .convertEntityKind(entityKind));
        etptBO.loadAssignment(propertyTypeCode, entityTypeCode);
        etptBO.deleteLoadedAssignment();
    }

    public int countPropertyTypedEntities(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, DtoConverters
                        .convertEntityKind(entityKind));
        return etptBO.countAssignmentValues(propertyTypeCode, entityTypeCode);
    }

    public final void registerPropertyType(final String sessionToken,
            final PropertyType propertyType)
    {
        assert sessionToken != null : "Unspecified session token";
        assert propertyType != null : "Unspecified property type";

        final Session session = getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.define(propertyType);
        propertyTypeBO.save();
    }

    public final void updatePropertyType(final String sessionToken,
            final IPropertyTypeUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.update(updates);
    }

    public final void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabulary != null : "Unspecified vocabulary";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
    }

    public final void updateVocabulary(final String sessionToken, final IVocabularyUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.update(updates);
    }

    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<String> vocabularyTerms, Long previousTermOrdinal)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";
        assert previousTermOrdinal != null : "Unspecified previous term ordinal";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.addNewTerms(vocabularyTerms, previousTermOrdinal);
        vocabularyBO.save();
    }

    public final void updateVocabularyTerm(final String sessionToken,
            final IVocabularyTermUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IVocabularyTermBO vocabularyTermBO =
                businessObjectFactory.createVocabularyTermBO(session);
        vocabularyTermBO.update(updates);
    }

    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.delete(termsToBeDeleted, termsToBeReplaced);
        vocabularyBO.save();
    }

    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId, Collection<NewAttachment> attachments)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        projectBO.define(projectIdentifier, description, leaderId);
        projectBO.save();
        for (NewAttachment attachment : attachments)
        {
            final AttachmentPE attachmentPE = AttachmentTranslator.translate(attachment);
            projectBO.addAttachment(attachmentPE);
        }
        projectBO.save();

    }

    public List<ExternalData> searchForDataSets(String sessionToken, DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);

        try
        {
            IHibernateSearchDAO searchDAO = getDAOFactory().getHibernateSearchDAO();

            final Collection<Long> datasetIds =
                    searchDAO.searchForEntityIds(criteria, DtoConverters
                            .convertEntityKind(EntityKind.DATA_SET));
            final IDatasetLister datasetLister = createDatasetLister(session);
            return datasetLister.listByDatasetIds(datasetIds);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public ExternalData getDataSetInfo(final String sessionToken, final TechId datasetId)
    {
        final Session session = getSession(sessionToken);
        final IExternalDataBO datasetBO = businessObjectFactory.createExternalDataBO(session);
        datasetBO.loadDataByTechId(datasetId);
        datasetBO.enrichWithParentsAndExperiment();
        datasetBO.enrichWithChildren();
        datasetBO.enrichWithProperties();
        final ExternalDataPE dataset = datasetBO.getExternalData();
        return ExternalDataTranslator.translate(dataset, getDataStoreBaseURL(), session
                .getBaseIndexURL(), false);
    }

    public List<ExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities relatedEntities)
    {
        final Session session = getSession(sessionToken);
        try
        {
            final Set<ExternalDataPE> resultSet = new LinkedHashSet<ExternalDataPE>();
            // TODO 2009-08-17, Piotr Buczek: optimize performance
            addRelatedDataSets(resultSet, relatedEntities.getEntities());
            final List<ExternalData> list = new ArrayList<ExternalData>(resultSet.size());
            for (final ExternalDataPE hit : resultSet)
            {
                list.add(ExternalDataTranslator.translate(hit, getDataStoreBaseURL(), session
                        .getBaseIndexURL(), false));
            }
            return list;
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    private void addRelatedDataSets(final Set<ExternalDataPE> resultSet,
            final List<? extends IEntityInformationHolder> relatedEntities)
    {
        final IExternalDataDAO externalDataDAO = getDAOFactory().getExternalDataDAO();
        for (IEntityInformationHolder entity : relatedEntities)
        {
            if (isEntityKindRelatedWithDataSets(entity.getEntityKind()))
            {
                List<ExternalDataPE> relatedDataSets =
                        externalDataDAO.listRelatedExternalData(entity);
                resultSet.addAll(relatedDataSets);
            }
        }
    }

    private boolean isEntityKindRelatedWithDataSets(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
            case SAMPLE:
                return true;
            default:
                return false;
        }
    }

    public List<Material> listMaterials(String sessionToken, MaterialType materialType,
            boolean withProperties)
    {
        final Session session = getSession(sessionToken);
        final IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        return materialLister.list(materialType, withProperties);
    }

    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        final Session session = getSession(sessionToken);
        try
        {
            IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
            entityTypeBO.define(entityType);
            entityTypeBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void updateSampleType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.SAMPLE, entityType);
    }

    public void registerMaterialType(String sessionToken, MaterialType entityType)
    {
        final Session session = getSession(sessionToken);
        try
        {
            IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
            entityTypeBO.define(entityType);
            entityTypeBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void updateMaterialType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.MATERIAL, entityType);
    }

    public void registerExperimentType(String sessionToken, ExperimentType entityType)
    {
        final Session session = getSession(sessionToken);
        try
        {
            IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
            entityTypeBO.define(entityType);
            entityTypeBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void updateExperimentType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.EXPERIMENT, entityType);
    }

    public void registerFileFormatType(String sessionToken, FileFormatType type)
    {
        checkSession(sessionToken);
        FileFormatTypePE fileFormatType = new FileFormatTypePE();
        try
        {
            fileFormatType.setCode(type.getCode());
            fileFormatType.setDescription(type.getDescription());
            getDAOFactory().getFileFormatTypeDAO().createOrUpdate(fileFormatType);
        } catch (final DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, String.format(
                    "File format type '%s' ", fileFormatType.getCode()), null);
        }
    }

    public void registerDataSetType(String sessionToken, DataSetType entityType)
    {
        final Session session = getSession(sessionToken);
        try
        {
            IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
            entityTypeBO.define(entityType);
            entityTypeBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void updateDataSetType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.DATA_SET, entityType);
    }

    private void updateEntityType(String sessionToken, EntityKind entityKind, EntityType entityType)
    {
        checkSession(sessionToken);
        try
        {
            IEntityTypeDAO entityTypeDAO =
                    getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind));
            EntityTypePE entityTypePE =
                    entityTypeDAO.tryToFindEntityTypeByCode(entityType.getCode());
            entityTypePE.setDescription(entityType.getDescription());
            updateSpecificEntityTypeProperties(entityKind, entityTypePE, entityType);
            entityTypeDAO.createOrUpdateEntityType(entityTypePE);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    private void updateSpecificEntityTypeProperties(EntityKind entityKind,
            EntityTypePE entityTypePE, EntityType entityType)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            SampleTypePE sampleTypePE = (SampleTypePE) entityTypePE;
            SampleType sampleType = (SampleType) entityType;
            sampleTypePE.setListable(sampleType.isListable());
            sampleTypePE.setAutoGeneratedCode(sampleType.isAutoGeneratedCode());
            sampleTypePE.setGeneratedCodePrefix(sampleType.getGeneratedCodePrefix());
            sampleTypePE.setContainerHierarchyDepth(sampleType.getContainerHierarchyDepth());
            sampleTypePE
                    .setGeneratedFromHierarchyDepth(sampleType.getGeneratedFromHierarchyDepth());
        } else if (entityKind == EntityKind.DATA_SET)
        {
            DataSetTypePE dataSetTypePE = (DataSetTypePE) entityTypePE;
            DataSetType dataSetType = (DataSetType) entityType;
            dataSetTypePE.setMainDataSetPath(dataSetType.getMainDataSetPath());
            dataSetTypePE.setMainDataSetPattern(dataSetType.getMainDataSetPattern());
        }
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IExternalDataTable externalDataTable =
                    businessObjectFactory.createExternalDataTable(session);
            externalDataTable.loadByDataSetCodes(dataSetCodes, false, false);
            List<ExternalDataPE> dataSets = externalDataTable.getExternalData();
            Map<DataSetTypePE, List<ExternalDataPE>> groupedDataSets =
                    new LinkedHashMap<DataSetTypePE, List<ExternalDataPE>>();
            for (ExternalDataPE dataSet : dataSets)
            {
                DataSetTypePE dataSetType = dataSet.getDataSetType();
                List<ExternalDataPE> list = groupedDataSets.get(dataSetType);
                if (list == null)
                {
                    list = new ArrayList<ExternalDataPE>();
                    groupedDataSets.put(dataSetType, list);
                }
                list.add(dataSet);
            }
            for (Map.Entry<DataSetTypePE, List<ExternalDataPE>> entry : groupedDataSets.entrySet())
            {
                DataSetTypePE dataSetType = entry.getKey();
                IDataSetTypeSlaveServerPlugin plugin = getDataSetTypeSlaveServerPlugin(dataSetType);
                plugin.deleteDataSets(session, entry.getValue(), reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteSamples(String sessionToken, List<TechId> sampleIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
            for (TechId id : sampleIds)
            {
                sampleBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteExperiments(String sessionToken, List<TechId> experimentIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
            for (TechId id : experimentIds)
            {
                experimentBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteVocabularies(String sessionToken, List<TechId> vocabularyIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
            for (TechId id : vocabularyIds)
            {
                vocabularyBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deletePropertyTypes(String sessionToken, List<TechId> propertyTypeIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
            for (TechId id : propertyTypeIds)
            {
                propertyTypeBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    // TODO 2009-06-24 IA: add unit tests to project deletion (all layers)
    public void deleteProjects(String sessionToken, List<TechId> projectIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
            for (TechId id : projectIds)
            {
                projectBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteSpaces(String sessionToken, List<TechId> groupIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
            for (TechId id : groupIds)
            {
                groupBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteExperimentAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
            experimentBO.loadDataByTechId(experimentId);
            deleteHolderAttachments(session, experimentBO.getExperiment(), fileNames, reason);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void updateExperimentAttachments(String sessionToken, TechId experimentId,
            Attachment attachment)
    {
        Session session = getSession(sessionToken);
        try
        {
            IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
            experimentBO.loadDataByTechId(experimentId);
            IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
            attachmentBO.updateAttachment(experimentBO.getExperiment(), attachment);
            attachmentBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteSampleAttachments(String sessionToken, TechId sampleId,
            List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
            sampleBO.loadDataByTechId(sampleId);
            deleteHolderAttachments(session, sampleBO.getSample(), fileNames, reason);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteProjectAttachments(String sessionToken, TechId projectId,
            List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
            projectBO.loadDataByTechId(projectId);
            deleteHolderAttachments(session, projectBO.getProject(), fileNames, reason);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    private void deleteHolderAttachments(Session session, AttachmentHolderPE holder,
            List<String> fileNames, String reason) throws DataAccessException
    {
        IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
        attachmentBO.deleteHolderAttachments(holder, fileNames, reason);
    }

    public List<Attachment> listExperimentAttachments(String sessionToken, TechId experimentId)
    {
        Session session = getSession(sessionToken);
        try
        {
            IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
            experimentBO.loadDataByTechId(experimentId);
            return AttachmentTranslator.translate(listHolderAttachments(session, experimentBO
                    .getExperiment()));
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<Attachment> listSampleAttachments(String sessionToken, TechId sampleId)
    {
        Session session = getSession(sessionToken);
        try
        {
            ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
            sampleBO.loadDataByTechId(sampleId);
            return AttachmentTranslator.translate(listHolderAttachments(session, sampleBO
                    .getSample()));
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<Attachment> listProjectAttachments(String sessionToken, TechId projectId)
    {
        Session session = getSession(sessionToken);
        try
        {
            IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
            projectBO.loadDataByTechId(projectId);
            return AttachmentTranslator.translate(listHolderAttachments(session, projectBO
                    .getProject()));
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    private List<AttachmentPE> listHolderAttachments(Session session, AttachmentHolderPE holder)
    {
        return getDAOFactory().getAttachmentDAO().listAttachments(holder);
    }

    public String uploadDataSets(String sessionToken, List<String> dataSetCodes,
            DataSetUploadContext uploadContext)
    {
        Session session = getSession(sessionToken);
        try
        {
            IExternalDataTable externalDataTable =
                    businessObjectFactory.createExternalDataTable(session);
            externalDataTable.loadByDataSetCodes(dataSetCodes, true, false);
            return externalDataTable.uploadLoadedDataSetsToCIFEX(uploadContext);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary)
    {
        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return vocabularyBO.countTermsUsageStatistics();
    }

    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return VocabularyTermTranslator.translateTerms(vocabularyBO.enrichWithTerms());
    }

    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        final List<DataSetTypePE> dataSetTypes = listEntityTypes(sessionToken, EntityKind.DATA_SET);
        return DataSetTypeTranslator.translate(dataSetTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    public LastModificationState getLastModificationState(String sessionToken)
    {
        checkSession(sessionToken);
        return lastModificationState;
    }

    public Project getProjectInfo(String sessionToken, TechId projectId)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        bo.enrichWithAttachments();
        final ProjectPE project = bo.getProject();
        return ProjectTranslator.translate(project);
    }

    public Project getProjectInfo(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadByProjectIdentifier(projectIdentifier);
        final ProjectPE project = bo.getProject();
        return ProjectTranslator.translate(project);
    }

    public IEntityInformationHolder getMaterialInformationHolder(String sessionToken,
            final MaterialIdentifier identifier)
    {
        checkSession(sessionToken);
        return createMaterialInformationHolder(identifier, getDAOFactory().getMaterialDAO()
                .tryFindMaterial(identifier));
    }

    public IEntityInformationHolder getEntityInformationHolder(String sessionToken,
            final EntityKind entityKind, final String permId)
    {
        checkSession(sessionToken);
        switch (entityKind)
        {
            case DATA_SET:
                return createInformationHolder(entityKind, permId, getDAOFactory()
                        .getExternalDataDAO().tryToFindDataSetByCode(permId));
            case SAMPLE:
            case EXPERIMENT:
                return createInformationHolder(entityKind, permId, getDAOFactory().getPermIdDAO()
                        .tryToFindByPermId(permId, DtoConverters.convertEntityKind(entityKind)));
            case MATERIAL:
                break;
        }
        throw UserFailureException.fromTemplate("Operation not available for "
                + entityKind.getDescription() + "s");
    }

    private IEntityInformationHolder createInformationHolder(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind kind, final String permId,
            IEntityInformationHolderDTO entityOrNull)
    {
        if (entityOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no %s with permId '%s'.", kind
                    .getDescription(), permId);
        }
        return createInformationHolder(kind, entityOrNull);
    }

    private IEntityInformationHolder createMaterialInformationHolder(
            final MaterialIdentifier identifier, IEntityInformationHolderDTO entityOrNull)
    {
        if (entityOrNull == null)
        {
            throw UserFailureException.fromTemplate(
                    "There is no Material of type '%s' with code '%s'.", identifier.getTypeCode(),
                    identifier.getCode());
        }
        return createInformationHolder(EntityKind.MATERIAL, entityOrNull);
    }

    private IEntityInformationHolder createInformationHolder(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind kind,
            IEntityInformationHolderDTO entity)
    {
        assert entity != null;
        final EntityType entityType =
                EntityHelper.createEntityType(kind, entity.getEntityType().getCode());
        final String code = entity.getCode();
        final Long id = HibernateUtils.getId(entity);
        return new BasicEntityInformationHolder(kind, entityType, code, id);
    }

    public String generateCode(String sessionToken, String prefix)
    {
        checkSession(sessionToken);
        return prefix + getDAOFactory().getCodeSequenceDAO().getNextCodeSequenceId();
    }

    public Date updateProject(String sessionToken, ProjectUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.update(updates);
        bo.save();
        return bo.getProject().getModificationDate();
    }

    private void deleteEntityTypes(String sessionToken, EntityKind entityKind, List<String> codes)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        for (String code : codes)
        {
            IEntityTypeBO bo = businessObjectFactory.createEntityTypeBO(session);
            bo.load(DtoConverters.convertEntityKind(entityKind), code);
            bo.delete();
        }
    }

    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.DATA_SET, entityTypesCodes);
    }

    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.EXPERIMENT, entityTypesCodes);

    }

    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.MATERIAL, entityTypesCodes);

    }

    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.SAMPLE, entityTypesCodes);
    }

    public void deleteFileFormatTypes(String sessionToken, List<String> codes)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        IFileFormatTypeDAO dao = getDAOFactory().getFileFormatTypeDAO();
        for (String code : codes)
        {
            FileFormatTypePE type = dao.tryToFindFileFormatTypeByCode(code);
            if (type == null)
            {
                throw new UserFailureException(String.format("File format type '%s' not found.",
                        code));
            } else
            {
                try
                {
                    dao.delete(type);
                } catch (DataIntegrityViolationException ex)
                {
                    throw new UserFailureException(
                            String
                                    .format(
                                            "File format type '%s' is being used. Use 'Data Set Search' to find all connected data sets.",
                                            code));
                }
            }
        }
    }

    public String getTemplateColumns(String sessionToken, EntityKind entityKind, String type,
            boolean autoGenerate, boolean withExperiments, BatchOperationKind operationKind)
    {
        List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        if (entityKind.equals(EntityKind.SAMPLE) && SampleType.isDefinedInFileSampleTypeCode(type))
        {
            types.addAll(getDAOFactory().getEntityTypeDAO(
                    DtoConverters.convertEntityKind(entityKind)).listEntityTypes());
        } else
        {
            types.add(findEntityType(entityKind, type));
        }
        StringBuilder sb = new StringBuilder();
        boolean firstSection = true;
        for (EntityTypePE entityType : types)
        {
            String section =
                    createTemplateForType(entityKind, autoGenerate, entityType, firstSection,
                            withExperiments, operationKind);
            if (types.size() != 1)
            {
                section =
                        String
                                .format(
                                        "[%s]\n%s%s\n",
                                        entityType.getCode(),
                                        firstSection ? "# Comments must be located after the type declaration ('[TYPE]').\n"
                                                : "", section);
            }
            sb.append(section);
            firstSection = false;
        }
        return sb.toString();
    }

    private String createTemplateForType(EntityKind entityKind, boolean autoGenerate,
            EntityTypePE entityType, boolean addComments, boolean withExperiments,
            BatchOperationKind operationKind)
    {
        List<String> columns = new ArrayList<String>();
        switch (entityKind)
        {
            case SAMPLE:
                if (autoGenerate == false)
                {
                    columns.add(NewSample.IDENTIFIER_COLUMN);
                }
                columns.add(NewSample.CONTAINER);
                columns.add(NewSample.PARENT);
                if (withExperiments)
                    columns.add(NewSample.EXPERIMENT);
                for (SampleTypePropertyTypePE etpt : ((SampleTypePE) entityType)
                        .getSampleTypePropertyTypes())
                {
                    columns.add(etpt.getPropertyType().getCode());
                }
                break;
            case MATERIAL:
                columns.add(NewMaterial.CODE);
                for (MaterialTypePropertyTypePE etpt : ((MaterialTypePE) entityType)
                        .getMaterialTypePropertyTypes())
                {
                    columns.add(etpt.getPropertyType().getCode());
                }
                break;
            default:
                break;
        }
        StringBuilder sb = new StringBuilder();
        for (String column : columns)
        {
            if (sb.length() != 0)
            {
                sb.append("\t");
            }
            sb.append(column);
        }
        if (entityKind.equals(EntityKind.SAMPLE) && addComments)
        {

            switch (operationKind)
            {
                case REGISTRATION:
                    sb.insert(0, NewSample.SAMPLE_REGISTRATION_TEMPLATE_COMMENT);
                    break;
                case UPDATE:
                    sb.insert(0, UpdatedSample.SAMPLE_UPDATE_TEMPLATE_COMMENT);
                    break;
            }
        }
        return sb.toString();
    }

    private EntityTypePE findEntityType(EntityKind entityKind, String type)
    {
        EntityTypePE typeOrNull =
                getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind))
                        .tryToFindEntityTypeByCode(type);
        if (typeOrNull == null)
        {
            throw new UserFailureException("Unknown " + entityKind.name() + " type '" + type + "'");
        }
        return typeOrNull;
    }

    public void updateFileFormatType(String sessionToken, AbstractType type)
    {
        checkSession(sessionToken);
        try
        {
            IFileFormatTypeDAO dao = getDAOFactory().getFileFormatTypeDAO();
            FileFormatTypePE typePE = dao.tryToFindFileFormatTypeByCode(type.getCode());
            typePE.setDescription(type.getDescription());
            dao.createOrUpdate(typePE);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }

    }

    public void updateProjectAttachments(String sessionToken, TechId projectId,
            Attachment attachment)
    {
        Session session = getSession(sessionToken);
        try
        {
            IProjectBO bo = businessObjectFactory.createProjectBO(session);
            bo.loadDataByTechId(projectId);
            IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
            attachmentBO.updateAttachment(bo.getProject(), attachment);
            attachmentBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }

    }

    public void updateSampleAttachments(String sessionToken, TechId sampleId, Attachment attachment)
    {
        Session session = getSession(sessionToken);
        try
        {
            ISampleBO bo = businessObjectFactory.createSampleBO(session);
            bo.loadDataByTechId(sampleId);
            IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
            attachmentBO.updateAttachment(bo.getSample(), attachment);
            attachmentBO.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken,
            DataStoreServiceKind dataStoreServiceKind)
    {
        checkSession(sessionToken);

        List<DatastoreServiceDescription> result = new ArrayList<DatastoreServiceDescription>();
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            result.addAll(convertAndFilter(dataStore.getServices(), dataStoreServiceKind));
        }
        return result;
    }

    private static List<DatastoreServiceDescription> convertAndFilter(
            Set<DataStoreServicePE> services, DataStoreServiceKind dataStoreServiceKind)
    {
        List<DatastoreServiceDescription> result = new ArrayList<DatastoreServiceDescription>();
        for (DataStoreServicePE service : services)
        {
            if (service.getKind() == dataStoreServiceKind)
            {
                result.add(DataStoreServiceTranslator.translate(service));
            }
        }
        Collections.sort(result);
        return result;
    }

    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        return externalDataTable.createReportFromDatasets(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), datasetCodes);
    }

    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        Map<String, String> parameterBindings = new HashMap<String, String>();
        externalDataTable.processDatasets(serviceDescription.getKey(), serviceDescription
                .getDatastoreCode(), datasetCodes, parameterBindings);
    }

    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup)
    {
        Session session = getSession(sessionToken);
        try
        {
            IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
            bo.define(newAuthorizationGroup);
            bo.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void deleteAuthorizationGroups(String sessionToken, List<TechId> groupIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IAuthorizationGroupBO authGroupBO =
                    businessObjectFactory.createAuthorizationGroupBO(session);
            for (TechId id : groupIds)
            {
                authGroupBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken)
    {
        checkSession(sessionToken);
        final List<AuthorizationGroupPE> persons =
                getDAOFactory().getAuthorizationGroupDAO().list();
        Collections.sort(persons);
        return AuthorizationGroupTranslator.translate(persons);
    }

    public Date updateAuthorizationGroup(String sessionToken, AuthorizationGroupUpdates updates)
    {
        final Session session = getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.update(updates);
        bo.save();
        return bo.getAuthorizationGroup().getModificationDate();
    }

    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId)
    {
        final Session session = getSession(sessionToken);
        IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizatonGroupId);
        return PersonTranslator.translate(bo.getAuthorizationGroup().getPersons());
    }

    public void addPersonsToAuthorizationGroup(String sessionToken, TechId authorizationGroupId,
            List<String> personsCodes)
    {
        List<String> inexistent =
                addExistingPersonsToAuthorizationGroup(sessionToken, authorizationGroupId,
                        personsCodes);
        if (inexistent.size() > 0)
        {
            registerPersons(sessionToken, inexistent);
            addExistingPersonsToAuthorizationGroup(sessionToken, authorizationGroupId, inexistent);
        }
    }

    private List<String> addExistingPersonsToAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes)
    {
        final Session session = getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizationGroupId);
        List<String> inexistent = bo.addPersons(personsCodes);
        bo.save();
        return inexistent;
    }

    public void removePersonsFromAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes)
    {
        final Session session = getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizationGroupId);
        bo.removePersons(personsCodes);
        bo.save();
    }

    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull)
    {
        checkSession(sessionToken);
        return getDAOFactory().getEventDAO().listDeletedDataSets(lastSeenDeletionEventIdOrNull);
    }

    // --- grid custom filters and columns

    private IGridCustomFilterOrColumnBO createGridCustomColumnBO(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        return businessObjectFactory.createGridCustomColumnBO(session);
    }

    private IGridCustomFilterOrColumnBO createGridCustomFilterBO(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        return businessObjectFactory.createGridCustomFilterBO(session);
    }

    private void registerFilterOrColumn(NewColumnOrFilter filter, IGridCustomFilterOrColumnBO bo)
    {
        try
        {
            bo.define(filter);
            bo.save();
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    private void deleteFiltersOrColumns(List<TechId> filterIds, IGridCustomFilterOrColumnBO bo)
    {
        try
        {
            for (TechId id : filterIds)
            {
                bo.deleteByTechId(id);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<GridCustomFilter> listFilters(String sessionToken, String gridId)
    {
        checkSession(sessionToken);
        try
        {
            List<GridCustomFilterPE> filters =
                    getDAOFactory().getGridCustomFilterDAO().listFilters(gridId);
            return GridCustomFilterTranslator.translate(filters);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public void registerFilter(String sessionToken, NewColumnOrFilter filter)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomFilterBO(sessionToken);
        registerFilterOrColumn(filter, bo);
    }

    public void deleteFilters(String sessionToken, List<TechId> filterIds)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomFilterBO(sessionToken);
        deleteFiltersOrColumns(filterIds, bo);
    }

    public void updateFilter(String sessionToken, IExpressionUpdates updates)
    {
        assert updates != null : "Unspecified updates";
        createGridCustomFilterBO(sessionToken).update(updates);
    }

    // -- columns

    public void registerGridCustomColumn(String sessionToken, NewColumnOrFilter column)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomColumnBO(sessionToken);
        registerFilterOrColumn(column, bo);
    }

    public void deleteGridCustomColumns(String sessionToken, List<TechId> columnIds)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomColumnBO(sessionToken);
        deleteFiltersOrColumns(columnIds, bo);
    }

    public void updateGridCustomColumn(String sessionToken, IExpressionUpdates updates)
    {
        assert updates != null : "Unspecified updates";
        createGridCustomColumnBO(sessionToken).update(updates);
    }

    // --

    public void keepSessionAlive(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
    }

    public void updateVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> terms)
    {
        Session session = getSession(sessionToken);
        IVocabularyBO bo = getBusinessObjectFactory().createVocabularyBO(session);
        bo.loadDataByTechId(vocabularyId);
        bo.updateTerms(terms);
        bo.save();
    }

    public void deleteMaterials(String sessionToken, List<TechId> materialIds, String reason)
    {
        Session session = getSession(sessionToken);
        try
        {
            IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
            for (TechId id : materialIds)
            {
                materialBO.deleteByTechId(id, reason);
            }
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public int lockDatasets(String sessionToken, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadByDataSetCodes(datasetCodes, false, true);
        return externalDataTable.lockDatasets();
    }

    public int unlockDatasets(String sessionToken, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadByDataSetCodes(datasetCodes, false, true);
        return externalDataTable.unlockDatasets();
    }

}
