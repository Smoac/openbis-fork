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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.reflections.Reflections;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.server.json.ClassReferences;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * This class contains tests that make sure that Jackson annotations are used correctly and
 * consistently in all the classes that are exposed through openBIS JSON-RPC APIs. 
 *
 * @author anttil
 */
public class JsonAnnotationTest
{
    private Collection<Class<?>> classes = new HashSet<Class<?>>();
    private Reflections reflections = new Reflections("");
    private Collection<Class<?>> empty = Collections.emptySet();
    private Map<String,Collection<Class<?>>> emptyMap = new HashMap<String, Collection<Class<?>>>();

    
    @BeforeClass
    public void findAllClassesUsedByJsonRpcApi() 
    {
        Class<?>[] jsonRpcInterfaces = {IDssServiceRpcGeneric.class, IScreeningApiServer.class, 
                IGeneralInformationChangingService.class, IGeneralInformationService.class,
                IWebInformationService.class, IQueryApiServer.class, IDssServiceRpcScreening.class};
        
        for (Class<?> jsonClass : jsonRpcInterfaces) 
        {
            classes.addAll(ClassReferences.search(jsonClass));
        }    
    }
    
    @Test(enabled = false)
    public void allJsonRpcApiClassesAreAnnotatedWithJsonTypeName() 
    {
        Collection<Class<?>> classesWithoutJsonTypeName = getAllJsonRpcClassesWithoutJsonTypeName();
        assertThat(classesWithoutJsonTypeName, is(empty));
    }
    
    
    @Test(enabled = false)
    public void allJsonTypeNamesAreUnique() 
    {   
        Map<String, Class<?>> map = new HashMap<String, Class<?>>();
        Map<String, Collection<Class<?>>> failMap = new HashMap<String, Collection<Class<?>>>();
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(JsonTypeName.class)) 
        {
            String name = clazz.getAnnotation(JsonTypeName.class).value();
            Class<?> c = map.put(name, clazz);
            if (c != null) 
            {
                @SuppressWarnings("unchecked")
                Collection<Class<?>> oldDuplicates = failMap.put(name, Arrays.asList(c, clazz));
                if (oldDuplicates != null) 
                {
                    failMap.get(name).addAll(oldDuplicates);
                }
            }
         }

        assertThat(failMap, is(emptyMap));
    }
    
    @Test(enabled = false)
    public void allJsonClassesWithSubClassesAreAnnotatedWithJsonSubTypes() 
    {
        Collection<Class<?>> classesWithoutAnnotation = new HashSet<Class<?>>();
        for (Class<?> clazz : classes) 
        {
            if (clazz.isEnum() == false && reflections.getSubTypesOf(clazz).isEmpty() == false) 
            {
                if (clazz.getAnnotation(JsonSubTypes.class) == null) {
                    classesWithoutAnnotation.add(clazz);
                }
            }
        }
        assertThat(classesWithoutAnnotation, is(empty));
    }

    @Test(enabled = false)
    public void allJsonSubTypesAnnotationsContainAllTheSubClasses() 
    {
        Map<String, Collection<Class<?>>> missingSubtypeAnnotations = new HashMap<String, Collection<Class<?>>>();
        for (Class<?> main : reflections.getTypesAnnotatedWith(JsonSubTypes.class)) 
        {
            JsonSubTypes types = main.getAnnotation(JsonSubTypes.class);
            Collection<Class<?>> annotated = new HashSet<Class<?>>();
            for (Type type : types.value()) 
            {
                annotated.add(type.value());
            }
            
            for (Class<?> subtype : reflections.getSubTypesOf(main)) 
            {
                annotated.remove(subtype);
            }
            
            if (!annotated.isEmpty())
            {
                missingSubtypeAnnotations.put(main.getCanonicalName(), annotated);
            }
            
        }
        assertThat(missingSubtypeAnnotations, is(emptyMap));
    }
    
    private Collection<Class<?>> getAllJsonRpcClassesWithoutJsonTypeName() 
    {
        Collection<Class<?>> classesWithoutJsonTypeName = new HashSet<Class<?>>();
        for (Class<?> clazz : classes) 
        {
            if (clazz.getAnnotation(JsonTypeName.class) == null) {
                classesWithoutJsonTypeName.add(clazz);
            }
        } 
        return classesWithoutJsonTypeName;
    }
}
