/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.phosphonetx.server.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=ProteinTableReport.class)
public class ProteinTableReportTest extends AbstractFileSystemTestCase
{
    private static final String EXAMPLE_PROTEINS =
            "# Protein abundances computed from file 'result.consensusXML'\n"
                    + "# Parameters (relevant only): top=3, average=median, include_all, consensus:normalize\n"
                    + "# Files/samples associated with abundance values below:"
                    + " 0: '/cluster/weisserh_B1012_053.featureXML',"
                    + " 1: '/cluster/weisserh_B1012_057.featureXML'\n"
                    + "\"protein\",\"n_proteins\",\"protein_score\",\"n_peptides\",\"abundance_0\",\"abundance_1\"\n"
                    + "\"13621327\",1,1,5,7846763.86498425,8159570.13467505\n"
                    + "\"13621329\",1,1,6,2976845.93289974,3670078.89677526\n"
                    + "\"13621337\",1,1,14,18114384.6420854,14119249.6148974\n";

    @BeforeMethod
    public void beforeMethod()
    {
        File originalFolder = new File(new File(workingDirectory, "1"), "original");
        originalFolder.mkdirs();
        File dataSetFolder = new File(originalFolder, "data-set-folder");
        dataSetFolder.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder, ProteinTableReport.PROTEIN_FILE_NAME), EXAMPLE_PROTEINS);
    }
    
    @Test
    public void test()
    {
        ProteinTableReport report = new ProteinTableReport(new Properties(), workingDirectory);
        DataSetProcessingContext context = new DataSetProcessingContext(Collections.<String, String>emptyMap(), null, "a@bc.de");
        DatasetDescriptionBuilder ds1 = new DatasetDescriptionBuilder("ds1").location("1");
        List<DatasetDescription> dataSets = Arrays.asList(ds1.getDatasetDescription());
        report.process(dataSets, context);
    }
}
