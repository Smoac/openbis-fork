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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows to change all fields of the selected primitive type of a specified object. Traverses the
 * object recursively.
 * 
 * @author Tomasz Pylak
 */
public class ReflectionPrimitiveFieldTraverser
{
    public static interface ReflectionFieldVisitor
    {
        /** @return new value for the field, null if field value should not be changed */
        <T> T tryVisit(T field);

        /** @return true if visiting objects of the specified classes can change visitor state */
        boolean isVisiting(Class<?> clazz);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Inherited
    public @interface TraversableStructure
    {
    }

    /** cannot be called for primitive types or collections */
    public static void traverse(Object object, ReflectionFieldVisitor fieldVisitor)
    {
        Class<?> clazz = object.getClass();
        new ReflectionPrimitiveFieldTraverser(fieldVisitor).traverseMutable(object, clazz);
    }

    // mutable classes are arrays and classes which are not collections and not primitives
    private void traverseMutable(Object object, Class<?> clazz)
    {
        if (isCollection(object))
        {
            throw new IllegalStateException("Cannot traverse collection " + object);
        }
        if (clazz.isPrimitive())
        {
            throw new IllegalStateException("Cannot traverse objects of primitive type " + object);
        }

        if (clazz.isArray())
        {
            traverseArray(clazz);
        } else
        {
            traverseFields(object, clazz);
        }
    }

    private final ReflectionFieldVisitor visitor;

    private ReflectionPrimitiveFieldTraverser(ReflectionFieldVisitor fieldVisitor)
    {
        this.visitor = fieldVisitor;
    }

    /**
     * Traverses all non-final and non-static fields with any visibility (including private fields)
     * of the object recurisively. Changes value of each primitive field if field visitor provides a
     * new one.
     */
    private void traverseFields(Object object, Class<?> clazz)
    {
        if (object == null)
        {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
        {
            // field.getAnnotations()[0].
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) == false && Modifier.isStatic(modifiers) == false)
            {
                try
                {
                    traverseField(object, field);
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    throw new IllegalStateException("Sould not happen: " + ex.getMessage());
                }
            }
        }
    }

    private void traverseField(Object object, Field field) throws IllegalAccessException
    {
        field.setAccessible(true);
        Object fieldValue = field.get(object);
        if (fieldValue == null)
        {
            return;
        }
        Class<?> clazz = fieldValue.getClass();

        if (clazz.isArray())
        {
            traverseArray(fieldValue);
        } else if (isCollection(fieldValue))
        {
            traverseCollectionField(object, field, (Collection<?>) fieldValue);
        } else if (clazz.isPrimitive())
        {
            Object newValue = visitor.tryVisit(fieldValue);
            if (newValue != null)
            {
                field.set(object, newValue);
            }
        } else
        {
            traverseFields(fieldValue, clazz);
        }
        // TODO 2010-10-05, Tomasz Pylak: handle Map, skip external classes
    }

    // FIXME 2010-10-05, Tomasz Pylak: handle mutable collections like in traverseArray
    private void traverseCollectionField(Object object, Field field, Collection<?> collection)
            throws IllegalArgumentException, IllegalAccessException
    {
        if (collection.size() == 0)
        {
            return;
        }
        Class<?> componentType = figureElementClass(collection);
        if (componentType.isPrimitive())
        {
            if (visitor.isVisiting(componentType))
            {
                traversePrimitiveCollection(object, field, collection);
            }
        } else
        {
            for (Object element : collection)
            {
                traverseMutable(element, componentType);
            }
        }
    }

    // assumes that all elements are of the same type
    private static Class<?> figureElementClass(Collection<?> collection)
    {
        Object firstElem = collection.iterator().next();
        Class<?> componentType = firstElem.getClass();
        return componentType;
    }

    private <T> void traversePrimitiveCollection(Object object, Field field,
            Collection<T> collection) throws IllegalArgumentException, IllegalAccessException
    {
        Collection<T> newCollection = visitPrimitiveVisitableCollection(collection);
        field.set(object, newCollection);
    }

    private <T> Collection<T> visitPrimitiveVisitableCollection(Collection<T> collection)
    {
        Collection<T> newCollection = createEmptyCollection(collection);
        for (T element : collection)
        {
            T newElement = visitor.tryVisit(element);
            newCollection.add(newElement != null ? newElement : element);
        }
        return newCollection;
    }

    // NOTE: works only for sets and lists
    private static <T> Collection<T> createEmptyCollection(Collection<T> collection)
    {
        if (collection.getClass().isAssignableFrom(List.class))
        {
            return new ArrayList<T>();
        } else if (collection.getClass().isAssignableFrom(Set.class))
        {
            return new HashSet<T>();
        } else
        {
            throw new IllegalStateException("Do not know how to create a collection of type "
                    + collection.getClass().getName());
        }
    }

    private void traverseArray(Object array)
    {
        int length = Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();
        boolean isPrimitive = componentType.isPrimitive();

        for (int index = 0; index < length; ++index)
        {
            Object element = Array.get(array, index);
            if (isPrimitive)
            {
                visitPrimitiveArrayElement(array, index, element, componentType);
            } else
            {
                if (isCollection(element))
                {
                    Collection<?> collection = (Collection<?>) element;
                    Class<?> primitiveElementType = tryGetPrimitiveElementType(collection);
                    if (primitiveElementType != null)
                    {
                        visitPrimitiveCollectionArrayElement(array, index, collection,
                                primitiveElementType);
                    } else
                    {
                        traverseMutable(element, componentType);
                    }
                } else
                {
                    traverseMutable(element, componentType);
                }
            }
        }
    }

    // array[index] contains collection of primitive types which will be modified if necessary
    private void visitPrimitiveCollectionArrayElement(Object array, int index,
            Collection<?> collection, Class<?> primitiveElementType)
    {
        if (visitor.isVisiting(primitiveElementType))
        {
            Collection newCollection = visitPrimitiveVisitableCollection(collection);
            Array.set(array, index, newCollection);
        }
    }

    // array[index] contains a value of primitive type which will be modified if necessary
    private void visitPrimitiveArrayElement(Object array, int index, Object element,
            Class<?> componentType)
    {
        if (visitor.isVisiting(componentType))
        {
            Object newElement = visitor.tryVisit(element);
            if (newElement != null)
            {
                Array.set(array, index, newElement);
            }
        }
    }

    private Class<?> tryGetPrimitiveElementType(Collection<?> collection)
    {
        Class<?> elementClass = figureElementClass(collection);
        if (elementClass.isPrimitive())
        {
            return elementClass;
        } else
        {
            return null;
        }
    }

    private static boolean isCollection(final Object o)
    {
        return o instanceof Collection<?>;
    }
}
