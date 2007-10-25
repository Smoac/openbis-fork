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


/**
 * Factory for objects of type {@link IFormattedData}.
 *
 * @author Franz-Josef Elmer
 */
class FormatedDataFactory
{
    private static final Map<String, Factory<IFormattedData>> factories = new HashMap<String, Factory<IFormattedData>>();
    
    static
    {
        register(UnknownFormat1_0.UNKNOWN_1_0, NoFormattedData.class);
    }
    
    static void register(Format format, Class<? extends IFormattedData> clazz)
    {
        String code = format.getCode();
        Factory<IFormattedData> factory = factories.get(code);
        if (factory == null)
        {
            factory = new Factory<IFormattedData>();
            factories.put(code, factory);
        }
        factory.register(format.getVersion(), clazz);
    }
    
    static Class<? extends IFormattedData> getFormatedDataInterfaceFor(Format format, Format defaultFormat)
    {
        Factory<IFormattedData> factory = getFactory(format, defaultFormat);
        return factory.getClassFor(format.getVersion());
    }

    static IFormattedData createFormatedData(IDirectory dataDirectory, Format format, Format defaultFormat)
    {
        Factory<IFormattedData> factory = getFactory(format, defaultFormat);
        return factory.create(IDirectory.class, dataDirectory, format.getVersion());
    }

    private static Factory<IFormattedData> getFactory(Format format, Format defaultFormat)
    {
        assert format != null : "Unspecified format.";
        String code = format.getCode();
        assert code != null : "Unspecified format code.";
        assert format.getVersion() != null : "Unspecified version.";
        
        Factory<IFormattedData> factory = factories.get(code);
        if (factory == null)
        {
            if (defaultFormat != null)
            {
                return getFactory(defaultFormat, null);
            }
            throw new DataStructureException("Unknown format code: " + code);
        }
        return factory;
    }
    
}
