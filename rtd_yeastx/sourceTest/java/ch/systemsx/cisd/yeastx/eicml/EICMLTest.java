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

import java.io.File;
import java.sql.SQLException;

import net.lemnik.eodsql.DataIterator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;

import static org.testng.AssertJUnit.*;

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

    @Test(dependsOnMethods = "testUploadEicML")
    public void testGetMsRuns()
    {
        final DataIterator<EICMSRunDTO> runs = eicmlDAO.getMsRuns();
        final EICMSRunDTO run = runs.next();
        assertFalse(runs.hasNext());
        assertEquals(22, run.getChromCount());
        runs.close(); // Shoudn't be necessary, just to be sure.
        final DataIterator<ChromatogramDTO> chromatograms = eicmlDAO.getChromatogramsForRun(run);
        int count = 0;
        while (chromatograms.hasNext())
        {
            chromatograms.next();
            ++count;
        }
        assertEquals(22, count);
        chromatograms.close(); // Shoudn't be necessary, just to be sure.
    }
}
