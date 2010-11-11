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
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * An <i>abstract</i> {@link AbstractBusinessObject} extension for <i>Business Object</i> which uses
 * {@link SampleIdentifier}.
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
        // TODO 2010-11-10, Piotr Buczek: use cache
        assert sampleIdentifier != null : "Sample identifier unspecified.";
        final SampleOwner sampleOwner = sampleOwnerFinder.figureSampleOwner(sampleIdentifier);
        final String sampleCode = sampleIdentifier.getSampleCode();
        final ISampleDAO sampleDAO = getSampleDAO();
        final SamplePE sample;
        if (sampleOwner.isDatabaseInstanceLevel())
        {
            sample =
                    sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode,
                            sampleOwner.tryGetDatabaseInstance());
        } else
        {
            assert sampleOwner.isGroupLevel() : "Must be of space level.";
            sample = sampleDAO.tryFindByCodeAndGroup(sampleCode, sampleOwner.tryGetGroup());
        }
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getExperiment());
        }
        return sample;
    }

    /**
     * Finds a sample with the given technical identifier.<br>
     * Note: this method will never return samples which are contained (part-of relation) in another
     * sample.
     * 
     * @return never <code>null</code> and prefers to throw an exception.
     */
    final SamplePE getSampleByTechId(final TechId sampleId) throws UserFailureException
    {
        final SamplePE sample = tryToGetSampleByTechId(sampleId);
        if (sample == null)
        {
            throw UserFailureException.fromTemplate("No sample could be found for ID '%s'.",
                    sampleId);
        }
        return sample;
    }

    private static final String PROPERTY_TYPES = "sampleType.sampleTypePropertyTypesInternal";

    private static final String EXPERIMENT = "experimentInternal";

    protected SamplePE tryToGetSampleByTechId(final TechId sampleId)
    {
        String[] connections =
            { PROPERTY_TYPES, EXPERIMENT };
        return getSampleDAO().tryGetByTechId(sampleId, connections);
    }
}
