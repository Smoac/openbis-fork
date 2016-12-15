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

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

/**
 * Some useful identifier methods on the <i>server</i> side.
 * <p>
 * This class is the only place which provides the correct way to resolve {@link DatabaseInstanceIdentifier} into a {@link DatabaseInstancePE}!
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class SpaceIdentifierHelper
{

    private SpaceIdentifierHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Class which transforms identifiers to the canonical form. Normalized database identifier always has a code, never UUID. Normalized space
     * identifier has always space code, even when it is a home space. It also has normalized database identifier.
     */
    public static class SpaceIdentifierNormalizer
    {
        public static SpaceIdentifierNormalizer create(final IAuthorizationDAOFactory daoFactory,
                final String homeSpaceCodeOrNull)
        {
            return new SpaceIdentifierNormalizer(homeSpaceCodeOrNull);
        }

        private final String homeSpaceCodeOrNull;

        private SpaceIdentifierNormalizer(final String homeSpaceCodeOrNull)
        {
            this.homeSpaceCodeOrNull = homeSpaceCodeOrNull;
        }

        public final SpaceIdentifier normalize(final SpaceIdentifier identifier)
        {
            return SpaceIdentifierHelper.normalize(identifier, homeSpaceCodeOrNull);
        }

        public final SampleIdentifier normalize(final SampleIdentifier identifier)
        {
            return SpaceIdentifierHelper.normalize(identifier, homeSpaceCodeOrNull);
        }
    }

    /** Transforms given space identifier to the canonical form. */
    private final static SpaceIdentifier normalize(final SpaceIdentifier spaceIdentifier,
            final String homeSpaceCodeOrNull)
    {
        String spaceCode = spaceIdentifier.getSpaceCode();
        if (spaceCode == null)
        {
            spaceCode = homeSpaceCodeOrNull;
        }
        return new SpaceIdentifier(spaceCode.toUpperCase());
    }

    /** Transforms given sample identifier to the canonical form. */
    private final static SampleIdentifier normalize(final SampleIdentifier sampleIdentifier,
            final String homeSpaceCodeOrNull)
    {
        if (sampleIdentifier.isDatabaseInstanceLevel())
        {
            return new SampleIdentifier(sampleIdentifier.getSampleCode());
        } else if (sampleIdentifier.isSpaceLevel())
        {
            final SpaceIdentifier spaceIdentifier =
                    normalize(sampleIdentifier.getSpaceLevel(), homeSpaceCodeOrNull);
            return new SampleIdentifier(spaceIdentifier, sampleIdentifier.getSampleCode());
        } else
        {
            throw InternalErr.error(sampleIdentifier);
        }
    }

    /** finds a space in the database for the given identifier */
    public static final SpacePE tryGetSpace(final SpaceIdentifier spaceIdentifier,
            final PersonPE person, final IAuthorizationDAOFactory daoFactory)
    {
        final String spaceCode = SpaceCodeHelper.getSpaceCode(person, spaceIdentifier);
        final ISpaceDAO groupDAO = daoFactory.getSpaceDAO();
        return groupDAO.tryFindSpaceByCode(spaceCode);
    }
    
    public static ProjectPE tryGetProject(ProjectIdentifier projectIdentifier,
            final PersonPE person, final IAuthorizationDAOFactory daoFactory)
    {
        String spaceCode = SpaceCodeHelper.getSpaceCode(person, projectIdentifier);
        IProjectDAO projectDAO = daoFactory.getProjectDAO();
        return projectDAO.tryFindProject(spaceCode, projectIdentifier.getProjectCode());
    }
}
