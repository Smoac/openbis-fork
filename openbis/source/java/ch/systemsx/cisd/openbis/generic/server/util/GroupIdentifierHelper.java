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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDatabaseInstanceFinder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.DatabaseInstanceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

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
     * always has a code, never UUID. Normalized space identifier has always space code, even when
     * it is a home space. It also has normalized database identifier.
     */
    public static class SpaceIdentifierNormalizer
    {
        public static SpaceIdentifierNormalizer create(final IAuthorizationDAOFactory daoFactory,
                final String homeSpaceCodeOrNull)
        {
            final IDatabaseInstanceFinder instanceFinder =
                    GroupIdentifierHelper.createCachedInstanceFinder(daoFactory);
            return new SpaceIdentifierNormalizer(instanceFinder, homeSpaceCodeOrNull);
        }

        private final IDatabaseInstanceFinder databaseInstanceFinder;

        private final String homeSpaceCodeOrNull;

        private SpaceIdentifierNormalizer(final IDatabaseInstanceFinder databaseInstanceFinder,
                final String homeSpaceCodeOrNull)
        {
            this.databaseInstanceFinder = databaseInstanceFinder;
            this.homeSpaceCodeOrNull = homeSpaceCodeOrNull;
        }

        public final SpaceIdentifier normalize(final SpaceIdentifier identifier)
        {
            return GroupIdentifierHelper.normalize(identifier, homeSpaceCodeOrNull,
                    databaseInstanceFinder);
        }

        public final SampleIdentifier normalize(final SampleIdentifier identifier)
        {
            return GroupIdentifierHelper.normalize(identifier, homeSpaceCodeOrNull,
                    databaseInstanceFinder);
        }
    }

    /** Transforms given space identifier to the canonical form. */
    private final static SpaceIdentifier normalize(final SpaceIdentifier spaceIdentifier,
            final String homeSpaceCodeOrNull, final IDatabaseInstanceFinder instanceFinder)
    {
        final DatabaseInstanceIdentifier instance = normalize(spaceIdentifier, instanceFinder);
        String spaceCode = spaceIdentifier.getSpaceCode();
        if (spaceCode == null)
        {
            spaceCode = homeSpaceCodeOrNull;
        }
        return new SpaceIdentifier(instance, spaceCode.toUpperCase());
    }

    /** Transforms given sample identifier to the canonical form. */
    private final static SampleIdentifier normalize(final SampleIdentifier sampleIdentifier,
            final String homeSpaceCodeOrNull, final IDatabaseInstanceFinder instanceFinder)
    {
        if (sampleIdentifier.isDatabaseInstanceLevel())
        {
            final DatabaseInstanceIdentifier instanceIdentifier =
                    normalize(sampleIdentifier.getDatabaseInstanceLevel(), instanceFinder);
            return new SampleIdentifier(instanceIdentifier, sampleIdentifier.getSampleCode());
        } else if (sampleIdentifier.isSpaceLevel())
        {
            final SpaceIdentifier spaceIdentifier =
                    normalize(sampleIdentifier.getSpaceLevel(), homeSpaceCodeOrNull, instanceFinder);
            return new SampleIdentifier(spaceIdentifier, sampleIdentifier.getSampleCode());
        } else
        {
            throw InternalErr.error(sampleIdentifier);
        }
    }

    private static DatabaseInstanceIdentifier normalize(
            final DatabaseInstanceIdentifier identifier,
            final IDatabaseInstanceFinder instanceFinder)
    {
        final String code =
                DatabaseInstanceIdentifierHelper.getDatabaseInstance(identifier, instanceFinder)
                        .getCode();
        return new DatabaseInstanceIdentifier(code.toUpperCase());
    }

    /**
     * Creates database instance finder which caches all existing database instance at the begining.
     */
    public final static IDatabaseInstanceFinder createCachedInstanceFinder(
            final IAuthorizationDAOFactory daoFactory)
    {
        final List<DatabaseInstancePE> instances =
                daoFactory.getDatabaseInstanceDAO().listDatabaseInstances();

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

                public DatabaseInstancePE tryFindDatabaseInstanceByCode(
                        final String databaseInstanceCode)
                {
                    return databaseInstancesByCode.tryGet(databaseInstanceCode);
                }

                public DatabaseInstancePE tryFindDatabaseInstanceByUUID(
                        final String databaseInstanceUUID)
                {
                    return databaseInstancesByUUID.tryGet(databaseInstanceUUID);
                }
            };
    }

    /**
     * Creates database instance finder which checks the database everytime when the instance is
     * searched.
     */
    private final static IDatabaseInstanceFinder createInstanceFinder(
            final IAuthorizationDAOFactory daoFactory)
    {
        return new IDatabaseInstanceFinder()
            {
                public DatabaseInstancePE getHomeDatabaseInstance()
                {
                    return daoFactory.getHomeDatabaseInstance();
                }

                public DatabaseInstancePE tryFindDatabaseInstanceByCode(
                        final String databaseInstanceCode)
                {
                    return daoFactory.getDatabaseInstanceDAO().tryFindDatabaseInstanceByCode(
                            databaseInstanceCode);
                }

                public DatabaseInstancePE tryFindDatabaseInstanceByUUID(
                        final String databaseInstanceUUID)
                {
                    return daoFactory.getDatabaseInstanceDAO().tryFindDatabaseInstanceByUUID(
                            databaseInstanceUUID);
                }
            };
    }

    /** finds a space in the database for the given identifier */
    public static final SpacePE tryGetGroup(final SpaceIdentifier spaceIdentifier,
            final PersonPE person, final IAuthorizationDAOFactory daoFactory)
    {
        final String spaceCode = SpaceCodeHelper.getSpaceCode(person, spaceIdentifier);
        final DatabaseInstancePE databaseInstance =
                getDatabaseInstance(spaceIdentifier, daoFactory);
        final ISpaceDAO groupDAO = daoFactory.getSpaceDAO();
        return groupDAO.tryFindSpaceByCodeAndDatabaseInstance(spaceCode, databaseInstance);
    }

    public final static DatabaseInstancePE getDatabaseInstance(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final IAuthorizationDAOFactory daoFactory) throws UserFailureException
    {
        IDatabaseInstanceFinder finder = createInstanceFinder(daoFactory);
        return DatabaseInstanceIdentifierHelper.getDatabaseInstance(databaseInstanceIdentifier,
                finder);
    }

    public final static DatabaseInstancePE tryGetDatabaseInstance(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final IAuthorizationDAOFactory daoFactory) throws UserFailureException
    {
        IDatabaseInstanceFinder finder = createInstanceFinder(daoFactory);
        return DatabaseInstanceIdentifierHelper.tryGetDatabaseInstance(databaseInstanceIdentifier,
                finder);
    }

}
