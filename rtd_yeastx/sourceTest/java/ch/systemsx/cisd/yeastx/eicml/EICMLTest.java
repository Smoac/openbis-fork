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

package ch.systemsx.cisd.yeastx.eicml;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;

/**
 * A test for thhe eicML tables.
 * 
 * @author Bernd Rinn
 */
public class EICMLTest extends AbstractDBTest
{

    private IEICMSRunDAO eicmlDAO;

    @BeforeMethod(alwaysRun = true)
    public void setDAO() throws SQLException
    {
        eicmlDAO = getEICMLDAO();
    }

    @Test
    public void testUploadEicML() throws SQLException
    {
        EICML2Database.uploadEicMLFile(conn, new File("resource/examples/example.eicML"),
                new DMDataSetDTO("data set perm id", "sample perm id", "sample name",
                        "experiment perm id", "experiment name"));
    }

    private void checkChromatograms(int count, ChromatogramDTO chrom)
    {
        assertEquals(count + 1, chrom.getId());
        switch (count)
        {
            case 0:
                assertEquals(158.0f, chrom.getQ1Mz());
                assertEquals(158.1f, chrom.getQ3LowMz());
                assertEquals(158.2f, chrom.getQ3HighMz());
                assertEquals("EIC 158", chrom.getLabel());
                assertEquals('+', chrom.getPolarity());
                assertEquals("1600: {385.234, 385.259, 385.284...425.159, 425.184, 425.209}", toString(chrom.getRunTimes()));
                assertEquals("1600: {752.0, 766.0, 762.0...1968.0, 1861.0, 1796.0}", toString(chrom.getIntensities()));
                break;
            case 1:
                assertEquals(184.0f, chrom.getQ1Mz());
                assertEquals(184.9f, chrom.getQ3LowMz());
                assertEquals(184.5f, chrom.getQ3HighMz());
                assertEquals("EIC 184", chrom.getLabel());
                assertEquals('-', chrom.getPolarity());
                assertEquals("1600: {614.059, 614.084, 614.109...653.984, 654.009, 654.034}", toString(chrom.getRunTimes()));
                assertEquals("1600: {598.0, 588.0, 615.0...564.0, 629.0, 599.0}", toString(chrom.getIntensities()));
                break;
            case 2:
                assertEquals(186f, chrom.getQ1Mz());
                assertEquals(186f, chrom.getQ3LowMz());
                assertEquals(186f, chrom.getQ3HighMz());
                assertEquals("EIC 186", chrom.getLabel());
                assertEquals('?', chrom.getPolarity());
                assertEquals("1744: {427.734, 427.759, 427.784...471.259, 471.284, 471.309}", toString(chrom.getRunTimes()));
                assertEquals("1744: {719.0, 659.0, 722.0...587.0, 582.0, 544.0}", toString(chrom.getIntensities()));
                break;
            case 3:
                assertEquals("1600: {639.909, 639.934, 639.959...679.834, 679.859, 679.884}", toString(chrom.getRunTimes()));
                assertEquals("1600: {781.0, 777.0, 758.0...1448.0, 1522.0, 1556.0}", toString(chrom.getIntensities()));
                break;
            case 4:
                assertEquals("1600: {392.709, 392.734, 392.759...432.634, 432.659, 432.684}", toString(chrom.getRunTimes()));
                assertEquals("1600: {594.0, 583.0, 592.0...603.0, 606.0, 629.0}", toString(chrom.getIntensities()));
                break;
            case 5:
                assertEquals("1600: {465.959, 465.984, 466.009...505.884, 505.909, 505.934}", toString(chrom.getRunTimes()));
                assertEquals("1600: {508.0, 499.0, 561.0...554.0, 581.0, 548.0}", toString(chrom.getIntensities()));
                break;
            case 6:
                assertEquals("1600: {612.459, 612.484, 612.509...652.384, 652.409, 652.434}", toString(chrom.getRunTimes()));
                assertEquals("1600: {1000.0, 984.0, 960.0...644.0, 622.0, 671.0}", toString(chrom.getIntensities()));
                break;
            case 7:
                assertEquals("2040: {441.309, 441.334, 441.359...492.234, 492.259, 492.284}", toString(chrom.getRunTimes()));
                assertEquals("2040: {541.0, 537.0, 556.0...567.0, 600.0, 533.0}", toString(chrom.getIntensities()));
                break;
            case 8:
                assertEquals("3200: {469.384, 469.409, 469.434...608.784, 608.809, 608.834}", toString(chrom.getRunTimes()));
                assertEquals("3200: {564.0, 582.0, 570.0...574.0, 574.0, 566.0}", toString(chrom.getIntensities()));
                break;
            case 9:
                assertEquals("1600: {534.559, 534.584, 534.609...574.484, 574.509, 574.534}", toString(chrom.getRunTimes()));
                assertEquals("1600: {585.0, 603.0, 618.0...595.0, 592.0, 600.0}", toString(chrom.getIntensities()));
                break;
            case 10:
                assertEquals("1600: {461.134, 461.159, 461.184...501.059, 501.084, 501.109}", toString(chrom.getRunTimes()));
                assertEquals("1600: {567.0, 512.0, 513.0...590.0, 583.0, 579.0}", toString(chrom.getIntensities()));
                break;
            case 11:
                assertEquals("1600: {528.509, 528.534, 528.559...568.434, 568.459, 568.484}", toString(chrom.getRunTimes()));
                assertEquals("1600: {541.0, 553.0, 534.0...562.0, 568.0, 565.0}", toString(chrom.getIntensities()));
                break;
            case 12:
                assertEquals("1600: {492.909, 492.934, 492.959...532.834, 532.859, 532.884}", toString(chrom.getRunTimes()));
                assertEquals("1600: {506.0, 495.0, 504.0...528.0, 543.0, 498.0}", toString(chrom.getIntensities()));
                break;
            case 13:
                assertEquals("1600: {565.109, 565.134, 565.159...605.034, 605.059, 605.084}", toString(chrom.getRunTimes()));
                assertEquals("1600: {544.0, 554.0, 557.0...699.0, 716.0, 706.0}", toString(chrom.getIntensities()));
                break;
            case 14:
                assertEquals("1600: {538.834, 538.859, 538.884...578.759, 578.784, 578.809}", toString(chrom.getRunTimes()));
                assertEquals("1600: {754.0, 739.0, 741.0...552.0, 539.0, 560.0}", toString(chrom.getIntensities()));
                break;
            case 15:
                assertEquals("1600: {718.034, 718.059, 718.084...757.959, 757.984, 758.009}", toString(chrom.getRunTimes()));
                assertEquals("1600: {544.0, 521.0, 561.0...540.0, 552.0, 537.0}", toString(chrom.getIntensities()));
                break;
            case 16:
                assertEquals("2382: {544.384, 544.409, 544.434...603.859, 603.884, 603.909}", toString(chrom.getRunTimes()));
                assertEquals("2382: {535.0, 557.0, 553.0...557.0, 538.0, 550.0}", toString(chrom.getIntensities()));
                break;
            case 17:
                assertEquals("1600: {621.609, 621.634, 621.659...661.534, 661.559, 661.584}", toString(chrom.getRunTimes()));
                assertEquals("1600: {541.0, 569.0, 557.0...560.0, 562.0, 558.0}", toString(chrom.getIntensities()));
                break;
            case 18:
                assertEquals("1600: {581.284, 581.309, 581.334...621.209, 621.234, 621.259}", toString(chrom.getRunTimes()));
                assertEquals("1600: {525.0, 535.0, 541.0...523.0, 577.0, 547.0}", toString(chrom.getIntensities()));
                break;
            case 19:
                assertEquals("1600: {692.584, 692.609, 692.634...732.509, 732.534, 732.559}", toString(chrom.getRunTimes()));
                assertEquals("1600: {543.0, 531.0, 549.0...529.0, 543.0, 526.0}", toString(chrom.getIntensities()));
                break;
            case 20:
                assertEquals("1741: {697.409, 697.434, 697.459...740.859, 740.884, 740.909}", toString(chrom.getRunTimes()));
                assertEquals("1741: {546.0, 563.0, 556.0...542.0, 533.0, 546.0}", toString(chrom.getIntensities()));
                break;
            case 21:
                assertEquals("1600: {703.609, 703.634, 703.659...743.534, 743.559, 743.584}", toString(chrom.getRunTimes()));
                assertEquals("1600: {528.0, 518.0, 525.0...502.0, 521.0, 514.0}", toString(chrom.getIntensities()));
                break;
            default:
                break;
        }
    }

