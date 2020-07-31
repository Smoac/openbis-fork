/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsAtLeast;
import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionDoesntContain;

/**
 * @author pkupczyk
 */
public class SearchSampleTest extends AbstractSampleTest
{

    private static final String[] ALL_SAMPLE_IDS_WITH_CONTAINER = new String[]
            {
                    "/CISD/CL1:A01", "/CISD/CL1:A03", "/CISD/MP002-1:A03", "/CISD/MP002-1:A04", "/CISD/MP002-1:A05",
                    "/CISD/MP002-1:A06", "/CISD/MP002-1:A07", "/CISD/MP002-1:A08", "/CISD/MP002-1:A09",
                    "/CISD/MP002-1:A10", "/CISD/MP002-1:A11", "/CISD/MP002-1:A12", "/CISD/MP002-1:A13",
                    "/CISD/MP002-1:A14", "/CISD/MP002-1:A15", "/CISD/MP002-1:A16", "/CISD/MP002-1:A17",
                    "/CISD/MP002-1:A18", "/CISD/MP002-1:A19", "/CISD/MP002-1:A20", "/CISD/MP002-1:A21",
                    "/CISD/MP002-1:A22", "/CISD/MP002-1:B03", "/CISD/MP002-1:B04", "/CISD/MP002-1:B05",
                    "/CISD/MP002-1:B06", "/CISD/MP002-1:B07", "/CISD/MP002-1:B08", "/CISD/MP002-1:B09",
                    "/CISD/MP002-1:B10", "/CISD/MP002-1:B11", "/CISD/MP002-1:B12", "/CISD/MP002-1:B13",
                    "/CISD/MP002-1:B14", "/CISD/MP002-1:B15", "/CISD/MP002-1:B16", "/CISD/MP002-1:B17",
                    "/CISD/MP002-1:B18", "/CISD/MP002-1:B19", "/CISD/MP002-1:B20", "/CISD/MP002-1:B21",
                    "/CISD/MP002-1:B22", "/CISD/MP002-1:C03", "/CISD/MP002-1:C04", "/CISD/MP002-1:C05",
                    "/CISD/MP002-1:C06", "/CISD/MP002-1:C07", "/CISD/MP002-1:C08", "/CISD/MP002-1:C09",
                    "/CISD/MP002-1:C10", "/CISD/MP002-1:C11", "/CISD/MP002-1:C12", "/CISD/MP002-1:C13",
                    "/CISD/MP002-1:C14", "/CISD/MP002-1:C15", "/CISD/MP002-1:C16", "/CISD/MP002-1:C17",
                    "/CISD/MP002-1:C18", "/CISD/MP002-1:C19", "/CISD/MP002-1:C20", "/CISD/MP002-1:C21",
                    "/CISD/MP002-1:C22", "/CISD/MP002-1:D03", "/CISD/MP002-1:D04", "/CISD/MP002-1:D05",
                    "/CISD/MP002-1:D06", "/CISD/MP002-1:D07", "/CISD/MP002-1:D08", "/CISD/MP002-1:D09",
                    "/CISD/MP002-1:D10", "/CISD/MP002-1:D11", "/CISD/MP002-1:D12", "/CISD/MP002-1:D13",
                    "/CISD/MP002-1:D14", "/CISD/MP002-1:D15", "/CISD/MP002-1:D16", "/CISD/MP002-1:D17",
                    "/CISD/MP002-1:D18", "/CISD/MP002-1:D19", "/CISD/MP002-1:D20", "/CISD/MP002-1:D21",
                    "/CISD/MP002-1:D22", "/CISD/MP002-1:E03", "/CISD/MP002-1:E04", "/CISD/MP002-1:E05",
                    "/CISD/MP002-1:E06", "/CISD/MP002-1:E07", "/CISD/MP002-1:E08", "/CISD/MP002-1:E09",
                    "/CISD/MP002-1:E10", "/CISD/MP002-1:E11", "/CISD/MP002-1:E12", "/CISD/MP002-1:E13",
                    "/CISD/MP002-1:E14", "/CISD/MP002-1:E15", "/CISD/MP002-1:E16", "/CISD/MP002-1:E17",
                    "/CISD/MP002-1:E18", "/CISD/MP002-1:E19", "/CISD/MP002-1:E20", "/CISD/MP002-1:E21",
                    "/CISD/MP002-1:E22", "/CISD/MP002-1:F03", "/CISD/MP002-1:F04", "/CISD/MP002-1:F05",
                    "/CISD/MP002-1:F06", "/CISD/MP002-1:F07", "/CISD/MP002-1:F08", "/CISD/MP002-1:F09",
                    "/CISD/MP002-1:F10", "/CISD/MP002-1:F11", "/CISD/MP002-1:F12", "/CISD/MP002-1:F13",
                    "/CISD/MP002-1:F14", "/CISD/MP002-1:F15", "/CISD/MP002-1:F16", "/CISD/MP002-1:F17",
                    "/CISD/MP002-1:F18", "/CISD/MP002-1:F19", "/CISD/MP002-1:F20", "/CISD/MP002-1:F21",
                    "/CISD/MP002-1:F22", "/CISD/MP002-1:G03", "/CISD/MP002-1:G04", "/CISD/MP002-1:G05",
                    "/CISD/MP002-1:G06", "/CISD/MP002-1:G07", "/CISD/MP002-1:G08", "/CISD/MP002-1:G09",
                    "/CISD/MP002-1:G10", "/CISD/MP002-1:G11", "/CISD/MP002-1:G12", "/CISD/MP002-1:G13",
                    "/CISD/MP002-1:G14", "/CISD/MP002-1:G15", "/CISD/MP002-1:G16", "/CISD/MP002-1:G17",
                    "/CISD/MP002-1:G18", "/CISD/MP002-1:G19", "/CISD/MP002-1:G20", "/CISD/MP002-1:G21",
                    "/CISD/MP002-1:G22", "/CISD/MP002-1:H03", "/CISD/MP002-1:H04", "/CISD/MP002-1:H05",
                    "/CISD/MP002-1:H06", "/CISD/MP002-1:H07", "/CISD/MP002-1:H08", "/CISD/MP002-1:H09",
                    "/CISD/MP002-1:H10", "/CISD/MP002-1:H11", "/CISD/MP002-1:H12", "/CISD/MP002-1:H13",
                    "/CISD/MP002-1:H14", "/CISD/MP002-1:H15", "/CISD/MP002-1:H16", "/CISD/MP002-1:H17",
                    "/CISD/MP002-1:H18", "/CISD/MP002-1:H19", "/CISD/MP002-1:H20", "/CISD/MP002-1:H21",
                    "/CISD/MP002-1:H22", "/CISD/MP002-1:I03", "/CISD/MP002-1:I04", "/CISD/MP002-1:I05",
                    "/CISD/MP002-1:I06", "/CISD/MP002-1:I07", "/CISD/MP002-1:I08", "/CISD/MP002-1:I09",
                    "/CISD/MP002-1:I10", "/CISD/MP002-1:I11", "/CISD/MP002-1:I12", "/CISD/MP002-1:I13",
                    "/CISD/MP002-1:I14", "/CISD/MP002-1:I15", "/CISD/MP002-1:I16", "/CISD/MP002-1:I17",
                    "/CISD/MP002-1:I18", "/CISD/MP002-1:I19", "/CISD/MP002-1:I20", "/CISD/MP002-1:I21",
                    "/CISD/MP002-1:I22", "/CISD/MP002-1:J03", "/CISD/MP002-1:J04", "/CISD/MP002-1:J05",
                    "/CISD/MP002-1:J06", "/CISD/MP002-1:J07", "/CISD/MP002-1:J08", "/CISD/MP002-1:J09",
                    "/CISD/MP002-1:J10", "/CISD/MP002-1:J11", "/CISD/MP002-1:J12", "/CISD/MP002-1:J13",
                    "/CISD/MP002-1:J14", "/CISD/MP002-1:J15", "/CISD/MP002-1:J16", "/CISD/MP002-1:J17",
                    "/CISD/MP002-1:J18", "/CISD/MP002-1:J19", "/CISD/MP002-1:J20", "/CISD/MP002-1:J21",
                    "/CISD/MP002-1:J22", "/CISD/MP002-1:K03", "/CISD/MP002-1:K04", "/CISD/MP002-1:K05",
                    "/CISD/MP002-1:K06", "/CISD/MP002-1:K07", "/CISD/MP002-1:K08", "/CISD/MP002-1:K09",
                    "/CISD/MP002-1:K10", "/CISD/MP002-1:K11", "/CISD/MP002-1:K12", "/CISD/MP002-1:K13",
                    "/CISD/MP002-1:K14", "/CISD/MP002-1:K15", "/CISD/MP002-1:K16", "/CISD/MP002-1:K17",
                    "/CISD/MP002-1:K18", "/CISD/MP002-1:K19", "/CISD/MP002-1:K20", "/CISD/MP002-1:K21",
                    "/CISD/MP002-1:K22", "/CISD/MP002-1:L03", "/CISD/MP002-1:L04", "/CISD/MP002-1:L05",
                    "/CISD/MP002-1:L06", "/CISD/MP002-1:L07", "/CISD/MP002-1:L08", "/CISD/MP002-1:L09",
                    "/CISD/MP002-1:L10", "/CISD/MP002-1:L11", "/CISD/MP002-1:L12", "/CISD/MP002-1:L13",
                    "/CISD/MP002-1:L14", "/CISD/MP002-1:L15", "/CISD/MP002-1:L16", "/CISD/MP002-1:L17",
                    "/CISD/MP002-1:L18", "/CISD/MP002-1:L19", "/CISD/MP002-1:L20", "/CISD/MP002-1:L21",
                    "/CISD/MP002-1:L22", "/CISD/MP002-1:M03", "/CISD/MP002-1:M04", "/CISD/MP002-1:M05",
                    "/CISD/MP002-1:M06", "/CISD/MP002-1:M07", "/CISD/MP002-1:M08", "/CISD/MP002-1:M09",
                    "/CISD/MP002-1:M10", "/CISD/MP002-1:M11", "/CISD/MP002-1:M12", "/CISD/MP002-1:M13",
                    "/CISD/MP002-1:M14", "/CISD/MP002-1:M15", "/CISD/MP002-1:M16", "/CISD/MP002-1:M17",
                    "/CISD/MP002-1:M18", "/CISD/MP002-1:M19", "/CISD/MP002-1:M20", "/CISD/MP002-1:M21",
                    "/CISD/MP002-1:M22", "/CISD/MP002-1:N03", "/CISD/MP002-1:N04", "/CISD/MP002-1:N05",
                    "/CISD/MP002-1:N06", "/CISD/MP002-1:N07", "/CISD/MP002-1:N08", "/CISD/MP002-1:N09",
                    "/CISD/MP002-1:N10", "/CISD/MP002-1:N11", "/CISD/MP002-1:N12", "/CISD/MP002-1:N13",
                    "/CISD/MP002-1:N14", "/CISD/MP002-1:N15", "/CISD/MP002-1:N16", "/CISD/MP002-1:N17",
                    "/CISD/MP002-1:N18", "/CISD/MP002-1:N19", "/CISD/MP002-1:N20", "/CISD/MP002-1:N21",
                    "/CISD/MP002-1:N22", "/CISD/MP002-1:O03", "/CISD/MP002-1:O04", "/CISD/MP002-1:O05",
                    "/CISD/MP002-1:O06", "/CISD/MP002-1:O07", "/CISD/MP002-1:O08", "/CISD/MP002-1:O09",
                    "/CISD/MP002-1:O10", "/CISD/MP002-1:O11", "/CISD/MP002-1:O12", "/CISD/MP002-1:O13",
                    "/CISD/MP002-1:O14", "/CISD/MP002-1:O15", "/CISD/MP002-1:O16", "/CISD/MP002-1:O17",
                    "/CISD/MP002-1:O18", "/CISD/MP002-1:O19", "/CISD/MP002-1:O20", "/CISD/MP002-1:O21",
                    "/CISD/MP002-1:O22", "/CISD/MP002-1:P03", "/CISD/MP002-1:P04", "/CISD/MP002-1:P05",
                    "/CISD/MP002-1:P06", "/CISD/MP002-1:P07", "/CISD/MP002-1:P08", "/CISD/MP002-1:P09",
                    "/CISD/MP002-1:P10", "/CISD/MP002-1:P11", "/CISD/MP002-1:P12", "/CISD/MP002-1:P13",
                    "/CISD/MP002-1:P14", "/CISD/MP002-1:P15", "/CISD/MP002-1:P16", "/CISD/MP002-1:P17",
                    "/CISD/MP002-1:P18", "/CISD/MP002-1:P19", "/CISD/MP002-1:P20", "/CISD/MP002-1:P21",
                    "/CISD/MP002-1:P22", "/MP:A03", "/MP:A04", "/MP:A05", "/MP:A06", "/MP:A07", "/MP:A08", "/MP:A09",
                    "/MP:A10", "/MP:A11", "/MP:A12", "/MP:A13", "/MP:A14", "/MP:A15", "/MP:A16", "/MP:A17", "/MP:A18",
                    "/MP:A19", "/MP:A20", "/MP:A21", "/MP:A22", "/MP:B03", "/MP:B04", "/MP:B05", "/MP:B06", "/MP:B07",
                    "/MP:B08", "/MP:B09", "/MP:B10", "/MP:B11", "/MP:B12", "/MP:B13", "/MP:B14", "/MP:B15", "/MP:B16",
                    "/MP:B17", "/MP:B18", "/MP:B19", "/MP:B20", "/MP:B21", "/MP:B22", "/MP:C03", "/MP:C04", "/MP:C05",
                    "/MP:C06", "/MP:C07", "/MP:C08", "/MP:C09", "/MP:C10", "/MP:C11", "/MP:C12", "/MP:C13", "/MP:C14",
                    "/MP:C15", "/MP:C16", "/MP:C17", "/MP:C18", "/MP:C19", "/MP:C20", "/MP:C21", "/MP:C22", "/MP:D03",
                    "/MP:D04", "/MP:D05", "/MP:D06", "/MP:D07", "/MP:D08", "/MP:D09", "/MP:D10", "/MP:D11", "/MP:D12",
                    "/MP:D13", "/MP:D14", "/MP:D15", "/MP:D16", "/MP:D17", "/MP:D18", "/MP:D19", "/MP:D20", "/MP:D21",
                    "/MP:D22", "/MP:E03", "/MP:E04", "/MP:E05", "/MP:E06", "/MP:E07", "/MP:E08", "/MP:E09", "/MP:E10",
                    "/MP:E11", "/MP:E12", "/MP:E13", "/MP:E14", "/MP:E15", "/MP:E16", "/MP:E17", "/MP:E18", "/MP:E19",
                    "/MP:E20", "/MP:E21", "/MP:E22", "/MP:F03", "/MP:F04", "/MP:F05", "/MP:F06", "/MP:F07", "/MP:F08",
                    "/MP:F09", "/MP:F10", "/MP:F11", "/MP:F12", "/MP:F13", "/MP:F14", "/MP:F15", "/MP:F16", "/MP:F17",
                    "/MP:F18", "/MP:F19", "/MP:F20", "/MP:F21", "/MP:F22", "/MP:G03", "/MP:G04", "/MP:G05", "/MP:G06",
                    "/MP:G07", "/MP:G08", "/MP:G09", "/MP:G10", "/MP:G11", "/MP:G12", "/MP:G13", "/MP:G14", "/MP:G15",
                    "/MP:G16", "/MP:G17", "/MP:G18", "/MP:G19", "/MP:G20", "/MP:G21", "/MP:G22", "/MP:H03", "/MP:H04",
                    "/MP:H05", "/MP:H06", "/MP:H07", "/MP:H08", "/MP:H09", "/MP:H10", "/MP:H11", "/MP:H12", "/MP:H13",
                    "/MP:H14", "/MP:H15", "/MP:H16", "/MP:H17", "/MP:H18", "/MP:H19", "/MP:H20", "/MP:H21", "/MP:H22",
                    "/MP:I03", "/MP:I04", "/MP:I05", "/MP:I06", "/MP:I07", "/MP:I08", "/MP:I09", "/MP:I10", "/MP:I11",
                    "/MP:I12", "/MP:I13", "/MP:I14", "/MP:I15", "/MP:I16", "/MP:I17", "/MP:I18", "/MP:I19", "/MP:I20",
                    "/MP:I21", "/MP:I22", "/MP:J03", "/MP:J04", "/MP:J05", "/MP:J06", "/MP:J07", "/MP:J08", "/MP:J09",
                    "/MP:J10", "/MP:J11", "/MP:J12", "/MP:J13", "/MP:J14", "/MP:J15", "/MP:J16", "/MP:J17", "/MP:J18",
                    "/MP:J19", "/MP:J20", "/MP:J21", "/MP:J22", "/MP:K03", "/MP:K04", "/MP:K05", "/MP:K06", "/MP:K07",
                    "/MP:K08", "/MP:K09", "/MP:K10", "/MP:K11", "/MP:K12", "/MP:K13", "/MP:K14", "/MP:K15", "/MP:K16",
                    "/MP:K17", "/MP:K18", "/MP:K19", "/MP:K20", "/MP:K21", "/MP:K22", "/MP:L03", "/MP:L04", "/MP:L05",
                    "/MP:L06", "/MP:L07", "/MP:L08", "/MP:L09", "/MP:L10", "/MP:L11", "/MP:L12", "/MP:L13", "/MP:L14",
                    "/MP:L15", "/MP:L16", "/MP:L17", "/MP:L18", "/MP:L19", "/MP:L20", "/MP:L21", "/MP:L22", "/MP:M03",
                    "/MP:M04", "/MP:M05", "/MP:M06", "/MP:M07", "/MP:M08", "/MP:M09", "/MP:M10", "/MP:M11", "/MP:M12",
                    "/MP:M13", "/MP:M14", "/MP:M15", "/MP:M16", "/MP:M17", "/MP:M18", "/MP:M19", "/MP:M20", "/MP:M21",
                    "/MP:M22", "/MP:N03", "/MP:N04", "/MP:N05", "/MP:N06", "/MP:N07", "/MP:N08", "/MP:N09", "/MP:N10",
                    "/MP:N11", "/MP:N12", "/MP:N13", "/MP:N14", "/MP:N15", "/MP:N16", "/MP:N17", "/MP:N18", "/MP:N19",
                    "/MP:N20", "/MP:N21", "/MP:N22", "/MP:O03", "/MP:O04", "/MP:O05", "/MP:O06", "/MP:O07", "/MP:O08",
                    "/MP:O09", "/MP:O10", "/MP:O11", "/MP:O12", "/MP:O13", "/MP:O14", "/MP:O15", "/MP:O16", "/MP:O17",
                    "/MP:O18", "/MP:O19", "/MP:O20", "/MP:O21", "/MP:O22", "/MP:P03", "/MP:P04", "/MP:P05", "/MP:P06",
                    "/MP:P07", "/MP:P08", "/MP:P09", "/MP:P10", "/MP:P11", "/MP:P12", "/MP:P13", "/MP:P14", "/MP:P15",
                    "/MP:P16", "/MP:P17", "/MP:P18", "/MP:P19", "/MP:P20", "/MP:P21", "/MP:P22", "/CISD/CL-3V:A01",
                    "/CISD/CL-3V:A02", "/CISD/B1B3:B01", "/CISD/B1B3:B03", "/CISD/C1:C01", "/CISD/C2:C02",
                    "/CISD/C3:C03", "/CISD/MP1-MIXED:A01", "/CISD/MP1-MIXED:A02", "/CISD/MP1-MIXED:A03",
                    "/CISD/MP1-MIXED:B02", "/CISD/MP2-NO-CL:A01", "/CISD/MP2-NO-CL:A02", "/CISD/MP2-NO-CL:A03",
                    "/CISD/MP2-NO-CL:B02", "/CISD/MP2-NO-CL:C03", "/CISD/PLATE_WELLSEARCH:WELL-A01",
                    "/CISD/PLATE_WELLSEARCH:WELL-A02"
            };

