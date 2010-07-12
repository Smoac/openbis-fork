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

package ch.systemsx.cisd.yeastx.quant;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.xml.JaxbXmlParser;
import ch.systemsx.cisd.common.xml.XmlDateAdapter;
import ch.systemsx.cisd.yeastx.quant.dto.MSConcentrationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationsDTO;

/**
 * Tests that *.quantML files can be parsed to the {@link MSQuantificationsDTO} bean.
 * 
 * @author Tomasz Pylak
 */
public class QuantMLParserTest extends AssertJUnit
{

    private MSQuantificationsDTO parse(File file)
    {
        return JaxbXmlParser.parse(MSQuantificationsDTO.class, file, true);
    }

    @Test
    public void testParseQuantFileRealContent()
    {
        File file = new File("resource/examples/TEST&TEST_PROJECT&TEST_EXP.quantML");
        MSQuantificationsDTO quantifications = parse(file);
        assertEquals(1, quantifications.getQuantifications().size());
        MSQuantificationDTO quantification = quantifications.getQuantifications().get(0);
        assertEquals("msSoft", quantification.getSource());
        assertTrue(quantification.isValid());
        assertNull(quantification.getComment());
        assertNull(quantification.getRegistrator());
        assertNull(quantification.getRegistrationDate());

        assertEquals(52, quantification.getConcentrations().size());
        MSConcentrationDTO concentration = quantification.getConcentrations().get(0);
        assertEquals(0.689155, concentration.getAmount());
        assertEquals("", concentration.getComment());
        assertTrue(concentration.isValid());
        assertEquals("20090822211007858-23605", concentration.getParentDatasetCode());
        assertEquals("", concentration.getInternalStandard());
        assertEquals("", concentration.getUnit());
        assertEquals(664.1, concentration.getQ1());
        assertEquals(408.0, concentration.getQ3());
        assertEquals(939.5, concentration.getRetentionTime());
        List<Long> compoundIds = concentration.getCompounds().getCompoundIds();
        assertEquals(1, compoundIds.size());
        assertEquals(23806, compoundIds.get(0).longValue());
    }

    @Test
    public void testParseQuantFileFullContent() throws Exception
    {
        File file = new File("resource/examples/allFields.quantML");
        MSQuantificationsDTO quantifications = parse(file);
        assertEquals(1, quantifications.getQuantifications().size());
        MSQuantificationDTO quantification = quantifications.getQuantifications().get(0);
        assertEquals("msSoft", quantification.getSource());
        assertTrue(quantification.isValid());
        assertEquals("no comment", quantification.getComment());
        assertEquals("John Doe", quantification.getRegistrator());
        Date expectedDate = new XmlDateAdapter().unmarshal("04-Apr-1980 12:00:21");
        assertEquals(expectedDate, quantification.getRegistrationDate());

        assertEquals(1, quantification.getConcentrations().size());
        MSConcentrationDTO concentration = quantification.getConcentrations().get(0);
        assertEquals(0.689155, concentration.getAmount());
        assertEquals("concentration comment", concentration.getComment());
        assertTrue(concentration.isValid());
        assertEquals("20090822211007858-23605", concentration.getParentDatasetCode());
        assertEquals("value1", concentration.getInternalStandard());
        assertEquals("kg", concentration.getUnit());
        assertEquals(664.1, concentration.getQ1());
        assertEquals(408.0, concentration.getQ3());
        assertEquals(939.5, concentration.getRetentionTime());
        List<Long> compundIds = concentration.getCompounds().getCompoundIds();
        assertEquals(2, compundIds.size());
        assertEquals(1, compundIds.get(0).longValue());
        assertEquals(2, compundIds.get(1).longValue());
    }
}
