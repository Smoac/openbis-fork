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

package ch.systemsx.cisd.yeastx.fiaml;

import java.io.File;
import java.sql.SQLException;

import net.lemnik.eodsql.DataIterator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;

import static org.testng.AssertJUnit.*;

/**
 * A test for thhe fiaML tables.
 * 
 * @author Bernd Rinn
 */
public class FIAMLTest extends AbstractDBTest
{
    private IFIAMSRunDAO fiamsDAO;

    @BeforeMethod(alwaysRun = true)
    public void setDAO() throws SQLException
    {
        fiamsDAO = getFIAMLDAO();
    }

    @Test(groups = "broken")
    public void testUploadFiaML() throws SQLException
    {
        FIAML2Database.uploadFiaMLFile(conn, new File("resource/examples/example.fiaML"),
                new DMDataSetDTO("data set perm id", "sample perm id", "sample name",
                        "experiment perm id", "experiment name"));
    }

    @Test(groups = "broken", dependsOnMethods = "testUploadFiaML")
    public void testGetMsRuns()
    {
        final DataIterator<FIAMSRunDTO> runs = fiamsDAO.getMsRuns();
        final FIAMSRunDTO run = runs.next();
        assertFalse(runs.hasNext());
        assertEquals(686, run.getProfileCount());
        runs.close(); // Shoudn't be necessary, just to be sure.
        final DataIterator<ProfileDTO> profiles = fiamsDAO.getProfilesForRun(run);
        int count = 0;
        while (profiles.hasNext())
        {
            final ProfileDTO prof = profiles.next();
            assertFalse(prof.getId() == 0);
            assertEquals(run.getId(), prof.getFiaMsRunId());
            ++count;
        }
        assertEquals(686, count);
        profiles.close(); // Shoudn't be necessary, just to be sure.
        final DataIterator<CentroidDTO> centroids = fiamsDAO.getCentroidsForRun(run);
        count = 0;
        while (centroids.hasNext())
        {
            final CentroidDTO cent = centroids.next();
            assertFalse(cent.getId() == 0);
            assertEquals(run.getId(), cent.getFiaMsRunId());
            ++count;
        }
        assertEquals(556, count);
        centroids.close(); // Shoudn't be necessary, just to be sure.
    }

}
