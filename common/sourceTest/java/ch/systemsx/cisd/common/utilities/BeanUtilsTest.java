/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

/**
 * Test cases for the {@link BeanUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class BeanUtilsTest
{

    /**
     * Test method for {@link ch.systemsx.cisd.common.utilities.BeanUtils#xmlEncode(java.lang.Object)}.
     */
    @Test
    public final void testXmlEnDecode()
    {
        List<SimpleBean> list = new ArrayList<SimpleBean>();
        SimpleBean bean = new SimpleBean();
        bean.setFirstName("Tanja");
        bean.setLastName("Berg");
        list.add(bean);
        bean = new SimpleBean();
        bean.setFirstName("Christian");
        bean.setLastName("Ribeaud");
        list.add(bean);
        String xml = BeanUtils.xmlEncode(list);
        List newList = (List) BeanUtils.xmlDecode(xml);
        assert newList.size() == 2;
        assertEquals(((SimpleBean)newList.get(0)).getFirstName(), "Tanja");
        assertEquals(((SimpleBean)newList.get(1)).getLastName(), "Ribeaud");
    }

    // This MUST be public.
    public final static class SimpleBean
    {
        private String firstName;

        private String lastName;

        /** Returns <code>firstName</code>. */
        public final String getFirstName()
        {
            return firstName;
        }

        /** Returns <code>lastName</code>. */
        public final String getLastName()
        {
            return lastName;
        }

        /** Sets <code>firstName</code>. */
        public final void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }

        /** Sets <code>lastName</code>. */
        public final void setLastName(String lastName)
        {
            this.lastName = lastName;
        }
    }
}
