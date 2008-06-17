/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.check;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HCSImageCheckerTest
{
    private final boolean verbose = false;

    @Test
    public final void testCorrectContainer() throws IOException
    {

        Assert.assertEquals(new HCSImageChecker(verbose).getHCSImageConsistencyReport(
                new File("testdata/bds_hcs")).noProblemsFound(), true);

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testContainerIsFile()
    {
        new HCSImageChecker(verbose).getHCSImageConsistencyReport(new File("testdata/bds_file"));
    }

    @Test
    public final void testMissingFiles() throws IOException
    {

        Assert.assertEquals(new HCSImageChecker(verbose).getHCSImageConsistencyReport(
                new File("testdata/bds_hcs_missing_files")).numberOfProblems(), 13);

    }

    @Test
    public final void testMissingDirectories() throws IOException
    {

        Assert.assertEquals(new HCSImageChecker(verbose).getHCSImageConsistencyReport(
                new File("testdata/bds_empty")).numberOfProblems(), 14);

    }

    @Test
    public final void testInconsistentData() throws IOException
    {

        Assert.assertEquals(new HCSImageChecker(verbose).getHCSImageConsistencyReport(
                new File("testdata/bds_hcs_inconsistent")).numberOfProblems(), 78);

    }

    @Test
    public final void testProblemWithMapping() throws IOException
    {

        Assert.assertEquals(new HCSImageChecker(verbose).getHCSImageConsistencyReport(
                new File("testdata/bds_hcs_mapping_error")).numberOfProblems(), 2);

    }
}
