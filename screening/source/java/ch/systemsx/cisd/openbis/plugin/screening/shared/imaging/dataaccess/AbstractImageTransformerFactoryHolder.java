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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

import org.apache.commons.lang.SerializationUtils;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractImageTransformerFactoryHolder extends AbstractHashable
{
    @ResultColumn("IMAGE_TRANSFORMER_FACTORY")
    private byte[] serializedImageTransformerFactory;

    public final byte[] getSerializedImageTransformerFactory()
    {
        return serializedImageTransformerFactory;
    }

    public final void setSerializedImageTransformerFactory(byte[] serializedImageTransformerFactory)
    {
        this.serializedImageTransformerFactory = serializedImageTransformerFactory;
    }
    
    public IImageTransformerFactory getImageTransformerFactory()
    {
        if (serializedImageTransformerFactory == null)
        {
            return null;
        }
        return (IImageTransformerFactory) SerializationUtils.deserialize(serializedImageTransformerFactory);
    }

}
