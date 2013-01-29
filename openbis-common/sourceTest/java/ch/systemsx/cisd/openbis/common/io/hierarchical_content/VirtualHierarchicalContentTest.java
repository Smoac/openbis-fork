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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeListMerger;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeMerger;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeMergerFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Unit tests for {@link VirtualHierarchicalContent}
 * 
 * @author Piotr Buczek
 */
public class VirtualHierarchicalContentTest extends AssertJUnit
{

    private IHierarchicalContent[] components; // real contents

    // mocks

    private Mockery context;

    private IVirtualNodeMergerFactory mergerFactory;

    private IVirtualNodeMerger nodeMerger;

    private IVirtualNodeListMerger nodeListMerger;

    private IHierarchicalContent component1;

    private IHierarchicalContent component2;

    private IHierarchicalContent component3;

    private IHierarchicalContentNode node1;

    private IHierarchicalContentNode node2;

    private IHierarchicalContentNode node3;

    private IHierarchicalContentNode mergedNode;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();

        mergerFactory = context.mock(IVirtualNodeMergerFactory.class);
        nodeMerger = context.mock(IVirtualNodeMerger.class);
        nodeListMerger = context.mock(IVirtualNodeListMerger.class);

        component1 = context.mock(IHierarchicalContent.class, "component1");
        component2 = context.mock(IHierarchicalContent.class, "component2");
        component3 = context.mock(IHierarchicalContent.class, "component3");

        node1 = context.mock(IHierarchicalContentNode.class, "node1");
        node2 = context.mock(IHierarchicalContentNode.class, "node2");
        node3 = context.mock(IHierarchicalContentNode.class, "node3");
        mergedNode = context.mock(IHierarchicalContentNode.class, "mergedNode");

        components = new IHierarchicalContent[]
            { component1, component2, component3 };
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private IHierarchicalContent createContent(IHierarchicalContent... contents)
    {
        return new VirtualHierarchicalContent(mergerFactory, Arrays.asList(contents));
    }

