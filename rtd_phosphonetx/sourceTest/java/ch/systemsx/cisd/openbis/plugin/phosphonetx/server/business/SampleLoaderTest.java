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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "db")
public class SampleLoaderTest extends AbstractLoaderTestCase
{

    @Test
    public void test()
    {
        SampleLoader loader = new SampleLoader(SESSION, daoFactory, boFactory);
        List<Sample> samples = loader.listSamplesWithParentsByTypeAndSpace("CELL_PLATE", "CISD");
        Collections.sort(samples, new Comparator<Sample>()
            {
                public int compare(Sample s1, Sample s2)
                {
                    return s1.getCode().compareTo(s2.getCode());
                }
            });
        StringBuilder builder = new StringBuilder();
        for (Sample sample : samples)
        {
            Sample parent = sample.getGeneratedFrom();
            builder.append(sample.getCode()).append(" ").append(getSortedProperties(sample));
            builder.append(" <- ").append(parent.getCode()).append(" ");
            builder.append(getSortedProperties(parent)).append('\n');
        }
        assertEquals("3VCP1 [] <- 3V-123 [OFFSET: 42]\n"
                   + "3VCP2 [] <- 3V-123 [OFFSET: 42]\n"
                   + "3VCP4 [] <- 3V-125 [OFFSET: 49]\n"
                   + "3VCP5 [] <- 3V-125 [OFFSET: 49]\n"
                   + "3VCP6 [] <- 3V-125 [OFFSET: 49]\n"
                   + "3VCP7 [COMMENT: test comment, ORGANISM: RAT, SIZE: 4711] <- 3V-125 [OFFSET: 49]\n"
                   + "3VCP8 [] <- 3V-125 [OFFSET: 49]\n" 
                   + "CP-TEST-1 [ANY_MATERIAL: 1 (GENE), BACTERIUM: BACTERIUM-X (BACTERIUM), "
                     + "COMMENT: very advanced stuff, ORGANISM: HUMAN, SIZE: 123] "
                     + "<- CP-TEST-2 [ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                     + "COMMENT: extremely simple stuff, ORGANISM: GORILLA, SIZE: 321]\n"
                   + "CP1-A1 [] <- DP1-A [OFFSET: 42]\n"
                   + "CP1-A2 [] <- DP1-A [OFFSET: 42]\n" 
                   + "CP1-B1 [] <- DP1-B [OFFSET: 42]\n"
                   + "CP2-A1 [] <- DP2-A [OFFSET: 42]\n", builder.toString());
    }

}
