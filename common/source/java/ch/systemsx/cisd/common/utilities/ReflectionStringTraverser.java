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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Allows to change all non-final and non-static strings referenced within the specified object. If
 * isDeep is true, traverses the object recursively; otherwise, performs a shallow traversal.
 * Handles lists and sets of strings.
 * 
 * @author Tomasz Pylak
 */
class ReflectionStringTraverser
{
    private static final Logger log = LogFactory.getLogger(LogCategory.OPERATION,
            ReflectionStringTraverser.class);

    @SuppressWarnings("unchecked")
    /** set of primitive types and its wrappers + Date.class */
    private final static Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(Arrays.asList(
            Boolean.class, boolean.class, Character.class, char.class, Byte.class, byte.class,
            Short.class, short.class, Integer.class, int.class, Long.class, long.class,
            Float.class, float.class, Double.class, double.class, Void.class, void.class,
            Date.class));

    public static interface ReflectionFieldVisitor
    {
        /**
         * @param value The value to modify
         * @param fieldOrNull The field the modification applies to; Can be null if the modification
         *            is applied to an array.
         * @return changed value or null if the value should not be changed
         */
        String tryVisit(String value, Object object, Field fieldOrNull);
    }

    /** cannot be called for primitive types or collections */
    public static void traverseDeep(Object object, ReflectionFieldVisitor fieldVisitor)
    {
        Class<?> clazz = object.getClass();
        new ReflectionStringTraverser(fieldVisitor, true).traverseMutable(object, clazz);
    }

    /** cannot be called for primitive types or collections */
    public static void traverseShallow(Object object, ReflectionFieldVisitor fieldVisitor)
    {
        Class<?> clazz = object.getClass();
        new ReflectionStringTraverser(fieldVisitor, false).traverseMutable(object, clazz);
    }

    // mutable classes are arrays and classes which are not primitives
    // or collections of mutable types
    private void traverseMutable(Object object, Class<?> clazz)
    {
        if (seenObjects.contains(object))
        {
            // We've already escaped this
            return;
        } else
        {
            seenObjects.add(object);
        }
        if (isPrimitive(clazz))
        {
            // do nothing
            return;
        }
        if (isMutable(object) == false)
        {
            LogUtils.logErrorWithFailingAssertion(log,
                    "Cannot traverse primitive collections or primitives " + object);
            return;
        } else if (clazz.isArray())
        {
            traverseArray(clazz);
        } else if (isCollection(object))
        {
            traverseMutableCollection((Collection<?>) object);
        } else
        {
            traverseFields(object, clazz);
        }
    }

    private boolean isPrimitive(Class<?> clazz)
    {
        return primitiveTypes.contains(clazz);
    }

    private final ReflectionFieldVisitor visitor;

    private final boolean isDeep;

    private final Set<Object> seenObjects = new HashSet<Object>();

