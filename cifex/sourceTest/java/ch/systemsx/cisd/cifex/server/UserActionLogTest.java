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

package ch.systemsx.cisd.cifex.server;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;

/**
 * Test cases for the {@link UserActionLog}. 
 *
 * @author Bernd Rinn
 */
@Friend(toClasses = UserActionLog.class)
public class UserActionLogTest
{

    @Test
    public void testChanged()
    {
        assertTrue(UserActionLog.changed("a", "A"));
        assertTrue(UserActionLog.changed("a", ""));
        assertTrue(UserActionLog.changed(null, "abc"));
        assertTrue(UserActionLog.changed("", "abc"));
        assertFalse(UserActionLog.changed("a", "a"));
        assertFalse(UserActionLog.changed("", ""));
        assertFalse(UserActionLog.changed(null, null));
        assertFalse(UserActionLog.changed(null, ""));
        assertFalse(UserActionLog.changed("", null));
    }
    
}
