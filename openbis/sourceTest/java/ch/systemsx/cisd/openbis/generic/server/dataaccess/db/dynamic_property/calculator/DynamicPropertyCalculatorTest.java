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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator;

import java.util.Arrays;
import java.util.Collection;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.AbstractEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.DynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityPropertyAdaptor;

/**
 * @author Piotr Buczek
 */
public class DynamicPropertyCalculatorTest extends AssertJUnit
{

    @Test
    public void testGetEntityCode()
    {
        final String entityCode1 = "ecode1";
        final String entityCode2 = "ecode2";
        final DynamicPropertyCalculator calculator =
                new DynamicPropertyCalculator("entity.getCode()");

        calculator.setEntity(createEntity(entityCode1, null));
        assertEquals(entityCode1, calculator.evalAsString());

        calculator.setEntity(createEntity(entityCode2, null));
        assertEquals(entityCode2, calculator.evalAsString());
    }

    @Test
    public void testGetEntityPropertyValue()
    {
        final DynamicPropertyCalculator calculator =
                new DynamicPropertyCalculator("entity.getPropertyValueByCode('p2')");

        final String entityCode = "ecode";

        IEntityPropertyAdaptor p1 = createProperty("p1", "v1");
        IEntityPropertyAdaptor p21 = createProperty("p2", "v21");
        IEntityPropertyAdaptor p22 = createProperty("p2", "v22");

        calculator.setEntity(createEntity(entityCode, null));
        assertEquals("", calculator.evalAsString());

        calculator.setEntity(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
            { p1 })));
        assertEquals("", calculator.evalAsString());

        calculator.setEntity(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
            { p1, p21 })));
        assertEquals(p21.getValueAsString(), calculator.evalAsString());

        calculator.setEntity(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
            { p1, p22 })));
        assertEquals(p22.getValueAsString(), calculator.evalAsString());
    }

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

    private static IEntityPropertyAdaptor createProperty(final String propertyTypeCode,
            final String value)
    {
        return new BasicPropertyAdaptor(propertyTypeCode, value);
    }

}
