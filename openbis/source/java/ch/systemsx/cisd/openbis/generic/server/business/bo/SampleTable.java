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
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwnerFinder;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwnerFinder.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * @author Tomasz Pylak
 */
public class SampleTable extends AbstractBusinessObject implements ISampleTable
{
    private final IDAOFactory daoFactory;

    SampleTable(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
        this.daoFactory = daoFactory;
    }

    public List<SamplePE> listSamples(SampleTypePE sampleTypeExample,
            List<SampleOwnerIdentifier> ownerIdentifiers)
    {
        SampleTypePE sampleType = getSampleTypeDAO().tryFindByExample(sampleTypeExample);
        if (sampleType == null)
        {
            throw new UserFailureException("Cannot find a sample type matching to "
                    + sampleTypeExample);
        }
        SampleOwnerFinder finder = new SampleOwnerFinder(daoFactory, findRegistrator());
        List<SamplePE> samples = new ArrayList<SamplePE>();
        for (SampleOwnerIdentifier sampleOwnerIdentifier : ownerIdentifiers)
        {
            SampleOwner owner = finder.figureSampleOwner(sampleOwnerIdentifier);
            samples.addAll(listSamples(sampleType, owner));
        }
        return samples;
    }

    private List<SamplePE> listSamples(SampleTypePE sampleType, SampleOwner owner)
    {
        ISampleDAO sampleDAO = getSampleDAO();
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
