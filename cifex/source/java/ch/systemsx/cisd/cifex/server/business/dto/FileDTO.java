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

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An <code>ID</code> extension which describes a file in the database.
 * 
 * @author Izabela Adamczyk
 */
public final class FileDTO extends ID
{

    private static final long serialVersionUID = 1L;

    /** The relative location of this file. */
    private String path;
    
    /** The comment the uploader provided. */
    private String comment;

    /**
     * Represents registrator of the file.
     */
    private UserDTO registerer;

    /** The date at which this file was registered. */
    private Date registrationDate;

    /** The date at which this file will expire and be deleted. */
    private Date expirationDate;

    /**
     * Users the file will be shared with. Not all FileDAO loading methods are obligated to fill
     * this list.
     */
    private List<UserDTO> sharingUsers = Collections.emptyList();

    private final Long registratorId;

    private final BasicFileDTO basicFileDTO = new BasicFileDTO();

    public FileDTO(final Long registererId)
    {
        this.registratorId = registererId;
    }

    public final String getName()
    {
        return basicFileDTO.getName();
    }

    public final void setName(final String name)
    {
        this.basicFileDTO.setName(name);
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
        getAndCheckID(registerer, registratorId);
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

    public final Long getRegistratorId()
    {
        return registratorId;
    }

    public final void setContentType(final String contentType)
    {
        this.basicFileDTO.setContentType(contentType);
    }

    public final String getContentType()
    {
        return basicFileDTO.getContentType();
    }

    public final Long getSize()
    {
        return basicFileDTO.getSize();
    }

    public final void setSize(final long size)
    {
        this.basicFileDTO.setSize(size);
    }

    public Integer getCrc32Value()
    {
        return basicFileDTO.getCrc32Value();
    }

    public void setCrc32Value(int crc32Value)
    {
        basicFileDTO.setCrc32Value(crc32Value);
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

}
