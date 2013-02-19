/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Date;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Immutable value object representing an attachment.
 * 
 * @since 1.22
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("Attachment")
public class Attachment implements Serializable, Comparable<Attachment>
{
    private static final long serialVersionUID = 1L;

    private String fileName;

    private int version;

    private String title;

    private String description;

    private Date registrationDate;

    private String userFirstName;

    private String userLastName;

    private String userEmail;

    private String userId;

    private String permLink;

    public static class AttachmentInitializer
    {
        private String fileName;

        private int version;

        private String title;

        private String description;

        private Date registrationDate;

        private String userFirstName;

        private String userLastName;

        private String userEmail;

        private String userId;

        private String permLink;

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }

        public int getVersion()
        {
            return version;
        }

        public void setVersion(int version)
        {
            this.version = version;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public Date getRegistrationDate()
        {
            return registrationDate;
        }

        public void setRegistrationDate(Date registrationDate)
        {
            this.registrationDate = registrationDate;
        }

        public String getUserFirstName()
        {
            return userFirstName;
        }

        public void setUserFirstName(String userFirstName)
        {
            this.userFirstName = userFirstName;
        }

        public String getUserLastName()
        {
            return userLastName;
        }

        public void setUserLastName(String userLastName)
        {
            this.userLastName = userLastName;
        }

        public String getUserEmail()
        {
            return userEmail;
        }

        public void setUserEmail(String userEmail)
        {
            this.userEmail = userEmail;
        }

        public String getUserId()
        {
            return userId;
        }

        public void setUserId(String userId)
        {
            this.userId = userId;
        }

        public String getPermLink()
        {
            return permLink;
        }

        public void setPermLink(String permLink)
        {
            this.permLink = permLink;
        }

    }

    //
    // Public API
    //

    public Attachment(AttachmentInitializer initializer)
    {
        setFileName(initializer.getFileName());
        setVersion(initializer.getVersion());
        setRegistrationDate(initializer.getRegistrationDate());
        setTitle(initializer.getTitle());
        setDescription(initializer.getDescription());
        setUserId(initializer.getUserId());
        setUserEmail(initializer.getUserEmail());
        setUserFirstName(initializer.getUserFirstName());
        setUserLastName(initializer.getUserLastName());
        setPermLink(initializer.getPermLink());
    }

    /**
     * Returns the file name of this attachment.
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Returns the version of this attachment. Starts with 1 and is increased by 1 whenever a user
     * uploads a new version of the same file.
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * Returns the title given by the user when uploading this attachment.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the description given by the user when uploading this attachment.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the date when this attachment was uploaded.
     */
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    /**
     * Returns the first name of the user who uploaded this attachment.
     */
    public String getUserFirstName()
    {
        return userFirstName;
    }

    /**
     * Returns the last name of the user who uploaded this attachment.
     */
    public String getUserLastName()
    {
        return userLastName;
    }

    /**
     * Returns the email of the user who uploaded this attachment.
     */
    public String getUserEmail()
    {
        return userEmail;
    }

    /**
     * Returns the user id of the user who uploaded this attachment.
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * Returns the permanent hyperlink of this attachment.
     */
    public String getPermLink()
    {
        return permLink;
    }

    //
    // JSON-RPC
    //

    private Attachment()
    {
    }

    private void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    private void setVersion(int version)
    {
        this.version = version;
    }

    private void setTitle(String title)
    {
        this.title = StringUtils.isBlank(title) ? "" : title;
    }

    private void setDescription(String description)
    {
        this.description = StringUtils.isBlank(description) ? "" : description;
    }

    private void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    private void setUserFirstName(String userFirstName)
    {
        this.userFirstName = userFirstName;
    }

    private void setUserLastName(String userLastName)
    {
        this.userLastName = userLastName;
    }

    private void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    private void setUserId(String userId)
    {
        this.userId = userId;
    }

    private void setPermLink(String permLink)
    {
        this.permLink = permLink;
    }

    @Override
    public int compareTo(final Attachment o)
    {
        final int byFile = getFileName().compareTo(o.getFileName());
        return (byFile == 0) ? version - o.version : 0;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Attachment other = (Attachment) obj;
        if (fileName == null)
        {
            if (other.fileName != null)
            {
                return false;
            }
        } else if (fileName.equals(other.fileName) == false)
        {
            return false;
        }
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "Attachment [fileName=" + fileName + ", version=" + version + ", title=" + title
                + ", description=" + description + ", registrationDate=" + registrationDate
                + ", userFirstName=" + userFirstName + ", userLastName=" + userLastName
                + ", userEmail=" + userEmail + ", userId=" + userId + ", permLink=" + permLink
                + "]";
    }

}
