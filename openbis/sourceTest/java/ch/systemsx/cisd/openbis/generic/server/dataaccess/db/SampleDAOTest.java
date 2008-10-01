/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationsDepthDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * Test cases for corresponding {@link SampleDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
    { "db", "sample" })
public final class SampleDAOTest extends AbstractDAOTest
{

    @Test
    public final void testListSamples()
    {
        SampleTypePE anySampleType = getAnySampleType();
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        SampleRelationsDepthDTO displayProperties = new SampleRelationsDepthDTO(0, 0);
        List<SamplePE> samples = sampleDAO.listSamples(anySampleType, displayProperties);
        assert samples.size() > 0;
    }

    private SampleTypePE getAnySampleType()
    {
        return daoFactory.getSampleTypeDAO().listSampleTypes(true).get(0);
    }
}
