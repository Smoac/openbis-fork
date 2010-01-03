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

package ch.systemsx.cisd.cifex.shared.basic.dto;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A client <i>DTO</i> which describes a file.
 * 
 * @author Christian Ribeaud
 */
public class FileInfoDTO implements IsSerializable, Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the file as a string.
     */
    private long id;

    /** File name. */
    private String name;

    /** The comment that the uploader provided. */
    private String comment;

    /**
     * Size of the file.
     * <p>
     * If <code>null</code> then the size has not been set.
     * </p>
     */
    private Long size;
    
    private long completeSize; 
    
    private int crc32Value;
    
    private String crc32Str;

    /**
     * Person who owns (controls) the file.
     */
    private BasicUserInfoDTO owner;

    /** User code of the person who (originally) registered the file. */
    private String registratorCode;

    /**
     * Date at which file has been registered.
     */
    private Date registrationDate;

    /**
     * Expiration date of the file.
     */
    private Date expirationDate;

    /** The content type of the file. */
    private String contentType;

    /**
     * Returns ID.
     * 
     * @return <code>null</code> when undefined.
     */
    public final long getID()
    {
        return id;
    }

    /**
     * Sets ID.
     * 
     * @param id New value. Can be <code>null</code>.
     */
    public final void setID(final long id)
    {
        this.id = id;
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(final String name)
    {
        this.name = name;
    }

    public final BasicUserInfoDTO getOwner()
    {
        return owner;
    }

    public final void setOwner(final BasicUserInfoDTO registerer)
    {
        this.owner = registerer;
    }

    public final String getRegistratorCode()
    {
        return registratorCode;
    }

    public final void setRegistratorCode(String registratorCode)
    {
        this.registratorCode = registratorCode;
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

    public final Long getSize()
    {
        return size;
    }

    public final void setSize(final Long size)
    {
        this.size = size;
    }

    public final long getCompleteSize()
    {
        return completeSize;
    }

    public final void setCompleteSize(long completeSize)
    {
        this.completeSize = completeSize;
    }

    public boolean isComplete()
    {
        return getSize() != null && getSize().longValue() == getCompleteSize();
    }

    public Integer getCrc32Value()
    {
        return crc32Value;
    }

    public void setCrc32Value(int crc32Value)
    {
        this.crc32Value = crc32Value;
    }

    public void setCrc32Str(String crc32Str)
    {
        this.crc32Str = crc32Str;
    }

    public String getCrc32Str()
    {
        return crc32Str;
    }

    public final String getContentType()
    {
        return contentType;
    }

    public final void setContentType(final String contentType)
    {
        this.contentType = contentType;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void updateFrom(final FileInfoDTO updateFile)
    {
        setComment(updateFile.getComment());
        setCompleteSize(updateFile.getCompleteSize());
        setContentType(updateFile.getContentType());
        setCrc32Str(updateFile.getCrc32Str());
        setCrc32Value(updateFile.getCrc32Value());
        setExpirationDate(updateFile.getExpirationDate());
        setName(updateFile.getName());
        setRegistrationDate(updateFile.getRegistrationDate());
        setRegistratorCode(updateFile.getRegistratorCode());
        setSize(updateFile.getSize());
    }
    

}
