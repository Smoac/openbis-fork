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
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * An <i>abstract</i> {@link AbstractBusinessObject} extension for <i>Business Object</i> which
 * uses {@link SampleIdentifier}.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractSampleIdentifierBusinessObject extends AbstractBusinessObject
{
    private final SampleOwnerFinder sampleOwnerFinder;

    AbstractSampleIdentifierBusinessObject(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
        sampleOwnerFinder = new SampleOwnerFinder(daoFactory, findRegistrator());

    }

    final SampleOwnerFinder getSampleOwnerFinder()
    {
        return sampleOwnerFinder;
    }

    /**
     * Finds a sample with the given identifier.<br>
     * Note: this method will never return samples which are contained (part-of relation) in another
     * sample.
     * 
     * @return never <code>null</code> and prefers to throw an exception.
     */
    final SamplePE getSampleByIdentifier(final SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        final SamplePE sample = tryToGetSampleByIdentifier(sampleIdentifier);
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found for identifier '%s'.", sampleIdentifier);
        }
        return sample;
    }

    protected SamplePE tryToGetSampleByIdentifier(final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Sample identifier unspecified.";
        final SampleOwner sampleOwner = sampleOwnerFinder.figureSampleOwner(sampleIdentifier);
        final String sampleCode = sampleIdentifier.getSampleCode();
        final ISampleDAO sampleDAO = getSampleDAO();
        final SamplePE sample;
        if (sampleOwner.isDatabaseInstanceLevel())
        {
            sample =
                    sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode, sampleOwner
                            .tryGetDatabaseInstance(), HierarchyType.CHILD);
        } else
        {
            assert sampleOwner.isGroupLevel() : "Must be of group level.";
            sample =
                    sampleDAO.tryFindByCodeAndGroup(sampleCode, sampleOwner.tryGetGroup(),
                            HierarchyType.CHILD);
        }
        return sample;
    }

}