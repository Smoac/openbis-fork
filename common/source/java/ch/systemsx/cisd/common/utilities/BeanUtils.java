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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import ch.systemsx.cisd.common.annotation.CollectionMapping;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * Some utilities around <i>Java Bean</i>s.
 * 
 * @author Christian Ribeaud
 * @author Bernd Rinn
 */
public final class BeanUtils
{

    /** A generally accepted variation for <code>Boolean</code> bean property. */
    private static final String BOOLEAN_GETTER_PREFIX = "is";

    /** The typical <i>getter</i> prefix for <i>bean</i> property. */
    private static final String GETTER_PREFIX = "get";

    /** The typical <i>setter</i> prefix for <i>bean</i> property. */
    private static final String SETTER_PREFIX = "set";

    private BeanUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Checks the list of bean objects item by item for public getters which return <code>null</code> or 0.
     * 
     * @param beanListToCheck The list of beans to check. Can be <code>null</code>.
     * @return <var>beanListToCheck</var> (the parameter itself)
     * @see #checkGettersNotNull(Object)
     * @throws IllegalStateException If at least one of the public getters returns <code>null</code> or 0.
     */
    public final static <T> List<T> checkGettersNotNull(List<T> beanListToCheck)
    {
        if (beanListToCheck == null)
        {
            return beanListToCheck;
        }
        for (Object bean : beanListToCheck)
        {
            checkGettersNotNull(bean);
        }
        return beanListToCheck;
    }

    /**
     * Checks bean object for public getters which return <code>null</code> or 0.
     * 
     * @param beanToCheck The bean to check. Can be <code>null</code>. Must not be an array type.
     * @return <var>beanToCheck</var> (the parameter itself)
     * @throws IllegalArgumentException If the <var>beanToCheck</var> is an array type.
     * @throws IllegalStateException If at least one of the public getters returns <code>null</code> or 0.
     */
    public final static <T> T checkGettersNotNull(T beanToCheck)
    {
        if (beanToCheck == null)
        {
            return beanToCheck;
        }
        // TODO 2007-07-11, Franz-Josef Elmer: Why arrays are not checked? Why they are forbidden and not ignored?
        if (beanToCheck.getClass().isArray())
        {
            throw new IllegalArgumentException("Arrays are not supported.");
        }
        for (Method method : beanToCheck.getClass().getMethods())
        {
            if (method.getName().startsWith(GETTER_PREFIX) && method.getParameterTypes().length == 0
                    && Modifier.isPublic(method.getModifiers()))
            {
                try
                {
                    final Object result = method.invoke(beanToCheck, ArrayUtils.EMPTY_OBJECT_ARRAY);
                    if (result == null)
                    {
                        throw new IllegalStateException("Method '" + method.getName() + "' returns null.");
                    } else if (isNull(result))
                    {
                        throw new IllegalStateException("Method '" + method.getName() + "' returns 0.");
                    }
                } catch (InvocationTargetException ex)
                {
                    final Throwable cause = ex.getCause();
                    if (cause instanceof Error)
                    {
                        throw (Error) cause;
                    }
                    throw CheckedExceptionTunnel.wrapIfNecessary((Exception) cause);
                } catch (IllegalAccessException ex)
                {
                    // Can't happen since we checked for isPublic()
                    throw new Error("Cannot call method '" + method.getName() + "'.");
                }
            }
        }
        return beanToCheck;
    }

    // TODO 2007-07-11, Franz-Josef Elmer: Why numbers with a value rounded to 0 are forbidden?
    private static boolean isNull(Object objectToCheck)
    {
        return (objectToCheck instanceof Number) && ((Number) objectToCheck).longValue() == 0;
    }

    /**
     * A map that provides annotations for given annotation classes.
     */
    public interface AnnotationMap
    {
        /**
         * Returns the name of the entity that this annotation stems from.
         */
        public String getAnnotatedEntity();

        /**
         * Returns the annotation for <var>annotationClazz</var>.
         */
        public <T extends Annotation> T getAnnotation(Class<T> annotationClazz);
    }

