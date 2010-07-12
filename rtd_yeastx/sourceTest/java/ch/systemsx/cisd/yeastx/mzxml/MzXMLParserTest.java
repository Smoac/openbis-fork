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

package ch.systemsx.cisd.yeastx.mzxml;

import java.io.File;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.xml.JaxbXmlParser;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzInstrumentDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzPrecursorDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzRunDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzScanDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzXmlDTO;
import ch.systemsx.cisd.yeastx.utils.XmlUtils;

/**
 * Tests that *.mzXML files can be parsed to the {@link MzRunDTO} bean.
 * 
 * @author Tomasz Pylak
 */
public class MzXMLParserTest extends AssertJUnit
{

    private MzRunDTO parse(String path)
    {
        File file = new File(path);
        return JaxbXmlParser.parse(MzXmlDTO.class, file, false).getRun();
    }

    @Test
    public void testParseFile()
    {
        MzRunDTO content = parse("resource/examples/example.mzXML");
        MzInstrumentDTO instrument = content.getInstrument();
        assertNotNull(instrument);
        assertEquals("ABI / SCIEX", instrument.getInstrumentManufacturer().getValue());
        assertEquals("QTrap", instrument.getInstrumentModel().getValue());
        assertEquals("TOFMS", instrument.getInstrumentType().getValue());
        assertEquals("ESI", instrument.getMethodIonisation().getValue());
        List<MzScanDTO> scans = content.getScans();
        assertEquals(19, scans.size());
        MzScanDTO scan = scans.get(0);
        assertEquals(1, scan.getNumber());
        assertEquals(3, scan.getLevel());
        assertEquals(725, scan.getPeaksCount());
        assertEquals("+", scan.getPolarity());
        assertEquals("MS3", scan.getScanType());
        assertEquals(0.006, XmlUtils.tryAsSeconds(scan.getRetentionTime()));
        assertEquals(174.6, scan.getLowMz());
        assertEquals(695.88, scan.getHighMz());
        assertNull(scan.getCollisionEnergy());

        List<MzPrecursorDTO> precursors = scan.getPrecursors();
        assertEquals(1, precursors.size());
        MzPrecursorDTO precursor = precursors.get(0);
        assertEquals(654.2, precursor.getMz());
        assertEquals(32333300.0, precursor.getIntensity());
        assertNull(precursor.getCharge());

        assertNumbers("174.6", "695.88", scan.getPeaksCount(), scan.getPeakPositions());
        assertNumbers("16666.666", "16666.666", scan.getPeaksCount(), scan.getPeakIntensities());
    }

    private void assertNumbers(String firstNumber, String lastNumber, int count, String numbers)
    {
        String[] numberArray = numbers.split(",");
        assertEquals(firstNumber, numberArray[0].trim());
        assertEquals(lastNumber, numberArray[numberArray.length - 1].trim());
        assertEquals(count, numberArray.length);
    }

}
