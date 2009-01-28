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

package ch.systemsx.cisd.cifex.upload.server;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.upload.IUploadService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IExtendedUploadService extends IUploadService
{
    /**
     * Creates a new upload session for the specified user, URL, files, e-mails of the recipients,
     * and comment.
     * 
     * @return unique upload session ID.
     */
    public String createSession(UserDTO user, String url);
}
