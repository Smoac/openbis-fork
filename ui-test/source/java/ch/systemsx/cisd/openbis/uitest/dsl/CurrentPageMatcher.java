/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.uitest.application.GuiApplicationRunner;

/**
 * @author anttil
 */
class CurrentPageMatcher extends TypeSafeMatcher<GuiApplicationRunner>
{

    private final Class<?> pageClass;

    public CurrentPageMatcher(Class<?> pageClass)
    {
        this.pageClass = pageClass;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("Browser showing page " + pageClass.getSimpleName());
    }

    @Override
    public boolean matchesSafely(GuiApplicationRunner openbis)
    {
        return openbis.tryLoad(pageClass) != null;
    }
}