    private ReflectionStringTraverser(ReflectionFieldVisitor fieldVisitor, boolean isDeep)
    {
        this.visitor = fieldVisitor;
        this.isDeep = isDeep;
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

        Field[] fields = getAllFields(clazz);
        for (Field field : fields)
        {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) == false && Modifier.isStatic(modifiers) == false)
            {
                try
                {
                    traverseField(object, field);
                } catch (Throwable t)
                {
                    t.printStackTrace();
                    LogUtils.logErrorWithFailingAssertion(
                            log,
                            "Failed accessing field <" + field.getName() + "> of "
                                    + object.getClass() + ":\n\t" + object);
                }
            }
        }
    }

    /**
     * Return a list of all fields (whatever access status, and on whatever superclass they were
     * defined) that can be found on this class.
     * <p>
     * This works like a union of {@link Class#getDeclaredFields()} which ignores super-classes, and
     * {@link Class#getFields()} which ignores non-public fields
     * 
     * @param clazz The class to introspect
     * @return The complete list of fields
     */
    private Field[] getAllFields(Class<?> clazz)
    {
        final Set<Field> result = new HashSet<Field>();
        Class<?> currentClass = clazz;
        while (currentClass != null)
        {
            for (Field field : currentClass.getDeclaredFields())
            {
                result.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }
        return result.toArray(new Field[result.size()]);
    }

    private void traverseField(Object object, Field field) throws IllegalAccessException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Traverse field <" + field.getName() + "> of " + object.getClass() + ":\n\t"
                    + object);
        }
        field.setAccessible(true);
        Object fieldValue = field.get(object);
        if (fieldValue == null)
        {
            return;
        }
        Class<?> clazz = fieldValue.getClass();

        if (clazz.isArray() && isDeep)
        {
            traverseArray(fieldValue);
        } else if (isCollection(fieldValue) && isDeep)
        {
            traverseCollectionField(object, field, (Collection<?>) fieldValue);
        } else if (isPrimitive(clazz))
        {
            // do nothing
            return;
        } else if (isString(fieldValue))
        {
            String newValue = visitor.tryVisit((String) fieldValue, object, field);
            if (newValue != null)
            {
                field.set(object, newValue);
            }
        } else
        {
            if (isDeep)
            {
                if (seenObjects.contains(fieldValue))
                {
                    // Don't revisit objects we've already seen.
                    return;
                } else
                {
                    seenObjects.add(fieldValue);
                    traverseFields(fieldValue, clazz);
                }
            }
        }
    }

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
            return; // do nothing
        }

        if (isStringClass(componentType))
        {
            Collection<?> newCollection = visitStringCollection(collection);
            field.set(object, newCollection);
        } else
        {
            traverseMutableCollection(collection);
        }
    }

    private void traverseMutableCollection(Collection<?> collection)
    {
        for (Object element : collection)
        {
            traverseMutable(element, element.getClass());
        }
    }

    private boolean isMutable(Object element)
    {
        return isString(element) == false && element.getClass().isPrimitive() == false
                && isStringCollection(element) == false;
    }

    private Collection<String> visitStringCollection(Object collection)
    {
        Collection<String> castedSource = asStringCollection(collection);
        Collection<String> newCollection = createEmptyCollection(castedSource);
        for (String element : castedSource)
        {
            String newElement = tryVisitString(element);
            newCollection.add(newElement != null ? newElement : element);
        }
        return newCollection;
    }

    private void traverseArray(Object array)
    {
        int length = Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();
        if (componentType.isPrimitive())
        {
            return; // do nothing
        }

        for (int index = 0; index < length; ++index)
        {
            Object element = Array.get(array, index);
            if (element == null)
            {
                continue;
            }
            if (isString(element))
            {
                visitStringArrayElement(array, index, (String) element, componentType);
            } else
            {
                if (isStringCollection(element))
                {
                    visitStringCollectionArrayElement(array, index, element);
                } else
                {
                    traverseMutable(element, componentType);
                }
            }
        }
    }

    // array[index] contains collection of primitive types which will be modified if necessary
    private void visitStringCollectionArrayElement(Object array, int index, Object collection)
    {
        Collection<String> newCollection = visitStringCollection(collection);
        Array.set(array, index, newCollection);
    }

    // array[index] contains a value of primitive type which will be modified if necessary
    private void visitStringArrayElement(Object array, int index, String element,
            Class<?> componentType)
    {
        String newElement = tryVisitString(element);
        if (newElement != null)
        {
            Array.set(array, index, newElement);
        }
    }

    private String tryVisitString(String element)
    {
        return visitor.tryVisit(element, element, null);
    }

    // assumes that all elements are of the same type
    private static Class<?> figureElementClass(Collection<?> collection)
    {
        Object firstElem = collection.iterator().next();
        return firstElem.getClass();
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> asStringCollection(Object collection)
    {
        return (Collection<String>) collection;
    }

    // NOTE: works only for sets and lists
    private static <T> Collection<T> createEmptyCollection(Collection<T> collection)
    {
        Class<?> clazz = collection.getClass();
        if (List.class.isAssignableFrom(clazz))
        {
            return new ArrayList<T>();
        } else if (Set.class.isAssignableFrom(clazz))
        {
            return new LinkedHashSet<T>(); // preserve order
        } else
        {
            throw new IllegalStateException("Do not know how to create a collection of type "
                    + clazz.getName());
        }
    }

    private static boolean isString(Object object)
    {
        return object instanceof String;
    }

    private static boolean isStringClass(Class<?> clazz)
    {
        return String.class.isAssignableFrom(clazz);
    }

    private static boolean isStringCollection(Collection<?> collection)
    {
        if (collection.isEmpty())
        {
            return false;
        }
        Class<?> elementClass = figureElementClass(collection);
        return isStringClass(elementClass);
    }

    private static boolean isStringCollection(final Object o)
    {
        return isCollection(o) && isStringCollection(((Collection<?>) o));
    }

    private static boolean isCollection(final Object o)
    {
        return o instanceof Collection<?>;
    }
}