    @Autowired
    protected ISQLExecutor sqlExecutor;

    @Test
    public void testSearchWhichReturnsSharedSamplesForSpaceUser()
    {
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withCode().thatEndsWith("P");
        testSearch(TEST_SPACE_USER, sampleSearchCriteria, "/DP", "/MP");
    }

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SamplePermId("200902091219327-1025"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithMultipleIds()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SamplePermId("200902091250077-1026"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithIdentifierThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatEquals("/CISD/CP-TEST-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatEquals("/CISD/CP-TEST-*");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withIdentifier().thatEquals("/CISD/CP-*-1");
        testSearch(TEST_USER, criteria3, "/CISD/CP-TEST-1");

        SampleSearchCriteria criteria4 = new SampleSearchCriteria();
        criteria4.withIdentifier().thatEquals("/*/CP-TEST-*");
        testSearch(TEST_USER, criteria4, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithIdentifierThatStartsWith()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatStartsWith("/CISD/CP-TEST");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatStartsWith("/CISD/*-test");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithIdentifierThatEndsWith()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatEndsWith("-TEST-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatEndsWith("-TEST-*");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/TEST-SPACE/CP-TEST-4", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithIdentifierThatContains()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatContains("CP-TEST");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/TEST-SPACE/CP-TEST-4");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatContains("CISD*-TEST");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("RP1-A2X");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X");
    }

    @Test
    public void testSearchWithCodeThatIsLessOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsLessThanOrEqualTo("3VCP5");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126", "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithCodeThatIsLessThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsLessThan("3VCP5");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithCodeThatIsGreaterThanOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsGreaterThanOrEqualTo("WELL-A01");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithCodeThatIsGreaterThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsGreaterThan("WELL-A01");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithPropertyThatIsLessThanOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsLessThanOrEqualTo("test comment");
        testSearch(TEST_USER, criteria, "/CISD/3VCP7", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithPropertyThatIsLessThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsLessThan("test comment");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");
    }

    @Test
    public void testSearchWithPropertyThatIsGreaterThanOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsGreaterThanOrEqualTo("test comment");
        testSearch(TEST_USER, criteria, "/CISD/3VCP7", "/CISD/CP-TEST-1", "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithPropertyThatIsGreaterThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsGreaterThan("test comment");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withProperty(propertyType.getPermId()).thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodes()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("RP1-A2X", "RP1-B1X"));
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithCodesEmpty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList());
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithCodesWithNull()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList(new String[] { null }));
        criteria.withOrOperator();
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithCodesWithObjectArray()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        CodesSearchCriteria codesSearchCriteria = criteria.withCodes();
        List codes = new ArrayList();
        codes.add("RP1-A2X");
        codesSearchCriteria.setFieldValue(codes);
        criteria.withOrOperator();
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X");
    }

    @Test
    public void testSearchWithCodesEmptyAndOther()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList());
        criteria.withCode().thatEquals("RP1-A2X");
        criteria.withOrOperator();
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X");
    }

    @Test
    public void testSearchWithCodeThatEqualsWithStarWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("RP1-*X");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithCodeThatEqualsWithQuestionMarkWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("CP???1");
        testSearch(TEST_USER, criteria, "/CISD/CP1-A1", "/CISD/CP1-B1", "/CISD/CP2-A1");
    }

    @Test
    public void testSearchWithCodeThatStartsWithStarWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("PLATE_WELLSEARCH:W*L-");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithCodeThatStartsWithQuestionMarkWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("CP?-");
        testSearch(TEST_USER, criteria, "/CISD/CP1-A1", "/CISD/CP1-A2", "/CISD/CP1-B1", "/CISD/CP2-A1");
    }

    @Test
    public void testSearchWithCodeThatEndsWithStarWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEndsWith("NOR*L");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-PARENT-NORMAL");
    }

    @Test
    public void testSearchWithCodeThatEndsWithQuestionMarkWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEndsWith("-??2");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchForAll()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        testSearch(TEST_USER, criteria, 701);
    }

    @Test
    public void testSearchForAllSpaceSamples()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode();
        testSearch(TEST_USER, criteria, 379);
    }

    @Test
    public void testSearchForAllSharedSamples()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria().withoutSpace();
        testSearch(TEST_USER, criteria, 322);
    }

    @Test
    public void testSearchWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criteria, 1);

        criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithCodeInContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("PLATE_WELLSEARCH:WELL-A01");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithTypeIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("REINFECT_PLATE"));
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withCode().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithCodeWithWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withCode().thatEquals("REINFECT_PLAT*");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withSemanticAnnotations().withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 6, samples.toString());
        for (Sample sample : samples)
        {
            assertEquals(sample.getType().getCode(), "DILUTION_PLATE");
        }
    }

    @Test
    public void testSearchWithTypeWithSemanticAnnotationsWithIdOrIdAndWithSampleCodeThatContains()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("P2");

        SemanticAnnotationSearchCriteria annotationCriteria = criteria.withType().withSemanticAnnotations();
        annotationCriteria.withOrOperator();
        annotationCriteria.withId().thatEquals(new SemanticAnnotationPermId("ST_MASTER_PLATE"));
        annotationCriteria.withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 2, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertContains("P2", sample.getCode());
            AssertionUtil.assertCollectionContains(Arrays.asList("MASTER_PLATE", "DILUTION_PLATE"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationIsDefinedAtPropertyAssignment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("ST_CELL_PLATE_PT_ORGANISM"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 14, samples.toString());
        for (Sample sample : samples)
        {
            assertEquals(sample.getType().getCode(), "CELL_PLATE");
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationIsDefinedAtPropertyTypeLevelOnly()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_ORGANISM"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 2, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContains(Arrays.asList("DELETION_TEST", "NORMAL"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithPropertyTypeWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withPropertyType().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_DESCRIPTION"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 11, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContains(Arrays.asList("MASTER_PLATE", "CONTROL_LAYOUT", "DELETION_TEST"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPermId().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithExperiment()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A01");
        criteria.withExperiment();
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithoutExperiment()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A01");
        criteria.withoutExperiment();
        testSearch(TEST_USER, criteria, "/CISD/CL1:A01", "/CISD/MP2-NO-CL:A01", "/CISD/CL-3V:A01", "/CISD/MP1-MIXED:A01");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP10"));
        testSearch(TEST_USER, criteria, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(new ExperimentPermId("200811050952663-1029"));
        testSearch(TEST_USER, criteria, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withPermId().thatEquals("200811050952663-1029");
        testSearch(TEST_USER, criteria, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withCode().thatEquals("EXP-TEST-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withId().thatEquals(new EntityTypePermId("COMPOUND_HCS"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withCode().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withPermId().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withCode().thatEquals("NOE");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-2", "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("/TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithParent()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = search(sessionToken, criteria, new SampleFetchOptions());
        final Set<String> actualSet = samples.stream().
                map(sample -> sample.getIdentifier().getIdentifier()).
                collect(Collectors.toSet());

        assertCollectionContainsAtLeast(actualSet, "/CISD/3V-125", "/CISD/3V-126", "/CISD/3VCP5", "/CISD/3VCP6",
                "/CISD/3VCP7", "/CISD/3VCP8", "/CISD/CL-3V:A02", "/CISD/CP-TEST-1", "/CISD/CP1-A1",
                "/CISD/CP1-A2", "/CISD/CP1-B1", "/CISD/CP2-A1", "/CISD/DP1-A", "/CISD/DP1-B", "/CISD/DP2-A",
                "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X", "/DP", "/TEST-SPACE/EV-INVALID");
        assertCollectionDoesntContain(actualSet, "/CISD/MP1-MIXED");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithChildren()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = search(sessionToken, criteria, new SampleFetchOptions());
        final Set<String> actualSet = samples.stream().
                map(sample -> sample.getIdentifier().getIdentifier()).
                collect(Collectors.toSet());

        assertCollectionContainsAtLeast(actualSet, "/CISD/3V-125", "/CISD/CL-3V:A02", "/CISD/CL1:A03",
                "/CISD/CP-TEST-2", "/CISD/CP1-A2", "/CISD/CP1-B1", "/CISD/CP2-A1", "/CISD/DP1-A", "/CISD/DP1-B",
                "/CISD/DP2-A", "/CISD/MP002-1", "/CISD/MP1-MIXED", "/CISD/MP2-NO-CL", "/MP", "/TEST-SPACE/EV-PARENT");
        assertCollectionDoesntContain(actualSet, "/CISD/B1B3", "/CISD/C1", "/CISD/C2", "/CISD/C3", "/CISD/CL-3V");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithParentWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(new SampleIdentifier("/CISD/MP002-1"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(new SamplePermId("200811050917877-331"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withPermId().thatEquals("200811050917877-331");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withCode().thatEquals("MP002-1");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withId().thatEquals(new SampleIdentifier("/CISD/3VCP6"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withId().thatEquals(new SamplePermId("200811050946559-980"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withPermId().thatEquals("200811050946559-980");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withCode().thatEquals("3VCP6");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithCodeAndWithContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("WELL");
        criteria.withContainer();
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithContainer()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer();
        testSearch(TEST_USER, criteria, ALL_SAMPLE_IDS_WITH_CONTAINER);
    }

    @Test
    public void testSearchWithoutContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A1");
        criteria.withoutContainer();
        testSearch(TEST_USER, criteria, "/CISD/CP1-A1", "/CISD/CP2-A1", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithContainerWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(new SamplePermId("200811050924274-994"));
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(new SampleIdentifier("/CISD/B1B3"));
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withPermId().thatEquals("200811050924274-994");
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withCode().thatEquals("B1B3");
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithIdSetToCodeId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagCode("TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermIdUnauthorized()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withCode().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, 15);
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        final Long count = (Long) sqlExecutor.execute("SELECT count(*) FROM samples WHERE modification_timestamp::DATE = ?::DATE",
                Arrays.asList("2009-08-18")).get(0).get("count");

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModificationDate().thatEquals("2009-08-18");
        testSearch(TEST_USER, criteria, Math.toIntExact(count));
    }

    @Test
    public void testSearchWithAnyFieldMatchingProperty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("\"very advanced stuff\"");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithAnyFieldMatchingRegistrationDateWithVariousFormats()
    {
        final SampleSearchCriteria shortFormatCriteria = new SampleSearchCriteria();
        shortFormatCriteria.withAnyField().thatEquals("2009-02-09");
        testSearch(TEST_USER, shortFormatCriteria, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1", "/TEST-SPACE/FV-TEST",
                "/TEST-SPACE/EV-TEST", "/TEST-SPACE/EV-INVALID", "/TEST-SPACE/EV-NOT_INVALID", "/TEST-SPACE/EV-PARENT",
                "/TEST-SPACE/EV-PARENT-NORMAL", "/TEST-SPACE/SAMPLE-TO-DELETE", "/CISD/CP-TEST-2",
                "/CISD/PLATE_WELLSEARCH", "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02",
                "/TEST-SPACE/CP-TEST-4", "/CISD/CP-TEST-3");

        final SampleSearchCriteria mediumFormatCriteria = new SampleSearchCriteria();
        mediumFormatCriteria.withAnyField().thatEquals("2009-02-09 12:09");
        testSearch(TEST_USER, mediumFormatCriteria, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1", "/TEST-SPACE/FV-TEST",
                "/TEST-SPACE/EV-TEST", "/TEST-SPACE/EV-INVALID", "/TEST-SPACE/EV-NOT_INVALID", "/TEST-SPACE/EV-PARENT",
                "/TEST-SPACE/EV-PARENT-NORMAL", "/TEST-SPACE/SAMPLE-TO-DELETE", "/CISD/CP-TEST-2",
                "/CISD/PLATE_WELLSEARCH", "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02",
                "/TEST-SPACE/CP-TEST-4");

        final SampleSearchCriteria longFormatCriteria = new SampleSearchCriteria();
        longFormatCriteria.withAnyField().thatEquals("2009-02-09 12:09:50");
        testSearch(TEST_USER, longFormatCriteria, "/CISD/CP-TEST-2", "/CISD/PLATE_WELLSEARCH",
                "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02", "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithAnyFieldMatchingRegistrationDateWithVariousCriteriaKinds()
    {
        final SampleSearchCriteria equalsCriteria = new SampleSearchCriteria();
        equalsCriteria.withAnyField().thatEquals("2009-02-09");
        testSearch(TEST_USER, equalsCriteria, 15);

        final SampleSearchCriteria startsWithCriteria = new SampleSearchCriteria();
        startsWithCriteria.withAnyField().thatStartsWith("2009-02-09");
        testSearch(TEST_USER, startsWithCriteria, 15);

        final SampleSearchCriteria endsWithCriteria = new SampleSearchCriteria();
        endsWithCriteria.withAnyField().thatEndsWith("2009-02-09");
        testSearch(TEST_USER, endsWithCriteria, 15);

        final SampleSearchCriteria containsCriteria = new SampleSearchCriteria();
        containsCriteria.withAnyField().thatContains("2009-02-09");
        testSearch(TEST_USER, containsCriteria, 15);
    }

    @Test
    public void testSearchWithAnyFieldMatchingModifiersUserId()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("test_role");
        testSearch(TEST_USER, criteria, "/CISD/CL1", "/CISD/CP-TEST-1", "/TEST-SPACE/FV-TEST");
    }

    @Test
    public void testSearchWithAnyFieldMatchingAttribute()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("\"CP-TEST-2\"");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("/CISD/CP-TEST-*");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withAnyField().thatStartsWith("/CISD/DYNA");
        testSearch(TEST_USER, criteria2, "/CISD/DYNA-TEST-1");

        SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withAnyField().thatEndsWith("-1");
        testSearch(TEST_USER, criteria3, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1", "/CISD/MP002-1");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("\"very advanced\"");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithAnyProperty2()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyProperty().thatEquals("very");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        List<Sample> samples = search(sessionToken, criteria, fo);

        for (Sample sample : samples)
        {
            System.out.println("-----");
            System.out.println(sample.getCode());
            for (Entry<String, String> entry : sample.getProperties().entrySet())
            {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("-----");
        }

        System.out.println(samples);
        v3api.logout(sessionToken);

    }

    @Test
    public void testSearchWithAndOperator()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatStartsWith("CP");
        criteria.withCode().thatEndsWith("-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithCachingNoCache()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.cacheMode(CacheMode.NO_CACHE);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        v3api.logout(sessionToken);

        assertEquals(samples1.get(0).getPermId(), samples2.get(0).getPermId());
        assertEquals(samples1.get(1).getPermId(), samples2.get(1).getPermId());

        assertNotSame(samples1.get(0), samples2.get(0));
        assertNotSame(samples1.get(1), samples2.get(1));
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-2"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-3"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        fo.sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/CP-TEST-3", "/CISD/CP-TEST-2", "/CISD/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/CP-TEST-4"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/3V-125"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().identifier().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/CP-TEST-1", "/TEST-SPACE/CP-TEST-4");

        fo.sortBy().identifier().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/TEST-SPACE/CP-TEST-4", "/CISD/CP-TEST-1", "/CISD/3V-125");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/CP-TEST-4"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/3V-125"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CP-TEST-1", "/TEST-SPACE/CP-TEST-4", "/CISD/3V-125");

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/TEST-SPACE/CP-TEST-4", "/CISD/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByPropertyWithTextValues()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("COMMENT").asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertEquals(samples1.get(0).getProperty("COMMENT"), "extremely simple stuff");
        assertEquals(samples1.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples1.get(2).getProperty("COMMENT"), "very advanced stuff");

        fo.sortBy().property("COMMENT").desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertEquals(samples2.get(0).getProperty("COMMENT"), "very advanced stuff");
        assertEquals(samples2.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples2.get(2).getProperty("COMMENT"), "extremely simple stuff");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByPropertyWithIntegerValues()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");
        criteria.withPermId().thatEquals("200811050946559-981");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("SIZE").asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertEquals(samples1.get(0).getProperty("SIZE"), "123");
        assertEquals(samples1.get(1).getProperty("SIZE"), "321");
        assertEquals(samples1.get(2).getProperty("SIZE"), "666");
        assertEquals(samples1.get(3).getProperty("SIZE"), "4711");

        fo.sortBy().property("SIZE").desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertEquals(samples2.get(0).getProperty("SIZE"), "4711");
        assertEquals(samples2.get(1).getProperty("SIZE"), "666");
        assertEquals(samples2.get(2).getProperty("SIZE"), "321");
        assertEquals(samples2.get(3).getProperty("SIZE"), "123");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByRegistrationDate()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050924274-995");
        criteria.withPermId().thatEquals("200811050927630-1004");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().registrationDate().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/B1B3:B01", "/CISD/MP1-MIXED:A01");

        fo.sortBy().registrationDate().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/MP1-MIXED:A01", "/CISD/B1B3:B01");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByModificationDate()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1053");
        criteria.withPermId().thatEquals("200811050928301-1012");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().modificationDate().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/MP2-NO-CL:B02", "/CISD/DYNA-TEST-1");

        fo.sortBy().modificationDate().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/DYNA-TEST-1", "/CISD/MP2-NO-CL:B02");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByMultipleFields()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050919915-9");
        criteria.withPermId().thatEquals("200811050944030-974");
        criteria.withPermId().thatEquals("200811050924274-995");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        fo.sortBy().registrationDate().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CL1:A01", "/CISD/CL-3V:A01", "/CISD/B1B3:B01");

        fo.sortBy().code().asc();
        fo.sortBy().registrationDate().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/CL-3V:A01", "/CISD/CL1:A01", "/CISD/B1B3:B01");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingTopLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        fo.sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/CP-TEST-3", "/CISD/CP-TEST-2", "/CISD/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/3VCP5", "/CISD/3VCP6", "/CISD/3VCP7", "/CISD/3VCP8");
        assertSampleIdentifiersInOrder(samples1.get(1).getChildren(), "/CISD/DP1-A", "/CISD/DP1-B");

        fo.withChildren().sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/3VCP8", "/CISD/3VCP7", "/CISD/3VCP6", "/CISD/3VCP5");
        assertSampleIdentifiersInOrder(samples2.get(1).getChildren(), "/CISD/DP1-B", "/CISD/DP1-A");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingTopLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();

        fo.from(0).count(1);
        final SearchResult<Sample> searchResult1 = v3api.searchSamples(sessionToken, criteria, fo);
        assertEquals(searchResult1.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult1.getObjects(), "/CISD/CP-TEST-1");

        fo.from(1).count(1);
        final SearchResult<Sample> searchResult2 = v3api.searchSamples(sessionToken, criteria, fo);
        assertEquals(searchResult2.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult2.getObjects(), "/CISD/CP-TEST-2");

        fo.from(2).count(1);
        final SearchResult<Sample> searchResult3 = v3api.searchSamples(sessionToken, criteria, fo);
        assertEquals(searchResult3.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult3.getObjects(), "/CISD/CP-TEST-3");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        fo.withChildren().from(0).count(1);
        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/3VCP5");
        assertSampleIdentifiersInOrder(samples1.get(1).getChildren(), "/CISD/DP1-A");

        fo.withChildren().from(1).count(1);
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/3VCP6");
        assertSampleIdentifiersInOrder(samples2.get(1).getChildren(), "/CISD/DP1-B");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingTopAndSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        fo.from(0).count(1);
        fo.withChildren().from(0).count(1);
        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/3VCP5");

        fo.from(1).count(1);
        fo.withChildren().from(1).count(1);
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/DP1-B");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchNumeric()
    {
        // SIZE: 4711 CODE: 3VCP7
        // SIZE: 123 CODE: CP-TEST-1
        // SIZE: 321 CODE: CP-TEST-2
        // SIZE: 666 CODE: CP-TEST-3

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions sortByCodeFO = new SampleFetchOptions();
        sortByCodeFO.sortBy().code().asc();
        sortByCodeFO.withProperties();

        // Greater or Equals - Giving integer as real
        SampleSearchCriteria criteriaGOE = new SampleSearchCriteria();
        criteriaGOE.withNumberProperty("SIZE").thatIsGreaterThanOrEqualTo(321.0);
        List<Sample> samplesGOE = search(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE, "/CISD/3VCP7", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        // Greater - Giving integer as real
        SampleSearchCriteria criteriaG = new SampleSearchCriteria();
        criteriaG.withNumberProperty("SIZE").thatIsGreaterThan(321.0);
        List<Sample> samplesG = search(sessionToken, criteriaG, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesG, "/CISD/3VCP7", "/CISD/CP-TEST-3");

        // Equals As Text - Real
        SampleSearchCriteria criteriaETxt2 = new SampleSearchCriteria();
        criteriaETxt2.withProperty("SIZE").thatEquals("666.0");
        List<Sample> samplesETxt2 = search(sessionToken, criteriaETxt2, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesETxt2, "/CISD/CP-TEST-3");

        // Equals As Text - Integer
        SampleSearchCriteria criteriaETxt = new SampleSearchCriteria();
        criteriaETxt.withProperty("SIZE").thatEquals("666");
        List<Sample> samplesETxt = search(sessionToken, criteriaETxt, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesETxt, "/CISD/CP-TEST-3");

        // Equals
        SampleSearchCriteria criteriaE = new SampleSearchCriteria();
        criteriaE.withNumberProperty("SIZE").thatEquals(666);
        List<Sample> samplesE = search(sessionToken, criteriaE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesE, "/CISD/CP-TEST-3");

        // Less
        SampleSearchCriteria criteriaL = new SampleSearchCriteria();
        criteriaL.withNumberProperty("SIZE").thatIsLessThan(666);
        List<Sample> samplesL = search(sessionToken, criteriaL, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesL, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");

        // Less or Equals
        SampleSearchCriteria criteriaLOE = new SampleSearchCriteria();
        criteriaLOE.withNumberProperty("SIZE").thatIsLessThanOrEqualTo(321);
        List<Sample> samplesLOE = search(sessionToken, criteriaLOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesLOE, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchListableOnlyShouldNotFindUnlistable()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("EV");
        List<Sample> samples = search(sessionToken, criteria, fo);
        List<String> identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-NOT_INVALID, "
                + "/TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST]");
        criteria.withType().withListable().thatEquals(true);

        samples = search(sessionToken, criteria, fo);

        identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, "
                + "/TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST]");
    }

    @Test
    public void testSearchUnlistableOnlyShouldFindListable()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("EV");
        List<Sample> samples = search(sessionToken, criteria, fo);
        List<String> identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-NOT_INVALID, "
                + "/TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST]");
        criteria.withType().withListable().thatEquals(false);

        samples = search(sessionToken, criteria, fo);

        identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-NOT_INVALID]");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/EV-TEST"));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            testSearch(user.getUserId(), criteria, "/TEST-SPACE/EV-TEST");
        } else
        {
            testSearch(user.getUserId(), criteria);
        }
    }

    @Test
    public void testSearchWithAnyFieldMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyField().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAnyPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyProperty().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria c = new SampleSearchCriteria();
        c.withCode().thatEquals("RP1-A2X");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpace();
        fo.withProject();

        v3api.searchSamples(sessionToken, c, fo);

        assertAccessLog(
                "search-samples  SEARCH_CRITERIA:\n'SAMPLE\n    with attribute 'code' equal to 'RP1-A2X'\n'\nFETCH_OPTIONS:\n'Sample\n    with Project\n    with Space\n'");
    }

    private void testSearch(String user, SampleSearchCriteria criteria, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Sample> samples = search(sessionToken, criteria, new SampleFetchOptions());
        assertSampleIdentifiers(samples, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, SampleSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Sample> samples = search(sessionToken, criteria, new SampleFetchOptions());
        assertEquals(samples.size(), expectedCount);
        v3api.logout(sessionToken);
    }

    private List<Sample> search(String sessionToken, SampleSearchCriteria criteria, SampleFetchOptions fetchOptions)
    {
        SearchResult<Sample> searchResult =
                v3api.searchSamples(sessionToken, criteria, fetchOptions);
        return searchResult.getObjects();
    }

    private List<String> extractIndentifiers(List<Sample> samples)
    {
        List<String> identifiers = new ArrayList<>();
        for (Sample sample : samples)
        {
            identifiers.add(sample.getIdentifier().getIdentifier());
        }
        return identifiers;
    }

}