    /**
     * An empty {@link AnnotationMap} (to be used when there are no annotations).
     */
    private static class EmptyAnnotationMap implements AnnotationMap
    {

        public String getAnnotatedEntity()
        {
            return "GENERIC";
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClazz)
        {
            return null;
        }

    }

    /**
     * An {@link AnnotationMap} that gets its annotation from a (setter) method.
     */
    private static class SetterAnnotationMap implements AnnotationMap
    {

        private final Method setterMethod;

        SetterAnnotationMap(Method setterMethod)
        {
            assert setterMethod != null;

            this.setterMethod = setterMethod;
        }

        public String getAnnotatedEntity()
        {
            return setterMethod.toGenericString();
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClazz)
        {
            return setterMethod.getAnnotation(annotationClazz);
        }

    }

    /**
     * Marker interface for converter classes. The real method are determined via reflection. A converter needs to match
     * both the source and the destination bean. If the destination bean has a setter <code>setFoo(FooClass foo)</code>
     * and the converter has a method <code>FooClass convertToFoo(SourceBeanClass sourceBean)</code>, then this will
     * be called instead of any getter of the <var>sourceBean</var>. Note that if there is a matching method
     * <code>convertToFoo()</code>, it needs to have an appropriate return type or else an
     * {@link IllegalArgumentException} will be thrown.
     */
    public interface Converter
    {
    }

    private static final AnnotationMap EMPTY_ANNOTATION_MAP = new EmptyAnnotationMap();

    private static final Converter NULL_CONVERTER = new Converter()
        {
        };

    @SuppressWarnings("unchecked")
    private static final Set<Class> immutableTypes =
            new HashSet<Class>(Arrays.asList(boolean.class, Boolean.class, byte.class, Byte.class, short.class,
                    Short.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class,
                    double.class, Double.class, String.class, Date.class));

    /**
     * Creates a new list of Beans of type <var>clazz</var>.
     * 
     * @param clazz element type of the new list.
     * @param sourceList The list to fill the new bean list from. Can be <code>null</code>, in which case the method
     *            returns <code>null</code>.
     * @return The new list filled from <var>sourceList</var> or <code>null</code>, if <var>sourceList</var> is
     *         <code>null</code>.
     */
    public final static <T, S> List<T> createBeanList(Class<T> clazz, List<S> sourceList)
    {
        return createBeanList(clazz, sourceList, null);
    }

    /**
     * Creates a new list of Beans of type <var>clazz</var>.
     * 
     * @param clazz element type of the new list.
     * @param sourceList The list to fill the new bean list from. Can be <code>null</code>, in which case the method
     *            returns <code>null</code>.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be
     *            <code>null</code>, in which case only standard conversions are allowed.
     * @return The new list filled from <var>sourceList</var> or <code>null</code>, if <var>sourceList</var> is
     *         <code>null</code>.
     */
    public final static <T, S> List<T> createBeanList(Class<T> clazz, List<S> sourceList, Converter converter)
    {
        assert clazz != null;

        if (sourceList == null)
        {
            return null;
        }

        final List<T> resultList = new ArrayList<T>();
        for (S element : sourceList)
        {
            resultList.add(BeanUtils.createBean(clazz, element, converter));
        }
        return resultList;
    }

    /**
     * Convenience method for {@link #createBean(Class, Object, ch.systemsx.cisd.common.utilities.BeanUtils.Converter)}
     * where <var>converter</var> is <code>NULL_CONVERTER</code>.
     */
    public static <T> T fillBean(Class<T> beanClass, T beanInstance, Object sourceBean)
    {
        return fillBean(beanClass, beanInstance, sourceBean, EMPTY_ANNOTATION_MAP, NULL_CONVERTER);
    }

    /**
     * Convenience method for {@link #createBean(Class, Object, ch.systemsx.cisd.common.utilities.BeanUtils.Converter)}
     * where <var>converter</var> is <code>NULL_CONVERTER</code>.
     */
    public static <T> T createBean(Class<T> beanClass, Object sourceBean)
    {
        return fillBean(beanClass, null, sourceBean, EMPTY_ANNOTATION_MAP, NULL_CONVERTER);
    }

