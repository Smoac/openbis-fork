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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for corresponding {@link SampleTable} class.
 * 
 * @author Piotr Buczek
 */
public final class SampleTableTest extends AbstractBOTest
{

    @Test
    public void testDelete()
    {
        final List<TechId> sampleIds = new ArrayList<TechId>();
        sampleIds.add(new TechId(1L));
        sampleIds.add(new TechId(2L));
        sampleIds.add(new TechId(3L));
        final String reason = "reason";
        context.checking(new Expectations()
            {
                {
                    PersonPE registrator = EXAMPLE_SESSION.tryGetPerson();
                    one(sampleDAO).delete(sampleIds, registrator, reason);
                }
            });
        final SampleTable sampleTableBO = createSampleTableBO();
        sampleTableBO.deleteByTechIds(sampleIds, reason);
        context.assertIsSatisfied();
    }

    private final SampleTable createSampleTableBO()
    {
        return new SampleTable(daoFactory, EXAMPLE_SESSION);
    }

}
