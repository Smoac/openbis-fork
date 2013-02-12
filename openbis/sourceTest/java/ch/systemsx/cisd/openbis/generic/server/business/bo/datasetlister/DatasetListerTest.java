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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityListingQueryTest;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class })
@Test(groups =
    { "db", "dataset" })
public class DatasetListerTest extends AbstractDAOTest
{
    private IDatasetLister lister;

    private SamplePE exampleSample;

    private long exampleSampleId;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DatasetListerDAO datasetListerDAO =
                DatasetListingQueryTest.createDatasetListerDAO(daoFactory);
        SecondaryEntityDAO secondaryEntityDAO =
                SecondaryEntityListingQueryTest.createSecondaryEntityDAO(daoFactory);
        lister = DatasetLister.create(datasetListerDAO, secondaryEntityDAO, "url", null);
        exampleSample =
                DatasetListingQueryTest.getSample("CISD", "CP-TEST-1",
                        datasetListerDAO.getDatabaseInstanceId(), daoFactory);
        exampleSampleId = exampleSample.getId();
    }

    @Test
    public void testListBySampleTechIdDirect()
    {
        List<ExternalData> datasets = lister.listBySampleTechId(new TechId(exampleSampleId), true);
        assertEquals(1, datasets.size());
        ExternalData externalData = datasets.get(0);
        assertEquals(exampleSampleId, externalData.getSample().getId().longValue());
        assertFalse(externalData.getProperties().isEmpty());
        assertNotNull(externalData.getExperiment());
    }

    @Test
    public void testListBySampleTechIdIndirect()
    {
        List<ExternalData> indirectlyConnectedChildDatasets =
                lister.listBySampleTechId(new TechId(exampleSampleId), false);
        assertEquals(5, indirectlyConnectedChildDatasets.size());
        System.err.println(Code.extractCodes(indirectlyConnectedChildDatasets).toString());

        assertNotNull(exampleSample.getGeneratedFrom());
        List<ExternalData> directlyConnectedParentDatasets =
                lister.listBySampleTechId(new TechId(exampleSample.getGeneratedFrom()), true);
        assertEquals(1, directlyConnectedParentDatasets.size());

        List<ExternalData> indirectlyConnectedParentDatasets =
                lister.listBySampleTechId(new TechId(exampleSample.getGeneratedFrom()), false);
        assertEquals(6, indirectlyConnectedParentDatasets.size());

        List<String> indirectlyConnectedParentDatasetCodes =
                Code.extractCodes(indirectlyConnectedParentDatasets);
        for (ExternalData childDataset : indirectlyConnectedChildDatasets)
        {
            assertTrue(childDataset.getCode() + " not found among "
                    + indirectlyConnectedParentDatasetCodes.toString(),
                    indirectlyConnectedParentDatasetCodes.contains(childDataset.getCode()));
        }
    }

    @Test
    public void testListParents()
    {
        Map<Long, Set<Long>> map = lister.listParentIds(Arrays.<Long> asList(2L, 8L, 9L));

        assertEquals(null, map.get(2L));
        assertEquals("[7]", map.get(8L).toString());
        List<Long> list = new ArrayList<Long>(map.get(9L));
        Collections.sort(list);
        assertEquals("[5, 6, 7]", list.toString());
    }

    @Test
    public void testListAllDataSetsFor()
    {
        HashSet<String> samplePermIDs =
                new HashSet<String>(Arrays.asList("200902091250077-1026", "200902091225616-1027"));
        List<SamplePE> samplePEs = daoFactory.getSampleDAO().listByPermID(samplePermIDs);
        List<Sample> samples =
                SampleTranslator.translate(samplePEs, "", new HashMap<Long, Set<Metaproject>>(),
                        new ManagedPropertyEvaluatorFactory(null, null));

        Map<Sample, List<ExternalData>> dataSets = lister.listAllDataSetsFor(samples);

        StringBuilder builder = new StringBuilder();
        for (Sample sample : samples)
        {
            builder.append(sample.getCode());
            appendChildren(builder, dataSets.get(sample), "  ");
            builder.append('\n');
        }
        assertEquals("CP-TEST-3\n  20081105092159333-3 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "    20081105092259000-8 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "    20081105092259000-9 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "      20081105092259900-0 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "        20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "      20081105092259900-1 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "        20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "  20110805092359990-17 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "    20081105092259000-18 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "CP-TEST-2\n  20081105092159222-2 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "    20081105092259000-9 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "      20081105092259900-0 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "        20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "      20081105092259900-1 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "        20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n",
                builder.toString());
        Map<String, ExternalData> dataSetsByCode = new HashMap<String, ExternalData>();
        for (Sample sample : samples)
        {
            List<ExternalData> rootDataSets = dataSets.get(sample);
            assertSameDataSetsForSameCode(dataSetsByCode, rootDataSets);
        }
    }

    @Test
    public void testListDataSetsByCode()
    {
        // 1st is deleted, 2nd has fake code, 3rd & 4th are ok
        List<ExternalData> dataSets =
                lister.listByDatasetCode(Arrays.asList("20081105092158673-1", "blabla",
                        "20081105092159111-1", "20081105092159188-3"));

        Collections.sort(dataSets, new Comparator<ExternalData>()
            {
                @Override
                public int compare(ExternalData o1, ExternalData o2)
                {
                    return (int) (o1.getId() - o2.getId());
                }
            });
        DataSet dataSet0 = dataSets.get(0).tryGetAsDataSet();
        assertEquals(4L, dataSet0.getId().longValue());
        assertEquals("20081105092159188-3", dataSet0.getCode());
        assertEquals("analysis/result", dataSet0.getLocation());
        assertEquals(null, dataSet0.getSize());
        assertEquals(Constants.DEFAULT_SPEED_HINT, dataSet0.getSpeedHint());

        DataSet dataSet1 = dataSets.get(1).tryGetAsDataSet();
        assertEquals(5L, dataSet1.getId().longValue());
        assertEquals("20081105092159111-1", dataSet1.getCode());
        assertEquals("a/1", dataSet1.getLocation());
        assertEquals(4711L, dataSet1.getSize().longValue());
        assertEquals(42, dataSet1.getSpeedHint());

        assertEquals(2, dataSets.size());
    }

    @Test
    public void testListByTrackingCriteriaWithNoSample()
    {
        List<ExternalData> dataSets = lister.listByTrackingCriteria(new TrackingDataSetCriteria(9));
        Collections.sort(dataSets, new Comparator<ExternalData>()
            {
                @Override
                public int compare(ExternalData o1, ExternalData o2)
                {
                    return (int) (o1.getId() - o2.getId());
                }
            });
        assertEquals("20081105092259900-0", dataSets.get(0).getCode());
        assertEquals("STANDARD", dataSets.get(0).getDataStore().getCode());
        assertEquals(0, dataSets.get(0).getProperties().size());
        assertEquals(24, dataSets.size());
    }

    @Test
    public void testListByTrackingCriteriaWithSampleType()
    {
        List<ExternalData> dataSets =
                lister.listByTrackingCriteria(new TrackingDataSetCriteria("CELL_PLATE", 6));
        Collections.sort(dataSets, new Comparator<ExternalData>()
            {
                @Override
                public int compare(ExternalData o1, ExternalData o2)
                {
                    return (int) (o1.getId() - o2.getId());
                }
            });
        assertEquals("20081105092159333-3", dataSets.get(0).getCode());
        assertEquals("STANDARD", dataSets.get(0).getDataStore().getCode());
        assertEquals("no comment", dataSets.get(0).getProperties().get(0).tryGetOriginalValue());
        assertEquals(1, dataSets.get(0).getProperties().size());
        assertEquals(2, dataSets.size());
    }

    @Test
    public void testListByDataSetIdsWithContainerDataSet()
    {
        final Long containerId = 13L;
        final Long containedId = 15L;
        List<ExternalData> datasets =
                lister.listByDatasetIds(Arrays.asList(containerId, containedId));
        assertEquals(2, datasets.size());
        ContainerDataSet containerDataSet = datasets.get(0).tryGetAsContainerDataSet();
        assertNotNull(containerDataSet);
        assertEquals(2, containerDataSet.getContainedDataSets().size());

        DataSet dataset1 = datasets.get(1).tryGetAsDataSet();
        assertNotNull(dataset1);
        assertEquals(2, (int) dataset1.getOrderInContainer());
        assertEquals(dataset1.tryGetContainer(), containerDataSet);
    }

    @Test
    public void testListByDataSetIdsWithLinkDataSets()
    {
        final Long ds1Id = 23L;
        final Long ds2Id = 24L;
        final Long ds3Id = 25L;

        List<ExternalData> datasets = lister.listByDatasetIds(Arrays.asList(ds1Id, ds2Id, ds3Id));

        assertEquals(3, datasets.size());
        assertTrue(datasets.get(0).isLinkData());
        assertTrue(datasets.get(1).isLinkData());
        assertTrue(datasets.get(2).isLinkData());
        assertNotNull(datasets.get(0).tryGetAsLinkDataSet());
        assertNotNull(datasets.get(1).tryGetAsLinkDataSet());
        assertNotNull(datasets.get(2).tryGetAsLinkDataSet());
        assertEquals("20120628092259000-23", datasets.get(0).getCode());
        assertEquals("20120628092259000-24", datasets.get(1).getCode());
        assertEquals("20120628092259000-25", datasets.get(2).getCode());
        assertNotNull(datasets.get(0).tryGetAsLinkDataSet().getExternalDataManagementSystem());
        assertNotNull(datasets.get(1).tryGetAsLinkDataSet().getExternalDataManagementSystem());
        assertNotNull(datasets.get(2).tryGetAsLinkDataSet().getExternalDataManagementSystem());
        assertEquals(1L, datasets.get(0).tryGetAsLinkDataSet().getExternalDataManagementSystem()
                .getId().longValue());
        assertEquals(1L, datasets.get(1).tryGetAsLinkDataSet().getExternalDataManagementSystem()
                .getId().longValue());
        assertEquals(2L, datasets.get(2).tryGetAsLinkDataSet().getExternalDataManagementSystem()
                .getId().longValue());
        assertEquals("DMS_1", datasets.get(0).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getCode());
        assertEquals("DMS_1", datasets.get(1).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getCode());
        assertEquals("DMS_2", datasets.get(2).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getCode());
        assertEquals("Test EDMS", datasets.get(0).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getLabel());
        assertEquals("Test EDMS", datasets.get(1).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getLabel());
        assertEquals("Test External openBIS instance", datasets.get(2).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getLabel());
        assertEquals("http://example.edms.pl/code=${code}", datasets.get(0).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getUrlTemplate());
        assertEquals("http://example.edms.pl/code=${code}", datasets.get(1).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getUrlTemplate());
        assertEquals("http://www.openbis.ch/perm_id=${code}", datasets.get(2).tryGetAsLinkDataSet()
                .getExternalDataManagementSystem().getUrlTemplate());
        assertFalse(datasets.get(0).tryGetAsLinkDataSet().getExternalDataManagementSystem()
                .isOpenBIS());
        assertFalse(datasets.get(1).tryGetAsLinkDataSet().getExternalDataManagementSystem()
                .isOpenBIS());
        assertTrue(datasets.get(2).tryGetAsLinkDataSet().getExternalDataManagementSystem()
                .isOpenBIS());
        assertEquals("CODE1", datasets.get(0).tryGetAsLinkDataSet().getExternalCode());
        assertEquals("CODE2", datasets.get(1).tryGetAsLinkDataSet().getExternalCode());
        assertEquals("CODE3", datasets.get(2).tryGetAsLinkDataSet().getExternalCode());
    }

    @Test
    public void testContainerParentPopulated()
    {
        final Long containedId = 15L;
        List<ExternalData> datasets = lister.listByDatasetIds(Arrays.asList(containedId));
        assertEquals(1, datasets.size());
        DataSet dataset = datasets.get(0).tryGetAsDataSet();
        assertNotNull(dataset);

        ContainerDataSet parent = dataset.tryGetContainer();
        assertNotNull(parent);
        assertEquals("20110509092359990-10", parent.getCode());
    }

    @Test
    public void testContainedDataSetzPopulated()
    {
        final Long containerId = 13L;
        List<ExternalData> datasets = lister.listByDatasetIds(Arrays.asList(containerId));
        assertEquals(1, datasets.size());
        ContainerDataSet containerDataSet = datasets.get(0).tryGetAsContainerDataSet();
        assertNotNull(containerDataSet);

        List<ExternalData> containedDataSets = containerDataSet.getContainedDataSets();
        assertEquals(2, containedDataSets.size());
        assertEquals("20110509092359990-11", containedDataSets.get(0).getCode());
        assertEquals("20110509092359990-12", containedDataSets.get(1).getCode());
    }

    @Test
    public void testListByDataStore()
    {
        List<ExternalData> list = lister.listByDataStore(1);

        Collections.sort(list, new Comparator<ExternalData>()
            {
                @Override
                public int compare(ExternalData o1, ExternalData o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
        // NOTE: deleted data set with id 2 is ommited
        ExternalData dataSet = list.get(0);
        assertEquals(5L, dataSet.getId().longValue());
        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("HCS_IMAGE", dataSet.getDataSetType().getCode());
        assertEquals("STANDARD", dataSet.getDataStore().getCode());
        assertEquals(1234178421646L, dataSet.getRegistrationDate().getTime());
        assertEquals(1225873319203L, dataSet.getProductionDate().getTime());
        assertEquals("EXP-TEST-1", dataSet.getExperiment().getCode());
        assertEquals("NEMO", dataSet.getExperiment().getProject().getCode());
        assertEquals("CISD", dataSet.getExperiment().getProject().getSpace().getCode());
        assertEquals("CISD", dataSet.getExperiment().getProject().getSpace().getInstance()
                .getCode());
        assertEquals("CP-TEST-1", dataSet.getSample().getCode());
        assertEquals("CISD", dataSet.getSample().getSpace().getCode());
        assertEquals(
                "[COMMENT: no comment, GENDER: FEMALE, BACTERIUM: BACTERIUM1 (BACTERIUM), ANY_MATERIAL: 1000_C (SIRNA)]",
                dataSet.getProperties().toString());
        assertEquals("a/1", ((DataSet) dataSet).getLocation());
        assertEquals("42", ((DataSet) dataSet).getShareId());
        assertEquals(4711L, ((DataSet) dataSet).getSize().longValue());
        assertEquals(DataSetArchivingStatus.AVAILABLE, ((DataSet) dataSet).getStatus());
        assertEquals(30, list.size());
    }

    @Test
    public void testListAllDataSetShareIdsByDataStore()
    {
        List<DataSetShareId> list = lister.listAllDataSetShareIdsByDataStore(1);

        Collections.sort(list, new Comparator<DataSetShareId>()
            {
                @Override
                public int compare(DataSetShareId o1, DataSetShareId o2)
                {
                    return o1.getDataSetCode().compareTo(o2.getDataSetCode());
                }
            });
        // NOTE: deleted data set "20081105092158673-1" is NOT ommited
        DataSetShareId dataSet1 = list.get(0);
        assertEquals("20081105092158673-1", dataSet1.getDataSetCode());
        assertEquals(null, dataSet1.getShareId()); // having no share id shouldn't fail
        DataSetShareId dataSet2 = list.get(1);
        assertEquals("20081105092159111-1", dataSet2.getDataSetCode());
        assertEquals("42", dataSet2.getShareId());
        assertEquals(31, list.size());
    }

    @Test
    public void testListLocationsByDatasetCodeForRootContainer()
    {
        IDatasetLocationNode root = lister.listLocationsByDatasetCode("ROOT_CONTAINER");
        Iterator<IDatasetLocationNode> rootComponents = root.getComponents().iterator();
        assertContainerLocation(root, "ROOT_CONTAINER", 2);

        IDatasetLocationNode container1 = rootComponents.next();
        Iterator<IDatasetLocationNode> container1Components = container1.getComponents().iterator();

        assertContainerLocation(container1, "CONTAINER_1", 2);
        assertComponentLocation(container1Components.next(), "COMPONENT_1A",
                "contained/COMPONENT_1A");
        assertComponentLocation(container1Components.next(), "COMPONENT_1B",
                "contained/COMPONENT_1B");

        IDatasetLocationNode container2 = rootComponents.next();
        Iterator<IDatasetLocationNode> container2Components = container2.getComponents().iterator();

        assertContainerLocation(container2, "CONTAINER_2", 1);
        assertComponentLocation(container2Components.next(), "COMPONENT_2A",
                "contained/COMPONENT_2A");
    }

    @Test
    public void testListLocationsByDatasetCodeForContainer()
    {
        IDatasetLocationNode container = lister.listLocationsByDatasetCode("CONTAINER_1");
        Iterator<IDatasetLocationNode> containerComponents = container.getComponents().iterator();
        assertContainerLocation(container, "CONTAINER_1", 2);

        assertComponentLocation(containerComponents.next(), "COMPONENT_1A",
                "contained/COMPONENT_1A");
        assertComponentLocation(containerComponents.next(), "COMPONENT_1B",
                "contained/COMPONENT_1B");
    }

    @Test
    public void testListLocationsByDatasetCodeForComponent()
    {
        IDatasetLocationNode component = lister.listLocationsByDatasetCode("COMPONENT_1A");
        assertComponentLocation(component, "COMPONENT_1A", "contained/COMPONENT_1A");
    }

    @Test
    public void testListLocationsByDatasetCodeForNotExisting()
    {
        IDatasetLocationNode component =
                lister.listLocationsByDatasetCode("COMPONENT_NOT_EXISTING");
        Assert.assertNull(component);
    }

    private void assertContainerLocation(IDatasetLocationNode containerNode, String containerCode,
            int numberOfComponents)
    {
        Assert.assertTrue(containerNode.isContainer());
        Assert.assertEquals(containerCode, containerNode.getLocation().getDataSetCode());
        Assert.assertNull(containerNode.getLocation().getDataSetLocation());
        Assert.assertEquals(numberOfComponents, containerNode.getComponents().size());
    }

    private void assertComponentLocation(IDatasetLocationNode componentNode, String componentCode,
            String componentLocation)
    {
        Assert.assertFalse(componentNode.isContainer());
        Assert.assertEquals(componentCode, componentNode.getLocation().getDataSetCode());
        Assert.assertEquals(componentLocation, componentNode.getLocation().getDataSetLocation());
        Assert.assertEquals(0, componentNode.getComponents().size());
    }

    private void assertSameDataSetsForSameCode(Map<String, ExternalData> dataSetsByCode,
            List<ExternalData> dataSets)
    {
        if (dataSets == null || dataSets.isEmpty())
        {
            return;
        }
        for (ExternalData dataSet : dataSets)
        {
            ExternalData previousDataSet = dataSetsByCode.put(dataSet.getCode(), dataSet);
            if (previousDataSet != null)
            {
                assertSame("Same data set object expected for " + dataSet.getCode(),
                        previousDataSet, dataSet);
            }
            List<ExternalData> children = dataSet.getChildren();
            assertSameDataSetsForSameCode(dataSetsByCode, children);
        }
    }

    private void appendChildren(StringBuilder builder, List<ExternalData> dataSets,
            String indentation)
    {
        if (dataSets.isEmpty() == false)
        {
            for (ExternalData dataSet : dataSets)
            {
                builder.append('\n').append(indentation).append(dataSet.getCode()).append(" (");
                builder.append(dataSet.getDataSetType().getCode()).append(") ");
                builder.append(getSortedProperties(dataSet));
                List<ExternalData> children = dataSet.getChildren();
                if (children != null && children.isEmpty() == false)
                {
                    appendChildren(builder, children, indentation + "  ");
                }
            }
        }
    }
}
