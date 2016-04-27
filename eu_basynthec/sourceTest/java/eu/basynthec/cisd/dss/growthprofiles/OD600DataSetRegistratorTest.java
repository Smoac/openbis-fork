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

package eu.basynthec.cisd.dss.growthprofiles;

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
public class OD600DataSetRegistratorTest extends AbstractBaSynthecDataSetRegistratorTest
{
    private static final DataSetType OD600 = new DataSetType("OD600");

    @Test
    public void testSimpleTransaction() throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties = createThreadProperties();
        createHandler(properties, false, true);
        createData("OD600-Example.xlsx");

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicOperationDetails =
                setUpDataSetRegistrationExpectations(OD600, TSV_MULTISTRAIN_EXPORT_DATA_SET_TYPE);

        handler.handle(markerFile);

        assertEquals(4, atomicOperationDetails.recordedObject().getDataSetRegistrations().size());

        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(1), "OD600");
        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(2), "OD600");
        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(3), "OD600");

        NewExternalData dataSet =
                atomicOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(OD600, dataSet.getDataSetType());

        HashMap<String, NewProperty> propertyMap =
                getDataSetPropertiesMap(dataSet.getDataSetProperties());
        NewProperty strainProperty = propertyMap.get(STRAIN_NAMES_PROP);

        assertNotNull(strainProperty);
        assert null != strainProperty;

        NewExternalData tsvSplitDataSet =
                atomicOperationDetails.recordedObject().getDataSetRegistrations().get(3);
        String location = tsvSplitDataSet.getLocation() + "/tsv";
        File tsvSplitFolder = new File(workingDirectory, "/1/" + location);
        String[] contents = tsvSplitFolder.list();
        Arrays.sort(contents);
        String[] expectedContents =
        { "OD600-Example.xlsx_JJS-MGP001.tsv", "OD600-Example.xlsx_JJS-MGP020.tsv",
                "OD600-Example.xlsx_JJS-MGP100.tsv",
                "OD600-Example.xlsx_JJS-MGP999.tsv", "OD600-Example.xlsx_MS.tsv",
                "OD600-Example.xlsx_WT 168 trp+.tsv" };
        assertEquals(Arrays.asList(expectedContents), Arrays.asList(contents));
        File tsvSplitFile = new File(tsvSplitFolder, "OD600-Example.xlsx_JJS-MGP001.tsv");
        checkTsvSplitContent(tsvSplitFile);

        context.assertIsSatisfied();
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/growth-profiles/";
    }

    private void checkTsvSplitContent(File tsvFile) throws IOException
    {
        String content = FileUtils.readFileToString(tsvFile);
        assertEquals(
                "RunNumber\tHumanReadable\t-19020.0\t-17220.0\t-15360.0\t-13620.0\t-11820.0\t-10020.0\t-8220.0\t-7020.0\t-4920.0\t-2820.0\t-1020.0\t-120.0\t720.0\t1500.0\t3660.0\t5460.0\t6060.0\t7200.0\t9000.0\n"
                        + "0\tOD600\t0.05\t0.064\t0.077\t0.089\t0.107\t0.127\t0.155\t0.176\t0.24\t0.33\t0.43\t0.49\t0.58\t0.66\t0.975\t1.42\t1.49\t2.09\t3.22\n"
                        + "1\tOD600\t0.05\t0.064\t0.077\t0.089\t0.107\t0.127\t0.155\t0.176\t0.24\t0.33\t0.43\t0.49\t0.58\t0.66\t0.975\t1.42\t1.49\t2.09\t3.22",
                content);
    }
}
