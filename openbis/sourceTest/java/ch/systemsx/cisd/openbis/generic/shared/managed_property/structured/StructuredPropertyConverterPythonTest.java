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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.structured;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ScriptUtilityFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;

/**
 * @author Kaloyan Enimanev
 */
public class StructuredPropertyConverterPythonTest extends AssertJUnit
{

    /**
     * test the API for creating {@link IElement} is usable from Jython.
     */
    @Test
    public void testAPIUsageFromJython()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        String script = getResourceAsString("structured-property-test.py");
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator(script);
        
        
        evaluator.configureUI(managedProperty);

        // the script will create several elements and serialize them in the property value
        String value = managedProperty.getValue();
        List<IElement> elements =
                ScriptUtilityFactory.createPropertyConverter().convertToElements(value);
        
        assertEquals(3, elements.size());
    }

    /**
     * if this becomes a common pattern, we might factor it out.
     */
    private String getResourceAsString(String resource)
    {
        InputStream in = null;
        try
        {
            in = getClass().getResourceAsStream(resource);
            return IOUtils.toString(in);
        } catch (IOException ioex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
        } finally
        {
            IOUtils.closeQuietly(in);
        }

    }
}