    private String toString(float[] a)
    {
        if (a.length < 6)
        {
            return ArrayUtils.toString(a);
        } else
        {
            return a.length + ": {" + a[0] + ", " + a[1] + ", " + a[2] + "..." + a[a.length - 3] + ", "
                    + a[a.length - 2] + ", " + a[a.length - 1] + "}";
        }
    }

    @Test(dependsOnMethods = "testUploadEicML")
    public void testGetMsRuns() throws ParseException
    {
        final DataIterator<EICMSRunDTO> runs = eicmlDAO.getMsRuns();
        final EICMSRunDTO run = runs.next();
        assertFalse(runs.hasNext());
        assertEquals(22, run.getChromCount());
        assertEquals(EICMLParser.getDateFormat().parse("16-Jun-2009 11:58:14"), run
                .getAcquisitionDate());
        assertEquals(370.109f, run.getStartTime());
        assertEquals(799.334f, run.getEndTime());
        assertEquals("TBD", run.getMethodSeparation());
        assertEquals("EI", run.getMethodIonisation());
        assertEquals("GC-TOF", run.getInstrumentType());
        assertEquals("LECO", run.getInstrumentManufacturer());
        assertEquals("Pegasus 3D", run.getInstrumentModel());
        assertEquals("T:\\users\\jenny\\toF for Nicola\\", run.getRawDataFilePath());
        assertEquals("P1_-4f_-Glc_all_2ul_1.cdf", run.getRawDataFileName());
        assertEquals(17L, run.getMsRunId().longValue());
        assertEquals(32L, run.getSetId().longValue());
        assertEquals("???", run.getOperator());
        runs.close(); // Shoudn't be necessary, just to be sure.
        final DataIterator<ChromatogramDTO> chromatograms = eicmlDAO.getChromatogramsForRun(run);
        int count = 0;
        while (chromatograms.hasNext())
        {
            final ChromatogramDTO chrom = chromatograms.next();
            checkChromatograms(count, chrom);
            ++count;
        }
        assertEquals(22, count);
        chromatograms.close(); // Shoudn't be necessary, just to be sure.
    }
}
