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

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

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
    private static final DataSetType DATA_SET_TYPE = new DataSetType("TRANSCRIPTOMICS");

    @Test
    public void testSimpleTransaction() throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties = createThreadProperties();
        createHandler(properties, false, true);
        createData("Transcriptomics-Example.xlsx");

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                setUpDataSetRegistrationExpectations(DATA_SET_TYPE);

        handler.handle(markerFile);

        assertEquals(1, atomicatOperationDetails.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        HashMap<String, NewProperty> propertyMap =
                getDataSetPropertiesMap(dataSet.getDataSetProperties());
        NewProperty strainProperty = propertyMap.get(STRAIN_NAMES_PROP);

        assertNotNull(strainProperty);
        assert null != strainProperty;
        assertEquals("MGP253,MGP776", strainProperty.getValue());
        context.assertIsSatisfied();
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/transcriptomics/";
    }
}