    /**
     * Fills a new bean <var>beanInstance</var> of type <var>beanClass</var> with values from <var>sourceBean</var>.
     * 
     * @param beanClass The class to create a new instance from.
     * @param beanInstance Instance of the bean to be filled. If <code>null</code> a new instance will be created.
     * @param sourceBean The bean to get the values from. Can be <code>null</code>, in which case the method returns
     *            <code>null</code>.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be
     *            <code>null</code>, in which case only standard conversions are allowed.
     * @return The new bean or <code>null</code> if <var>sourceBean</var> is <code>null</code>.
     */
    public static <T> T fillBean(Class<T> beanClass, T beanInstance, Object sourceBean, Converter converter)
    {
        Converter c = converter;
        if (c == null)
        {
            c = NULL_CONVERTER;
        }
        return fillBean(beanClass, beanInstance, sourceBean, EMPTY_ANNOTATION_MAP, c);
    }

    /**
     * Creates a new bean of type <var>beanClass</var> and fills it with values from <var>sourceBean</var>.
     * 
     * @param beanClass The class to create a new instance from.
     * @param sourceBean The bean to get the values from. Can be <code>null</code>, in which case the method returns
     *            <code>null</code>.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be
     *            <code>null</code>, in which case only standard conversions are allowed.
     * @return The new bean or <code>null</code> if <var>sourceBean</var> is <code>null</code>.
     */
    public static <T> T createBean(Class<T> beanClass, Object sourceBean, Converter converter)
    {
        Converter c = converter;
        if (c == null)
        {
            c = NULL_CONVERTER;
        }
        return fillBean(beanClass, null, sourceBean, EMPTY_ANNOTATION_MAP, c);
    }

