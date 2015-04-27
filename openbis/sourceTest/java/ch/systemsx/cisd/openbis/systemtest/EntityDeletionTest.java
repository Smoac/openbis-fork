/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class EntityDeletionTest extends BaseTest
{
    @Test
    public final void testTrashExperimentsWithOneLevelOfDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph(
                "E1, samples: S50, data sets: DS60 DS61\n"
                + "E2, samples: S51\n"
                + "E3\n"
                );
        
        deleteExperiments(g.e(1), g.e(2), g.e(3));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1), g.e(2), g.e(3));
        assertDeleted(g.s(50), g.s(51));
        assertDeleted(g.ds(60), g.ds(61));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashExperimentsWithDeepDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S50, data sets: DS60 DS61 DS70 DS71\n"
                + "E2, samples: S51\n"
                + "E3\n"
                + "S50, components: S52, data sets: DS70 DS71\n"
                + "S51, components: S53\n"
                );

        deleteExperiments(g.e(1), g.e(2), g.e(3));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1), g.e(2), g.e(3));
        assertDeleted(g.s(50), g.s(51), g.s(52), g.s(53));
        assertDeleted(g.ds(60), g.ds(61), g.ds(70), g.ds(71));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashExperimentWithSamplesAndDataSetsAndNoExternalLinks()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");
        
        deleteExperiments(g.e(1), g.e(2));

        assertEquals("", renderGraph(g));
        assertDeleted(g.e(1), g.e(2));
        assertDeleted(g.s(1));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashExperimentWithARelatedDataSetComponentWhichBelongsToAnExternalExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");
        
        deleteExperiments(g.e(1));
        
        assertEquals("E2, data sets: DS2\n", renderGraph(g));
        assertDeleted(g.e(1));
        assertDeleted(g.s(1));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashExperimentWithARelatedDataSetInAnExternalContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");
        
        failTrashExperiment(g.e(2), g.ds(2), g.ds(1), g.s(1));
        
        assertEquals("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public void testTrashPublishedExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS3 DS4 DS5 DS6\n"
                + "E2, data sets: DS1 DS2\n"
                + "DS1, components: DS3\n"
                + "DS2, components: DS6\n"
                + "DS3, components: DS4\n"
                + "DS5, parents: DS6\n");
        
        deleteExperiments(g.e(2));
        
        assertEquals("E1, data sets: DS3 DS4 DS5 DS6\n"
                + "DS3, components: DS4\n"
                + "DS5, parents: DS6\n", renderGraph(g));
        assertDeleted(g.e(2));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashPublishedExperimentWithOrginalExperimentWithSamples()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS2 DS3\n"
                + "E2, data sets: DS1\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS1, components: DS2\n"
                + "DS2, components: DS3\n"
                );
        
        deleteExperiments(g.e(2));
        
        assertEquals("E1, samples: S1, data sets: DS2 DS3\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS2, components: DS3\n", renderGraph(g));
        assertDeleted(g.e(2));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashOrginalExperimentWithSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS2 DS3\n"
                + "E2, data sets: DS1\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS1, components: DS2\n"
                + "DS2, components: DS3\n"
                );
        
        failTrashExperiment(g.e(1), g.ds(2), g.ds(1), g.e(2));
        
        assertEquals("E1, samples: S1, data sets: DS2 DS3\n"
                + "E2, data sets: DS1\n"
                + "S1, data sets: DS2 DS3\n"
                + "DS1, components: DS2\n"
                + "DS2, components: DS3\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashExperimentsWithContainerDataSetWithPhysicalDataSetFromAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "DS1, components: DS2\n");
        
        deleteExperiments(g.e(1));
        
        assertEquals("E2, data sets: DS2\n", renderGraph(g));
        assertDeleted(g.e(1));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashSamplesAlreadyTrashed()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1\nS2\n");
        
        
        assertEquals("", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashSamplesWithOneLevelOfDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S20\n"
                + "S3, data sets: DS60[NET]\n"
                + "S20, data sets: DS61[NET]\n");
        
        deleteSamples(g.s(1), g.s(3));
        
        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(3), g.s(20));
        assertDeleted(g.ds(60), g.ds(61));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashSampleWithTwoLevelOfDependencies()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2 S3, data sets: DS1[NET]\n"
                + "S2, data sets: DS2[NET]\n"
                + "S3, data sets: DS3[NET]\n");
        
        deleteSamples(g.s(1));
        
        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2), g.s(3));
        assertDeleted(g.ds(1), g.ds(2), g.ds(3));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashSampleWithAComponentWithAContainerDataSetWithAComponentDataSetOfFirstSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2, data sets: DS2[NET]\n"
                + "S2, data sets: DS1[NECT]\n"
                + "DS1[NECT], components: DS2[NET]\n");
        
        deleteSamples(g.s(1));
        
        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashSampleWithAComponentWithAComponentDataSetOfAContainerDataSetOfFirstSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2, data sets: DS1[NECT]\n"
                + "S2, data sets: DS2[NET]\n"
                + "DS1[NECT], components: DS2[NET]\n");
        
        deleteSamples(g.s(1));
        
        assertEquals("", renderGraph(g));
        assertDeleted(g.s(1), g.s(2));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashSampleWithAnExperimentSampleWithADataSet()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S2, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS1\n");
        
        failTrashSample(g.s(1), g.s(2), g.e(1));
        
        assertEquals("E1, samples: S2, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS1\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashExperimentSampleWithADataSet()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "S1, data sets: DS1\n");
        
        deleteSamples(g.s(1));
        
        assertEquals("", renderGraph(g));
        assertModified(g.e(1));
        assertDeleted(g.s(1));
        assertDeleted(g.ds(1));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashSampleWithComponentWithDataSetWithComponentOfAnotherSample()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "S3, data sets: DS1[NECT]\n"
                + "DS1[NECT], components: DS2[NET]\n");
        
        failTrashSample(g.s(1), g.ds(2), g.ds(1), g.s(3));
        
        assertEquals("S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "S3, data sets: DS1[NECT]\n"
                + "DS1[NECT], components: DS2[NET]\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashSampleWithComponentWithDataSetWithComponentOfAnotherExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "DS1, components: DS2[NET]\n");
        
        failTrashSample(g.s(1), g.ds(2), g.ds(1), g.e(1));
        
        assertEquals("E1, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS2[NET]\n"
                + "DS1, components: DS2[NET]\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public void testTrashSampleWithDataSetWithDataSetComponentIndirectlyDependentOnOutsideContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS3[NET] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "S2, data sets: DS2[NECT]\n"
                + "DS1[NECT], components: DS3[NET] DS4[NECT] DS5[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n");
        
        failTrashSample(g.s(1), g.ds(4), g.ds(2), g.s(2));
        
        assertEquals("S1, data sets: DS1[NECT] DS3[NET] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "S2, data sets: DS2[NECT]\n"
                + "DS1[NECT], components: DS3[NET] DS4[NECT] DS5[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n", renderGraph(g));
        assertUnmodifiedAndUndeleted(g);
    }

    @Test
    public final void testTrashDataSets()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS2[NECT] DS3[NET] DS4[NET] DS5[NET] DS6[NET]\n"
                + "DS1[NECT], components: DS5[NET]\n"
                + "DS2[NECT], components: DS6[NET]\n");
        
        deleteDataSets(g.ds(1), g.ds(2), g.ds(3));
        
        assertEquals("S1, data sets: DS4[NET]\n", renderGraph(g));
        assertModified(g.s(1));
        assertDeleted(g.ds(1), g.ds(2), g.ds(3), g.ds(5), g.ds(6));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public final void testTrashDataSetAndComponentsBelongingToSameExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1 DS2\n"
                + "E2, data sets: DS3\n"
                + "DS1, components: DS2 DS3\n");
        
        deleteDataSets(g.ds(1));
        
        assertEquals("E2, data sets: DS3\n", renderGraph(g));
        assertModified(g.e(1));
        assertDeleted(g.ds(1), g.ds(2));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public void testTrashDataSetsWithDataSetInAContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS2[NECT] DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT]\n"
                + "DS2[NECT], components: DS3[NET]\n");
        
        deleteDataSets(g.ds(2));
        
        assertEquals("S1, data sets: DS1[NECT]\n", renderGraph(g));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertDeleted(g.ds(2), g.ds(3));
        assertUnmodifiedAndUndeleted(g);
    }
    
    @Test
    public void testTrashDataSetsWithDataSetComponentIndirectlyDependentOnOutsideContainer()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NECT] DS2[NECT] DS3[NET] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "DS1[NECT], components: DS3[NET] DS4[NECT] DS5[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n");

        deleteDataSets(g.ds(1));

        assertEquals("S1, data sets: DS2[NECT] DS4[NECT] DS5[NET] DS6[NET]\n"
                + "DS2[NECT], components: DS4[NECT]\n"
                + "DS4[NECT], components: DS5[NET] DS6[NET]\n", renderGraph(g));
        assertModified(g.s(1));
        assertDeleted(g.ds(1), g.ds(3));
        assertUnmodifiedAndUndeleted(g);
    }
    
    private void failTrashExperiment(ExperimentNode experimentNode, DataSetNode originalDataSet,
            DataSetNode relatedDataSet, EntityNode outsiderNode)
    {
        try
        {
            deleteExperiments(experimentNode);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertExceptionMessage(originalDataSet, relatedDataSet, outsiderNode, ex);
        }
    }
    
    private void failTrashSample(SampleNode sampleNode, SampleNode relatedSample, EntityNode outsiderNode)
    {
        try
        {
            deleteSamples(sampleNode);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("The sample " + entityGraphManager.getSample(relatedSample).getIdentifier() 
                    + " belongs to " + renderOutsider(outsiderNode) + " is outside the deletion set.", 
                    ex.getMessage());
        }
    }

    private void failTrashSample(SampleNode sampleNode, DataSetNode originalDataSet,
            DataSetNode relatedDataSet, EntityNode outsiderNode)
    {
        try
        {
            deleteSamples(sampleNode);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertExceptionMessage(originalDataSet, relatedDataSet, outsiderNode, ex);
        }
    }
    
    private void assertExceptionMessage(DataSetNode originalDataSet, DataSetNode relatedDataSet, 
            EntityNode outsiderNode, UserFailureException ex)
    {
        assertEquals("The data set " + entityGraphManager.getDataSetCodeOrNull(originalDataSet) 
                + " is a component of the data set " + entityGraphManager.getDataSetCodeOrNull(relatedDataSet) 
                + " which belongs to " + renderOutsider(outsiderNode) + " outside the deletion set.", 
                ex.getMessage());
    }

    private String renderOutsider(EntityNode outsiderNode)
    {
        String outsiderType;
        String outsiderIdentifier;
        if (outsiderNode instanceof ExperimentNode)
        {
            outsiderType = "experiment";
            outsiderIdentifier = entityGraphManager.getExperimentIdentifierOrNull((ExperimentNode) outsiderNode);
        } else
        {
            outsiderType = "sample";
            outsiderIdentifier = entityGraphManager.getSample((SampleNode) outsiderNode).getIdentifier();
        }
        String outsider = outsiderType + " " 
        + outsiderIdentifier;
        return outsider;
    }
    
    private void deleteExperiments(ExperimentNode...experimentNodes)
    {
        List<String> experimentIdentifiers = new ArrayList<String>();
        for (ExperimentNode experimentNode : experimentNodes)
        {
            experimentIdentifiers.add(entityGraphManager.getExperimentIdentifierOrNull(experimentNode));
        }
        deleteExperiments(experimentIdentifiers, createAdminUser());
    }

    private void deleteSamples(SampleNode...sampleNodes)
    {
        List<String> samplePermIds = new ArrayList<String>();
        for (SampleNode sampleNode : sampleNodes)
        {
            samplePermIds.add(entityGraphManager.getSamplePermIdOrNull(sampleNode));
        }
        deleteSamples(samplePermIds, createAdminUser());
    }
    
    private void deleteDataSets(DataSetNode...dataSetNodes)
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (DataSetNode dataSetNode : dataSetNodes)
        {
            dataSetCodes.add(entityGraphManager.getDataSetCodeOrNull(dataSetNode));
        }
        deleteDataSets(dataSetCodes, createAdminUser());
    }
    
    private String createAdminUser()
    {
        return create(aSession().withInstanceRole(RoleCode.ADMIN));
    }
    
    protected void deleteExperiments(List<String> experimentIdentifiers, String userSessionToken)
    {
        List<ExperimentIdentifier> identifiers = new ArrayList<ExperimentIdentifier>();
        for (String identifier : experimentIdentifiers)
        {
            identifiers.add(ExperimentIdentifierFactory.parse(identifier));
        }
        List<TechId> experimentIds = TechId.createList(commonServer.listExperiments(userSessionToken, identifiers));
        commonServer.deleteExperiments(userSessionToken, experimentIds, "test", DeletionType.TRASH);
    }
    
    protected void deleteSamples(List<String> samplePermIds, String userSessionToken)
    {
        Sample[] samples = loadSamples(samplePermIds);
        commonServer.deleteSamples(userSessionToken, TechId.createList(Arrays.asList(samples)), "test", DeletionType.TRASH);
    }

    protected void deleteDataSets(List<String> dataSetCodes, String userSessionToken)
    {
        commonServer.deleteDataSets(userSessionToken, dataSetCodes, "test", DeletionType.TRASH, true);
    }
    
}
