/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

/**
 * Test cases for the {@link Template}.
 * 
 * @author Franz-Josef Elmer
 */
public class TemplateTest
{
    @Test
    public void testEmptyTemplate()
    {
        assertEquals("", new Template("").createText());
    }

    @Test
    public void testWithoutPlaceholders()
    {
        assertEquals("hello", new Template("hello").createText());
    }

    @Test
    public void testWithOnePlaceholder()
    {
        Template template = new Template("hello ${name}!");
        template.bind("name", "world");
        assertEquals("hello world!", template.createText());
    }

    @Test
    public void testWithTwiceTheSamePlaceholder()
    {
        Template template = new Template("hello ${name}${name}");
        template.bind("name", "world");
        assertEquals("hello worldworld", template.createText());
    }

    @Test
    public void testWithTwoPlaceholders()
    {
        Template template = new Template("hello ${name}, do you know ${name2}?");
        template.bind("name", "world");
        template.bind("name2", "Albert Einstein");
        assertEquals("hello world, do you know Albert Einstein?", template.createText());
    }

    @Test
    public void testWithEscaping()
    {
        Template template = new Template("hello $${name}. I have 25$.");
        assertEquals("hello ${name}. I have 25$.", template.createText());
    }

    @Test
    public void testNamelessPlaceholderInTemplate()
    {
        try
        {
            new Template("hello ${}");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Nameless placeholder ${} found.", e.getMessage());
        }
    }

    @Test
    public void testUnfinishedPlaceholderInTemplate()
    {
        try
        {
            new Template("hello ${name");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Incomplete placeholder detected at the end.", e.getMessage());
        }
        try
        {
            new Template("hello ${");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Incomplete placeholder detected at the end.", e.getMessage());
        }
        try
        {
            new Template("hello $");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Incomplete placeholder detected at the end.", e.getMessage());
        }
    }

    @Test
    public void testBindUnknownPlaceholder()
    {
        Template template = new Template("hello ${name}!");
        try
        {
            template.bind("blabla", "blub");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Unknown variable 'blabla'.", e.getMessage());
        }
    }

    @Test
    public void testAttemptToBind()
    {
        Template template = new Template("${greeting} ${name}!");
        assertEquals(true, template.attemptToBind("greeting", "Hi"));
        assertEquals(false, template.attemptToBind("blabla", "blub"));
        assertEquals("Hi ${name}!", template.createText(false));
    }

    @Test
    public void testIncompleteBinding()
    {
        Template template = new Template("${greeting} ${name}!");
        try
        {
            template.createText(true);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e)
        {
            assertEquals("The following variables are not bound: greeting name ", e.getMessage());
        }

        template.bind("greeting", "hello");
        assertEquals("hello ${name}!", template.createText(false));
        try
        {
            template.createText(true);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e)
        {
            assertEquals("The following variables are not bound: name ", e.getMessage());
        }
    }

    @Test
    public void testCreateFreshCopy()
    {
        Template template = new Template("hello ${name}.${name}!");
        Template template1 = template.createFreshCopy();
        try
        {
            template1.createText();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e)
        {
            assertEquals("The following variables are not bound: name ", e.getMessage());
        }
        template1.bind("name", "world");
        assertEquals("hello world.world!", template1.createText());
        assertEquals("hello ${name}.${name}!", template.createText(false));

        Template template2 = template.createFreshCopy();
        template2.bind("name", "universe");
        assertEquals("hello universe.universe!", template2.createText());
        assertEquals("hello world.world!", template1.createText());
        assertEquals("hello ${name}.${name}!", template.createText(false));
    }
}
