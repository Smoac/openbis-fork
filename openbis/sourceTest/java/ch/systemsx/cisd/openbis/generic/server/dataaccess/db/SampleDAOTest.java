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

import org.hibernate.Hibernate;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

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
        SampleTypePE fstType = getSampleType(SampleTypeCode.MASTER_PLATE);
        SampleTypePE secType = getSampleType(SampleTypeCode.DILUTION_PLATE);
        SampleTypePE thrType = getSampleType(SampleTypeCode.CELL_PLATE);

        thrType.setContainerHierarchyDepth(1);
        thrType.setGeneratedFromHierarchyDepth(1);

        SamplePE sampleA = createSample(fstType, "grandParent", null);
        SamplePE sampleB = createSample(secType, "parent", sampleA);
        SamplePE sampleC = createSample(thrType, "child", sampleB);
        save(sampleA, sampleB, sampleC);

        SamplePE well = createSample(thrType, "well", null);
        SamplePE container = createSample(secType, "container", null);
        SamplePE superContainer = createSample(secType, "superContainer", null);
        well.setContainer(container);
        container.setContainer(superContainer);
        save(superContainer, container, well);

        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        // clear session to avoid using samples from first level cache
        sessionFactory.getCurrentSession().clear();
        List<SamplePE> samples = sampleDAO.listSamples(thrType);

        SamplePE foundWell = findSample(well, samples);
        AssertJUnit.assertTrue(Hibernate.isInitialized(foundWell.getContainer()));
        SamplePE foundContainer = foundWell.getContainer();
        AssertJUnit.assertFalse(Hibernate.isInitialized(foundContainer.getContainer()));

        sampleC = findSample(sampleC, samples);
        AssertJUnit.assertTrue(Hibernate.isInitialized(sampleC.getGeneratedFrom()));
        SamplePE parent = sampleC.getGeneratedFrom();
        AssertJUnit.assertFalse(Hibernate.isInitialized(parent.getGeneratedFrom()));
    }

    private SamplePE findSample(SamplePE sample, List<SamplePE> samples)
    {
        int sampleIx = samples.indexOf(sample);
        assert sampleIx != -1 : "sample not found " + sample;
        return samples.get(sampleIx);
    }

    private void save(SamplePE... samples)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        for (SamplePE samplePE : samples)
        {
            sampleDAO.createSample(samplePE);
        }
    }

    private SampleTypePE getSampleType(SampleTypeCode sampleTypeCode)
    {
        SampleTypePE sampleType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode.getCode());
        assert sampleType != null;
        return sampleType;
    }

    final SamplePE createSample(final SampleTypePE type, final String code, SamplePE generatorOrNull)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(getSystemPerson());
        sample.setCode(code);
        sample.setSampleType(type);
        sample.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        sample.setGeneratedFrom(generatorOrNull);
        return sample;
    }
}
