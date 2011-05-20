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

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

/**
 * Unit tests for {@link VirtualHierarchicalContent}
 * 
 * @author Piotr Buczek
 */
public class VirtualHierarchicalContentTest extends AbstractFileSystemTestCase
{

    private IHierarchicalContent[] components;

    // mocks

    private Mockery context;

    private IHierarchicalContent component1;

    private IHierarchicalContent component2;

    private IHierarchicalContent component3;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();

        component1 = context.mock(IHierarchicalContent.class, "component 1");
        component2 = context.mock(IHierarchicalContent.class, "component 2");
        component3 = context.mock(IHierarchicalContent.class, "component 3");

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
        return new VirtualHierarchicalContent(Arrays.asList(contents));
    }

    @Test
    public void testFailWithNullOrEmptyComponents()
    {
        try
        {
            createContent();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Undefined contents", ex.getMessage());
        }

        try
        {
            createContent(new IHierarchicalContent[0]);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Undefined contents", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    // TODO public void testEqualsAndHashCode()

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

    @Test
    public void testGetRootNode()
    {
        final IHierarchicalContent virtualContent = createContent(components);

        context.checking(new Expectations()
            {
                {
                    for (IHierarchicalContent component : components)
                    {
                        one(component).getRootNode();
                    }
                }
            });

        IHierarchicalContentNode root1 = virtualContent.getRootNode();
        // 2nd call uses cache (doesn't invoke getRootNode() on components)
        IHierarchicalContentNode root2 = virtualContent.getRootNode();
        assertSame(root1, root2);
        // TODO merging & handling exceptions not tested

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
                    one(component1).getNode(relativePath);
                    one(component2).getNode(relativePath);
                    one(component3).getNode(relativePath);
                    will(throwException(new IllegalArgumentException("")));
                }
            });

        IHierarchicalContentNode node = virtualContent.getNode(relativePath);
        assertNotNull(node);
        // TODO merging & handling exceptions not tested

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithRelativePathPattern()
    {
        final IHierarchicalContent virtualContent = createContent(components);
        final String pattern = "rel.*path.?pattern";

        context.checking(new Expectations()
            {
                {
                    for (IHierarchicalContent component : components)
                    {
                        one(component).listMatchingNodes(pattern);
                    }
                }
            });

        List<IHierarchicalContentNode> nodeList = virtualContent.listMatchingNodes(pattern);
        assertNotNull(nodeList);
        // TODO merging & handling exceptions not tested

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithStartingPath()
    {
        final IHierarchicalContent virtualContent = createContent(components);
        final String startingPath = "some/dir";
        final String pattern = "file.*name.?pattern";

        context.checking(new Expectations()
            {
                {
                    for (IHierarchicalContent component : components)
                    {
                        one(component).listMatchingNodes(startingPath, pattern);
                    }
                }
            });

        List<IHierarchicalContentNode> nodeList =
                virtualContent.listMatchingNodes(startingPath, pattern);
        assertNotNull(nodeList);
        // TODO merging & handling exceptions not tested

        context.assertIsSatisfied();
    }

}
