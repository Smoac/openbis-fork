/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class ParentsPropertiesSectionBuilderTest extends AssertJUnit
{
    @Test
    public void testNoSample()
    {
        ParentsPropertiesSectionBuilder builder = new ParentsPropertiesSectionBuilder();

        Map<String, List<IEntityProperty>> sections = builder.getSections();

        assertEquals("{}", sections.toString());
    }

    @Test
    public void testSingleSample()
    {
        ParentsPropertiesSectionBuilder builder = new ParentsPropertiesSectionBuilder();

        builder.addParent(new SampleBuilder("/S/S1").property("answer", "42").getSample());
        Map<String, List<IEntityProperty>> sections = builder.getSections();

        assertEquals("{Properties of /S/S1=[answer: 42]}", sections.toString());
    }

    @Test
    public void testTwoSamplesWithNoCommonProperties()
    {
        ParentsPropertiesSectionBuilder builder = new ParentsPropertiesSectionBuilder();

        builder.addParent(new SampleBuilder("/S/S1").property("answer", "43").getSample());
        builder.addParent(new SampleBuilder("/S/S2").property("answer", "42")
                .property("question", "6 x 7").getSample());
        Map<String, List<IEntityProperty>> sections = builder.getSections();

        assertEquals("{Properties of /S/S1=[answer: 43], "
                + "Properties of /S/S2=[answer: 42, question: 6 x 7]}", sections.toString());
    }

    @Test
    public void testTwoSamplesWithOneCommonProperties()
    {
        ParentsPropertiesSectionBuilder builder = new ParentsPropertiesSectionBuilder();

        builder.addParent(new SampleBuilder("/S/S1").property("answer", "42").getSample());
        builder.addParent(new SampleBuilder("/S/S2").property("answer", "42")
                .property("question", "6 x 7").getSample());
        Map<String, List<IEntityProperty>> sections = builder.getSections();

        assertEquals("{Properties common by all parents=[answer: 42], "
                + "Properties of /S/S2=[question: 6 x 7]}", sections.toString());
    }

    @Test
    public void testThreeSamplesWithtwoCommonProperties()
    {
        ParentsPropertiesSectionBuilder builder = new ParentsPropertiesSectionBuilder();

        SampleBuilder s1 =
                new SampleBuilder("/S/S1").property("answer", "42").property("question", "6 x 7");
        s1.property("property").type(DataTypeCode.INTEGER).value(101);
        builder.addParent(s1.getSample());
        SampleBuilder s2 =
                new SampleBuilder("/S/S2").property("question", "6 x 7").property("answer", "42");
        s2.property("property", "101");
        builder.addParent(s2.getSample());
        SampleBuilder s3 =
                new SampleBuilder("/S/S3").property("question", "6 x 7").property("answer", "42");
        s3.property("property", "101");
        builder.addParent(s3.getSample());
        Map<String, List<IEntityProperty>> sections = builder.getSections();

        assertEquals("{Properties common by all parents=[answer: 42, question: 6 x 7], "
                + "Properties of /S/S1=[property: 101], " + "Properties of /S/S2=[property: 101], "
                + "Properties of /S/S3=[property: 101]}", sections.toString());
    }
}
