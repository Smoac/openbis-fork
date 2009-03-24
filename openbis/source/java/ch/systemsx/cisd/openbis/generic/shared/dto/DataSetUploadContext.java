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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Context data needed for uploading data sets to a CIFEX server.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetUploadContext implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;
    
    private String cifexURL;

    private String userID;
    
    private String password;
    
    private String comment;

    private String email;

    public final String getCifexURL()
    {
        return cifexURL;
    }

    public final void setCifexURL(String cifexURL)
    {
        this.cifexURL = cifexURL;
    }

    public final String getUserID()
    {
        return userID;
    }

    public final void setUserID(String userID)
    {
        this.userID = userID;
    }

    public final String getPassword()
    {
        return password;
    }

    public final void setPassword(String password)
    {
        this.password = password;
    }

    public final String getComment()
    {
        return comment;
    }

    public final void setComment(String comment)
    {
        this.comment = comment;
    }
    
    public final String getUserEMail()
    {
        return email;
    }

    public final void setUserEMail(String email)
    {
        this.email = email;
    }
}
