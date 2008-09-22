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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.util.List;

import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDatabaseInstanceFinder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedGroupException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Some useful identifier methods on the <i>server</i> side.
 * <p>
 * This class is the only place which provides the correct way to resolve
 * {@link DatabaseInstanceIdentifier} into a {@link DatabaseInstancePE}!
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class GroupIdentifierHelper
{

    private GroupIdentifierHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Class which transforms identifiers to the canonical form. Normalized database identifier
     * always has a code, never UUID. Normalized group identifier has always group code, even when
     * it is a home group. It also has normalized database identifier.
     */
    public static class GroupIdentifierNormalizer
    {
        public static GroupIdentifierNormalizer create(final IAuthorizationDAOFactory daoFactory,
                final String homeGroupCodeOrNull)
        {
            final IDatabaseInstanceFinder instanceFinder =
                    GroupIdentifierHelper.createCachedInstanceFinder(daoFactory);
            return new GroupIdentifierNormalizer(instanceFinder, homeGroupCodeOrNull);
        }

        private final IDatabaseInstanceFinder databaseInstanceFinder;

        private final String homeGroupCodeOrNull;

        private GroupIdentifierNormalizer(final IDatabaseInstanceFinder databaseInstanceFinder,
                final String homeGroupCodeOrNull)
        {
            this.databaseInstanceFinder = databaseInstanceFinder;
            this.homeGroupCodeOrNull = homeGroupCodeOrNull;
        }

        public final GroupIdentifier normalize(final GroupIdentifier identifier)
        {
            return GroupIdentifierHelper.normalize(identifier, homeGroupCodeOrNull,
                    databaseInstanceFinder);
        }

        public final SampleIdentifier normalize(final SampleIdentifier identifier)
        {
            return GroupIdentifierHelper.normalize(identifier, homeGroupCodeOrNull,
                    databaseInstanceFinder);
        }
    }

    /** Transforms given group identifier to the canonical form. */
    private final static GroupIdentifier normalize(final GroupIdentifier groupIdentifier,
            final String homeGroupCodeOrNull, final IDatabaseInstanceFinder instanceFinder)
    {
        final DatabaseInstanceIdentifier instance = normalize(groupIdentifier, instanceFinder);
        String groupCode = groupIdentifier.getGroupCode();
        if (groupCode == null)
        {
            groupCode = homeGroupCodeOrNull;
        }
        return new GroupIdentifier(instance, groupCode.toUpperCase());
    }

    /** Transforms given sample identifier to the canonical form. */
    private final static SampleIdentifier normalize(final SampleIdentifier sampleIdentifier,
            final String homeGroupCodeOrNull, final IDatabaseInstanceFinder instanceFinder)
    {
        if (sampleIdentifier.isDatabaseInstanceLevel())
        {
            final DatabaseInstanceIdentifier instanceIdentifier =
                    normalize(sampleIdentifier.getDatabaseInstanceLevel(), instanceFinder);
            return new SampleIdentifier(instanceIdentifier, sampleIdentifier.getSampleCode());
        } else if (sampleIdentifier.isGroupLevel())
        {
            final GroupIdentifier groupIdentifier =
                    normalize(sampleIdentifier.getGroupLevel(), homeGroupCodeOrNull, instanceFinder);
            return new SampleIdentifier(groupIdentifier, sampleIdentifier.getSampleCode());
        } else
        {
            throw InternalErr.error(sampleIdentifier);
        }
    }

    private static DatabaseInstanceIdentifier normalize(final DatabaseInstanceIdentifier identifier,
            final IDatabaseInstanceFinder instanceFinder)
    {
        final String code = getDatabaseInstance(identifier, instanceFinder).getCode();
        return new DatabaseInstanceIdentifier(code.toUpperCase());
    }

    /** Creates database instance finder which caches all existing database instance at the begining. */
    public final static IDatabaseInstanceFinder createCachedInstanceFinder(
            final IAuthorizationDAOFactory daoFactory)
    {
        final List<DatabaseInstancePE> instances =
                daoFactory.getDatabaseInstancesDAO().listDatabaseInstances();

        final TableMap<String, DatabaseInstancePE> databaseInstancesByCode =
                new TableMap<String, DatabaseInstancePE>(instances, KeyExtractorFactory
                        .getDatabaseInstanceByCodeKeyExtractor());

        final TableMap<String, DatabaseInstancePE> databaseInstancesByUUID =
                new TableMap<String, DatabaseInstancePE>(instances, KeyExtractorFactory
                        .getDatabaseInstanceByUUIDKeyExtractor());

        return new IDatabaseInstanceFinder()
            {
                public DatabaseInstancePE getHomeDatabaseInstance()
                {
                    return daoFactory.getHomeDatabaseInstance();
                }

                public DatabaseInstancePE tryFindDatabaseInstanceByCode(final String databaseInstanceCode)
                {
                    return databaseInstancesByCode.tryGet(databaseInstanceCode);
                }

                public DatabaseInstancePE tryFindDatabaseInstanceByUUID(final String databaseInstanceUUID)
                {
                    return databaseInstancesByUUID.tryGet(databaseInstanceUUID);
                }
            };
    }

    /**
     * Creates database instance finder which checks the database everytime when the instance is
     * searched.
     */
    private final static IDatabaseInstanceFinder createInstanceFinder(final IAuthorizationDAOFactory daoFactory)
    {
        return new IDatabaseInstanceFinder()
            {
                public DatabaseInstancePE getHomeDatabaseInstance()
                {
                    return daoFactory.getHomeDatabaseInstance();
                }

                public DatabaseInstancePE tryFindDatabaseInstanceByCode(final String databaseInstanceCode)
                {
                    return daoFactory.getDatabaseInstancesDAO().tryFindDatabaseInstanceByCode(
                            databaseInstanceCode);
                }

                public DatabaseInstancePE tryFindDatabaseInstanceByUUID(
                        final String databaseInstanceUUID)
                {
                    return daoFactory.getDatabaseInstancesDAO().tryFindDatabaseInstanceByUUID(
                            databaseInstanceUUID);
                }
            };
    }

    /** finds a group in the database for the given identifier */
    public static final GroupPE tryGetGroup(final GroupIdentifier groupIdentifier, final PersonPE person,
            final IAuthorizationDAOFactory daoFactory)
    {
        final String groupCode = getGroupCode(person, groupIdentifier);
        final Long databaseInstanceId = getDatabaseInstanceId(groupIdentifier, daoFactory);
        final IGroupDAO groupDAO = daoFactory.getGroupDAO();
        return groupDAO.tryFindGroupByCodeAndDatabaseInstanceId(groupCode, databaseInstanceId);
    }

    /**
     * Tries to find out the group.
     * <p>
     * If not specified in given {@link GroupIdentifier} must be specified as home group in given
     * {@link PersonPE}.
     * </p>
     * 
     * @throws UndefinedGroupException if no group could be found.
     */
    public final static String getGroupCode(final PersonPE person,
            final GroupIdentifier groupIdentifier) throws UndefinedGroupException
    {
        if (groupIdentifier.isHomeGroup())
        {
            final GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup == null)
            {
                throw new UndefinedGroupException();
            }
            return homeGroup.getCode();
        } else
        {
            return groupIdentifier.getGroupCode();
        }
    }

    /**
     * Returns the database instance id.
     * 
     * @throws UserFailureException whether code found in given {@link DatabaseInstanceIdentifier}
     *             does not exist.
     */
    public final static long getDatabaseInstanceId(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final IAuthorizationDAOFactory daoFactory) throws UserFailureException
    {
        return getDatabaseInstance(databaseInstanceIdentifier, createInstanceFinder(daoFactory))
                .getId();
    }

    public final static DatabaseInstancePE getDatabaseInstance(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final IAuthorizationDAOFactory daoFactory) throws UserFailureException
    {
        return getDatabaseInstance(databaseInstanceIdentifier, createInstanceFinder(daoFactory));
    }

    public final static DatabaseInstancePE getDatabaseInstance(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final IDatabaseInstanceFinder instanceFinder) throws UserFailureException
    {
        final DatabaseInstancePE instancePE =
                tryGetDatabaseInstance(databaseInstanceIdentifier, instanceFinder);
        if (instancePE == null)
        {
            throw UserFailureException.fromTemplate("Database instance '%s' does not exist.",
                    databaseInstanceIdentifier);
        }
        return instancePE;
    }

    public final static DatabaseInstancePE tryGetDatabaseInstance(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final IAuthorizationDAOFactory daoFactory) throws UserFailureException
    {
        return tryGetDatabaseInstance(databaseInstanceIdentifier, createInstanceFinder(daoFactory));
    }

    private final static DatabaseInstancePE tryGetDatabaseInstance(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final IDatabaseInstanceFinder instanceFinder) throws UserFailureException
    {
        if (databaseInstanceIdentifier.isHomeDatabase())
        {
            return instanceFinder.getHomeDatabaseInstance();
        } else
        {
            final String databaseInstanceCode =
                    databaseInstanceIdentifier.getDatabaseInstanceCode();
            return tryGetDatabaseInstanceByCode(instanceFinder, databaseInstanceCode);
        }
    }

    private static DatabaseInstancePE tryGetDatabaseInstanceByCode(
            final IDatabaseInstanceFinder instanceFinder, final String code)
    {
        final boolean isUUID = UuidUtil.isValidUUID(code);
        if (isUUID)
        {
            return instanceFinder.tryFindDatabaseInstanceByUUID(code);
        } else
        {
            return instanceFinder.tryFindDatabaseInstanceByCode(code);
        }
    }
}
