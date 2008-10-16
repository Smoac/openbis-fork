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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * Test cases for corresponding {@link SamplePropertyDAO} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = SamplePropertyDAO.class)
@Test(groups =
    { "db", "sampleProperty" })
public final class SamplePropertyDAOTest extends AbstractDAOTest
{

    @Test
    public void testListProperties() throws Exception
    {

        SampleTypePE type =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(
                        SampleTypeCode.MASTER_PLATE.getCode());
        DatabaseInstancePE instance = daoFactory.getDatabaseInstancesDAO().getHomeInstance();
        GroupPE group = getSystemPerson().getHomeGroup();

        SamplePE instanceSample = createInstanceSample(type, instance);
        SamplePE groupSample = createGroupSample(type, group);

        final List<String> propertyCodes = new ArrayList<String>();
        propertyCodes.add("USER.DESCRIPTION");

        List<SampleIdentifier> identifiers = new ArrayList<SampleIdentifier>();
        identifiers.add(instanceSample.getSampleIdentifier());
        identifiers.add(groupSample.getSampleIdentifier());

        // daoFactory.getSamplePropertyDAO().listSampleProperties(identifiers, propertyCodes);
    }

    private SamplePE createGroupSample(SampleTypePE type, GroupPE group)
    {
        SamplePE sample = new SamplePE();
        sample.setCode("GROUP_SAMPLE");
        sample.setRegistrator(getSystemPerson());
        sample.setGroup(group);
        sample.setSampleType(type);
        daoFactory.getSampleDAO().createSample(sample);
        return sample;
    }

    private SamplePE createInstanceSample(SampleTypePE type, DatabaseInstancePE instance)
    {
        SamplePE sample = new SamplePE();
        sample.setCode("INSTANCE_SAMPLE");
        sample.setRegistrator(getSystemPerson());
        sample.setDatabaseInstance(instance);
        sample.setSampleType(type);
        daoFactory.getSampleDAO().createSample(sample);
        return sample;
    }

}
