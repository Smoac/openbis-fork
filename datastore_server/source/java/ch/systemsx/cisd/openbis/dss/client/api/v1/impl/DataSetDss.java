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

package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

import java.io.File;
import java.io.InputStream;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetDss implements IDataSetDss
{
    private final String code;

    private final IDssServiceRpcGeneric service;

    private final AuthenticatedState parent;

    public DataSetDss(String code, IDssServiceRpcGeneric service, AuthenticatedState parent)
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

    public FileInfoDssDTO[] listFiles(String startPath, boolean isRecursive)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.listFiles(this, startPath, isRecursive);
    }

    public File tryLinkToContents(String overrideStoreRootPathOrNull)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.tryLinkToContents(this, overrideStoreRootPathOrNull);
    }

    public File getLinkOrCopyOfContents(String overrideStoreRootPathOrNull, File downloadDir)
            throws IllegalArgumentException, InvalidSessionException
    {
        return parent.getLinkOrCopyOfContents(this, overrideStoreRootPathOrNull, downloadDir);
    }

    public AuthenticatedState getParent()
    {
        return parent;
    }

    public IDssServiceRpcGeneric getService()
    {
        return service;
    }

}
