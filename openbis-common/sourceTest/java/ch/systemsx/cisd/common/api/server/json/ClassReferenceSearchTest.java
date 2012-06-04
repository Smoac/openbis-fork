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

package ch.systemsx.cisd.common.api.server.json;

import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.server.json.util.ClassReferences;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;

public class ClassReferenceSearchTest
{

    public class BaseClass 
    {
    }
    public interface BaseInterface 
    {
    }
    public class SimpleClass extends BaseClass 
    {
    }
    public class AnotherSimpleClass implements BaseInterface 
    {
    }
    public class NotExtendedSimpleClass 
    {
    }
    

    public interface SimpleInterface 
    {
        public AnotherSimpleClass getSimple(SimpleClass argument);
    }
    @Test
    public void findsDirectSimpleReferences() 
    {
        Class<?>[] expected = {SimpleClass.class, AnotherSimpleClass.class};
        Collection<Class<?>> result = ClassReferences.search(SimpleInterface.class);
        assertThat(result, hasItems(expected));
        assertThat(result.size(), is(expected.length));
    }
 
    
    public interface CollectionInterface 
    {
        Collection<SimpleClass> getCollection(List<AnotherSimpleClass> argument);
    }
    @Test
    public void findsReferencesInCollections() 
    {
        Class<?>[] expected = {SimpleClass.class, AnotherSimpleClass.class};
        Collection<Class<?>> result = ClassReferences.search(CollectionInterface.class);
        assertThat(result, hasItems(expected));
        assertThat(result.size(), is(expected.length));
    }

    
    public interface ArrayInterface 
    {
        SimpleClass[] getArray(AnotherSimpleClass[] argument);
    }
    @Test
    public void findsReferencesInArrays() 
    {
        Class<?>[] expected = {SimpleClass.class, AnotherSimpleClass.class};
        Collection<Class<?>> result = ClassReferences.search(ArrayInterface.class);
        assertThat(result, hasItems(expected));
        assertThat(result.size(), is(expected.length));
    }
    
    public interface SubTypeInterface 
    {
       public BaseClass getBaseclass(BaseInterface argument); 
    }
    @Test
    public void findsSubTypesOfReferencedClassesAndInterfaces() 
    {
        Class<?>[] expected = {SimpleClass.class, 
                               AnotherSimpleClass.class,
                               BaseClass.class,
                               BaseInterface.class};
        Collection<Class<?>> result = ClassReferences.search(SubTypeInterface.class);
        assertThat(result, hasItems(expected));
        assertThat(result.size(), is(expected.length));
    }
    
    
    public interface GetterInterface 
    {
        public void method(InterfaceWithOneGetter argument);
    }
    public interface InterfaceWithOneGetter 
    {
        public BaseClass doSomething();
        public BaseInterface getCalculatedValue(BaseInterface argument);
        public NotExtendedSimpleClass getSimpleClass();
    }
    @Test
    public void searchesOnlyGettersOfReferencedClasses() 
    {
        Class<?>[] expected = {InterfaceWithOneGetter.class, 
                               NotExtendedSimpleClass.class};
        Collection<Class<?>> result = ClassReferences.search(GetterInterface.class);
        assertThat(result, hasItems(expected));
        assertThat(result.size(), is(expected.length));
    }
}
