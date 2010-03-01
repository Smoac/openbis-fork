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

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link StructureChecker} class.
 * 
 * @author Christian Ribeaud
 */
public class StructureCheckerTest extends AbstractCheckerTest
{
    private final boolean verbose = false;

    @Test
    public final void testCorrectContainer() throws IOException
    {

        final ProblemReport structureConsistencyReport =
                new StructureChecker(verbose).getStructureConsistencyReport(new File(
                        "testdata/bds_ok"));
        assertEquals(structureConsistencyReport.toString(), "");
        assertEquals(structureConsistencyReport.noProblemsFound(), true);

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testContainerIsFile()
    {
        new StructureChecker(verbose).getStructureConsistencyReport(new File("testdata/bds_file"));
    }

    @Test
    public final void testEmptyContainer()
    {

        assertEquals(new StructureChecker(verbose).getStructureConsistencyReport(
                new File("testdata/bds_empty")).numberOfProblems(), 4);
    }

    @Test
    public final void testEmptyDirectories()
    {

        assertEquals(new StructureChecker(verbose).getStructureConsistencyReport(
                new File("testdata/bds_empty_dirs")).numberOfProblems(), 13);
    }

    @Test
    public final void testMissingFiles()
    {
        final ProblemReport report =
                new StructureChecker(verbose).getStructureConsistencyReport(new File(
                        "testdata/bds_missing_files"));
        assertEquals(report.numberOfProblems(), 23);
    }

    @Test
    public final void testEmptyFiles()
    {
        assertEquals(new StructureChecker(verbose).getStructureConsistencyReport(
                new File("testdata/bds_empty_files")).numberOfProblems(), 27);
    }

    @Test
    public final void testWrongFileFormat()
    {
        final ProblemReport structureConsistencyReport =
                new StructureChecker(verbose).getStructureConsistencyReport(new File(
                        "testdata/bds_wrong_values"));
        assertEquals(structureConsistencyReport.numberOfProblems(), 11);
    }

    @Test
    public final void testNewLinesPresentInKeyValuePairFiles()
    {
        final File dir = new File("testdata/bds_new_lines");
        final String path = dir.getAbsolutePath();

        assertEquals(errorFoundNotTrimmed(path, "major", "/version")
                + errorFoundNotTrimmed(path, "minor", "/version")
                + errorFoundNotTrimmed(path, "code", "/metadata/data_set")
                + errorFoundNotTrimmed(path, "production_timestamp", "/metadata/data_set")
                + errorFoundNotTrimmed(path, "producer_code", "/metadata/data_set")
                + errorFoundNotTrimmed(path, "observable_type", "/metadata/data_set")
                + errorFoundNotTrimmed(path, "is_measured", "/metadata/data_set")
                + errorFoundNotTrimmed(path, "is_complete", "/metadata/data_set")
                + errorFoundNotTrimmed(path, "major", "/metadata/format/version")
                + errorFoundNotTrimmed(path, "minor", "/metadata/format/version")
                + errorFoundNotTrimmed(path, "code", "/metadata/format")
                + errorFoundNotTrimmed(path, "instance_code", "/metadata/experiment_identifier")
                + errorFoundNotTrimmed(path, "instance_uuid", "/metadata/experiment_identifier")
                + errorFoundNotTrimmed(path, "space_code", "/metadata/experiment_identifier")
                + errorFoundNotTrimmed(path, "project_code", "/metadata/experiment_identifier")
                + errorFoundNotTrimmed(path, "experiment_code", "/metadata/experiment_identifier")
                + errorFoundNotTrimmed(path, "experiment_registration_timestamp", "/metadata")
                + errorFoundNotTrimmed(path, "first_name", "/metadata/experiment_registrator")
                + errorFoundNotTrimmed(path, "last_name", "/metadata/experiment_registrator")
                + errorFoundNotTrimmed(path, "email", "/metadata/experiment_registrator")
                + errorFoundNotTrimmed(path, "space_code", "/metadata/sample")
                + errorFoundNotTrimmed(path, "instance_code", "/metadata/sample")
                + errorFoundNotTrimmed(path, "instance_uuid", "/metadata/sample")
                + errorFoundNotTrimmed(path, "type_description", "/metadata/sample")
                + errorFoundNotTrimmed(path, "type_code", "/metadata/sample")
                + errorFoundNotTrimmed(path, "code", "/metadata/sample"), new StructureChecker(
                verbose).getStructureConsistencyReport(dir).toString());
    }
}
