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

import ch.systemsx.cisd.yeastx.mzxml.dto.MzInstrumentDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzPeaksDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzPrecursorDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzRunDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzScanDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzXmlDTO;
import ch.systemsx.cisd.yeastx.utils.JaxbXmlParser;
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
        MzRunDTO content = parse("resource/examples/example1.mzXML");
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
        assertEquals(2, scan.getLevel());
        assertEquals(5408, scan.getPeaksCount());
        assertEquals("+", scan.getPolarity());
        assertEquals("EPI", scan.getScanType());
        assertEquals(0.005, XmlUtils.asSeconds(scan.getRetentionTime()));
        assertEquals(150.12, scan.getLowMz());
        assertEquals(699.78, scan.getHighMz());
        assertNull(scan.getCollisionEnergy());

        List<MzPrecursorDTO> precursors = scan.getPrecursors();
        assertEquals(1, precursors.size());
        MzPrecursorDTO precursor = precursors.get(0);
        assertEquals(654.2, precursor.getMz());
        assertEquals(59916700.0, precursor.getIntensity());
        assertNull(precursor.getCharge());

        MzPeaksDTO peaks = scan.getPeaks();
        float[] peakFloats = XmlUtils.asFloats(peaks.getPeaks());
        assertEquals(10816, peakFloats.length);

        assertEquals(150.12F, peakFloats[0]);
        assertEquals(16666.666F, peakFloats[1]);
        assertEquals(699.78F, peakFloats[10814]);
        assertEquals(16666.666F, peakFloats[10815]);
    }

    @Test
    public void testParseExamplesFiles()
    {
        assertNotNull(parse("resource/examples/example2.mzXML"));
        assertNotNull(parse("resource/examples/example3.mzXML"));
        assertNotNull(parse("resource/examples/example4.mzXML"));
    }

}
