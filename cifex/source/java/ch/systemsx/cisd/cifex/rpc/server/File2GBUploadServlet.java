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

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileUploadClient;
import ch.systemsx.cisd.cifex.server.AbstractFileUploadDownloadServlet;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;

/**
 * Servlet which are triggered by the Web client in order to start the Java upload client via Java
 * Web Start.
 * 
 * @author Franz-Josef Elmer
 */
public class File2GBUploadServlet extends AbstractFileUploadDownloadServlet
{
    private static final long serialVersionUID = 1L;

    public File2GBUploadServlet()
    {
    }

    @Private
    File2GBUploadServlet(IExtendedCIFEXRPCService service, IDomainModel domainModel)
    {
        super(service, domainModel);
    }

    @Override
    protected String getOperationName()
    {
        return "upload";
    }

    @Override
    protected String getTitle()
    {
        return "CIFEX File Uploader";
    }

    @Override
    protected String getMainClassName()
    {
        return FileUploadClient.class.getName();
    }

}
