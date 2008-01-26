/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business.dto;

import java.util.Date;
import java.util.List;

/**
 * An <code>ID</code> extension which describes a file in the database.
 * 
 * @author Izabela Adamczyk
 */
public final class FileDTO extends ID
{
    private String name;

    private String path;

    /**
     * Represents registerer of the file. Id of registerer will be always filled while loading from database, but other
     * fields will be filled only by some loading methods (e.g getFile(Long id)).
     */
    private UserDTO registerer;

    private Date registrationDate;

    private Date expirationDate;

    /** Ids of users the file will be shared with. Not all FileDAO loading methods are obligated to fill this list. */
    private List<UserDTO> sharingUsers;

    private final Long registererId;

    private String contentType;

    public FileDTO(final Long registererId)
    {
        this.registererId = registererId;
    }

    public String getName()
    {
        return name;
    }

    public final void setName(final String name)
    {
        this.name = name;
    }

    public final String getPath()
    {
        return path;
    }

    public final void setPath(final String path)
    {
        this.path = path;
    }

    public final UserDTO getRegisterer()
    {
        return registerer;
    }

    public final void setRegisterer(final UserDTO registerer)
    {
        getAndCheckID(registerer, registererId);
        this.registerer = registerer;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final Date getExpirationDate()
    {
        return expirationDate;
    }

    public final void setExpirationDate(final Date expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public List<UserDTO> getSharingUsers()
    {
        return sharingUsers;
    }

    public void setSharingUsers(final List<UserDTO> sharingUsers)
    {
        this.sharingUsers = sharingUsers;
    }

    public final Long getRegistererId()
    {
        return registererId;
    }

    public final void setContentType(final String contentType)
    {
        this.contentType = contentType;
    }

    public final String getContentType()
    {
        return contentType;
    }

}
