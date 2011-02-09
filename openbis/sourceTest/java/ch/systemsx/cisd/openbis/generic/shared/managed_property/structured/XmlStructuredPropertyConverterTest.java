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

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;

/**
 * @author Kaloyan Enimanev
 */
public class XmlStructuredPropertyConverterTest extends AssertJUnit
{

    private IElementFactory factory = new ElementFactory();

    private XmlStructuredPropertyConverter converter = new XmlStructuredPropertyConverter(factory);

    @Test
    public void testNoNestedElements()
    {

        List<IElement> elements =
                Arrays.asList(
                        factory.createElement("testname").addAttribute("attr1", "value1")
                                .addAttribute("attr2", "value2").setData("dummy&<>data"),
                        factory.createSampleLink("permIdSample").addAttribute("sampleAttrKey",
                                "sampleAttrVal"),
                        factory.createMaterialLink("materialCode", "typeCode").addAttribute(
                                "materialAttrKey", "materialAttrVal")
        );

        String persistentValue = converter.convertToString(elements);
        List<IElement> deserialized = converter.convertStringToElements(persistentValue);

        assertEquals(elements, deserialized);
    }

    @Test
    public void testWithNestedElements()
    {

        List<IElement> elements =
                Arrays.asList(
                        factory.createElement("name1")
                                .addAttribute("attr1", "value1")
                                .addChildren(factory.createSampleLink("nestedPermId1"),
                                        factory.createMaterialLink("code1", "typeCode1")),
                        factory.createSampleLink("permIdSample").addChildren(
                                factory.createElement("nested1").addAttribute("na1", "nav2")),
                        factory.createMaterialLink("materialCode", "typeCode").addChildren(
                                factory.createElement("nested2").addAttribute("na2", "nav2")));

        String persistentValue = converter.convertToString(elements);
        List<IElement> deserialized = converter.convertStringToElements(persistentValue);

        assertEquals(elements, deserialized);
    }
}
