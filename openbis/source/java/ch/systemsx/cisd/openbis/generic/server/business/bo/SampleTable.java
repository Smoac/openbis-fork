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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwnerFinder;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * @author Tomasz Pylak
 */
public final class SampleTable extends AbstractBusinessObject implements ISampleTable
{
    private final IDAOFactory daoFactory;

    public SampleTable(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
        this.daoFactory = daoFactory;
    }

    public List<SamplePE> listSamples(final ListSampleCriteriaDTO criteria)
    {
        final SampleTypePE sampleType =
                getSampleTypeDAO().tryFindByExample(criteria.getSampleType());
        if (sampleType == null)
        {
            throw new UserFailureException("Cannot find a sample type matching to "
                    + criteria.getSampleType());
        }
        final SampleOwnerFinder finder = new SampleOwnerFinder(daoFactory, findRegistrator());
        final List<SamplePE> samples = new ArrayList<SamplePE>();
        for (final SampleOwnerIdentifier sampleOwnerIdentifier : criteria.getOwnerIdentifiers())
        {
            final SampleOwner owner = finder.figureSampleOwner(sampleOwnerIdentifier);
            samples.addAll(listSamples(sampleType, owner));
        }
        setValidProcedure(samples);
        return samples;
    }

    private static void setValidProcedure(List<SamplePE> samples)
    {
        for (SamplePE sample : samples)
        {
            enrichWithProcedure(sample);
        }
    }

    /**
     * Enriches given <code>sample</code> with at most one procedure that contains a
     * non-invalidated experiment.
     * <p>
     * So if <code>sample</code> belongs only to invalidated experiments or does not belong to any
     * experiment at all, no procedure are joined.
     * </p>
     */
    private final static void enrichWithProcedure(final SamplePE samplePE)
    {
        assert samplePE != null : "Unspecified procedure holder.";
        samplePE.setValidProcedure(tryGetValidProcedure(samplePE.getProcedures()));
    }

    /**
     * Throws exception if there are more than 1 valid procedures or return <code>null</code> if
     * no valid procedure could be found.
     */
    private final static ProcedurePE tryGetValidProcedure(final List<ProcedurePE> procedures)
    {
        ProcedurePE foundProcedure = null;
        for (final ProcedurePE procedure : procedures)
        {
            final ExperimentPE experiment = procedure.getExperiment();
            // Invalid experiment can not be considered.
            if (experiment.getInvalidation() == null)
            {
                if (foundProcedure != null)
                {
                    throw UserFailureException.fromTemplate(
                            "Expected exactly one valid procedure, but found %d: %s", procedures
                                    .size(), procedures);
                }
                foundProcedure = procedure;
            }
        }
        return foundProcedure;
    }

    private List<SamplePE> listSamples(final SampleTypePE sampleType, final SampleOwner owner)
    {
        final ISampleDAO sampleDAO = getSampleDAO();
        if (owner.isGroupLevel())
        {
            return sampleDAO.listSamplesByTypeAndGroup(sampleType, owner.tryGetGroup());
        } else
        {
            return sampleDAO.listSamplesByTypeAndDatabaseInstance(sampleType, owner
                    .tryGetDatabaseInstance());
        }
    }
}
