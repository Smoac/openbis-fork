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

package ch.systemsx.cisd.openbis.dss.component.impl;

import java.io.InputStream;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.component.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDss;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpcV1;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetDss implements IDataSetDss
{
    private final String code;

    private final IDssServiceRpcV1 service;

    private final AuthenticatedState parent;

    // private String ownerUrl;

    public DataSetDss(String code, IDssServiceRpcV1 service, AuthenticatedState parent)
    {
        this.code = code;
        this.service = service;
        this.parent = parent;
    }

    public String getCode()
    {
        return code;
    }

    public InputStream getFile(String path) throws IllegalArgumentException,
            InvalidSessionException
    {
        return parent.getFile(this, path);
    }

    public FileInfoDss[] listFiles(String startPath, boolean isRecursive)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.listFiles(this, startPath, isRecursive);
    }

    IDssServiceRpcV1 getService()
    {
        return service;
    }

}
