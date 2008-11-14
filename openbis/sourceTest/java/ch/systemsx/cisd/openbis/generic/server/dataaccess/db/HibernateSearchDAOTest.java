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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Test cases for corresponding {@link HibernateSearchDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "hibernateSearch" })
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

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] getC11FieldsAndTerm()
    {
        return new Object[][]
            {
                { new String[]
                    { "code" }, "C11" },
                { new String[]
                    { "code" }, "code:C11" },
                { new String[]
                    { "code", "registrator.lastName" }, "code:C11 AND registrator.lastName:Doe" },
                { new String[]
                    { "code", "registrator.lastName" }, "code:C11 AND registrator.lastName:d*" },
                { new String[]
                    { "code", "registrator.lastName" }, "code:C11 AND registrator.lastName:*oe" } };
    }

    @Test
    public final void testSearchEntitiesByTermFailed()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        boolean fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(null, null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            hibernateSearchDAO.searchEntitiesByTerm(MaterialPE.class,
                    ArrayUtils.EMPTY_STRING_ARRAY, "");
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test(dataProvider = "getC11FieldsAndTerm")
    public final void testSearchEntitiesByTermForSampleC11(final String[] fields, final String term)
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        final String code = "C11";
        final String lastName = "Doe";
        final int size = 3;
        final List<SamplePE> samples =
                hibernateSearchDAO.searchEntitiesByTerm(SamplePE.class, fields, term);
        checkSamples(samples, size, code, lastName);
    }

    @Test
    public final void testSearchEntitiesByTermForExperiment()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        final List<ExperimentPE> experiments =
                hibernateSearchDAO.searchEntitiesByTerm(ExperimentPE.class, new String[]
                    { "code" }, "exp");
        assertEquals(2, experiments.size());
        assertEquals("EXP-X", experiments.get(0).getCode());
        assertEquals("EXP-REUSE", experiments.get(1).getCode());
    }

    @Test
    public final void testSearchEntitiesByTermForMaterial()
    {
        final IHibernateSearchDAO hibernateSearchDAO = daoFactory.getHibernateSearchDAO();
        final List<MaterialPE> experiments =
                hibernateSearchDAO.searchEntitiesByTerm(MaterialPE.class, new String[]
                    { "materialProperties.value", "materialProperties.vocabularyTerm.code" },
                        "adenovirus");
        assertEquals(2, experiments.size());
        assertEquals("AD3", experiments.get(0).getCode());
        assertEquals("AD5", experiments.get(1).getCode());
    }
}
