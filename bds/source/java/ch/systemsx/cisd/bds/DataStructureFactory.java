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

import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataStructureFactory
{
    private static final Map<Version, Class<? extends AbstractDataStructure>> repository =
            new HashMap<Version, Class<? extends AbstractDataStructure>>();
    
    void register(Version version, Class<? extends AbstractDataStructure> clazz)
    {
        repository.put(version, clazz);
    }
    
    public static Class<? extends AbstractDataStructure> getDataStructureClassFor(Version version)
    {
        Class<? extends AbstractDataStructure> clazz = null;
        for (Version v = version; v.getMinor() >= 0 && clazz == null; v = v.getPreviousMinorVersion())
        {
            clazz = repository.get(v);
        }
        if (clazz == null)
        {
            throw new UserFailureException("No data structure class found for version " + version);
        }
        return clazz;
    }
    
    public static AbstractDataStructure createDataStructure(IStorage storage, Version version)
    {
        Class<? extends AbstractDataStructure> clazz = getDataStructureClassFor(version);
        Constructor<? extends AbstractDataStructure> constructor;
        try
        {
            constructor = clazz.getConstructor(new Class[] {IStorage.class});
        } catch (Exception ex1)
        {
            throw new EnvironmentFailureException(clazz + " has no constructor with argument of type "
                    + IStorage.class.getCanonicalName());
        }
        try
        {
            return constructor.newInstance(new Object[] {storage});
        } catch (InvocationTargetException ex)
        {
            throw new UserFailureException("Couldn't create data structure for version " + version, ex.getCause());
        } catch (Exception ex)
        {
            throw new UserFailureException("Couldn't create data structure for version " + version, ex);
        }
    }

    private DataStructureFactory()
    {
    }
}
