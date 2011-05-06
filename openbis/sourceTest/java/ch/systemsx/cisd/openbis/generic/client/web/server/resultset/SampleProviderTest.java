/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class SampleProviderTest extends AbstractProviderTest
{
    @BeforeMethod
    public final void setUpExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(server).listSampleTypes(SESSION_TOKEN);
                    SampleTypeBuilder st1 = new SampleTypeBuilder().code("ALPHA");
                    st1.propertyType("TEXT", "text", DataTypeCode.MULTILINE_VARCHAR);
                    st1.propertyType("NUMBER", "number", DataTypeCode.REAL);
                    st1.propertyType("TIMESTAMP", "Time stamp", DataTypeCode.TIMESTAMP);
                    will(returnValue(Arrays.asList(st1.getSampleType())));
                }
            });
    }

    @Test
    public void testBrowse()
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        ListSampleDisplayCriteria2 listCriteria = new ListSampleDisplayCriteria2(criteria);
        SampleProvider sampleProvider = new SampleProvider(server, SESSION_TOKEN, listCriteria);
        final SampleBuilder s1 = new SampleBuilder("DB:/MY-SPACE/S1").id(1).type("ALPHA");
        s1.property("NAME", "hello");
        s1.property("MY-MATERIAL").value(new MaterialBuilder().code("WATER").type("FLUID"));
        final SampleBuilder s2 = new SampleBuilder("DB:/MY-SPACE/S2").id(2).type("BETA");
        s2.property("NUMBER").value(2.5);
        s2.property("TIMESTAMP").value(new Date(42));
        context.checking(new Expectations()
            {
                {
                    one(server).listSamples(SESSION_TOKEN, criteria);
                    will(returnValue(Arrays.asList(s1.getSample(), s2.getSample())));
                }
            });

        TypedTableModel<Sample> tableModel = sampleProvider.getTableModel(Integer.MAX_VALUE);

        assertEquals(
                "[CODE, SUBCODE, DATABASE_INSTANCE, SPACE, SAMPLE_IDENTIFIER, SAMPLE_TYPE, "
                        + "IS_INSTANCE_SAMPLE, IS_INVALID, REGISTRATOR, REGISTRATION_DATE, "
                        + "EXPERIMENT, EXPERIMENT_IDENTIFIER, PROJECT, "
                        + "PERM_ID, SHOW_DETAILS_LINK, generatedFromParent, containerParent, "
                        + "property-USER-NUMBER, property-USER-TIMESTAMP, property-USER-NAME, property-USER-MY-MATERIAL, property-USER-TEXT]",
                getHeaderIDs(tableModel).toString());
        assertEquals(
                "[VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, "
                        + "VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, VARCHAR, VARCHAR, VARCHAR, "
                        + "VARCHAR, VARCHAR, VARCHAR, VARCHAR, REAL, TIMESTAMP, VARCHAR, MATERIAL, MULTILINE_VARCHAR]",
                getHeaderDataTypes(tableModel).toString());
        assertEquals("[null, null, null, null, null, null, null, null, null, null, null, null, "
                + "null, null, null, null, null, null, null, null, MATERIAL, null]",
                getHeaderEntityKinds(tableModel).toString());
        List<TableModelRowWithObject<Sample>> rows = tableModel.getRows();
        assertSame(s1.getSample(), rows.get(0).getObjectOrNull());
        assertEquals(
                "[S1, S1, DB, MY-SPACE, DB:/MY-SPACE/S1, ALPHA, no, no, , , , , , , , , , , , "
                        + "hello, WATER (FLUID), ]", rows.get(0).getValues().toString());
        assertSame(s2.getSample(), rows.get(1).getObjectOrNull());
        assertEquals(
                "[S2, S2, DB, MY-SPACE, DB:/MY-SPACE/S2, BETA, no, no, , , , , , , , , , 2.5, 1970-01-01 01:00:00 +0100, , , ]",
                rows.get(1).getValues().toString());
        assertEquals(2, rows.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearch()
    {
        final DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        ListSampleDisplayCriteria2 listCriteria = new ListSampleDisplayCriteria2(criteria);
        SampleProvider sampleProvider = new SampleProvider(server, SESSION_TOKEN, listCriteria);
        final SampleBuilder s1 = new SampleBuilder("DB:/S1").id(1).type("ALPHA");
        s1.registrator(new PersonBuilder().name("Albert", "Einstein").getPerson());
        s1.invalidate().date(new Date(4711));
        Sample p1 = new SampleBuilder("/AB/CD").getSample();
        s1.property("NAME", "hello").permID("123").permLink("http").childOf(p1);
        final SampleBuilder s2 = new SampleBuilder("DB:/MY-SPACE/S:2").id(2).type("BETA");
        s2.experiment(new ExperimentBuilder().identifier("DB:/SPACE1/P1/EXP1").getExperiment());
        Sample p2 = new SampleBuilder("/DE/FG").getSample();
        s2.partOf(new SampleBuilder("DB:/A/B").getSample()).childOf(p1, p2);
        context.checking(new Expectations()
            {
                {
                    one(server).searchForSamples(SESSION_TOKEN, criteria);
                    will(returnValue(Arrays.asList(s1.getSample(), s2.getSample())));
                }
            });

        TypedTableModel<Sample> tableModel = sampleProvider.getTableModel(Integer.MAX_VALUE);

        assertEquals(
                "[CODE, SUBCODE, DATABASE_INSTANCE, SPACE, SAMPLE_IDENTIFIER, SAMPLE_TYPE, "
                        + "IS_INSTANCE_SAMPLE, IS_INVALID, REGISTRATOR, REGISTRATION_DATE, "
                        + "EXPERIMENT, EXPERIMENT_IDENTIFIER, PROJECT, "
                        + "PERM_ID, SHOW_DETAILS_LINK, generatedFromParent, containerParent, "
                        + "property-USER-NAME, property-USER-TIMESTAMP, property-USER-NUMBER, property-USER-TEXT]",
                getHeaderIDs(tableModel).toString());
        assertEquals(
                "[VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, "
                        + "VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, VARCHAR, VARCHAR, VARCHAR, "
                        + "VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, REAL, MULTILINE_VARCHAR]",
                getHeaderDataTypes(tableModel).toString());
        assertEquals(
                "[null, null, null, null, null, null, null, null, null, null, null, null, null, "
                        + "null, null, null, null, null, null, null, null]",
                getHeaderEntityKinds(tableModel).toString());
        List<TableModelRowWithObject<Sample>> rows = tableModel.getRows();
        assertSame(s1.getSample(), rows.get(0).getObjectOrNull());
        assertEquals("[S1, S1, DB, , DB:/S1, ALPHA, yes, yes, Einstein, Albert, "
                + "Thu Jan 01 01:00:04 CET 1970, , , , 123, http, /AB/CD, , hello, , , ]", rows
                .get(0).getValues().toString());
        assertSame(s2.getSample(), rows.get(1).getObjectOrNull());
        assertEquals("[S:2, 2, DB, MY-SPACE, DB:/MY-SPACE/S:2, BETA, no, no, , , EXP1, "
                + "DB:/SPACE1/P1/EXP1, P1, , , /AB/CD\n/DE/FG\n, DB:/A/B, , , , ]", rows.get(1)
                .getValues().toString());
        assertEquals(2, rows.size());
        context.assertIsSatisfied();
    }

}
