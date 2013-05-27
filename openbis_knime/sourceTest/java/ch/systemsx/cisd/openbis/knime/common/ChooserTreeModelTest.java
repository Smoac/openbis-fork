/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import static ch.systemsx.cisd.openbis.knime.common.ChooserTreeModel.LOADING_TEXT;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.internal.NamedSequence;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class ChooserTreeModelTest extends AssertJUnit
{
    private static final class NonThreadChooserTreeModel extends ChooserTreeModel
    {
        private static final long serialVersionUID = 1L;

        NonThreadChooserTreeModel(DataSetOwnerType entityType, String sessionToken,
                IGeneralInformationService service)
        {
            this(entityType, true, sessionToken, service);
        }
        
        NonThreadChooserTreeModel(DataSetOwnerType entityType, boolean ownerEntity, String sessionToken,
                IGeneralInformationService service)
        {
            super(entityType, ownerEntity, sessionToken, service);
        }

        @Override
        void execute(Runnable runnable)
        {
            runnable.run();
        }
    }
    
    private static final class MockNodeAction implements IAsyncNodeAction
    {
        private boolean executed;
        private Throwable handledException;

        @Override
        public void execute(Runnable runnable)
        {
            executed = true;
            runnable.run();
        }

        @Override
        public void handleException(Throwable throwable)
        {
            handledException = throwable;
        }
    }
    
    private static final String SESSION_TOKEN = "s-token";

    private static final String LISTABLE_SAMPLE_TYPE = "ST1";

    private static final String NON_LISTABLE_SAMPLE_TYPE = "ST2";
    
    private static final String SPACE_1 = "SP1";

    private static final String SPACE_2 = "SP2";

    private static final String P_1 = "P1";

    private static final String P_2 = "P2";
    
    private static final String EXP_1 = "EXP1";
    
    private static final String EXP_2 = "EXP2";

    private static final String SAMPLE_1 = "S1";
    
    private static final String SAMPLE_2 = "S2";
    
    private Mockery context;

    private IGeneralInformationService service;

    private ChooserTreeModel experimentChooserModel;

    private ChooserTreeModel sampleChooserModel;

    private ChooserTreeModel dataSetChooserModel;

    private MockNodeAction nodeAction;

    private TreeModelListener treeModelListener;

    private RecordingMatcher<SearchCriteria> criteriaMatcher;

    private NamedSequence searchForSequence;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
        treeModelListener = context.mock(TreeModelListener.class);
        nodeAction = new MockNodeAction();
        context.checking(new Expectations()
            {
                {
                    atLeast(3).of(service).listSampleTypes(SESSION_TOKEN);
                    SampleType.SampleTypeInitializer sampleTypeInitializer =
                            new SampleType.SampleTypeInitializer();
                    sampleTypeInitializer.setCode(LISTABLE_SAMPLE_TYPE);
                    sampleTypeInitializer.setListable(true);
                    SampleType listableSampleType = new SampleType(sampleTypeInitializer);
                    sampleTypeInitializer.setCode(NON_LISTABLE_SAMPLE_TYPE);
                    sampleTypeInitializer.setListable(false);
                    SampleType nonListableSampleType = new SampleType(sampleTypeInitializer);
                    will(returnValue(Arrays.asList(listableSampleType, nonListableSampleType)));

                    atLeast(3).of(service).listProjects(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(new Project(SPACE_1, P_1),
                            new Project(SPACE_2, P_1),
                            new Project(SPACE_1, P_2))));
                }
            });
        experimentChooserModel =
                new NonThreadChooserTreeModel(DataSetOwnerType.EXPERIMENT, SESSION_TOKEN, service);
        experimentChooserModel.addTreeModelListener(treeModelListener);
        sampleChooserModel =
                new NonThreadChooserTreeModel(DataSetOwnerType.SAMPLE, SESSION_TOKEN, service);
        sampleChooserModel.addTreeModelListener(treeModelListener);
        dataSetChooserModel =
                new NonThreadChooserTreeModel(DataSetOwnerType.DATA_SET, SESSION_TOKEN, service);
        dataSetChooserModel.addTreeModelListener(treeModelListener);
        criteriaMatcher = new RecordingMatcher<SearchCriteria>();
        searchForSequence = new NamedSequence("criteria-matcher");
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testSpaces()
    {
        Object root = experimentChooserModel.getRoot();
        Object space1 = experimentChooserModel.getChild(root, 0);
        Object space2 = experimentChooserModel.getChild(root, 1);
        Object loadingNode1 = experimentChooserModel.getChild(space1, 0);
        Object loadingNode2 = experimentChooserModel.getChild(space2, 0);

        assertEquals(2, experimentChooserModel.getChildCount(root));
        assertEquals(SPACE_1, space1.toString());
        assertEquals(SPACE_2, space2.toString());
        assertEquals(1, experimentChooserModel.getChildCount(space1));
        assertEquals(1, experimentChooserModel.getChildCount(space2));
        assertEquals(LOADING_TEXT, loadingNode1.toString());
        assertEquals(LOADING_TEXT, loadingNode2.toString());
        assertEquals(0, experimentChooserModel.getChildCount(loadingNode1));
        assertEquals(0, experimentChooserModel.getChildCount(loadingNode2));
        assertSelectibility(false, experimentChooserModel, root);
        assertSelectibility(false, experimentChooserModel, space1);
        assertSelectibility(false, experimentChooserModel, loadingNode1);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandSpaceNodeInDataSetChooserForOwner()
    {
        Object root = dataSetChooserModel.getRoot();
        Object space1 = dataSetChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> matcher = prepareTreeStructureChanged(1);

        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, space1), nodeAction);

        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        assertEquals(2, dataSetChooserModel.getChildCount(space1));
        assertEquals("P1", dataSetChooserModel.getChild(space1, 0).toString());
        assertEquals("P2", dataSetChooserModel.getChild(space1, 1).toString());
        assertEquals(createPath(dataSetChooserModel, space1).toString(), 
                matcher.recordedObject().getTreePath().toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandSpaceNodeInDataSetChooserForNoOwner()
    {
        NonThreadChooserTreeModel chooser 
                = new NonThreadChooserTreeModel(DataSetOwnerType.DATA_SET, false, SESSION_TOKEN, service);
        chooser.addTreeModelListener(treeModelListener);
        Object root = chooser.getRoot();
        Object space1 = chooser.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> matcher = prepareTreeStructureChanged(1);
        
        chooser.expandNode(createPath(chooser, space1), nodeAction);
        
        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        assertEquals(2, chooser.getChildCount(space1));
        assertEquals("P1", chooser.getChild(space1, 0).toString());
        assertEquals("P2", chooser.getChild(space1, 1).toString());
        assertEquals(createPath(chooser, space1).toString(), 
                matcher.recordedObject().getTreePath().toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandSpaceNodeInSampleChooserForOwner()
    {
        Object root = sampleChooserModel.getRoot();
        Object space1 = sampleChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> matcher = prepareTreeStructureChanged(1);
        
        sampleChooserModel.expandNode(createPath(sampleChooserModel, space1), nodeAction);
        
        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        assertEquals(2, sampleChooserModel.getChildCount(space1));
        assertEquals("P1", sampleChooserModel.getChild(space1, 0).toString());
        assertEquals("P2", sampleChooserModel.getChild(space1, 1).toString());
        assertEquals(createPath(sampleChooserModel, space1).toString(), 
                matcher.recordedObject().getTreePath().toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandSpaceNodeInSampleChooserForNoOwner()
    {
        NonThreadChooserTreeModel chooser 
        = new NonThreadChooserTreeModel(DataSetOwnerType.SAMPLE, false, SESSION_TOKEN, service);
        chooser.addTreeModelListener(treeModelListener);
        Object root = chooser.getRoot();
        Object space1 = chooser.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> matcher = prepareTreeStructureChanged(1);
        Sample s2 = sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_2);
        Sample s1 = sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_1);
        prepareSearchForSamples(s2, s1);
        
        chooser.expandNode(createPath(chooser, space1), nodeAction);
        
        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        assertEquals(4, chooser.getChildCount(space1));
        assertEquals("/SP1/S1", chooser.getChild(space1, 0).toString());
        assertEquals("/SP1/S2", chooser.getChild(space1, 1).toString());
        assertEquals("P1", chooser.getChild(space1, 2).toString());
        assertEquals("P2", chooser.getChild(space1, 3).toString());
        assertEquals(createPath(chooser, space1).toString(), 
                matcher.recordedObject().getTreePath().toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCollapseSpaceNode()
    {
        Object root = dataSetChooserModel.getRoot();
        Object space1 = dataSetChooserModel.getChild(root, 0);
        prepareTreeStructureChanged(1);
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        assertEquals(2, dataSetChooserModel.getChildCount(space1));

        dataSetChooserModel.collapsNode(createPath(dataSetChooserModel, space1));
        
        Object project11 = dataSetChooserModel.getChild(space1, 0);
        assertEquals(P_1, project11.toString());
        assertEquals(2, dataSetChooserModel.getChildCount(space1));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandProjectNodeInExperimentChooser()
    {
        Object root = experimentChooserModel.getRoot();
        Object space1 = experimentChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> matcher = prepareTreeStructureChanged(2);
        experimentChooserModel.expandNode(createPath(experimentChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = experimentChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        
        experimentChooserModel.expandNode(createPath(experimentChooserModel, project11), nodeAction);

        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        assertEquals(2, experimentChooserModel.getChildCount(project11));
        Object child1 = experimentChooserModel.getChild(project11, 0);
        assertEquals(EXP_1, child1.toString());
        assertEquals(0, experimentChooserModel.getChildCount(child1));
        assertEquals(true, experimentChooserModel.isLeaf(child1));
        Object child2 = experimentChooserModel.getChild(project11, 1);
        assertEquals(EXP_2, child2.toString());
        assertEquals(0, experimentChooserModel.getChildCount(child2));
        assertEquals(true, experimentChooserModel.isLeaf(child2));
        assertEquals(createPath(experimentChooserModel, space1).toString(), 
                matcher.getRecordedObjects().get(0).getTreePath().toString());
        assertEquals(createPath(experimentChooserModel, project11).toString(), 
                matcher.getRecordedObjects().get(1).getTreePath().toString());
        assertEquals(2, matcher.getRecordedObjects().size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandProjectNodeInSampleChooser()
    {
        Object root = sampleChooserModel.getRoot();
        Object space1 = sampleChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> matcher = prepareTreeStructureChanged(2);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = sampleChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        
        sampleChooserModel.expandNode(createPath(sampleChooserModel, project11), nodeAction);
        
        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        assertEquals(2, sampleChooserModel.getChildCount(project11));
        Object child1 = sampleChooserModel.getChild(project11, 0);
        assertEquals(EXP_1, child1.toString());
        assertEquals(1, sampleChooserModel.getChildCount(child1));
        assertEquals(LOADING_TEXT, sampleChooserModel.getChild(child1, 0).toString());
        assertEquals(false, sampleChooserModel.isLeaf(child1));
        Object child2 = sampleChooserModel.getChild(project11, 1);
        assertEquals(EXP_2, child2.toString());
        assertEquals(1, sampleChooserModel.getChildCount(child2));
        assertEquals(LOADING_TEXT, sampleChooserModel.getChild(child2, 0).toString());
        assertEquals(false, sampleChooserModel.isLeaf(child2));
        assertEquals(createPath(sampleChooserModel, space1).toString(), 
                matcher.getRecordedObjects().get(0).getTreePath().toString());
        assertEquals(createPath(sampleChooserModel, project11).toString(), 
                matcher.getRecordedObjects().get(1).getTreePath().toString());
        assertEquals(2, matcher.getRecordedObjects().size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCollapsProjectNode()
    {
        Object root = dataSetChooserModel.getRoot();
        Object space1 = dataSetChooserModel.getChild(root, 0);
        prepareTreeStructureChanged(3);
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = dataSetChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        TreePath path = createPath(dataSetChooserModel, project11);
        dataSetChooserModel.expandNode(path, nodeAction);
        assertEquals(null, nodeAction.handledException);
        assertEquals(2, dataSetChooserModel.getChildCount(project11));
        
        dataSetChooserModel.collapsNode(path);

        assertEquals(1, dataSetChooserModel.getChildCount(project11));
        assertEquals(LOADING_TEXT, dataSetChooserModel.getChild(project11, 0).toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandExperimentNodeInSampleChooser()
    {
        Object root = sampleChooserModel.getRoot();
        Object space1 = sampleChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> eventMatcher = prepareTreeStructureChanged(3);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = sampleChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, project11), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object exp1 = sampleChooserModel.getChild(project11, 0);
        assertEquals(EXP_1, exp1.toString());
        Sample sample = sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_1);
        prepareSearchForSamples(sample);
        
        sampleChooserModel.expandNode(createPath(sampleChooserModel, exp1), nodeAction);
        
        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        List<TreeModelEvent> recordedObjects = eventMatcher.getRecordedObjects();
        assertEquals(createPath(sampleChooserModel, space1).toString(), 
                recordedObjects.get(0).getTreePath().toString());
        assertEquals(createPath(sampleChooserModel, project11).toString(), 
                recordedObjects.get(1).getTreePath().toString());
        assertEquals(createPath(sampleChooserModel, exp1).toString(), 
                recordedObjects.get(2).getTreePath().toString());
        assertEquals(3, recordedObjects.size());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[" 
                + "SearchCriteria.AttributeMatchClause[ATTRIBUTE,TYPE,ST1,EQUALS]]," 
                + "[SearchSubCriteria[EXPERIMENT,SearchCriteria[MATCH_ALL_CLAUSES,"
                + "[SearchCriteria.AttributeMatchClause[ATTRIBUTE,PERM_ID," 
                + "PERM-/SP1/P1/EXP1,EQUALS]],[]]]]]", criteriaMatcher.recordedObject().toString());
        Object child = sampleChooserModel.getChild(exp1, 0);
        IChooserTreeNode<?> childNodeObject = getTreeNode(child);
        assertEquals(ChooserTreeNodeType.SAMPLE, childNodeObject.getNodeType());
        assertSame(sample, childNodeObject.getNodeObject());
        assertEquals(1, sampleChooserModel.getChildCount(exp1));
        assertEquals(LOADING_TEXT, sampleChooserModel.getChild(child, 0).toString());
        assertEquals(1, sampleChooserModel.getChildCount(child));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandExperimentNodeInDataSetChooser()
    {
        Object root = dataSetChooserModel.getRoot();
        Object space1 = dataSetChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> eventMatcher = prepareTreeStructureChanged(3);
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = dataSetChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, project11), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object exp1 = dataSetChooserModel.getChild(project11, 0);
        assertEquals(EXP_1, exp1.toString());
        Sample sample = sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_1);
        prepareSearchForSamples(sample);
        DataSet dataSet = dataSet("DS-1");
        prepareSearchForDataSets(dataSet);
        
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, exp1), nodeAction);

        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        List<TreeModelEvent> recordedObjects = eventMatcher.getRecordedObjects();
        assertEquals(createPath(dataSetChooserModel, space1).toString(), 
                recordedObjects.get(0).getTreePath().toString());
        assertEquals(createPath(dataSetChooserModel, project11).toString(), 
                recordedObjects.get(1).getTreePath().toString());
        assertEquals(createPath(dataSetChooserModel, exp1).toString(), 
                recordedObjects.get(2).getTreePath().toString());
        assertEquals(3, recordedObjects.size());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,["
                + "SearchCriteria.AttributeMatchClause[ATTRIBUTE,TYPE,ST1,EQUALS]],"
                + "[SearchSubCriteria[EXPERIMENT,SearchCriteria[MATCH_ALL_CLAUSES,"
                + "[SearchCriteria.AttributeMatchClause[ATTRIBUTE,PERM_ID,"
                + "PERM-/SP1/P1/EXP1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(0).toString());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[],[SearchSubCriteria[EXPERIMENT,"
                + "SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause["
                + "ATTRIBUTE,PERM_ID,PERM-/SP1/P1/EXP1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(1).toString());
        assertEquals(2, criteriaMatcher.getRecordedObjects().size());
        Object dataSetChild = dataSetChooserModel.getChild(exp1, 0);
        IChooserTreeNode<?> dataSetObject = getTreeNode(dataSetChild);
        assertEquals(ChooserTreeNodeType.DATA_SET, dataSetObject.getNodeType());
        assertSame(dataSet, dataSetObject.getNodeObject());
        assertEquals(true, dataSetChooserModel.isLeaf(dataSetChild));
        Object sampleChild = dataSetChooserModel.getChild(exp1, 1);
        IChooserTreeNode<?> sampleObject = getTreeNode(sampleChild);
        assertEquals(ChooserTreeNodeType.SAMPLE, sampleObject.getNodeType());
        assertSame(sample, sampleObject.getNodeObject());
        assertEquals(2, dataSetChooserModel.getChildCount(exp1));
        assertEquals(LOADING_TEXT, dataSetChooserModel.getChild(sampleChild, 0).toString());
        assertEquals(1, dataSetChooserModel.getChildCount(sampleChild));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCollapsExperimentNode()
    {
        Object root = sampleChooserModel.getRoot();
        Object space1 = sampleChooserModel.getChild(root, 0);
        prepareTreeStructureChanged(4);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = sampleChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, project11), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object exp1 = sampleChooserModel.getChild(project11, 0);
        assertEquals(EXP_1, exp1.toString());
        prepareSearchForSamples(sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_2),
                sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_1));
        TreePath path = createPath(sampleChooserModel, exp1);
        sampleChooserModel.expandNode(path, nodeAction);
        assertEquals(null, nodeAction.handledException);
        assertEquals(2, sampleChooserModel.getChildCount(exp1));
        
        sampleChooserModel.collapsNode(path);

        assertEquals(1, sampleChooserModel.getChildCount(exp1));
        assertEquals(LOADING_TEXT, sampleChooserModel.getChild(exp1, 0).toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExpandSampleNodeInSampleChooser()
    {
        Object root = sampleChooserModel.getRoot();
        Object space1 = sampleChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> eventMatcher = prepareTreeStructureChanged(4);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = sampleChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, project11), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object exp1 = sampleChooserModel.getChild(project11, 0);
        prepareSearchForSamples(sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_1));
        sampleChooserModel.expandNode(createPath(sampleChooserModel, exp1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object sample1 = sampleChooserModel.getChild(exp1, 0);
        String experimentIdentifier = "/" + SPACE_1 + "/" + P_1 + "/" + EXP_1;
        Sample childSampleWithExperiment =
                sample(NON_LISTABLE_SAMPLE_TYPE, SPACE_2, "CHILD_WE", experimentIdentifier);
        Sample childSampleWithoutExperiment = sample(NON_LISTABLE_SAMPLE_TYPE, SPACE_2, "CHILD_WOE");
        prepareSearchForSamplesWithChildren(childSampleWithExperiment, childSampleWithoutExperiment);
        Sample containedSampleWithoutExperiment = sample(LISTABLE_SAMPLE_TYPE, SPACE_1, "CONT_WOE");
        Sample containedSampleWithExperiment =
                sample(LISTABLE_SAMPLE_TYPE, SPACE_1, "CONT_WE", experimentIdentifier);
        prepareSearchForSamples(containedSampleWithExperiment, containedSampleWithoutExperiment);

        sampleChooserModel.expandNode(createPath(sampleChooserModel, sample1), nodeAction);
        
        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        List<TreeModelEvent> recordedObjects = eventMatcher.getRecordedObjects();
        assertEquals(createPath(sampleChooserModel, space1).toString(),
                recordedObjects.get(0).getTreePath().toString());
        assertEquals(createPath(sampleChooserModel, project11).toString(),
                recordedObjects.get(1).getTreePath().toString());
        assertEquals(createPath(sampleChooserModel, exp1).toString(),
                recordedObjects.get(2).getTreePath().toString());
        assertEquals(createPath(sampleChooserModel, sample1).toString(),
                recordedObjects.get(3).getTreePath().toString());
        assertEquals(4, recordedObjects.size());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,["
                + "SearchCriteria.AttributeMatchClause[ATTRIBUTE,TYPE,ST1,EQUALS]],"
                + "[SearchSubCriteria[EXPERIMENT,SearchCriteria[MATCH_ALL_CLAUSES,"
                + "[SearchCriteria.AttributeMatchClause[ATTRIBUTE,PERM_ID,"
                + "PERM-/SP1/P1/EXP1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(0).toString());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause["
                + "ATTRIBUTE,PERM_ID,PERM-/SP1/S1,EQUALS]],[]]",
                criteriaMatcher.getRecordedObjects().get(1).toString());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[],[SearchSubCriteria[SAMPLE_CONTAINER,"
                + "SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause["
                + "ATTRIBUTE,PERM_ID,PERM-/SP1/S1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(2).toString());
        assertEquals(3, criteriaMatcher.getRecordedObjects().size());
        Object child = sampleChooserModel.getChild(exp1, 0);
        Object child1 = sampleChooserModel.getChild(child, 0);
        IChooserTreeNode<?> sampleNode1 = getTreeNode(child1);
        assertEquals(ChooserTreeNodeType.SAMPLE, sampleNode1.getNodeType());
        assertSame(containedSampleWithExperiment, sampleNode1.getNodeObject());
        assertEquals(1, sampleChooserModel.getChildCount(child1));
        assertEquals(LOADING_TEXT, sampleChooserModel.getChild(child1, 0).toString());
        Object child2 = sampleChooserModel.getChild(child, 1);
        IChooserTreeNode<?> sampleNode2 = getTreeNode(child2);
        assertEquals(ChooserTreeNodeType.SAMPLE, sampleNode2.getNodeType());
        assertSame(childSampleWithExperiment, sampleNode2.getNodeObject());
        assertEquals(1, sampleChooserModel.getChildCount(child2));
        assertEquals(LOADING_TEXT, sampleChooserModel.getChild(child2, 0).toString());
        assertEquals(2, sampleChooserModel.getChildCount(child));
        context.assertIsSatisfied();
    }

    @Test
    public void testExpandSampleNodeInDataSetChooser()
    {
        Object root = dataSetChooserModel.getRoot();
        Object space1 = dataSetChooserModel.getChild(root, 0);
        RecordingMatcher<TreeModelEvent> eventMatcher = prepareTreeStructureChanged(4);
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = dataSetChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, project11), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object exp1 = dataSetChooserModel.getChild(project11, 0);
        prepareSearchForSamples(sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_1));
        prepareSearchForDataSets();
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, exp1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object sample1 = dataSetChooserModel.getChild(exp1, 0);
        String experimentIdentifier = "/" + SPACE_1 + "/" + P_1 + "/" + EXP_1;
        prepareSearchForSamplesWithChildren();
        Sample containedSampleWithoutExperiment = sample(LISTABLE_SAMPLE_TYPE, SPACE_1, "CONT_WOE");
        Sample containedSampleWithExperiment =
                sample(LISTABLE_SAMPLE_TYPE, SPACE_1, "CONT_WE", experimentIdentifier);
        prepareSearchForSamples(containedSampleWithExperiment, containedSampleWithoutExperiment);
        DataSet dataSet = dataSet("DS1");
        prepareSearchForDataSets(dataSet);
        
        dataSetChooserModel.expandNode(createPath(dataSetChooserModel, sample1), nodeAction);
        
        assertEquals(null, nodeAction.handledException);
        assertEquals(true, nodeAction.executed);
        List<TreeModelEvent> recordedObjects = eventMatcher.getRecordedObjects();
        assertEquals(createPath(dataSetChooserModel, space1).toString(),
                recordedObjects.get(0).getTreePath().toString());
        assertEquals(createPath(dataSetChooserModel, project11).toString(),
                recordedObjects.get(1).getTreePath().toString());
        assertEquals(createPath(dataSetChooserModel, exp1).toString(),
                recordedObjects.get(2).getTreePath().toString());
        assertEquals(createPath(dataSetChooserModel, sample1).toString(),
                recordedObjects.get(3).getTreePath().toString());
        assertEquals(4, recordedObjects.size());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,["
                + "SearchCriteria.AttributeMatchClause[ATTRIBUTE,TYPE,ST1,EQUALS]],"
                + "[SearchSubCriteria[EXPERIMENT,SearchCriteria[MATCH_ALL_CLAUSES,"
                + "[SearchCriteria.AttributeMatchClause[ATTRIBUTE,PERM_ID,"
                + "PERM-/SP1/P1/EXP1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(0).toString());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[],[SearchSubCriteria[EXPERIMENT,"
                + "SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause["
                + "ATTRIBUTE,PERM_ID,PERM-/SP1/P1/EXP1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(1).toString());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause["
                + "ATTRIBUTE,PERM_ID,PERM-/SP1/S1,EQUALS]],[]]",
                criteriaMatcher.getRecordedObjects().get(2).toString());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[],[SearchSubCriteria[SAMPLE_CONTAINER,"
                + "SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause["
                + "ATTRIBUTE,PERM_ID,PERM-/SP1/S1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(3).toString());
        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,[],[SearchSubCriteria[SAMPLE," 
        		+ "SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause[" 
        		+ "ATTRIBUTE,PERM_ID,PERM-/SP1/S1,EQUALS]],[]]]]]",
                criteriaMatcher.getRecordedObjects().get(4).toString());
        assertEquals(5, criteriaMatcher.getRecordedObjects().size());
        Object child = dataSetChooserModel.getChild(exp1, 0);
        Object child1 = dataSetChooserModel.getChild(child, 0);
        IChooserTreeNode<?> dataSetNode = getTreeNode(child1);
        assertEquals(ChooserTreeNodeType.DATA_SET, dataSetNode.getNodeType());
        assertSame(dataSet, dataSetNode.getNodeObject());
        assertEquals(0, dataSetChooserModel.getChildCount(child1));
        assertEquals(true, dataSetChooserModel.isLeaf(child1));
        Object child2 = dataSetChooserModel.getChild(child, 1);
        IChooserTreeNode<?> sampleNode2 = getTreeNode(child2);
        assertEquals(ChooserTreeNodeType.SAMPLE, sampleNode2.getNodeType());
        assertSame(containedSampleWithExperiment, sampleNode2.getNodeObject());
        assertEquals(1, dataSetChooserModel.getChildCount(child2));
        assertEquals(LOADING_TEXT, dataSetChooserModel.getChild(child2, 0).toString());
        Object child3 = dataSetChooserModel.getChild(child, 2);
        IChooserTreeNode<?> sampleNode3 = getTreeNode(child3);
        assertEquals(ChooserTreeNodeType.SAMPLE, sampleNode3.getNodeType());
        assertSame(containedSampleWithoutExperiment, sampleNode3.getNodeObject());
        assertEquals(1, dataSetChooserModel.getChildCount(child3));
        assertEquals(LOADING_TEXT, dataSetChooserModel.getChild(child3, 0).toString());
        assertEquals(3, dataSetChooserModel.getChildCount(child));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCollapsSampleNode()
    {
        Object root = sampleChooserModel.getRoot();
        Object space1 = sampleChooserModel.getChild(root, 0);
        prepareTreeStructureChanged(5);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, space1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object project11 = sampleChooserModel.getChild(space1, 0);
        prepareListExperiments(project11);
        sampleChooserModel.expandNode(createPath(sampleChooserModel, project11), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object exp1 = sampleChooserModel.getChild(project11, 0);
        assertEquals(EXP_1, exp1.toString());
        prepareSearchForSamples(sample(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_2));
        sampleChooserModel.expandNode(createPath(sampleChooserModel, exp1), nodeAction);
        assertEquals(null, nodeAction.handledException);
        Object sample1 = sampleChooserModel.getChild(exp1, 0);
        String experimentIdentifier = "/" + SPACE_1 + "/" + P_1 + "/" + EXP_1;
        Sample s1 = sample(LISTABLE_SAMPLE_TYPE, SPACE_2, SAMPLE_1, experimentIdentifier);
        Sample s2 = sample(LISTABLE_SAMPLE_TYPE, SPACE_2, SAMPLE_2, experimentIdentifier);
        prepareSearchForSamplesWithChildren(s2, s1);
        prepareSearchForSamples();
        TreePath path = createPath(sampleChooserModel, sample1);
        sampleChooserModel.expandNode(path, nodeAction);
        assertEquals(null, nodeAction.handledException);
        assertEquals(2, sampleChooserModel.getChildCount(sample1));
        
        sampleChooserModel.collapsNode(path);

        assertEquals(1, sampleChooserModel.getChildCount(sample1));
        assertEquals(LOADING_TEXT, sampleChooserModel.getChild(sample1, 0).toString());
        context.assertIsSatisfied();
    }
    
    private void prepareSearchForSamplesWithChildren(final Sample...childSamples)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).searchForSamples(with(SESSION_TOKEN), with(criteriaMatcher),
                            with(EnumSet.of(SampleFetchOption.CHILDREN)));
                    if (childSamples.length == 0)
                    {
                        will(returnValue(Arrays.asList()));
                    } else
                    {
                        will(returnValue(Arrays.asList(sampleWithChildren(childSamples))));
                    }
                    inSequence(searchForSequence);
                }
            });
    }
    
    private void prepareSearchForSamples(final Sample...samples)
    {
        context.checking(new Expectations()
        {
            {
                one(service).searchForSamples(with(SESSION_TOKEN), with(criteriaMatcher),
                        with(new IsNull<EnumSet<SampleFetchOption>>()));
                will(returnValue(Arrays.asList(samples)));
                inSequence(searchForSequence);
            }
        });
    }
    
    private void prepareSearchForDataSets(final DataSet... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).searchForDataSets(with(SESSION_TOKEN), with(criteriaMatcher));
                    will(returnValue(Arrays.asList(dataSets)));
                    inSequence(searchForSequence);
                }
            });
    }
    
    private void prepareListExperiments(final Object projectObject)
    {
        context.checking(new Expectations()
            {
                {
                    Project project = getWrappedObject(projectObject);
                    one(service).listExperiments(SESSION_TOKEN, Arrays.asList(project), null);
                    will(returnValue(Arrays.asList(experiment(project, EXP_2),
                            experiment(project, EXP_1))));
                }
            });
    }

    private RecordingMatcher<TreeModelEvent> prepareTreeStructureChanged(final int numberOfEvents)
    {
        final RecordingMatcher<TreeModelEvent> matcher = new RecordingMatcher<TreeModelEvent>();
        context.checking(new Expectations()
            {
                {
                    exactly(numberOfEvents).of(treeModelListener).treeStructureChanged(
                            with(matcher));
                }
            });
        return matcher;
    }
    
    private DataSet dataSet(String code)
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setDataSetTypeCode("DT");
        initializer.setCode(code);
        initializer.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        return new DataSet(initializer);
    }
    
    private Sample sampleWithChildren(Sample...children)
    {
        SampleInitializer initializer =
                createSampleInitializer(LISTABLE_SAMPLE_TYPE, SPACE_1, SAMPLE_2, null);
        initializer.setRetrievedFetchOptions(EnumSet.of(SampleFetchOption.CHILDREN));
        initializer.setChildren(Arrays.asList(children));
        return new Sample(initializer);
    }
    
    private Sample sample(String sampleTypeCode, String spaceCode, String sampleCode)
    {
        return sample(sampleTypeCode, spaceCode, sampleCode, null);
    }
    
    private Sample sample(String sampleTypeCode, String spaceCode, String sampleCode,
            String experimentIdentifierOrNull)
    {
        return new Sample(createSampleInitializer(sampleTypeCode, spaceCode, sampleCode,
                experimentIdentifierOrNull));
    }

    private SampleInitializer createSampleInitializer(String sampleTypeCode, String spaceCode,
            String sampleCode, String experimentIdentifierOrNull)
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setSampleTypeCode(sampleTypeCode);
        initializer.setSampleTypeId(new Long(sampleTypeCode.hashCode()));
        initializer.setCode(sampleCode);
        initializer.setIdentifier("/" + spaceCode + "/" + sampleCode);
        initializer.setPermId("PERM-" + initializer.getIdentifier());
        initializer.setId((long) initializer.getIdentifier().hashCode());
        initializer.setExperimentIdentifierOrNull(experimentIdentifierOrNull);
        initializer.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        return initializer;
    }
    
    private Experiment experiment(Project project, String experimentCode)
    {
        ExperimentInitializer initializer = new ExperimentInitializer();
        initializer.setCode(experimentCode);
        initializer.setExperimentTypeCode("ET");
        initializer.setIdentifier(project.getIdentifier() + "/" + experimentCode);
        initializer.setPermId("PERM-" + initializer.getIdentifier());
        initializer.setId((long) initializer.getIdentifier().hashCode());
        initializer.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        return new Experiment(initializer);
    }
    
    private IChooserTreeNode<?> getTreeNode(Object node)
    {
        return (IChooserTreeNode<?>) ((DefaultMutableTreeNode) node).getUserObject();
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getWrappedObject(Object node)
    {
        return ((IChooserTreeNode<T>) ((DefaultMutableTreeNode) node).getUserObject()).getNodeObject();
    }
    
    private void assertSelectibility(boolean expected, ChooserTreeModel model, Object node)
    {
        assertEquals(expected, model.isSelectable(createPath(model, node))); 
    }

    private TreePath createPath(ChooserTreeModel model, Object node)
    {
        return new TreePath(model.getPathToRoot((TreeNode) node));
    }

}
