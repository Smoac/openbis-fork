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

package ch.systemsx.cisd.bds;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * General purpose factory for versioned classes with one-argument constructors.
 *
 * @author Franz-Josef Elmer
 */
class Factory<T>
{
    private final Map<Version, Class<? extends T>> repository = new HashMap<Version, Class<? extends T>>();

    void register(Version version, Class<? extends T> clazz)
    {
        repository.put(version, clazz);
    }

    Class<? extends T> getClassFor(Version version)
    {
        
        Version v = version;
        while (true)
        {
            Class<? extends T> clazz = repository.get(v);
            if (clazz != null)
            {
                return clazz;
            }
            if (v.getMinor() == 0)
            {
                throw new DataStructureException("No class found for version " + version);
            }
            v = v.getPreviousMinorVersion();
        }
    }

    T create(Class<?> argumentClass, Object argument, Version version)
    {
        Class<? extends T> clazz = getClassFor(version);
        Constructor<? extends T> constructor;
        try
        {
            constructor = clazz.getConstructor(new Class[] {argumentClass});
        } catch (Exception ex1)
        {
            throw new EnvironmentFailureException(clazz + " has no constructor with argument of type "
                    + argumentClass.getCanonicalName());
        }
        try
        {
            return constructor.newInstance(new Object[] {argument});
        } catch (InvocationTargetException ex)
        {
            throw new DataStructureException("Couldn't create instance of " + clazz + " for version " + version, ex
                    .getCause());
        } catch (Exception ex)
        {
            throw new DataStructureException("Couldn't create instance of " + clazz + " for version " + version, ex);
        }
    }

}
