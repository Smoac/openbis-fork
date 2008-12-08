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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.DtoConverters;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.ListSampleCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.ResultSetTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.RoleCodeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SearchableEntityTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.AbstractClientService;

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

    @SuppressWarnings("unchecked")
    private final <K> IResultSetManager<K> getResultSetManager()
    {
        return (IResultSetManager<K>) getHttpSession().getAttribute(
                SessionConstants.OPENBIS_RESULT_SET_MANAGER);
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return commonServer;
    }

    //
    // IGenericClientService
    //

    public final List<Group> listGroups(final String databaseInstanceCode)
    {
        try
        {
            final DatabaseInstanceIdentifier identifier =
                    new DatabaseInstanceIdentifier(databaseInstanceCode);
            final List<Group> result = new ArrayList<Group>();
            final List<GroupPE> groups = commonServer.listGroups(getSessionToken(), identifier);
            for (final GroupPE group : groups)
            {
                result.add(GroupTranslator.translate(group));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroup(final String groupCode, final String descriptionOrNull,
            final String groupLeaderOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerGroup(sessionToken, groupCode, descriptionOrNull,
                    groupLeaderOrNull);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<Person> listPersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {

        try
        {
            final List<Person> result = new ArrayList<Person>();
            final List<PersonPE> persons = commonServer.listPersons(getSessionToken());
            for (final PersonPE person : persons)
            {
                result.add(PersonTranslator.translate(person));
            }
            return result;
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

    public final List<RoleAssignment> listRoles()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<RoleAssignment> result = new ArrayList<RoleAssignment>();
            final List<RoleAssignmentPE> roles = commonServer.listRoles(getSessionToken());
            for (final RoleAssignmentPE role : roles)
            {
                result.add(RoleAssignmentTranslator.translate(role));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            final String sessionToken = getSessionToken();
            commonServer.registerGroupRole(sessionToken, RoleCodeTranslator.translate(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.registerInstanceRole(sessionToken, RoleCodeTranslator
                    .translate(roleSetCode), person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void deleteGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            final String sessionToken = getSessionToken();
            commonServer.deleteGroupRole(sessionToken, RoleCodeTranslator.translate(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final void deleteInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            commonServer.deleteInstanceRole(sessionToken,
                    RoleCodeTranslator.translate(roleSetCode), person);
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
            final List<SampleTypePE> sampleTypes = commonServer.listSampleTypes(getSessionToken());
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

    public final ResultSet<Sample> listSamples(final ListSampleCriteria listCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final IResultSetManager<String> resultSetManager = getResultSetManager();
            final IResultSet<String, Sample> result =
                    resultSetManager.getResultSet(listCriteria, new IOriginalDataProvider<Sample>()
                        {

                            //
                            // IDataRetriever
                            //

                            public final List<Sample> getOriginalData()
                            {
                                final List<SamplePE> samples =
                                        commonServer.listSamples(getSessionToken(),
                                                ListSampleCriteriaTranslator
                                                        .translate(listCriteria));
                                final List<Sample> list = new ArrayList<Sample>(samples.size());
                                for (final SamplePE sample : samples)
                                {
                                    list.add(SampleTranslator.translate(sample));
                                }
                                return list;
                            }
                        });
            return ResultSetTranslator.translate(result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final ResultSet<Experiment> listExperiments(final ListExperimentsCriteria listCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final IResultSetManager<String> resultSetManager = getResultSetManager();
            final IResultSet<String, Experiment> result =
                    resultSetManager.getResultSet(listCriteria,
                            new IOriginalDataProvider<Experiment>()
                                {
                                    public final List<Experiment> getOriginalData()
                                    {
                                        final List<ExperimentPE> experiments =
                                                commonServer.listExperiments(getSessionToken(),
                                                        ExperimentTranslator.translate(listCriteria
                                                                .getExperimentType()),
                                                        new ProjectIdentifier(listCriteria
                                                                .getGroupCode(), listCriteria
                                                                .getProjectCode()));
                                        final List<Experiment> list =
                                                new ArrayList<Experiment>(experiments.size());
                                        for (final ExperimentPE experiment : experiments)
                                        {
                                            list
                                                    .add(ExperimentTranslator
                                                            .translate(
                                                                    experiment,
                                                                    ExperimentTranslator.LoadableFields.PROPERTIES));
                                        }
                                        return list;
                                    }
                                });
            return ResultSetTranslator.translate(result);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<ExternalData> listExternalData(final String sampleIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final List<ExternalDataPE> externalData =
                    commonServer.listExternalData(getSessionToken(), identifier);
            return BeanUtils.createBeanList(ExternalData.class, externalData);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<SearchableEntity> listSearchableEntities()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
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

    public final ResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final IResultSetConfig<String> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] matchingEntities =
                    SearchableEntityTranslator.translate(searchableEntityOrNull);
            final IResultSetManager<String> resultSetManager = getResultSetManager();
            final IResultSet<String, MatchingEntity> result =
                    resultSetManager.getResultSet(resultSetConfig,
                            new IOriginalDataProvider<MatchingEntity>()
                                {

                                    //
                                    // IDataRetriever
                                    //

                                    public final List<MatchingEntity> getOriginalData()
                                    {
                                        return BeanUtils.createBeanList(MatchingEntity.class,
                                                commonServer.listMatchingEntities(
                                                        getSessionToken(), matchingEntities,
                                                        queryText), DtoConverters
                                                        .getMatchingEntityConverter());
                                    }
                                });
            return ResultSetTranslator.translate(result);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void removeResultSet(final String resultSetKey)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final IResultSetManager<String> resultSetManager = getResultSetManager();
            resultSetManager.removeResultSet(resultSetKey);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<Project> listProjects()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<Project> result = new ArrayList<Project>();
            final List<ProjectPE> projects = commonServer.listProjects(getSessionToken());
            for (final ProjectPE project : projects)
            {
                result.add(ProjectTranslator.translate(project));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<ExperimentType> listExperimentTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<ExperimentType> result = new ArrayList<ExperimentType>();
            final List<ExperimentTypePE> projects =
                    commonServer.listExperimentTypes(getSessionToken());
            for (final ExperimentTypePE expType : projects)
            {
                result.add(ExperimentTranslator.translate(expType));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }
}
