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

package eu.basynthec.cisd.dss.transcriptomics;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

import eu.basynthec.cisd.dss.AbstractBaSynthecDataSetRegistratorTest;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TranscriptomicsDataSetRegistratorTest extends AbstractBaSynthecDataSetRegistratorTest
{
    private static final DataSetType TRANSCRIPTOMICS = new DataSetType("TRANSCRIPTOMICS");

    @Test
    public void testSimpleTransaction() throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties = createThreadProperties();
        createHandler(properties, false, true);
        createData("Transcriptomics-Example.xlsx");

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicOperationDetails =
                setUpDataSetRegistrationExpectations(TRANSCRIPTOMICS,
                        TSV_MULTISTRAIN_EXPORT_DATA_SET_TYPE);

        handler.handle(markerFile);

        assertEquals(4, atomicOperationDetails.recordedObject().getDataSetRegistrations().size());

        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(1), "TRANSCRIPTOMICS");
        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(2), "TRANSCRIPTOMICS");
        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(3), "TRANSCRIPTOMICS");

        NewExternalData dataSet =
                atomicOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(TRANSCRIPTOMICS, dataSet.getDataSetType());

        HashMap<String, NewProperty> propertyMap =
                getDataSetPropertiesMap(dataSet.getDataSetProperties());
        NewProperty strainProperty = propertyMap.get(STRAIN_NAMES_PROP);

        assertNotNull(strainProperty);
        assert null != strainProperty;
        assertEquals("JJS-MGP253,JJS-MGP776", strainProperty.getValue());

        NewExternalData tsvDataSet =
                atomicOperationDetails.recordedObject().getDataSetRegistrations().get(2);
        String location = tsvDataSet.getLocation() + "/tsv-multi/";
        File tsvFile =
                new File(new File(workingDirectory, "/1/" + location),
                        "Transcriptomics-Example.xlsx.tsv");
        checkTsvContent(tsvFile);

        NewExternalData tsvSplitDataSet =
                atomicOperationDetails.recordedObject().getDataSetRegistrations().get(3);
        location = tsvSplitDataSet.getLocation() + "/tsv/";
        File tsvSplitFolder = new File(workingDirectory, "/1/" + location);
        String[] contents = tsvSplitFolder.list();
        Arrays.sort(contents);
        String[] expectedContents =
        { "Transcriptomics-Example.xlsx_JJS-MGP253.tsv",
                "Transcriptomics-Example.xlsx_JJS-MGP776.tsv" };
        assertEquals(Arrays.asList(expectedContents), Arrays.asList(contents));
        File tsvSplitFile = new File(tsvSplitFolder, "Transcriptomics-Example.xlsx_JJS-MGP253.tsv");
        checkSplitTsvContent(tsvSplitFile);
        context.assertIsSatisfied();
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/transcriptomics/";
    }

    private void checkTsvContent(File tsvFile) throws IOException
    {
        String content = FileUtils.readFileToString(tsvFile);
        assertEquals("Locustag\tJJS-MGP253-1 66687802\tJJS-MGP776-2 66730002\n"
                + "BSU00010\t13.7953\t13.5517\n" + "BSU00020\t13.5907\t13.3277\n"
                + "BSU00030\t13.8489\t13.6306\n" + "BSU00040\t14.3564\t14.1073\n"
                + "BSU00050\t14.5239\t14.1992\n" + "BSU00060\t14.3293\t13.933\n"
                + "BSU00070\t14.481\t14.1348\n" + "BSU00090\t15.474\t15.2813\n"
                + "BSU00100\t14.4332\t14.1945\n" + "BSU00110\t15.2669\t14.9582\n"
                + "BSU00120\t15.3344\t15.112\n" + "BSU_misc_RNA_1\t15.4497\t15.2485\n"
                + "BSU00130\t13.6604\t13.5385\n" + "BSU00180\t9.8208\t9.971\n"
                + "BSU_misc_RNA_2\t13.6614\t14.0933\n" + "BSU00190\t13.464\t13.1213\n"
                + "BSU00200\t14.6102\t14.4169\n" + "BSU00210\t13.5285\t13.2043\n"
                + "BSU00220\t13.1007\t12.8862\n" + "BSU00230\t11.8547\t11.6761\n"
                + "BSU00240\t10.8623\t11.1397\n" + "BSU00250\t11.6694\t11.429\n"
                + "BSU00260\t11.7669\t11.4658\n" + "BSU00270\t12.2675\t11.8745\n"
                + "BSU00280\t12.5574\t12.1608\n", content);
    }

    private void checkSplitTsvContent(File tsvFile) throws IOException
    {
        String content = FileUtils.readFileToString(tsvFile);
        assertEquals("Locustag\t1 66687802\n" + "BSU00010\t13.7953\n" + "BSU00020\t13.5907\n"
                + "BSU00030\t13.8489\n" + "BSU00040\t14.3564\n" + "BSU00050\t14.5239\n"
                + "BSU00060\t14.3293\n" + "BSU00070\t14.481\n" + "BSU00090\t15.474\n"
                + "BSU00100\t14.4332\n" + "BSU00110\t15.2669\n" + "BSU00120\t15.3344\n"
                + "BSU_misc_RNA_1\t15.4497\n" + "BSU00130\t13.6604\n" + "BSU00180\t9.8208\n"
                + "BSU_misc_RNA_2\t13.6614\n" + "BSU00190\t13.464\n" + "BSU00200\t14.6102\n"
                + "BSU00210\t13.5285\n" + "BSU00220\t13.1007\n" + "BSU00230\t11.8547\n"
                + "BSU00240\t10.8623\n" + "BSU00250\t11.6694\n" + "BSU00260\t11.7669\n"
                + "BSU00270\t12.2675\n" + "BSU00280\t12.5574", content);
    }
}
