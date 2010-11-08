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

package ch.systemsx.cisd.common.utilities;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ReflectingStringEscaperTest extends AssertJUnit
{
    public class TestBean
    {
        private String foo;

        private String bar;

        private String baz;
    }

    @Test
    public void testEscaper()
    {
        TestBean bean = new TestBean();
        bean.foo = "<a>foo</a>";
        bean.bar = "<b>bar</b>";
        bean.baz = "<i>baz</i>";

        TestBean escaped = ReflectingStringEscaper.escape(bean, "foo", "baz");
        assertEquals(bean, escaped);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", bean.foo);
        assertEquals("<b>bar</b>", bean.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", bean.baz);
    }
}
