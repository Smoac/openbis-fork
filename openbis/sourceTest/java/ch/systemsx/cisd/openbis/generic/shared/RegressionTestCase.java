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

package ch.systemsx.cisd.openbis.generic.shared;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;

/**
 * @author Franz-Josef Elmer
 */
public class RegressionTestCase extends AssertJUnit
{
    protected void assertMandatoryMethodAnnotations(Class<?> clazz)
    {
        assertMandatoryMethodAnnotations(clazz, "");
    }

    protected void assertMandatoryMethodAnnotations(Class<?> clazz, String exceptions)
    {
        List<Class<? extends Annotation>> mandatoryAnnotations =
                new ArrayList<Class<? extends Annotation>>();
        // TODO: Check RolesAllowed for implementing class
        // mandatoryAnnotations.add(RolesAllowed.class);
        mandatoryAnnotations.add(Transactional.class);

        final String noMissingAnnotationsMsg =
                "Missing annotations in class " + clazz.getCanonicalName() + ":\n";
        StringBuilder problems = new StringBuilder(noMissingAnnotationsMsg);
        for (Method m : clazz.getDeclaredMethods())
        {
            List<String> missingAnnotations = new ArrayList<String>();
            for (Class<? extends Annotation> c : mandatoryAnnotations)
            {
                if (m.getAnnotation(c) == null)
                {
                    missingAnnotations.add(c.getSimpleName());
                }
            }
            if (missingAnnotations.size() > 0)
            {
                problems.append(String.format("%s: %s\n", m.getName(),
                        StringUtils.join(missingAnnotations, ", ")));
            }
        }
        assertEquals(noMissingAnnotationsMsg + exceptions, problems.toString());
    }
}
