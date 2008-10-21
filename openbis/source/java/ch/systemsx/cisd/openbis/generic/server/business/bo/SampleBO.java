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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwnerFinder;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * The unique {@link ISampleBO} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBO extends AbstractBusinessObject implements ISampleBO
{
    private final SampleOwnerFinder sampleOwnerFinder;

    private SamplePE sample;

    public SampleBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
        this.sampleOwnerFinder = new SampleOwnerFinder(daoFactory, session.tryGetPerson());
    }

    //
    // ISampleBO
    //

    public final SamplePE getSample()
    {
        assert sample != null : "Unloaded sample.";
        return sample;
    }

    public final void loadBySampleIdentifier(final SampleIdentifier identifier)
    {
        final SampleOwner sampleOwner = sampleOwnerFinder.figureSampleOwner(identifier);
        final String sampleCode = identifier.getSampleCode();
        final ISampleDAO sampleDAO = getSampleDAO();
        if (sampleOwner.isDatabaseInstanceLevel())
        {
            sample =
                    sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode, sampleOwner
                            .tryGetDatabaseInstance());
        } else
        {
            assert sampleOwner.isGroupLevel() : "Must be of group level.";
            sample = sampleDAO.tryFindByCodeAndGroup(sampleCode, sampleOwner.tryGetGroup());
        }
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
    }
}
