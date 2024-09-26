/*
 * Copyright ETH 2024 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.coreplugin;

import junit.framework.TestCase;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.Properties;
import java.util.function.Function;

public class PropertyValueResolverTest extends TestCase {

    private final Function<String, String> systemArgMethod = key -> null;
    private final Function<String, String> envVarMethod = key -> null;
    private final Properties properties = new Properties();
    private PropertyValueResolver resolver;


    @BeforeMethod
    @Override
    public void setUp()
    {
        properties.setProperty("some.variable.name", "propertyValue");
        resolver = new PropertyValueResolver(properties, systemArgMethod, envVarMethod);
    }

    @Test
    public void testResolveNullProperty()
    {
        String result = resolver.resolvePropertyValue(null);
        assertNull(result);
    }

    @Test
    public void testResolveEmptyProperty()
    {
        String result = resolver.resolvePropertyValue("");
        assertEquals("", result);
    }

    @Test
    public void testResolveVariable_noMatchFound()
    {
        String result = resolver.resolvePropertyValue("${not.existing.variable.name}");
        assertEquals("${not.existing.variable.name}", result);
    }

    @Test
    public void testResolveVariable_propertyFound()
    {
        String result = resolver.resolvePropertyValue("${some.variable.name}");
        assertEquals("propertyValue", result);
    }

    @Test
    public void testResolveVariable_envVariableFound()
    {
        resolver = new PropertyValueResolver(properties, systemArgMethod, key -> "envVarValue");

        String result = resolver.resolvePropertyValue("${some.variable.name}");
        assertEquals("envVarValue", result);
    }

    @Test
    public void testResolveVariable_sysArgFound()
    {
        resolver = new PropertyValueResolver(properties, key -> "sysArgValue", key -> "envVarValue");

        String result = resolver.resolvePropertyValue("${some.variable.name}");
        assertEquals("sysArgValue", result);
    }

    @Test
    public void testResolveVariable_noMatchFound_defaultValue()
    {
        String result = resolver.resolvePropertyValue("${not.existing.variable.name:some_value}");
        assertEquals("some_value", result);
    }

    @Test
    public void testResolveVariable_noMatchFound_defaultValueEmpty()
    {
        String result = resolver.resolvePropertyValue("${not.existing.variable.name:}");
        assertEquals("", result);
    }

}
