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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class TranslatorTest extends AssertJUnit
{
    private DataSetBuilder ds1;

    private DataSetBuilder ds2;

    @BeforeMethod
    public void setUp()
    {
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").getExperiment();
        Sample sample = new SampleBuilder("/S/S1").getSample();

        ds1 =
                new DataSetBuilder().code("ds1").type("T1").experiment(experiment).sample(sample)
                        .property("A", "42");
        ds2 =
                new DataSetBuilder().code("ds2").type("T2").experiment(experiment)
                        .property("B", "true");
    }

    @Test
    public void testTranslateExternalDataWithNoConnectionsAndRetrievingNoConnections()
    {
        DataSet translated =
                Translator.translate(ds1.getDataSet(), EnumSet.noneOf(DataSet.Connections.class));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertChildrenNotRetrieved(translated);
        assertParentsNotRetrieved(translated);
    }

    @Test
    public void testTranslateExternalDataWithNoConnectionsAndRetrievingAllConnections()
    {
        DataSet translated =
                Translator.translate(ds1.getDataSet(),
                        EnumSet.of(Connections.CHILDREN, Connections.PARENTS));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertEquals(0, translated.getChildrenCodes().size());
        assertEquals(0, translated.getParentCodes().size());
    }

    @Test
    public void testTranslateExternalDataWithConnectionsAndRetrievingNoConnections()
    {
        ds1.child(ds2.getDataSet());
        DataSet translated =
                Translator.translate(ds1.getDataSet(), EnumSet.noneOf(DataSet.Connections.class));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertChildrenNotRetrieved(translated);
        assertParentsNotRetrieved(translated);
    }

    @Test
    public void testTranslateExternalDataWithConnectionsRetrievingOnlyChildrenConnections()
    {
        ds1.child(ds2.getDataSet());
        DataSet translated =
                Translator.translate(ds1.getDataSet(), EnumSet.of(Connections.CHILDREN));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertEquals("[ds2]", translated.getChildrenCodes().toString());
        assertParentsNotRetrieved(translated);
    }

    @Test
    public void testTranslateExternalDataWithConnectionsRetrievingOnlyParentConnections()
    {
        ds2.parent(ds1.getDataSet());
        DataSet translated =
                Translator.translate(ds2.getDataSet(), EnumSet.of(Connections.PARENTS));
        assertBasicAttributes(ds2.getDataSet(), translated);
        assertChildrenNotRetrieved(translated);
        assertEquals("[ds1]", translated.getParentCodes().toString());
    }

    @Test
    public void testTranslateExternalDataWithConnectionsRetrievingAllConnections()
    {
        ds2.parent(ds1.getDataSet());
        ds2.child(ds2.getDataSet());
        DataSet translated =
                Translator.translate(ds2.getDataSet(),
                        EnumSet.of(Connections.CHILDREN, Connections.PARENTS));
        assertBasicAttributes(ds2.getDataSet(), translated);
        assertEquals("[ds2]", translated.getChildrenCodes().toString());
        assertEquals("[ds1]", translated.getParentCodes().toString());
    }

    private void assertBasicAttributes(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet originalDataSet,
            DataSet translatedDataSet)
    {
        assertEquals(originalDataSet.getCode(), translatedDataSet.getCode());
        assertEquals(originalDataSet.getDataSetType().getCode(),
                translatedDataSet.getDataSetTypeCode());
        assertEquals(originalDataSet.getExperiment().getIdentifier(),
                translatedDataSet.getExperimentIdentifier());
        assertEquals(originalDataSet.getSampleIdentifier(),
                translatedDataSet.getSampleIdentifierOrNull());
        List<IEntityProperty> originalProperties = originalDataSet.getProperties();
        HashMap<String, String> translatedProperties = translatedDataSet.getProperties();
        for (IEntityProperty property : originalProperties)
        {
            assertEquals(property.getValue(),
                    translatedProperties.get(property.getPropertyType().getCode()));
        }
        assertEquals(originalProperties.size(), translatedProperties.size());
    }

    private void assertChildrenNotRetrieved(DataSet dataSet)
    {
        try
        {
            dataSet.getChildrenCodes();
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Children codes were not retrieved for data set " + dataSet.getCode()
                    + ".", ex.getMessage());
        }
    }

    private void assertParentsNotRetrieved(DataSet dataSet)
    {
        try
        {
            dataSet.getParentCodes();
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Parent codes were not retrieved for data set " + dataSet.getCode() + ".",
                    ex.getMessage());
        }
    }

}