    /**
     * Fills the specified bean instance with values from <var>sourceBean</var>.
     * 
     * @param beanClass The class to create a new instance from.
     * @param beanInstance Instance of the bean to be filled. If <code>null</code> a new instance will be created.
     * @param sourceBean The bean to get the values from. Can be <code>null</code>, in which case the method returns
     *            <code>null</code>.
     * @param setterAnnotations The annotations attached to the setter that can be used to determine how the result
     *            should be created.
     * @param converter The {@link Converter} to use to perform non-standard conversions when filling the bean. Can be
     *            <code>null</code>, in which case only standard conversions are allowed.
     * @return The new bean or <code>null</code> if <var>sourceBean</var> is <code>null</code>.
     */
    private static <T> T fillBean(Class<T> beanClass, T beanInstance, Object sourceBean,
            AnnotationMap setterAnnotations, Converter converter)
    {
        assert beanClass != null : "undefined bean class";
        assert setterAnnotations != null : "undefined setter annotations for " + beanClass;
        assert converter != null : "undefined converter for " + beanClass;

        if (sourceBean == null)
        {
            return null;
        }

        try
        {
            final T destinationBean =
                    (beanInstance != null) ? beanInstance : instantiateBean(beanClass, sourceBean, setterAnnotations);
            if (isArray(destinationBean))
            {
                if (isArray(sourceBean))
                {
                    copyArrayToArray(destinationBean, sourceBean, converter);
                } else if (isCollection(sourceBean))
                {
                    copyCollectionToArray(destinationBean, (Collection<?>) sourceBean, converter);
                }
            } else if (isCollection(destinationBean))
            {
                if (isArray(sourceBean))
                {
                    copyArrayToCollection((Collection<?>) destinationBean, sourceBean, setterAnnotations, converter);
                } else if (isCollection(sourceBean))
                {
                    copyCollectionToCollection((Collection<?>) destinationBean, (Collection<?>) sourceBean,
                            setterAnnotations, converter);
                }
            } else
            {
                copyBean(destinationBean, sourceBean, converter);
            }
            return destinationBean;
        } catch (InvocationTargetException ex)
        {
            final Throwable cause = ex.getCause();
            if (cause instanceof Error)
            {
                throw (Error) cause;
            }
            throw CheckedExceptionTunnel.wrapIfNecessary((Exception) cause);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static boolean isCollection(Object o)
    {
        return o instanceof Collection<?>;
    }

    private static boolean isCollection(Class<?> clazz)
    {
        return Collection.class.isAssignableFrom(clazz);
    }

    private static boolean isArray(Object o)
    {
        return o.getClass().isArray();
    }

    private static boolean isArray(final Class<?> clazz)
    {
        return clazz.isArray();
    }

    private static <T> T instantiateBean(Class<T> beanClass, Object sourceBean, AnnotationMap setterAnnotations)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        if (sourceBean == null)
        {
            return null;
        }
        final boolean beanIsArray = isArray(beanClass);
        final boolean beanIsCollection = isCollection(beanClass);
        if (beanIsArray || beanIsCollection)
        {
            final Integer size = getSize(sourceBean);
            if (size == null)
            {
                return null;
            }
            if (beanIsArray)
            {
                return createArray(beanClass, size);
            }
            if (beanIsCollection)
            {
                return createCollection(beanClass, size, setterAnnotations);
            }
        }
        return beanClass.newInstance();
    }

    private static Integer getSize(Object o)
    {
        if (isArray(o))
        {
            return Array.getLength(o);
        } else if (isCollection(o))
        {
            return ((Collection<?>) o).size();
        } else
        { // Don't know how to get the size of o.
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createArray(Class<T> beanClass, int length) throws NegativeArraySizeException
    {
        return (T) Array.newInstance(beanClass.getComponentType(), length);
    }

    @SuppressWarnings("unchecked")
    // No way to avoid the warning since the compiler doesn't accept something like ArrayList<String>.class
    private static <T> T createCollection(Class<T> beanClass, int size, AnnotationMap setterAnnotations)
            throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException
    {
        final CollectionMapping mapping = getCollectionMapping(setterAnnotations);
        try
        {
            final Constructor<? extends Collection> constructorWithSize =
                    mapping.collectionClass().getConstructor(new Class[]
                        { int.class });
            return constructCollection(constructorWithSize, size);
        } catch (NoSuchMethodException ex)
        { // Happens e.g. for a LinkedList
            return constructCollection(mapping.collectionClass());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T constructCollection(final Constructor<? extends Collection> constructorWithSize, int size)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        // This conversion _can_ go wrong if the concrete collection class doesn't implement the right sub-interface of
        // collection, e.g. when using a HashSet as concrete collection class where a List is required.
        return (T) constructorWithSize.newInstance(new Object[]
            { size });
    }

    @SuppressWarnings("unchecked")
    private static <T> T constructCollection(final Class<? extends Collection> collectionClazz)
            throws InstantiationException, IllegalAccessException
    {
        // This conversion _can_ go wrong if the concrete collection class doesn't implement the right sub-interface of
        // collection, e.g. when using a HashSet as concrete collection class where a List is required.
        return (T) collectionClazz.newInstance();
    }

    private static void copyArrayToArray(Object destination, Object source, Converter converter)
            throws IllegalAccessException, InvocationTargetException
    {
        if (destination == null)
        {
            return;
        }
        final Class<?> componentType = destination.getClass().getComponentType();
        final int length = Array.getLength(destination);
        if (immutableTypes.contains(componentType))
        {
            if (componentType == source.getClass().getComponentType())
            {
                System.arraycopy(source, 0, destination, 0, length);
            } else
            {
                for (int index = 0; index < length; ++index)
                {
                    final Object sourceElement = Array.get(source, index);
                    Array.set(destination, index, sourceElement);
                }
            }
        } else
        {
            for (int index = 0; index < length; ++index)
            {
                final Object sourceElement = Array.get(source, index);
                final Object destinationElement = createBean(componentType, sourceElement, converter);
                Array.set(destination, index, destinationElement);
            }
        }
    }

    private static void copyCollectionToArray(Object destination, Collection<?> source, Converter converter)
            throws IllegalAccessException, InvocationTargetException
    {
        if (destination == null)
        {
            return;
        }
        assert Array.getLength(destination) == source.size();
        final Class<?> componentType = destination.getClass().getComponentType();
        if (immutableTypes.contains(componentType))
        {
            int index = 0;
            for (Object sourceElement : source)
            {
                Array.set(destination, index++, sourceElement);
            }
        } else
        {
            int index = 0;
            for (Object sourceElement : source)
            {
                final Object destinationElement = createBean(componentType, sourceElement, converter);
                Array.set(destination, index++, destinationElement);
            }
        }
    }

    private static void copyArrayToCollection(Collection<?> destination, Object source,
            AnnotationMap setterAnnotations, Converter converter)
    {
        if (destination == null)
        {
            return;
        }
        final Class<?> componentType = getCollectionComponentType(setterAnnotations);
        final int length = getSize(source);
        if (immutableTypes.contains(componentType))
        {
            for (int index = 0; index < length; ++index)
            {
                final Object sourceElement = Array.get(source, index);
                addToUntypedCollection(destination, sourceElement);
            }
        } else
        {
            for (int index = 0; index < length; ++index)
            {
                final Object sourceElement = Array.get(source, index);
                final Object destinationElement = createBean(componentType, sourceElement, converter);
                addToUntypedCollection(destination, destinationElement);
            }
        }
    }

    private static void copyCollectionToCollection(Collection<?> destination, Collection<?> source,
            AnnotationMap setterAnnotations, Converter converter)
    {
        if (destination == null)
        {
            return;
        }
        final Class<?> componentType = getCollectionComponentType(setterAnnotations);
        if (immutableTypes.contains(componentType))
        {
            for (Object sourceElement : source)
            {
                addToUntypedCollection(destination, sourceElement);
            }
        } else
        {
            for (Object sourceElement : source)
            {
                final Object destinationElement = createBean(componentType, sourceElement, converter);
                addToUntypedCollection(destination, destinationElement);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void addToUntypedCollection(Collection destination, Object element)
    {
        destination.add(element);
    }

    private static Class<?> getCollectionComponentType(AnnotationMap setterAnnotations)
    {
        return getCollectionMapping(setterAnnotations).elementClass();
    }

    private static CollectionMapping getCollectionMapping(AnnotationMap setterAnnotations)
    {
        final CollectionMapping mapping = setterAnnotations.getAnnotation(CollectionMapping.class);
        if (mapping == null)
        {
            throw new IllegalArgumentException("No collection mapping specified for '"
                    + setterAnnotations.getAnnotatedEntity() + "'.");
        }
        return mapping;
    }

    private static void copyBean(Object destination, Object source, Converter converter) throws IllegalAccessException,
            InvocationTargetException
    {
        if (destination == null)
        {
            return;
        }
        final Collection<Method> destinationSetters = scanForPublicMethods(destination, SETTER_PREFIX, 1).values();
        final Map<String, Method> sourceGetters = scanForPublicMethods(source, GETTER_PREFIX, 0);
        scanForPublicMethods(source, sourceGetters, BOOLEAN_GETTER_PREFIX, 0, boolean.class, Boolean.class);
        for (Method setter : destinationSetters)
        {
            final Object newBean = emergeNewBean(setter, source, sourceGetters, converter);
            if (newBean != null)
            {
                try
                {
                    setter.invoke(destination, new Object[]
                        { newBean });
                } catch (IllegalArgumentException ex)
                {
                    final String defaultJavaArgumentTypeMismatchMessage = "argument type mismatch";
                    if (defaultJavaArgumentTypeMismatchMessage.equals(ex.getMessage()))
                    {
                        throw new IllegalArgumentException(defaultJavaArgumentTypeMismatchMessage + ": method '"
                                + setter.toGenericString() + "': cannot assign from '"
                                + newBean.getClass().getCanonicalName() + "'.");
                    } else
                    {
                        throw ex;
                    }
                }
            }
        }
    }

    private static Object emergeNewBean(Method setter, Object source, Map<String, Method> sourceGetters,
            Converter converter) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        final AnnotationMap annotationMap = new SetterAnnotationMap(setter);
        final Method converterMethod = getConverterMethod(setter, source, converter);
        if (converterMethod != null)
        {
            return converterMethod.invoke(converter, new Object[]
                { source });
        }
        final Method getter = getGetter(setter, sourceGetters, annotationMap);
        if (getter == null)
        {
            return null;
        }
        final Object oldBean = getter.invoke(source, ArrayUtils.EMPTY_OBJECT_ARRAY);
        final Class<?> parameterType = setter.getParameterTypes()[0];
        if (parameterType.isPrimitive() || immutableTypes.contains(parameterType))
        {
            return oldBean;
        } else
        {
            return fillBean(parameterType, null, oldBean, annotationMap, converter);
        }
    }

    private static Method getConverterMethod(Method setter, Object sourceBean, Converter converter)
    {
        if (converter != NULL_CONVERTER)
        {
            try
            {
                final Method converterMethod =
                        converter.getClass().getMethod(
                                "convertTo" + setter.getName().substring(SETTER_PREFIX.length()), new Class[]
                                    { sourceBean.getClass() });
                if (converterMethod.isAccessible() == false)
                {
                    converterMethod.setAccessible(true);
                }
                return converterMethod;
            } catch (NoSuchMethodException ex)
            {
                // Nothing to do here - there just isn't any converter method for this setter.
            }
        }
        return null;
    }

    private static Method getGetter(Method setter, Map<String, Method> sourceGetters, AnnotationMap annotationMap)
    {
        String propertyName = setter.getName().substring(SETTER_PREFIX.length());
        final Method getter = sourceGetters.get(GETTER_PREFIX + propertyName);
        if (getter != null)
        {
            return getter;
        } else
        {
            final Class<?> type = setter.getParameterTypes()[0];
            // If parameter type is boolean, then try with 'is' as prefix
            if (type == boolean.class || type == Boolean.class)
            {
                return sourceGetters.get(BOOLEAN_GETTER_PREFIX + propertyName);
            }
        }
        return null;
    }

    private static Map<String, Method> scanForPublicMethods(Object bean, String prefix, int numberOfParameters)
    {
        final Map<String, Method> methodMap = new HashMap<String, Method>();
        scanForPublicMethods(bean, methodMap, prefix, numberOfParameters, (Set<Class<?>>) null);
        return methodMap;
    }

    private static void scanForPublicMethods(Object bean, Map<String, Method> methodMap, String prefix,
            int numberOfParameters, Class<?>... returnValueTypes)
    {
        scanForPublicMethods(bean, methodMap, prefix, numberOfParameters, new HashSet<Class<?>>(Arrays
                .asList(returnValueTypes)));
    }

    private static void scanForPublicMethods(Object bean, Map<String, Method> methodMap, String prefix,
            int numberOfParameters, Set<Class<?>> returnValueTypes)
    {
        for (Method method : bean.getClass().getMethods())
        {
            final String methodName = method.getName();
            if (methodName.startsWith(prefix) && method.getParameterTypes().length == numberOfParameters
                    && Modifier.isPublic(method.getModifiers()))
            {
                if (returnValueTypes == null || returnValueTypes.contains(method.getReturnType()))
                {
                    methodMap.put(methodName, method);
                }
            }
        }
    }

    /**
     * Returns a map of <code>PropertyDescriptor</code>s keyed by {@link PropertyDescriptor#getName()}.
     * <p>
     * It introspects given class and remove each (bean) property that does not have a write method (like
     * <code>class</code>).
     * </p>
     */
    public final static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz)
    {
        try
        {
            Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
            final List<PropertyDescriptor> descriptors =
                    new ArrayList<PropertyDescriptor>(Arrays.asList(Introspector.getBeanInfo(clazz)
                            .getPropertyDescriptors()));
            for (Iterator<PropertyDescriptor> iter = descriptors.iterator(); iter.hasNext();)
            {
                final PropertyDescriptor descriptor = iter.next();
                // If no write method, remove it. For instance 'class' property does not have any
                // write method.
                if (descriptor.getWriteMethod() != null)
                {
                    // Put the descriptor name in lower case.
                    map.put(descriptor.getName().toLowerCase(), descriptor);
                }
            }
            return map;
        } catch (IntrospectionException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

}
