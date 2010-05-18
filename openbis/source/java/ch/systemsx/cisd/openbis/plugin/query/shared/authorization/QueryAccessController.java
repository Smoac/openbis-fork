/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.authorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationAdvisor;
import ch.systemsx.cisd.openbis.generic.server.authorization.DefaultAccessController;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.Role;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.Role.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author Piotr Buczek
 */
public class QueryAccessController
{

    private static final Logger authorizationLog =
            LogFactory.getLogger(LogCategory.AUTH, AuthorizationAdvisor.class);

    private static Map<String, DatabaseDefinition> definitionsByDbKey;

    public static void initialize(Map<String, DatabaseDefinition> definitions)
    {
        definitionsByDbKey = definitions;
    }

    public static void checkWriteAccess(Session session, String dbKey, String operation)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        PersonPE person = session.tryGetPerson();
        GroupPE dataSpaceOrNull = database.tryGetDataSpace();
        RoleSet minimalRole = database.getCreatorMinimalRole();

        checkAuthorization(session, operation, database, person, dataSpaceOrNull, minimalRole);
    }

    public static void checkReadAccess(Session session, String dbKey)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        PersonPE person = session.tryGetPerson();
        GroupPE dataSpaceOrNull = database.tryGetDataSpace();
        RoleSet minimalRole = RoleSet.OBSERVER;

        checkAuthorization(session, "perform", database, person, dataSpaceOrNull, minimalRole);
    }

    private static void checkAuthorization(Session session, String operation,
            DatabaseDefinition database, PersonPE person, GroupPE dataSpaceOrNull,
            RoleSet minimalRole)
    {
        if (isAuthorized(person, dataSpaceOrNull, minimalRole) == false)
        {
            String errorMsg =
                    createErrorMessage(operation, session.getUserName(), dataSpaceOrNull,
                            minimalRole, database.getLabel());
            throw createAuthorizationError(session, operation, errorMsg);
        }
    }

    static boolean isAuthorized(PersonPE person, GroupPE dataSpaceOrNull, RoleSet minimalRole)
    {
        final Set<Role> requiredRoles = minimalRole.getRoles();
        if (person != null)
        {
            List<RoleWithIdentifier> userRoles = DefaultAccessController.getUserRoles(person);
            userRoles.retainAll(requiredRoles);
            if (userRoles.size() > 0)
            {
                if (dataSpaceOrNull == null)
                {
                    return true;
                } else
                {
                    return isSpaceMatching(userRoles, dataSpaceOrNull);
                }
            } else
            {
                return false;
            }
        }
        return false;
    }

    private static boolean isSpaceMatching(List<RoleWithIdentifier> userRoles,
            final GroupPE requiredSpace)
    {

        for (final RoleWithIdentifier role : userRoles)
        {
            final RoleLevel roleGroup = role.getRoleLevel();
            if (roleGroup.equals(RoleLevel.SPACE) && role.getAssignedGroup().equals(requiredSpace))
            {
                return true;
            } else if (roleGroup.equals(RoleLevel.INSTANCE)
                    && role.getAssignedDatabaseInstance().equals(
                            requiredSpace.getDatabaseInstance()))
            {
                // permissions on the database instance level allow to access all groups in this
                // instance
                return true;
            }
        }
        return false;
    }

    private static String createErrorMessage(String operation, String userName,
            GroupPE dataSpaceOrNull, RoleSet minimalRole, String database)
    {
        String minimalRoleDescription = minimalRole.name();
        if (dataSpaceOrNull != null)
        {
            minimalRoleDescription += " in space " + dataSpaceOrNull.getCode();
        }

        return String.format("User '%s' does not have enough privileges to %s "
                + "a query in database '%s'. One needs to be at least %s.", userName, operation,
                database, minimalRoleDescription);
    }

    private static AuthorizationFailureException createAuthorizationError(Session session,
            String operation, String errorMessage)
    {
        final String groupCode = session.tryGetHomeGroupCode();
        authorizationLog
                .info(String.format("[USER:'%s' SPACE:%s]: Authorization failure while "
                        + "trying to %s a query: %s", session.getUserName(),
                        groupCode == null ? "<UNDEFINED>" : "'" + groupCode + "'", operation,
                        errorMessage));
        return new AuthorizationFailureException(errorMessage);
    }

    /**
     * Filters the rows of {@link TableModel} on magic columns (experiment, sample or data set
     * referenced by 'experiment_key', 'sample_key', 'data_set_key').
     */
    public static TableModel filterResults(Session session, String dbKey, IDAOFactory factory,
            TableModel table)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        // If the data space has been configured, it is assumed that all data belongs to that space
        // and no further filtering is needed.
        if (database.tryGetDataSpace() == null)
        {
            EntityKind[] kinds =
                { EntityKind.EXPERIMENT, EntityKind.SAMPLE, EntityKind.DATA_SET };
            PersonPE person = session.tryGetPerson();
            for (EntityKind kind : kinds)
            {
                filterByKind(table, person, kind, factory);
            }
        }
        return table;
    }

    private static void filterByKind(TableModel table, PersonPE person, EntityKind kind,
            IDAOFactory factory)
    {
        List<Integer> columnsToFilter = getColumnsToFilter(table, kind);
        Set<String> entityIdentifiers = getValues(table, columnsToFilter);
        Map<String, GroupPE> entitySpaces = loadGroups(entityIdentifiers, kind, factory);
        Iterator<TableModelRow> rowIterator = table.getRows().iterator();
        rowLoop: while (rowIterator.hasNext())
        {
            TableModelRow row = rowIterator.next();
            for (int c : columnsToFilter)
            {
                ISerializableComparable value = row.getValues().get(c);
                if (value != null
                        && isAuthorized(person, entitySpaces.get(value.toString()),
                                RoleSet.OBSERVER) == false)
                {
                    rowIterator.remove();
                    continue rowLoop;
                }
            }
        }
    }

    private static Set<String> getValues(TableModel table, List<Integer> columns)
    {
        Set<String> values = new HashSet<String>();
        for (TableModelRow row : table.getRows())
        {
            for (int c : columns)
            {
                ISerializableComparable value = row.getValues().get(c);
                if (value != null)
                {
                    values.add(value.toString());
                }
            }
        }
        return values;
    }

    private static List<Integer> getColumnsToFilter(TableModel table, EntityKind kind)
    {
        List<Integer> columns = new ArrayList<Integer>();
        for (int i = 0; i < table.getHeader().size(); i++)
        {
            TableModelColumnHeader header = table.getHeader().get(i);
            EntityKind headerEntityKindOrNull = header.tryGetEntityKind();
            if (headerEntityKindOrNull != null && headerEntityKindOrNull.equals(kind))
            {
                columns.add(i);
            }
        }
        return columns;
    }

    private static HashMap<String, GroupPE> loadGroups(Set<String> values, EntityKind kind,
            IDAOFactory factory)
    {
        HashMap<String, GroupPE> map = new HashMap<String, GroupPE>();
        switch (kind)
        {
            case EXPERIMENT:
                for (ExperimentPE e : factory.getExperimentDAO().listByPermID(values))
                {
                    map.put(e.getPermId(), e.getProject().getGroup());
                }
                break;
            case SAMPLE:
                for (SamplePE e : factory.getSampleDAO().listByPermID(values))
                {
                    GroupPE group = e.getGroup();
                    if (group != null)
                    {
                        map.put(e.getPermId(), group);
                    }
                }
                break;
            case DATA_SET:
                for (ExternalDataPE e : factory.getExternalDataDAO().listByCode(values))
                {
                    map.put(e.getCode(), e.getExperiment().getProject().getGroup());
                }
                break;
            case MATERIAL:
                throw new UnsupportedOperationException();
        }
        return map;
    }
}
