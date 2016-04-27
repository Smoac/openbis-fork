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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Some utilities around <i>Java Bean</i>s.
 * 
 * @author Christian Ribeaud
 */
public final class BeanUtils
{

    private BeanUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Encodes given object into a XML string.
     * <p>
     * To decode the returned XML string, you should use {@link #xmlDecode(String)}.
     * </p>
     */
    public final static String xmlEncode(Object object)
    {
        assert object != null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(out));
        encoder.writeObject(object);
        encoder.close();
        return out.toString();
    }

    /**
     * This method decodes given XML string into an <code>Object</code>.
     * <p>
     * By using this method, we assume that the returned <code>Object</code> has been encoded using
     * {@link #xmlEncode(Object)}.
     * </p>
     */
    public final static Object xmlDecode(String xmlString)
    {
        assert xmlString != null;
        ByteArrayInputStream in = new ByteArrayInputStream(xmlString.getBytes());
        XMLDecoder encoder = new XMLDecoder(new BufferedInputStream(in));
        Object result = encoder.readObject();
        encoder.close();
        return result;
    }
}
