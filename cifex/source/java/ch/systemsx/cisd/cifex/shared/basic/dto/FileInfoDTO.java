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
    private String idStr;

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
    
    private Integer crc32Value;

    /**
     * Person who registered the file.
     */
    private BasicUserInfoDTO registerer;

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
     */
    public final String getIDStr()
    {
        return idStr;
    }

    /**
     * Sets ID.
     */
    public final void setIDStr(final String idStr)
    {
        this.idStr = idStr;
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(final String name)
    {
        this.name = name;
    }

    public final BasicUserInfoDTO getRegisterer()
    {
        return registerer;
    }

    public final void setRegisterer(final BasicUserInfoDTO registerer)
    {
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

    public final Long getSize()
    {
        return size;
    }

    public final void setSize(final Long size)
    {
        this.size = size;
    }

    public void setCrc32Value(Integer crc32Value)
    {
        this.crc32Value = crc32Value;
    }

    public Integer getCrc32Value()
    {
        return crc32Value;
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

}
