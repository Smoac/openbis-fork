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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Test cases for corresponding {@link HibernateSearchDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "hibernateSearch", "broken" })
// TODO 2008-11-04, Christian Ribeaud: Put this test in the broken till we have a good strategy how
// to include the Hibernate Search tests.
public final class HibernateSearchDAOTest extends AbstractDAOTest
{

    private final static void checkSamples(final List<SamplePE> samples, final int size,
            final String code, final String lastName)
    {
        assertTrue(samples.size() > 0);
        assertEquals(size, samples.size());
        for (final SamplePE samplePE : samples)
        {
            assertEquals(code, samplePE.getCode());
            assertEquals(lastName, samplePE.getRegistrator().getLastName());
        }
    }

    @Test
    public final void testSearchEntitiesByTermForC11()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        final String code = "C11";
        final String lastName = "System User";
        final int size = 69;
        List<SamplePE> samples =
                hibernateSearchDAO.searchEntitiesByTerm(SamplePE.class, new String[]
                    { "code" }, "C11");
        checkSamples(samples, size, code, lastName);
        samples = hibernateSearchDAO.searchEntitiesByTerm(SamplePE.class, new String[]
            { "code" }, "code:C11");
        checkSamples(samples, size, code, lastName);
        samples = hibernateSearchDAO.searchEntitiesByTerm(SamplePE.class, new String[]
            { "code", "registrator.lastName" }, "code:C11 AND registrator.lastName:System User");
        checkSamples(samples, size, code, lastName);
        samples = hibernateSearchDAO.searchEntitiesByTerm(SamplePE.class, new String[]
            { "code", "registrator.lastName" }, "code:C11 AND registrator.lastName:System*");
        checkSamples(samples, size, code, lastName);
    }

}
