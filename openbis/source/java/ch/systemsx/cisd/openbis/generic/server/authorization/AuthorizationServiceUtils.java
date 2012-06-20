/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExperimentIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SamplePermIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * The class containing some authorizing methods used in ETLService
 * 
 * @author Jakub Straszewski
 */
public class AuthorizationServiceUtils
{
    IDAOFactory daoFactory;

    public AuthorizationServiceUtils(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    public boolean doesUserHaveRole(String user, String roleCode, String spaceOrNull)
    {
        // get the user by name
        PersonPE person = getUserByName(user);

        RoleWithHierarchy methodRole;
        try
        {
            // get the role
            RoleLevel roleLevel = (spaceOrNull == null) ? RoleLevel.INSTANCE : RoleLevel.SPACE;
            methodRole =
                    RoleWithHierarchy.valueOf(roleLevel,
                            RoleWithHierarchy.RoleCode.valueOf(roleCode));
        } catch (Exception e)
        {
            // the only possible reason for this exception is incorrect role

            // ETL_SERVICE role is intentionally omitted in the error messages
            if (spaceOrNull == null)
            {
                throw new IllegalArgumentException(
                        "Incorrect role "
                                + roleCode
                                + " specified. The correct roles for space are ADMIN, USER, POWER_USER, OBSERVER");
            } else
            {
                throw new IllegalArgumentException("Incorrect role " + roleCode
                        + " specified. The correct instance roles are ADMIN, OBSERVER");
            }
        }

        if (person.getAllPersonRoles().size() == 0)
        {
            return false;
        }

        final List<RoleWithIdentifier> userRoles = DefaultAccessController.getUserRoles(person);
        // getRoles() takes all the roles stronger/equal than the role
        DefaultAccessController.retainMatchingRoleWithIdentifiers(userRoles, methodRole.getRoles());

        if (userRoles.size() == 0)
        {
            return false;
        }

        if (spaceOrNull != null)
        {
            SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();

            predicate.init(new AuthorizationDataProvider(daoFactory));

            final Status status =
                    predicate.evaluate(person, userRoles, new SpaceIdentifier(spaceOrNull));

            return (status.getFlag().equals(StatusFlag.OK));
        }
        return true;

    }

    private PersonPE getUserByName(String user)
    {
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(user);
        if (person == null)
        {
            throw new IllegalArgumentException("The user with id " + user + " doesn't exist");
        }
        return person;
    }

    public List<String> filterDataSetCodes(String user, List<String> dataSetCodes)
    {
        PersonPE person = getUserByName(user);
        List<RoleWithIdentifier> userRoles = DefaultAccessController.getUserRoles(person);

        LinkedList<String> resultList = new LinkedList<String>();
        for (String dataSetCode : dataSetCodes)
        {
            if (canAccessDataSet(person, userRoles, dataSetCode))
            {
                resultList.add(dataSetCode);
            }
        }
        return resultList;
    }

    private boolean canAccessDataSet(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            String dataSetCode)
    {
        DataSetCodePredicate predicate = new DataSetCodePredicate();

        predicate.init(new AuthorizationDataProvider(daoFactory));

        final Status status = predicate.evaluate(person, allowedRoles, dataSetCode);

        return (status.getFlag().equals(StatusFlag.OK));
    }

    public List<String> filterExperimentIds(String user, List<String> experimentIds)
    {
        PersonPE person = getUserByName(user);
        List<RoleWithIdentifier> userRoles = DefaultAccessController.getUserRoles(person);

        LinkedList<String> resultList = new LinkedList<String>();
        for (String experimentId : experimentIds)
        {
            if (canAccessExperiment(person, userRoles, experimentId))
            {
                resultList.add(experimentId);
            }
        }
        return resultList;
    }

    private boolean canAccessExperiment(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            String experimentId)
    {
        ExperimentIdentifierPredicate predicate = new ExperimentIdentifierPredicate();

        predicate.init(new AuthorizationDataProvider(daoFactory));

        final Status status = predicate.evaluate(person, allowedRoles, experimentId);

        return (status.getFlag().equals(StatusFlag.OK));
    }

    public List<String> filterSampleIds(String user, List<String> sampleIds)
    {
        PersonPE person = getUserByName(user);
        List<RoleWithIdentifier> userRoles = DefaultAccessController.getUserRoles(person);

        LinkedList<String> resultList = new LinkedList<String>();
        for (String samplePermId : sampleIds)
        {
            if (canAccessSample(person, userRoles, samplePermId))
            {
                resultList.add(samplePermId);
            }
        }
        return resultList;
    }

    private boolean canAccessSample(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            String samplePermId)
    {

        SamplePermIdPredicate predicate = new SamplePermIdPredicate();

        predicate.init(new AuthorizationDataProvider(daoFactory));

        final Status status = predicate.evaluate(person, allowedRoles, new PermId(samplePermId));

        return (status.getFlag().equals(StatusFlag.OK));
    }

}
