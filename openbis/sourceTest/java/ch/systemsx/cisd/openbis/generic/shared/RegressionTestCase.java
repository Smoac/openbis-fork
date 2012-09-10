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

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;

/**
 * @author Franz-Josef Elmer
 */
public class RegressionTestCase extends AssertJUnit
{
    protected void assertMandatoryMethodAnnotations(Class<?> clazz)
    {
        assertMandatoryMethodAnnotations(clazz, clazz);
    }

    protected void assertMandatoryMethodAnnotations(Class<?> interfaceClass,
            Class<?> implementingClass)
    {
        assertMandatoryMethodAnnotations(interfaceClass, implementingClass, "");
    }

    protected void assertMandatoryMethodAnnotations(Class<?> interfaceClass,
            Class<?> implementingClass, String exceptions)
    {
        List<Class<? extends Annotation>> mandatoryAnnotations =
                new ArrayList<Class<? extends Annotation>>();
        mandatoryAnnotations.add(RolesAllowed.class);
        mandatoryAnnotations.add(Transactional.class);

        assertMandatoryMethodAnnotations(mandatoryAnnotations, interfaceClass, implementingClass,
                exceptions);
    }

    protected void assertMandatoryMethodAnnotations(
            List<Class<? extends Annotation>> mandatoryAnnotations, Class<?> interfaceClass,
            Class<?> implementingClass, String exceptions)
    {
        final String noMissingAnnotationsMsg =
                "Annotation checking for interface " + interfaceClass.getName()
                        + " and implementing class " + implementingClass.getName()
                        + ": The mandatory annotations doesn't appear in the following methods:\n";
        assertEquals(true, interfaceClass.isInterface());
        assertEquals(true, interfaceClass.isAssignableFrom(implementingClass));

        StringBuilder problems = new StringBuilder(noMissingAnnotationsMsg);
        for (Method interfaceMethod : interfaceClass.getDeclaredMethods())
        {
            List<String> missingAnnotations = new ArrayList<String>();
            for (Class<? extends Annotation> annotationClass : mandatoryAnnotations)
            {
                if (interfaceMethod.getAnnotation(annotationClass) == null)
                {
                    try
                    {
                        Method implementedMethod =
                                implementingClass.getMethod(interfaceMethod.getName(),
                                        interfaceMethod.getParameterTypes());
                        if (implementedMethod.getAnnotation(annotationClass) == null)
                        {
                            missingAnnotations.add(annotationClass.getSimpleName());
                        }
                    } catch (Exception ex)
                    {
                        fail("Method '" + interfaceMethod.toGenericString() + "' of interface "
                                + interfaceClass.getName()
                                + " is not defined in implementing class "
                                + implementingClass.getName() + ".");
                    }
                }
            }
            if (missingAnnotations.size() > 0)
            {
                problems.append(String.format("%s: %s\n", interfaceMethod.getName(),
                        StringUtils.join(missingAnnotations, ", ")));
            }
        }
        assertEquals(noMissingAnnotationsMsg + exceptions, problems.toString());
    }
}
