/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.proteomics.systemtests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = { "slow", "systemtest" })
public class ProteomicsDataServiceTest extends AbstractProteomicsSystemTestCase
{
    private static final String EXPERIMENT_IDENTIFIER = "/MS_DATA/A/E";

    private String sessionToken;

    private Long experimentId;

    private long[] sampleIds;

    @BeforeTest
    public void setUpExamples()
    {
        sessionToken = authenticateAs("test");
        getCommonServer().registerSpace(sessionToken, "MS_DATA", null);
        getCommonServer().registerProject(sessionToken, ProjectIdentifierFactory.parse("/MS_DATA/A"),
                null, null, Arrays.<NewAttachment> asList());
        NewExperiment experiment = new NewExperiment();
        experiment.setExperimentTypeCode("MS_SEARCH");
        experiment.setIdentifier(EXPERIMENT_IDENTIFIER);
        experimentId = getGenericServer().registerExperiment(sessionToken, experiment, Arrays.<NewAttachment> asList()).getId();
        NewSamplesWithTypes samples = new NewSamplesWithTypes();
        SampleType sampleType = new SampleTypeBuilder().code("MS_INJECTION").getSampleType();
        samples.setEntityType(sampleType);
        List<NewSample> msInjectionSamples = createMsInjectionSamples("/TEST-SPACE/CP-TEST-4", null, "/CISD/3VCP5");
        samples.setNewEntities(msInjectionSamples);
        getGenericServer().registerSamples(sessionToken, Arrays.asList(samples));
        IServiceForDataStoreServer serviceForDSS = getServiceForDSS();
        for (int i = 0; i < msInjectionSamples.size(); i++)
        {
            NewSample sample = msInjectionSamples.get(i);
            NewExternalData dataSet = new NewExternalData();
            dataSet.setDataSetType(new DataSetTypeBuilder().code("UNKNOWN").getDataSetType());
            dataSet.setDataSetKind(DataSetKind.PHYSICAL);
            dataSet.setCode("DS-" + i);
            dataSet.setFileFormatType(new FileFormatType("XML"));
            dataSet.setDataStoreCode("STANDARD");
            dataSet.setLocation("a/b/c/" + dataSet.getCode());
            dataSet.setLocatorType(new LocatorType("RELATIVE_LOCATION"));
            dataSet.setStorageFormat(StorageFormat.PROPRIETARY);
            serviceForDSS.registerDataSet(sessionToken, SampleIdentifierFactory.parse(sample),
                    dataSet);
        }
        ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(experimentId));
        List<Sample> samples2 = getCommonServer().listSamples(sessionToken, criteria);
        sampleIds = new long[samples2.size()];
        for (int i = 0; i < sampleIds.length; i++)
        {
            sampleIds[i] = samples2.get(i).getId();
        }
        Arrays.sort(sampleIds);
    }

    @Test
    public void testListRawDataSamplesForAdminUser()
    {
        List<MsInjectionDataInfo> samples = getDataService().listRawDataSamples(sessionToken, "test");

        assertEquals("MSI-0:/TEST-SPACE/CP-TEST-4:/TEST-SPACE/NOE/EXP-TEST-2, "
                + "MSI-2:/CISD/3VCP5:/CISD/NEMO/EXP10", renderMsInjectionDataInfos(samples));
    }

    @Test
    public void testListRawDataSamplesForSpaceUser()
    {
        List<MsInjectionDataInfo> samples = getDataService().listRawDataSamples(sessionToken, "test_role");

        assertEquals("MSI-2:/CISD/3VCP5:/CISD/NEMO/EXP10", renderMsInjectionDataInfos(samples));
    }

    @Test
    public void testListAllRawDataSamplesForAdminUser()
    {
        List<MsInjectionDataInfo> samples = getDataService().listAllRawDataSamples(sessionToken, "test");

        assertEquals("MSI-0:/TEST-SPACE/CP-TEST-4:/TEST-SPACE/NOE/EXP-TEST-2, "
                + "MSI-2:/CISD/3VCP5:/CISD/NEMO/EXP10", renderMsInjectionDataInfos(samples));
    }

    @Test
    public void testListAllRawDataSamplesForSpaceUser()
    {
        List<MsInjectionDataInfo> samples = getDataService().listAllRawDataSamples(sessionToken, "test_role");

        assertEquals("MSI-0:null:null, "
                + "MSI-2:/CISD/3VCP5:/CISD/NEMO/EXP10", renderMsInjectionDataInfos(samples));
    }

    @Test
    public void testProcessingRawDataForAdminUser()
    {
        try
        {
            getDataService().processingRawData(sessionToken, "test", "test-processing", sampleIds, "UNKNOWN");
        } catch (EnvironmentFailureException ex)
        {
            AssertionUtil.assertContains("[DS-0, DS-2]", ex.getMessage());
        }
    }

    @Test
    public void testProcessingRawDataForSpaceUser()
    {
        try
        {
            getDataService().processingRawData(sessionToken, "test_role", "test-processing", sampleIds, "UNKNOWN");
        } catch (EnvironmentFailureException ex)
        {
            AssertionUtil.assertContains("[DS-2]", ex.getMessage());
        }
    }

    private String renderMsInjectionDataInfos(List<MsInjectionDataInfo> infos)
    {
        Collections.sort(infos, new SimpleComparator<MsInjectionDataInfo, String>()
            {
                @Override
                public String evaluate(MsInjectionDataInfo item)
                {
                    return item.getMsInjectionSampleCode();
                }
            });
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (MsInjectionDataInfo info : infos)
        {
            builder.append(info.getMsInjectionSampleCode() + ":" + info.getBiologicalSampleIdentifier() + ":"
                    + info.getBiologicalExperimentIdentifier());
        }
        return builder.toString();
    }

    private List<NewSample> createMsInjectionSamples(String... parentIdentifiers)
    {
        List<NewSample> samples = new ArrayList<NewSample>();
        SampleType sampleType = new SampleTypeBuilder().code("MS_INJECTION").getSampleType();
        for (int i = 0; i < parentIdentifiers.length; i++)
        {
            NewSample sample = new NewSample();
            sample.setParentIdentifier(parentIdentifiers[i]);
            sample.setIdentifier("/MS_DATA/MSI-" + i);
            sample.setSampleType(sampleType);
            sample.setExperimentIdentifier(EXPERIMENT_IDENTIFIER);
            samples.add(sample);
        }
        return samples;
    }
}