    @Test
    public void testFailWithNullOrEmptyComponents()
    {
        try
        {
            new VirtualHierarchicalContent(mergerFactory, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Undefined contents", ex.getMessage());
        }

        try
        {
            new VirtualHierarchicalContent(mergerFactory, new ArrayList<IHierarchicalContent>());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Undefined contents", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testEqualsAndHashCode()
    {
        IHierarchicalContent virtualContent = createContent(components);
        IHierarchicalContent virtualContentSameComponents = createContent(components.clone());
        assertEquals(virtualContent, virtualContentSameComponents);
        assertEquals(virtualContent.hashCode(), virtualContentSameComponents.hashCode());

        IHierarchicalContent[] subComponents =
            { component1, component2 };
        IHierarchicalContent virtualContentSubComponents = createContent(subComponents);
        assertFalse(virtualContent.equals(virtualContentSubComponents));
        assertFalse(virtualContent.hashCode() == virtualContentSubComponents.hashCode());

        IHierarchicalContent[] reorderedComponents = new IHierarchicalContent[]
            { component1, component3, component2 };
        IHierarchicalContent virtualContentReorderedComponents = createContent(reorderedComponents);
        assertFalse(virtualContent.equals(virtualContentReorderedComponents));
        assertFalse(virtualContent.hashCode() == virtualContentReorderedComponents.hashCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testClose()
    {
        final IHierarchicalContent virtualContent = createContent(components);

        context.checking(new Expectations()
            {
                {
                    for (IHierarchicalContent component : components)
                    {
                        one(component).close();
                    }
                }
            });
        virtualContent.close();

        context.assertIsSatisfied();
    }

    //
    // tests mocking IVirtualNodeMergerFactory
    //

    @Test
    public void testGetRootNode()
    {
        final IHierarchicalContent virtualContent = createContent(components);

        context.checking(new Expectations()
            {
                {
                    one(mergerFactory).createNodeMerger();
                    will(returnValue(nodeMerger));

                    one(component1).getRootNode();
                    will(returnValue(node1));
                    one(component2).getRootNode();
                    will(returnValue(node2));
                    // component3 will not be added to merged nodes
                    one(component3).getRootNode();
                    will(returnValue(null)); // no exception expected

                    one(nodeMerger).addNode(node1);
                    one(nodeMerger).addNode(node2);
                    one(nodeMerger).tryCreateMergedNode();
                    will(returnValue(mergedNode));
                }
            });

        IHierarchicalContentNode virtualRoot = virtualContent.getRootNode();
        // 2nd call uses cache (no method in expectation should be invoked twice)
        IHierarchicalContentNode virtualRoot2 = virtualContent.getRootNode();
        assertSame(virtualRoot, virtualRoot2);
        assertSame(mergedNode, virtualRoot);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNode()
    {
        final IHierarchicalContent virtualContent = createContent(components);
        final String relativePath = "rel/path";

        context.checking(new Expectations()
            {
                {
                    one(mergerFactory).createNodeMerger();
                    will(returnValue(nodeMerger));

                    one(component1).getNode(relativePath);
                    will(returnValue(node1));
                    one(component2).getNode(relativePath);
                    will(returnValue(node2));
                    // component3 will not be added to merged nodes
                    one(component3).getNode(relativePath);
                    will(throwException(new IllegalArgumentException("")));

                    one(nodeMerger).addNode(node1);
                    one(nodeMerger).addNode(node2);
                    one(nodeMerger).tryCreateMergedNode();
                    will(returnValue(mergedNode));
                }
            });

        IHierarchicalContentNode node = virtualContent.getNode(relativePath);
        assertSame(mergedNode, node);

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithRelativePathPattern()
    {
        final IHierarchicalContent virtualContent = createContent(components);
        final String pattern = "rel.*path.?pattern";

        // contents of these lists is not significant in the test
        final List<IHierarchicalContentNode> list1 = Arrays.asList(node1, node2);
        final List<IHierarchicalContentNode> list2 = Arrays.asList(node3);
        final List<IHierarchicalContentNode> list3 = Arrays.asList();
        final List<IHierarchicalContentNode> mergedNodeList = Arrays.asList(mergedNode);

        context.checking(new Expectations()
            {
                {
                    one(mergerFactory).createNodeListMerger();
                    will(returnValue(nodeListMerger));

                    one(component1).listMatchingNodes(pattern);
                    will(returnValue(list1));
                    one(component2).listMatchingNodes(pattern);
                    will(returnValue(list2));
                    one(component3).listMatchingNodes(pattern);
                    will(returnValue(list3));

                    one(nodeListMerger).addNodes(list1);
                    one(nodeListMerger).addNodes(list2);
                    one(nodeListMerger).addNodes(list3);
                    one(nodeListMerger).createMergedNodeList();
                    will(returnValue(mergedNodeList));
                }
            });

        List<IHierarchicalContentNode> nodeList = virtualContent.listMatchingNodes(pattern);
        assertSame(mergedNodeList, nodeList);

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithStartingPath()
    {
        final IHierarchicalContent virtualContent = createContent(components);
        final String startingPath = "some/dir";
        final String pattern = "file.*name.?pattern";

        // contents of these lists is not important in the test
        final List<IHierarchicalContentNode> list1 = Arrays.asList(node1, node2);
        final List<IHierarchicalContentNode> list2 = Arrays.asList(node3);
        final List<IHierarchicalContentNode> list3 = Arrays.asList();
        final List<IHierarchicalContentNode> mergedNodeList = Arrays.asList(mergedNode);

        context.checking(new Expectations()
            {
                {
                    one(mergerFactory).createNodeListMerger();
                    will(returnValue(nodeListMerger));

                    one(component1).listMatchingNodes(startingPath, pattern);
                    will(returnValue(list1));
                    one(component2).listMatchingNodes(startingPath, pattern);
                    will(returnValue(list2));
                    one(component3).listMatchingNodes(startingPath, pattern);
                    will(returnValue(list3));

                    one(nodeListMerger).addNodes(list1);
                    one(nodeListMerger).addNodes(list2);
                    one(nodeListMerger).addNodes(list3);
                    one(nodeListMerger).createMergedNodeList();
                    will(returnValue(mergedNodeList));
                }
            });

        List<IHierarchicalContentNode> nodeList =
                virtualContent.listMatchingNodes(startingPath, pattern);
        assertSame(mergedNodeList, nodeList);

        context.assertIsSatisfied();
    }

}
