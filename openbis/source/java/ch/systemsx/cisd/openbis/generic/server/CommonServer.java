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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAttachmentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAuthorizationGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyTermBO;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.AuthorizationGroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.TypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonServer extends AbstractServer<ICommonServer> implements ICommonServer
{
    private final IAuthenticationService authenticationService;

    private final ICommonBusinessObjectFactory businessObjectFactory;

    private final LastModificationState lastModificationState;

    public CommonServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final ICommonBusinessObjectFactory businessObjectFactory,
            LastModificationState lastModificationState)
    {
        super(sessionManager, daoFactory);
        this.authenticationService = authenticationService;
        this.businessObjectFactory = businessObjectFactory;
        this.lastModificationState = lastModificationState;
    }

    ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
    }

    // Call this when session object is not needed but you want just to
    // refresh/check the session.
    private void checkSession(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
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
    public final ICommonServer createLogger(final boolean invocationSuccessful,
            final long elapsedTime)
    {
        return new CommonServerLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    //
    // IGenericServer
    //

    public final List<Group> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
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

    public final void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
        groupBO.define(groupCode, descriptionOrNull);
        groupBO.save();
    }

    public final void updateGroup(final String sessionToken, final IGroupUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSessionManager().getSession(sessionToken);
        final IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
        groupBO.update(updates);
    }

    public final void registerPerson(final String sessionToken, final String userID)
    {
        registerPersons(sessionToken, Arrays.asList(userID));
    }

    private final void registerPersons(final String sessionToken, final List<String> userIDs)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listByCodes(userIDs);
        if (persons.size() > 0)
        {
            throw UserFailureException.fromTemplate("Following persons already exist: [%s]",
                    StringUtils.join(userIDs, ","));
        }
        final String applicationToken = authenticationService.authenticateApplication();
        if (applicationToken == null)
        {
            throw new EnvironmentFailureException("Authentication service cannot be accessed.");
        }
        List<String> unknownUsers = new ArrayList<String>();
        for (String userID : userIDs)
        {
            try
            {
                final Principal principal =
                        authenticationService.getPrincipal(applicationToken, userID);
                createPerson(principal, session.tryGetPerson());
            } catch (final IllegalArgumentException e)
            {
                unknownUsers.add(userID);
            }
        }
        if (unknownUsers.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Following persons unknown by the authentication service: [%s]", StringUtils
                            .join(userIDs, ","));
        }
    }

    public final List<RoleAssignment> listRoleAssignments(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<RoleAssignmentPE> roles =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        return RoleAssignmentTranslator.translate(roles);
    }

    public final void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final Grantee grantee)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setGrantee(grantee);
        newRoleAssignment.setGroupIdentifier(groupIdentifier);
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setGrantee(grantee);
        newRoleAssignment.setDatabaseInstanceIdentifier(new DatabaseInstanceIdentifier(
                DatabaseInstanceIdentifier.HOME));
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final Grantee grantee)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindGroupRoleAssignment(roleCode,
                        groupIdentifier.getGroupCode(), grantee);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given group role does not exist.");
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
                        "For safety reason you cannot give away your own group admin power. "
                                + "Ask instance admin to do that for you.");
            }
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    public final void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        final Session session = getSessionManager().getSession(sessionToken);
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
        return SampleTypeTranslator.translate(sampleTypes);
    }

    public final List<Sample> listSamples(final String sessionToken,
            final ListSampleCriteria criteria)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(criteria);
    }

    public final List<ExternalData> listSampleExternalData(final String sessionToken,
            final TechId sampleId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadBySampleTechId(sampleId);
        return getSortedExternalDataFrom(externalDataTable, session.getBaseIndexURL());
    }

    public final List<ExternalData> listExperimentExternalData(final String sessionToken,
            final TechId experimentId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.loadByExperimentTechId(experimentId);
        return getSortedExternalDataFrom(externalDataTable, session.getBaseIndexURL());
    }

    private List<ExternalData> getSortedExternalDataFrom(
            final IExternalDataTable externalDataTable, final String baseIndexURL)
    {
        final List<ExternalDataPE> externalData = externalDataTable.getExternalData();
        Collections.sort(externalData);
        return ExternalDataTranslator.translate(externalData, getDataStoreBaseURL(), baseIndexURL);
    }

    public final List<PropertyType> listPropertyTypes(final String sessionToken,
            boolean withRelations)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IPropertyTypeTable propertyTypeTable =
                businessObjectFactory.createPropertyTypeTable(session);
        if (withRelations)
            propertyTypeTable.loadWithRelations();
        else
            propertyTypeTable.load();
        final List<PropertyTypePE> propertyTypes = propertyTypeTable.getPropertyTypes();
        Collections.sort(propertyTypes);
        return PropertyTypeTranslator.translate(propertyTypes);
    }

    public final List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText)
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
                        searchableEntity, queryText, dataProvider));
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
        final Session session = getSessionManager().getSession(sessionToken);
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
        return MaterialTypeTranslator.translate(materialTypes);
    }

    private <T extends EntityTypePE> List<T> listEntityTypes(String sessionToken,
            EntityKind entityKind)
    {
        checkSession(sessionToken);
        final List<T> types = getDAOFactory().getEntityTypeDAO(entityKind).listEntityTypes();
        Collections.sort(types);
        return types;
    }

    public final List<DataType> listDataTypes(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<DataTypePE> dataTypePEs = getDAOFactory().getPropertyTypeDAO().listDataTypes();
        final List<DataType> dataTypes =
                BeanUtils.createBeanList(DataType.class, dataTypePEs, DtoConverters
                        .getDataTypeConverter());
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
        return BeanUtils.createBeanList(Vocabulary.class, vocabularies, DtoConverters
                .getVocabularyConverter());
    }

    private void enrichWithTerms(final VocabularyPE vocabularyPE)
    {
        HibernateUtils.initialize(vocabularyPE.getTerms());
    }

    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSessionManager().getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, entityKind);
        etptBO.createAssignment(propertyTypeCode, entityTypeCode, isMandatory, defaultValue);
        return String.format("%s property type '%s' successfully assigned to %s type '%s'",
                isMandatory ? "Mandatory" : "Optional", propertyTypeCode, entityKind.getLabel(),
                entityTypeCode);
    }

    public void updatePropertyTypeAssignment(final String sessionToken,
            final EntityKind entityKind, final String propertyTypeCode,
            final String entityTypeCode, final boolean isMandatory, final String defaultValue)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSessionManager().getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, entityKind);
        etptBO.loadAssignment(propertyTypeCode, entityTypeCode);
        etptBO.updateLoadedAssignment(isMandatory, defaultValue);
    }

    public void unassignPropertyType(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSessionManager().getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, entityKind);
        etptBO.loadAssignment(propertyTypeCode, entityTypeCode);
        etptBO.deleteLoadedAssignment();
    }

    public int countPropertyTypedEntities(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSessionManager().getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, entityKind);
        etptBO.loadAssignment(propertyTypeCode, entityTypeCode);
        return etptBO.getLoadedAssignment().getPropertyValues().size();
    }

    public final void registerPropertyType(final String sessionToken,
            final PropertyType propertyType)
    {
        assert sessionToken != null : "Unspecified session token";
        assert propertyType != null : "Unspecified property type";

        final Session session = getSessionManager().getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.define(propertyType);
        propertyTypeBO.save();
    }

    public final void updatePropertyType(final String sessionToken,
            final IPropertyTypeUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSessionManager().getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.update(updates);
    }

    public final void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabulary != null : "Unspecified vocabulary";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
    }

    public final void updateVocabulary(final String sessionToken, final IVocabularyUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.update(updates);
    }

    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<String> vocabularyTerms)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.addNewTerms(vocabularyTerms);
        vocabularyBO.save();
    }

    public final void updateVocabularyTerm(final String sessionToken,
            final IVocabularyTermUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyTermBO vocabularyTermBO =
                businessObjectFactory.createVocabularyTermBO(session);
        vocabularyTermBO.update(updates);
    }

    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";

        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.delete(termsToBeDeleted, termsToBeReplaced);
        vocabularyBO.save();
    }

    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId, Collection<NewAttachment> attachments)
    {
        final Session session = getSessionManager().getSession(sessionToken);
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

    public List<ExternalData> searchForDataSets(String sessionToken, DataSetSearchCriteria criteria)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IHibernateSearchDAO searchDAO = getDAOFactory().getHibernateSearchDAO();
            final List<ExternalDataPE> searchHits = searchDAO.searchForDataSets(criteria);
            final List<ExternalData> list = new ArrayList<ExternalData>(searchHits.size());
            for (final ExternalDataPE hit : searchHits)
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

    public List<ExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities relatedEntities)
    {
        System.out.println("listRelatedDataSets");
        final Session session = getSessionManager().getSession(sessionToken);
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

    public List<Material> listMaterials(String sessionToken, MaterialType materialType)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IMaterialTable materialTable = businessObjectFactory.createMaterialTable(session);
        materialTable.load(materialType.getCode());
        final List<MaterialPE> materials = materialTable.getMaterials();
        Collections.sort(materials);
        return MaterialTranslator.translate(materials);
    }

    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        final Session session = getSessionManager().getSession(sessionToken);
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
        final Session session = getSessionManager().getSession(sessionToken);
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
        final Session session = getSessionManager().getSession(sessionToken);
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
        final Session session = getSessionManager().getSession(sessionToken);
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
            IEntityTypeDAO entityTypeDAO = getDAOFactory().getEntityTypeDAO(entityKind);
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
            sampleTypePE.setContainerHierarchyDepth(sampleType.getContainerHierarchyDepth());
            sampleTypePE
                    .setGeneratedFromHierarchyDepth(sampleType.getGeneratedFromHierarchyDepth());
        }
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason)
    {
        Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IExternalDataTable externalDataTable =
                    businessObjectFactory.createExternalDataTable(session);
            externalDataTable.loadByDataSetCodes(dataSetCodes);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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

    public void deleteGroups(String sessionToken, List<TechId> groupIds, String reason)
    {
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
        try
        {
            IExternalDataTable externalDataTable =
                    businessObjectFactory.createExternalDataTable(session);
            externalDataTable.loadByDataSetCodes(dataSetCodes);
            return externalDataTable.uploadLoadedDataSetsToCIFEX(uploadContext);
        } catch (final DataAccessException ex)
        {
            throw createUserFailureException(ex);
        }
    }

    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return vocabularyBO.countTermsUsageStatistics();
    }

    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return VocabularyTermTranslator.translateTerms(vocabularyBO.enrichWithTerms());
    }

    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        final List<DataSetTypePE> dataSetTypes = listEntityTypes(sessionToken, EntityKind.DATA_SET);
        return DataSetTypeTranslator.translate(dataSetTypes);
    }

    public LastModificationState getLastModificationState(String sessionToken)
    {
        checkSession(sessionToken);
        return lastModificationState;
    }

    public Project getProjectInfo(String sessionToken, TechId projectId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        bo.enrichWithAttachments();
        final ProjectPE project = bo.getProject();
        return ProjectTranslator.translate(project);
    }

    public IEntityInformationHolder getEntityInformationHolder(String sessionToken,
            final ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind,
            final String permId)
    {
        getSessionManager().getSession(sessionToken);
        switch (entityKind)
        {
            case DATA_SET:
                return createInformationHolder(entityKind, permId, getDAOFactory()
                        .getExternalDataDAO().tryToFindDataSetByCode(permId));
            case SAMPLE:
                return createInformationHolder(entityKind, permId, getDAOFactory().getPermIdDAO()
                        .tryToFindByPermId(permId, EntityKind.SAMPLE));
            case EXPERIMENT:
                return createInformationHolder(entityKind, permId, getDAOFactory().getPermIdDAO()
                        .tryToFindByPermId(permId, EntityKind.EXPERIMENT));
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
            throw UserFailureException.fromTemplate("There is no %s with permId '%s'", kind
                    .getDescription(), permId);
        }
        final EntityType entityType =
                EntityHelper.createEntityType(kind, entityOrNull.getEntityType().getCode());
        final String code = entityOrNull.getCode();
        final Long id = HibernateUtils.getId(entityOrNull);
        final String identifier = entityOrNull.getIdentifier();
        return new BasicEntityInformationHolder(kind, entityType, identifier, code, id);
    }

    public String generateCode(String sessionToken, String prefix)
    {
        getSessionManager().getSession(sessionToken);
        return prefix + getDAOFactory().getCodeSequenceDAO().getNextCodeSequenceId();
    }

    public Date updateProject(String sessionToken, ProjectUpdatesDTO updates)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.update(updates);
        bo.save();
        return bo.getProject().getModificationDate();
    }

    private void deleteEntityTypes(String sessionToken, EntityKind entityKind, List<String> codes)
            throws UserFailureException
    {
        final Session session = getSessionManager().getSession(sessionToken);
        for (String code : codes)
        {
            IEntityTypeBO bo = businessObjectFactory.createEntityTypeBO(session);
            bo.load(entityKind, code);
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
            boolean autoGenerate, boolean withExperiments)
    {
        List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        if (entityKind.equals(EntityKind.SAMPLE) && SampleType.isDefinedInFileSampleTypeCode(type))
        {
            types.addAll(getDAOFactory().getEntityTypeDAO(entityKind).listEntityTypes());
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
                            withExperiments);
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
            EntityTypePE entityType, boolean addComments, boolean withExperiments)
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
            sb.insert(0, NewSample.SAMPLE_REGISTRATION_TEMPLATE_COMMENT);
        }
        return sb.toString();
    }

    private EntityTypePE findEntityType(EntityKind entityKind, String type)
    {
        EntityTypePE typeOrNull =
                getDAOFactory().getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(type);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
                result.add(convert(service));
            }
        }
        return result;
    }

    private static DatastoreServiceDescription convert(DataStoreServicePE service)
    {
        String[] datasetTypeCodes = extractCodes(service.getDatasetTypes());
        String dssCode = service.getDataStore().getCode();
        return new DatastoreServiceDescription(service.getKey(), service.getLabel(),
                datasetTypeCodes, dssCode);
    }

    private static String[] extractCodes(Set<DataSetTypePE> datasetTypes)
    {
        String[] codes = new String[datasetTypes.size()];
        int i = 0;
        for (DataSetTypePE datasetType : datasetTypes)
        {
            codes[i] = datasetType.getCode();
            i++;
        }
        return codes;
    }

    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        Session session = getSessionManager().getSession(sessionToken);
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        return externalDataTable.createReportFromDatasets(serviceDescription, datasetCodes);
    }

    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        Session session = getSessionManager().getSession(sessionToken);
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.processDatasets(serviceDescription, datasetCodes);
    }

    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup)
    {
        Session session = getSessionManager().getSession(sessionToken);
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
        Session session = getSessionManager().getSession(sessionToken);
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
        final Session session = getSessionManager().getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.update(updates);
        bo.save();
        return bo.getAuthorizationGroup().getModificationDate();
    }

    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
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
        final Session session = getSessionManager().getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizationGroupId);
        List<String> inexistent = bo.addPersons(personsCodes);
        bo.save();
        return inexistent;
    }

    public void removePersonsFromAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizationGroupId);
        bo.removePersons(personsCodes);
        bo.save();
    }

    public List<DeletedDataSet> listDeletedDataSets(String sessionToken, Date since)
    {
        getSessionManager().getSession(sessionToken);
        return getDAOFactory().getEventDAO().listDeletedDataSets(since);
    }

}
