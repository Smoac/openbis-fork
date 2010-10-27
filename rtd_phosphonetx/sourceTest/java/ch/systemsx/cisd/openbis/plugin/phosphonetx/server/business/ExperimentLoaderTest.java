/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "db")
public class ExperimentLoaderTest extends AbstractLoaderTestCase
{
    @Test
    public void test()
    {
        ExperimentLoader loader = new ExperimentLoader(daoFactory);
        List<Sample> samples = loadSamples(980l, 981l, 982l, 983l, 984l);
        
        loader.enrichWithExperiments(samples);
        
        StringBuilder builder = new StringBuilder();
        for (Sample sample : samples)
        {
            builder.append(sample.getId()).append(' ').append(sample.getCode());
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                builder.append(": ").append(experiment.getCode()).append(' ');
                builder.append(getSortedProperties(experiment));
            }
            builder.append('\n');
        }
        assertEquals("980 3V-126\n" 
                   + "981 DP\n"
                   + "982 3VCP1: EXP1 [DESCRIPTION: A simple experiment, GENDER: MALE]\n"
                   + "983 3VCP2\n"
                   + "984 3VCP3: EXP1 [DESCRIPTION: A simple experiment, GENDER: MALE]\n",
                builder.toString());
    }
    
    private List<Sample> loadSamples(Long... ids)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        List<Sample> list = new ArrayList<Sample>();
        for (Long id : ids)
        {
            list.add(SampleTranslator.translate(sampleDAO.tryGetByTechId(new TechId(id)), ""));
   
        }
        return list;
    }
}
