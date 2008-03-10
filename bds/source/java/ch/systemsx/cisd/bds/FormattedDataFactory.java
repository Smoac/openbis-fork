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

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.hcs.HCSImageFormatV1_0;
import ch.systemsx.cisd.bds.hcs.HCSImageFormattedData;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Factory for objects of type {@link IFormattedData}.
 * 
 * @author Franz-Josef Elmer
 */
public final class FormattedDataFactory
{
    private static final Map<String, Factory<IFormattedData>> factories =
            new HashMap<String, Factory<IFormattedData>>();

    static
    {
        register(UnknownFormatV1_0.UNKNOWN_1_0, NoFormattedData.class);
        register(HCSImageFormatV1_0.HCS_IMAGE_1_0, HCSImageFormattedData.class);
    }

    private FormattedDataFactory()
    {
        // This class cannot be instantiated.
    }

    private final static void register(final Format format, final Class<? extends IFormattedData> clazz)
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

    /** Uses this public method to create a new instance of <code>IFormattedData</code>. */
    public final static IFormattedData createFormattedData(final IDirectory dataDirectory, final Format format,
            final Format defaultFormatOrNull, final IFormatParameters formatParameters)
    {
        final Format supportedFormat = getSupportedFormat(format, defaultFormatOrNull);
        final Factory<IFormattedData> factory = factories.get(supportedFormat.getCode());
        final FormattedDataContext context = new FormattedDataContext(dataDirectory, supportedFormat, formatParameters);
        return factory.create(FormattedDataContext.class, context, format.getVersion());
    }

    private final static Format getSupportedFormat(final Format format, final Format defaultFormatOrNull)
    {
        assert format != null : "Unspecified format.";
        final String code = format.getCode();
        assert code != null : "Unspecified format code.";
        assert format.getVersion() != null : "Unspecified version.";
        
        final Factory<IFormattedData> factory = factories.get(code);
        if (factory == null)
        {
            if (defaultFormatOrNull != null)
            {
                return getSupportedFormat(defaultFormatOrNull, null);
            }
            throw new DataStructureException("Unknown format code '" + code + "'.");
        }
        return format;
    }

}
