/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.managed_property;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AbstractBOTest;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityLinkElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.ElementFactory;

/**
 * @author Piotr Buczek
 */
public class EntityInformationProviderTest extends AbstractBOTest
{

    private EntityInformationProvider provider;

    private ElementFactory elementFactory;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        provider = new EntityInformationProvider(daoFactory);
        elementFactory = new ElementFactory();
    }

    @Test
    public void testGetIdentifier()
    {
        final String mCode = "mCode";
        final String mTypeCode = "mTypeCode";
        final String sPermId = "sPermId";
        final String ePermId = "ePermId";
        final String dPermId = "dPermId";
        IEntityLinkElement sampleLink = elementFactory.createSampleLink(sPermId);
        IEntityLinkElement experimentLink = elementFactory.createExperimentLink(ePermId);
        IEntityLinkElement materialLink = elementFactory.createMaterialLink(mCode, mTypeCode);
        IEntityLinkElement datasetLink = elementFactory.createDataSetLink(dPermId);

        final SamplePE sample = CommonTestUtils.createSample();
        final ExperimentPE experiment =
                CommonTestUtils.createExperiment(new ExperimentIdentifier("proj", "exp"));
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID(sPermId);
                    will(returnValue(sample));

                    one(experimentDAO).tryGetByPermID(ePermId);
                    will(returnValue(experiment));
                    one(materialDAO).tryFindMaterial(new MaterialIdentifier(mCode, mTypeCode));
                    // will(returnValue(any(MaterialPE.class)));

                    one(externalDataDAO).tryToFindDataSetByCode(dPermId);
                    // will(returnValue(any(DataPE.class)));
                }
            });

        String sIdentifier = provider.getIdentifier(sampleLink);
        assertEquals(sample.getIdentifier(), sIdentifier);
        String eIdentifier = provider.getIdentifier(experimentLink);
        assertEquals(experiment.getIdentifier(), eIdentifier);
        String mIdentifier = provider.getIdentifier(materialLink);
        String dIdentifier = provider.getIdentifier(datasetLink);
    }

}
