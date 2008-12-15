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

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Logger class for {@link CommonServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class CommonServerLogger extends AbstractServerLogger implements ICommonServer
{
    /**
     * Creates an instance for the specified session manager and invocation status. The session
     * manager is used to retrieve user information which will be a part of the log message.
     */
    CommonServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful)
    {
        super(sessionManager, invocationSuccessful);
    }

    //
    // IGenericServer
    //

    public List<GroupPE> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final String command = "list_groups";
        if (identifier == null || identifier.getDatabaseInstanceCode() == null)
        {
            logAccess(sessionToken, command);
        } else
        {
            logAccess(sessionToken, command, "DATABASE-INSTANCE(%s)", identifier);
        }
        return null;
    }

    public void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        logTracking(sessionToken, "register_group", "CODE(%s)", groupCode);
    }

    public List<PersonPE> listPersons(final String sessionToken)
    {
        logAccess(sessionToken, "list_persons");
        return null;
    }

    public void registerPerson(final String sessionToken, final String userID)
    {
        logTracking(sessionToken, "register_person", "CODE(%s)", userID);

    }

    public List<RoleAssignmentPE> listRoles(final String sessionToken)
    {
        logAccess(sessionToken, "list_roles");
        return null;
    }

    public void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s)  PERSON(%s)", roleCode, person);

    }

    public void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) PERSON(%s)", roleCode, person);

    }

    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_sample_types");
        return null;
    }

    public final List<SamplePE> listSamples(final String sessionToken,
            final ListSampleCriteriaDTO criteria)
    {
        logAccess(sessionToken, "list_samples", "TYPE(%s) OWNERS(%s) CONTAINER(%s) EXPERIMENT(%s)",
                criteria.getSampleType(), criteria.getOwnerIdentifiers(), criteria
                        .getContainerIdentifier(), criteria.getExperimentIdentifier());
        return null;
    }

    public final List<SamplePropertyPE> listSamplesProperties(final String sessionToken,
            final ListSampleCriteriaDTO criteria, final List<PropertyTypePE> propertyCodes)
    {
        logAccess(sessionToken, "list_samples_properties", "CRITERIA(%s) PROPERTIES(%s)", criteria,
                propertyCodes.size());
        return null;
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<ExternalDataPE> listExternalData(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "list_external_data", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<SearchHit> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText)
    {
        logAccess(sessionToken, "list_matching_entities", "SEARCHABLE-ENTITIES(%s) QUERY-TEXT(%s)",
                Arrays.toString(searchableEntities), queryText);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)", newSample
                .getSampleType(), newSample.getIdentifier());
    }

    public List<ExperimentPE> listExperiments(final String sessionToken,
            final ExperimentTypePE experimentType, final ProjectIdentifier project)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) PROJECT(%s)", experimentType, project);
        return null;
    }

    public List<ProjectPE> listProjects(final String sessionToken)
    {
        logAccess(sessionToken, "list_projects");
        return null;
    }

    public List<ExperimentTypePE> listExperimentTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_experiment_types");
        return null;
    }

    public List<PropertyTypePE> listPropertyTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_property_types");
        return null;
    }

    public final List<DataTypePE> listDataTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_data_types");
        return null;
    }
}
