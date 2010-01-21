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

package ch.systemsx.cisd.cifex.rpc.client;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * The implementation of {@link ICIFEXComponent}.
 * 
 * @author Bernd Rinn
 */
public class CIFEXComponent implements ICIFEXComponent
{
    private final ICIFEXRPCService service;

    public CIFEXComponent(ICIFEXRPCService service)
    {
        this.service = service;
    }

    public ICIFEXDownloader createDownloader(final String sessionID)
    {
        return Downloader.create(service, sessionID);
    }

    public ICIFEXUploader createUploader(final String sessionID)
    {
        return new Uploader(service, sessionID);
    }

    public void deleteFile(final String sessionID, final long fileId)
            throws InvalidSessionException, UserFailureException
    {
        service.deleteFile(sessionID, fileId);
    }

    public FileInfoDTO[] listDownloadFiles(final String sessionID) throws InvalidSessionException,
            EnvironmentFailureException
    {
        return service.listDownloadFiles(sessionID);
    }

    public FileInfoDTO[] listOwnedFiles(final String sessionID) throws InvalidSessionException,
            EnvironmentFailureException
    {
        return service.listOwnedFiles(sessionID);
    }

    public String login(final String user, final String password)
            throws AuthorizationFailureException
    {
        return service.login(user, password);
    }

    public void logout(final String sessionID)
    {
        service.logout(sessionID);
    }

    public void setSessionUser(String sessionID, String newSessionUser)
    {
        service.setSessionUser(sessionID, newSessionUser);
    }

    public void checkSession(final String sessionID) throws InvalidSessionException
    {
        service.checkSession(sessionID);
    }

}
