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
import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.ArrayUtils;
import org.testng.AssertJUnit;
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

    private static final String DATA_SET_PERM_ID = "data set perm id eicml";

    private IEICMSRunDAO eicmlDAO;

    @BeforeMethod(alwaysRun = true)
    public void setDAO() throws SQLException
    {
        eicmlDAO = QueryTool.getQuery(datasource, IEICMSRunDAO.class);
    }

    @Test
    public void testUploadEicML() throws SQLException
    {
        EICML2Database db = new EICML2Database(datasource);
        // TODO 2009-11-11, Tomasz Pylak: write rollback test
        db.upload(new File("resource/examples/example.eicML"), createDataset());
        db.commit();
    }

    private DMDataSetDTO createDataset()
    {
        return new DMDataSetDTO(DATA_SET_PERM_ID, "sample perm id eicml", "sample name eicml",
                "experiment perm id eicml", "experiment name eicml");
    }

    @Test(dependsOnMethods = "testUploadEicML")
    public void testGetMSRunByDatasetPermId() throws ParseException
    {
        EICMSRunDTO run = eicmlDAO.getMSRunByDatasetPermId(DATA_SET_PERM_ID);
        AssertJUnit.assertNotNull(run);
        assertRunCorrect(run);
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
                assertEquals("1600: {385.234, 385.259, 385.284...425.159, 425.184, 425.209}",
                        toString(chrom.getRunTimes()));
                assertEquals("1600: {752.0, 766.0, 762.0...1968.0, 1861.0, 1796.0}", toString(chrom
                        .getIntensities()));
                break;
            case 1:
                assertEquals(184.0f, chrom.getQ1Mz());
                assertEquals(184.9f, chrom.getQ3LowMz());
                assertEquals(184.5f, chrom.getQ3HighMz());
                assertEquals("EIC 184", chrom.getLabel());
                assertEquals('-', chrom.getPolarity());
                assertEquals("1600: {614.059, 614.084, 614.109...653.984, 654.009, 654.034}",
                        toString(chrom.getRunTimes()));
                assertEquals("1600: {598.0, 588.0, 615.0...564.0, 629.0, 599.0}", toString(chrom
                        .getIntensities()));
                break;
            case 2:
                assertEquals(186f, chrom.getQ1Mz());
                assertEquals(186f, chrom.getQ3LowMz());
                assertEquals(186f, chrom.getQ3HighMz());
                assertEquals("EIC 186", chrom.getLabel());
                assertEquals('?', chrom.getPolarity());
                assertEquals("1744: {427.734, 427.759, 427.784...471.259, 471.284, 471.309}",
                        toString(chrom.getRunTimes()));
                assertEquals("1744: {719.0, 659.0, 722.0...587.0, 582.0, 544.0}", toString(chrom
                        .getIntensities()));
                break;
            case 3:
                assertEquals("1600: {639.909, 639.934, 639.959...679.834, 679.859, 679.884}",
                        toString(chrom.getRunTimes()));
                assertEquals("1600: {781.0, 777.0, 758.0...1448.0, 1522.0, 1556.0}", toString(chrom
                        .getIntensities()));
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
            return a.length + ": {" + a[0] + ", " + a[1] + ", " + a[2] + "..." + a[a.length - 3]
                    + ", " + a[a.length - 2] + ", " + a[a.length - 1] + "}";
        }
    }

    @Test(dependsOnMethods = "testUploadEicML")
    public void testGetMsRuns() throws ParseException
    {
        final DataIterator<EICMSRunDTO> runs = eicmlDAO.getMsRuns();
        final EICMSRunDTO run = runs.next();
        assertFalse(runs.hasNext());
        assertEquals(4, run.getChromCount());
        assertRunCorrect(run);
        final DataIterator<ChromatogramDTO> chromatograms = eicmlDAO.getChromatogramsForRun(run);
        int count = 0;
        while (chromatograms.hasNext())
        {
            final ChromatogramDTO chrom = chromatograms.next();
            checkChromatograms(count, chrom);
            ++count;
        }
        assertEquals(run.getChromCount(), count);
    }

    private void assertRunCorrect(final EICMSRunDTO run) throws ParseException
    {
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
    }
}
