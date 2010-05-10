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

package ch.systemsx.cisd.openbis.knime.query;

import static ch.systemsx.cisd.openbis.knime.query.QueryNodeModel.PASSWORD_KEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.util.KnimeEncryption;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumnDataType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Util
{
    static byte[] serializeQueryDescription(QueryDescription queryDescriptionOrNull)
    {
        if (queryDescriptionOrNull == null)
        {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            new ObjectOutputStream(baos).writeObject(queryDescriptionOrNull);
            return baos.toByteArray();
        } catch (IOException ex)
        {
            return null;
        }
    }
    
    static QueryDescription deserializeQueryDescription(byte[] serializeQueryDescriptionOrNull)
    {
        if (serializeQueryDescriptionOrNull == null)
        {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(serializeQueryDescriptionOrNull);
        try
        {
            return (QueryDescription) new ObjectInputStream(bais).readObject();
        } catch (Exception ex)
        {
            return null;
        }
    }
    
    static ColumnType getColumnType(QueryTableColumnDataType dataType)
    {
        switch (dataType)
        {
            case DOUBLE: return ColumnType.DOUBLE;
            case LONG: return ColumnType.LONG;
            default: return ColumnType.STRING;
        }
    }

    static String getDecryptedPassword(NodeSettingsRO settings)
    {
        try
        {
            return KnimeEncryption.decrypt(settings.getString(PASSWORD_KEY, ""));
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    static String getEncryptedPassword(char[] bytes)
    {
        try
        {
            return KnimeEncryption.encrypt(bytes);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
}
