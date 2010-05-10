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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.io.File;
import java.sql.SQLException;

import net.lemnik.eodsql.DataIterator;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.generic.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.db.generic.GenericUtils;

/**
 * A test for the fiaML tables.
 * 
 * @author Bernd Rinn
 */
public class FIAMLTest extends AbstractDBTest
{
    private IFIAMSRunDAO fiamsDAO;

    @BeforeMethod(alwaysRun = true)
    public void setDAO() throws SQLException
    {
        fiamsDAO = DBUtils.getQuery(datasource, IFIAMSRunDAO.class);
    }

    @AfterMethod(alwaysRun = true)
    public void close()
    {
        fiamsDAO.close();
    }

    @Test
    public void testUploadFiaML() throws SQLException
    {
        FIAML2Database fiaML2Database = new FIAML2Database(datasource);
        fiaML2Database.upload(new File("resource/examples/example.fiaML"), new DMDataSetDTO(
                "data set perm id fiaml", "sample perm id", "sample name", "experiment perm id",
                "experiment name"));
        fiaML2Database.commit();
    }

    @Test(dependsOnMethods = "testUploadFiaML")
    public void testGetMsRuns()
    {
        final DataIterator<FIAMSRunDTO> runs = fiamsDAO.getMsRuns();
        final FIAMSRunDTO run = runs.next();
        assertFalse(runs.hasNext());
        assertEquals(686, run.getProfileCount());
        final DataIterator<ProfileDTO> profiles = fiamsDAO.getProfilesForRun(run);
        assertFalse(profiles.isClosed());
        int count = 0;
        while (profiles.hasNext())
        {
            final ProfileDTO prof = profiles.next();
            assertFalse(prof.getId() == 0);
            assertEquals(run.getId(), prof.getFiaMsRunId());
            ++count;
        }
        assertEquals(686, count);
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
        fiamsDAO.close();
    }

    @Test(dependsOnMethods = "testGetMsRuns")
    public void testUploadFiaMLFileTwiceWithOneDAO() throws SQLException
    {
        FIAML2Database fiaML2Database = new FIAML2Database(datasource);
        fiaML2Database.upload(new File("resource/examples/example.fiaML"), new DMDataSetDTO(
                "data set perm id2", "sample perm id2", "sample name", "experiment perm id2",
                "experiment name"));
        fiaML2Database.commit();
        fiaML2Database.upload(new File("resource/examples/example.fiaML"), new DMDataSetDTO(
                "data set perm id3", "sample perm id", "sample name", "experiment perm id",
                "experiment name"));
        fiaML2Database.commit();
    }

    @Test(dependsOnMethods = "testUploadFiaMLFileTwiceWithOneDAO", expectedExceptions = DataIntegrityViolationException.class)
    public void testCreateSameDataSetTwiceFailing() throws SQLException
    {
        try
        {
            GenericUtils.createDataSet(fiamsDAO, new DMDataSetDTO("data set perm id 4",
                    "sample perm id4", "sample name", "experiment perm id", "experiment name"));
            // This will fail with a DataIntegrityViolationException.
            GenericUtils.createDataSet(fiamsDAO, new DMDataSetDTO("data set perm id 4",
                    "sample perm id4", "sample name", "experiment perm id", "experiment name"));
            // There is transaction commit inside createDataSet method before DS is added to DB.
            // DS created in first invocation will be be commited in second invocation.
        } catch (RuntimeException ex)
        {
            // This isn't actually necessary for PostgreSQL, but e.g. Oracle does commit a
            // transaction when no explicit rollback is given.
            fiamsDAO.rollback();
            throw ex;
        }
    }

    @Test(dependsOnMethods = "testCreateSameDataSetTwiceFailing")
    public void testDataSets() throws SQLException
    {
        assertEquals(2, fiamsDAO.listDataSetsForSample("sample perm id").length);
        assertEquals(1, fiamsDAO.listDataSetsForSample("sample perm id2").length);
        assertEquals(1, fiamsDAO.listDataSetsForSample("sample perm id4").length);
    }

}
