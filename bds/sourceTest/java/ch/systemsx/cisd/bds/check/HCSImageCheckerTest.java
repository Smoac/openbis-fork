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

/**
 * Test cases for corresponding {@link HCSImageChecker} class.
 * 
 * @author Christian Ribeaud
 */
public class HCSImageCheckerTest extends AbstractCheckerTest
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

    @Test
    public final void testNotTrimmedKeyValuePairFiles() throws IOException
    {

        final File dir = new File("testdata/bds_new_lines");
        final String path = dir.getAbsolutePath();

        Assert.assertEquals(new HCSImageChecker(verbose).getHCSImageConsistencyReport(dir)
                .toString(), errorFoundNotTrimmed(path, "metadata/data_set/observable_type", "")
                + errorFoundNotTrimmed(path, "metadata/format/code", "")
                + errorFoundNotTrimmed(path, "major", "/metadata/format/version")
                + errorFoundNotTrimmed(path, "minor", "/metadata/format/version")
                + errorFoundNotTrimmed(path, "metadata/parameters/plate_geometry/rows", "")
                + errorFoundNotTrimmed(path, "metadata/parameters/plate_geometry/columns", "")
                + errorFoundNotTrimmed(path, "metadata/parameters/well_geometry/rows", "")
                + errorFoundNotTrimmed(path, "metadata/parameters/well_geometry/columns", "")
                + errorFoundNotTrimmed(path, "metadata/parameters/number_of_channels", "")
                + errorFoundNotTrimmed(path, "wavelength", "/annotations/channel1")
                + errorFoundNotTrimmed(path, "wavelength", "/annotations/channel2")
                + errorFoundNotTrimmed(path, "metadata/parameters/contains_original_data", ""));

    }
}
