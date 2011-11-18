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

package eu.basynthec.cisd.dss.metabolomics;

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
public class MetabolomicsDataSetRegistratorTest extends AbstractBaSynthecDataSetRegistratorTest
{
    private static final DataSetType DATA_SET_TYPE = new DataSetType("METABOLITE_INTENSITIES");

    @Test
    public void testSimpleTransaction() throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties = createThreadProperties();
        createHandler(properties, false, true);
        createData("Metabolomics-Example.xlsx");

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicOperationDetails =
                setUpDataSetRegistrationExpectations(DATA_SET_TYPE, TSV_DATA_SET_TYPE);

        handler.handle(markerFile);

        assertEquals(3, atomicOperationDetails.recordedObject().getDataSetRegistrations().size());

        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(1), "METABOLITE_INTENSITIES");
        checkDataTypeProperty(atomicOperationDetails.recordedObject().getDataSetRegistrations()
                .get(2), "METABOLITE_INTENSITIES");

        NewExternalData dataSet =
                atomicOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        HashMap<String, NewProperty> propertyMap =
                getDataSetPropertiesMap(dataSet.getDataSetProperties());
        NewProperty strainProperty = propertyMap.get(STRAIN_NAMES_PROP);

        assertNotNull(strainProperty);
        assert null != strainProperty;
        assertEquals("CHASSIS 1", strainProperty.getValue());
        context.assertIsSatisfied();
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/metabolomics/";
    }
}
