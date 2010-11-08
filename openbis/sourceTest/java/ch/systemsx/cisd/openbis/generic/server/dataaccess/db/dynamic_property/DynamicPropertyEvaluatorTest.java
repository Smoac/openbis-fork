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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.AbstractEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.BasicPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Piotr Buczek
 */
public class DynamicPropertyEvaluatorTest extends AssertJUnit // TODO extend AbstractDAOTest
{

    @Test
    public void testEvaluateProperties()
    {
        final DynamicPropertyEvaluator evaluator = new DynamicPropertyEvaluator();

        // in this test we check
        // - successful evaluation of varchar dynamic property
        // - successful evaluation of integer dynamic property
        // - error handling
        // -- storing validation error when script evaluates to string instead of expected integer
        // -- storing python error when script tries to invoke nonexisting method

        Set<SamplePropertyPE> properties = new HashSet<SamplePropertyPE>();
        // create normal properties
        SamplePropertyPE p1 = createSampleProperty("p1", "v1");
        SamplePropertyPE p2 = createSampleProperty("p2", "v2");
        SamplePropertyPE p3 = createSampleProperty("p3", "v3");
        properties.add(p1);
        properties.add(p2);
        properties.add(p3);

        // create dynamic properties
        final ScriptPE script1 =
                createScript("s1", "entity.propertyValue('p1') + ' ' + entity.propertyValue('p2')");
        final ScriptPE script2 = createScript("s2", "entity.properties().size()");
        final String s3 = "entity.getCode()";
        final ScriptPE script3 = createScript("s3", s3);
        final SamplePropertyPE dp1 = createDynamicSampleProperty("p_d1", script1);
        final SamplePropertyPE dp1Error =
                createDynamicSampleProperty(createPropertyType("p_d1_error", DataTypeCode.INTEGER),
                        script1);
        final SamplePropertyPE dp2 =
                createDynamicSampleProperty(createPropertyType("p_d2", DataTypeCode.INTEGER),
                        script2);
        final SamplePropertyPE dp3Error = createDynamicSampleProperty("p_d3_error", script3);
        properties.add(dp1);
        properties.add(dp1Error);
        properties.add(dp2);
        properties.add(dp3Error);

        // create sample with all properties created above and evaluate dynamic properties
        final SamplePE sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);

        // check if evaluated values are correct
        final String expectedDp1Value = String.format("%s %s", p1.getValue(), p2.getValue());
        assertEquals(expectedDp1Value, dp1.getValue());
        final String expectedDp1ErrorValue =
                String.format("%sERROR: Integer value '%s' has improper format.",
                        BasicConstant.ERROR_PROPERTY_PREFIX, expectedDp1Value);
        assertEquals(expectedDp1ErrorValue, dp1Error.getValue());
        assertEquals(properties.size() + "", dp2.getValue());
        final String expectedDp3ErrorValue =
                String.format("%sERROR: Error evaluating '%s': AttributeError: getCode",
                        BasicConstant.ERROR_PROPERTY_PREFIX, s3);
        assertEquals(expectedDp3ErrorValue, dp3Error.getValue());
    }

    private SamplePE createSample(String code, Set<SamplePropertyPE> properties)
    {
        final SamplePE result = new SamplePE();
        result.setCode(code);
        result.setProperties(properties);
        return result;
    }

    @Test
    public void testEvaluateProperty()
    {
        final DynamicPropertyEvaluator evaluator = new DynamicPropertyEvaluator();

        IEntityPropertyAdaptor p1 = createProperty("p1", "v1");
        IEntityPropertyAdaptor p2 = createProperty("p2", "v2");

        final IEntityAdaptor entityAdaptor =
                createEntity("e", Arrays.asList(new IEntityPropertyAdaptor[]
                    { p1, p2 }));
        final ScriptPE script =
                createScript("s1", "entity.propertyValue('p1') + ' ' + entity.propertyValue('p2')");

        final PropertyTypePE propertyType = createPropertyType("dynamic");
        final EntityTypePropertyTypePE etpt =
                createDynamicSamplePropertyAssignment(propertyType, script);
        final String result = evaluator.evaluateProperty(entityAdaptor, etpt);
        assertEquals(p1.valueAsString() + " " + p2.valueAsString(), result);
    }

    // @Test
    // public void testEvaluatePropertyFailsValidation()
    // {
    //
    // }
    //
    // @Test
    // public void testEvaluatePropertyDependingOnAnotherDynamicProperty()
    // {
    //
    // }

    private static IEntityAdaptor createEntity(final String code,
            final Collection<IEntityPropertyAdaptor> properties)
    {
        final AbstractEntityAdaptor result = new AbstractEntityAdaptor(code);
        if (properties != null)
        {
            for (IEntityPropertyAdaptor property : properties)
            {
                result.addProperty(property);
            }
        }
        return result;
    }

    private static ScriptPE createScript(String name, String script)
    {
        ScriptPE result = new ScriptPE();
        result.setName(name);
        result.setScript(script);
        return result;
    }

    private static SampleTypePropertyTypePE createDynamicSamplePropertyAssignment(
            final PropertyTypePE propertyType, ScriptPE script)
    {
        final SampleTypePropertyTypePE assignment = new SampleTypePropertyTypePE();
        assignment.setPropertyType(propertyType);
        assignment.setDynamic(true);
        assignment.setScript(script);
        return assignment;
    }

    private static PropertyTypePE createPropertyType(String propertyCode)
    {
        return createPropertyType(propertyCode, DataTypeCode.VARCHAR);
    }

    private static PropertyTypePE createPropertyType(String propertyCode, DataTypeCode dataTypeCode)
    {
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode(propertyCode);
        final DataTypePE dataType = new DataTypePE();
        dataType.setCode(dataTypeCode);
        propertyType.setType(dataType);
        return propertyType;
    }

    private static SamplePropertyPE createSampleProperty(final String propertyTypeCode,
            final String value)
    {
        PropertyTypePE propertyType = createPropertyType(propertyTypeCode);
        return createSampleProperty(propertyType, value);
    }

    private static SamplePropertyPE createSampleProperty(final PropertyTypePE propertyType,
            final String value)
    {
        final SamplePropertyPE result = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyType = new SampleTypePropertyTypePE();
        entityTypePropertyType.setPropertyType(propertyType);
        result.setEntityTypePropertyType(entityTypePropertyType);
        result.setValue(value);
        return result;
    }

    private static SamplePropertyPE createDynamicSampleProperty(final String propertyTypeCode,
            final ScriptPE script)
    {
        PropertyTypePE propertyType = createPropertyType(propertyTypeCode);
        return createDynamicSampleProperty(propertyType, script);
    }

    private static SamplePropertyPE createDynamicSampleProperty(final PropertyTypePE propertyType,
            final ScriptPE script)
    {
        final SamplePropertyPE result = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyType =
                createDynamicSamplePropertyAssignment(propertyType, script);
        result.setEntityTypePropertyType(entityTypePropertyType);
        return result;
    }

    private static IEntityPropertyAdaptor createProperty(final String propertyTypeCode,
            final String value)
    {
        return new BasicPropertyAdaptor(propertyTypeCode, value);
    }

}
