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

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.common.exceptions.UserFailureException;


/**
 * Factory for objects of type {@link IFormatedData}.
 *
 * @author Franz-Josef Elmer
 */
class FormatedDataFactory
{
    private static final Map<String, Factory<IFormatedData>> factories = new HashMap<String, Factory<IFormatedData>>();
    
    static
    {
        register(UnknownFormat1_0.UNKNOWN_1_0, NoFormatedData.class);
    }
    
    static void register(Format format, Class<? extends IFormatedData> clazz)
    {
        String code = format.getCode();
        Factory<IFormatedData> factory = factories.get(code);
        if (factory == null)
        {
            factory = new Factory<IFormatedData>();
            factories.put(code, factory);
        }
        factory.register(format.getVersion(), clazz);
    }
    
    static Class<? extends IFormatedData> getFormatedDataInterfaceFor(Format format, Format defaultFormat)
    {
        Factory<IFormatedData> factory = getFactory(format, defaultFormat);
        return factory.getClassFor(format.getVersion());
    }

    static IFormatedData createFormatedData(IDirectory dataDirectory, Format format, Format defaultFormat)
    {
        Factory<IFormatedData> factory = getFactory(format, defaultFormat);
        return factory.create(IDirectory.class, dataDirectory, format.getVersion());
    }

    private static Factory<IFormatedData> getFactory(Format format, Format defaultFormat)
    {
        assert format != null : "Unspecified format.";
        String code = format.getCode();
        assert code != null : "Unspecified format code.";
        assert format.getVersion() != null : "Unspecified version.";
        
        Factory<IFormatedData> factory = factories.get(code);
        if (factory == null)
        {
            if (defaultFormat != null)
            {
                return getFactory(defaultFormat, null);
            }
            throw new UserFailureException("Unkown format code: " + code);
        }
        return factory;
    }
    
}
