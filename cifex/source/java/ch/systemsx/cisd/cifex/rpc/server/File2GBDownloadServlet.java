/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.server;

import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClient;
import ch.systemsx.cisd.cifex.server.AbstractFileUploadDownloadServlet;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;

/**
 * A servlet for delivering the WebStart download client.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class File2GBDownloadServlet extends AbstractFileUploadDownloadServlet
{
    private static final long serialVersionUID = 1L;

    public File2GBDownloadServlet()
    {
    }

    protected File2GBDownloadServlet(IExtendedCIFEXRPCService service, IDomainModel domainModel)
    {
        super(service, domainModel);
    }

    @Override
    protected String getOperationName()
    {
        return "download";
    }

    @Override
    protected String getTitle()
    {
        return "CIFEX File Downloader";
    }

    @Override
    protected String getMainClassName()
    {
        return FileDownloadClient.class.getName();
    }

}
