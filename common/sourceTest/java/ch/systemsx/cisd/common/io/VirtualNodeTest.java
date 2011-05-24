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

package ch.systemsx.cisd.common.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.io.VirtualHierarchicalContent.IVirtualNodeListMerger;
import ch.systemsx.cisd.common.io.VirtualHierarchicalContent.IVirtualNodeMergerFactory;
import ch.systemsx.cisd.common.io.VirtualHierarchicalContent.VirtualNode;

/**
 * Unit tests for {@link VirtualNode}
 * 
 * @author Piotr Buczek
 */
public class VirtualNodeTest extends AssertJUnit
{

    private List<IHierarchicalContentNode> nodes; // real nodes

    // mocks

    private Mockery context;

    private IVirtualNodeMergerFactory mergerFactory;

    @SuppressWarnings("unused")
    private IVirtualNodeListMerger nodeListMerger;

    private IHierarchicalContentNode node1;

    private IHierarchicalContentNode node2;

    private IHierarchicalContentNode node3;

    @SuppressWarnings("unused")
    private IHierarchicalContentNode mergedNode;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();

        mergerFactory = context.mock(IVirtualNodeMergerFactory.class);
        nodeListMerger = context.mock(IVirtualNodeListMerger.class);

        node1 = context.mock(IHierarchicalContentNode.class, "node1");
        node2 = context.mock(IHierarchicalContentNode.class, "node2");
        node3 = context.mock(IHierarchicalContentNode.class, "node3");
        mergedNode = context.mock(IHierarchicalContentNode.class, "mergedNode");

        nodes = Arrays.asList(node1, node2, node3);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    //
    // tests mocking IVirtualNodeMergerFactory
    //

    private IHierarchicalContentNode createVirtualNode()
    {
        return new VirtualNode(mergerFactory, nodes);
    }

    @Test
    public void testFailWithNullOrEmptyNodes()
    {
        try
        {
            new VirtualNode(mergerFactory, null);
            fail("Expected AssertionError");
        } catch (AssertionError ex)
        {
            assertEquals("Undefined nodes.", ex.getMessage());
        }

        try
        {
            new VirtualNode(mergerFactory, new ArrayList<IHierarchicalContentNode>());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Resource doesn't exist.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    // @Test
    // public void testEqualsAndHashCode()
    // {
    // IHierarchicalContent virtualContent = createContent(components);
    // IHierarchicalContent virtualContentSameComponents = createContent(components.clone());
    // assertEquals(virtualContent, virtualContentSameComponents);
    // assertEquals(virtualContent.hashCode(), virtualContentSameComponents.hashCode());
    //
    // IHierarchicalContent[] subComponents =
    // { component1, component2 };
    // IHierarchicalContent virtualContentSubComponents = createContent(subComponents);
    // assertFalse(virtualContent.equals(virtualContentSubComponents));
    // assertFalse(virtualContent.hashCode() == virtualContentSubComponents.hashCode());
    //
    // IHierarchicalContent[] reorderedComponents = new IHierarchicalContent[]
    // { component1, component3, component2 };
    // IHierarchicalContent virtualContentReorderedComponents = createContent(reorderedComponents);
    // assertFalse(virtualContent.equals(virtualContentReorderedComponents));
    // assertFalse(virtualContent.hashCode() == virtualContentReorderedComponents.hashCode());
    //
    // context.assertIsSatisfied();
    // }

    @Test
    public void testGetName()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final String nodeName = "some name";

        context.checking(new Expectations()
            {
                {
                    // first nodes name is taken
                    one(node1).getName();
                    will(returnValue(nodeName));
                }
            });

        String virtualName = virtualNode.getName();
        assertEquals(nodeName, virtualName);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetRelativePath()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final String relativePath = "relative/path";

        context.checking(new Expectations()
            {
                {
                    // first nodes path is taken
                    one(node1).getRelativePath();
                    will(returnValue(relativePath));
                }
            });

        String virtualRelativePath = virtualNode.getRelativePath();
        assertEquals(relativePath, virtualRelativePath);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetParentRelativePath()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final String parentRelativePath = "parent/relative/path";

        context.checking(new Expectations()
            {
                {
                    // first nodes path is taken
                    one(node1).getParentRelativePath();
                    will(returnValue(parentRelativePath));
                }
            });

        String virtualParentRelativePath = virtualNode.getParentRelativePath();
        assertEquals(parentRelativePath, virtualParentRelativePath);

        context.assertIsSatisfied();
    }

    @Test
    public void testExists()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        // contract: at least one node needs to exist for virtual node to exist

        // 1st case: 2nd out of 3 nodes exist, 3rd node is not asked at all
        context.checking(new Expectations()
            {
                {
                    one(node1).exists();
                    will(returnValue(false));

                    one(node2).exists();
                    will(returnValue(true));
                }
            });
        assertTrue(virtualNode.exists());

        // 2st case: all nodes don't exist
        context.checking(new Expectations()
            {
                {
                    for (IHierarchicalContentNode node : nodes)
                    {
                        one(node).exists();
                        will(returnValue(false));
                    }
                }
            });
        assertFalse(virtualNode.exists());

        context.assertIsSatisfied();
    }

    @Test
    public void testIsDirectory()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        context.checking(new Expectations()
            {
                {
                    // first node is asked twice giving different answer
                    one(node1).isDirectory();
                    will(returnValue(true));

                    one(node1).isDirectory();
                    will(returnValue(false));
                }
            });

        // check twice with different answer from mocked node
        assertTrue(virtualNode.isDirectory());
        assertFalse(virtualNode.isDirectory());

        context.assertIsSatisfied();
    }

    // TODO 2011-05-24, Piotr Buczek: write remaining tests
}
