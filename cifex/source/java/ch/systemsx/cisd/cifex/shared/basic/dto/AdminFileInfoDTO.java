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

package ch.systemsx.cisd.cifex.shared.basic.dto;

/**
 * A client <i>DTO</i> which describes a file with additional admin information.
 *
 * @author Bernd Rinn
 */
public final class AdminFileInfoDTO extends FileInfoDTO
{

    private static final long serialVersionUID = 1L;
    
    private long completeSize; 
    
    private BasicUserInfoDTO[] sharingUsers;
    
    public final long getCompleteSize()
    {
        return completeSize;
    }

    public final void setCompleteSize(long completeSize)
    {
        this.completeSize = completeSize;
    }

    public BasicUserInfoDTO[] getSharingUsers()
    {
        return sharingUsers;
    }

    public void setSharingUsers(final BasicUserInfoDTO[] sharingUsers)
    {
        this.sharingUsers = sharingUsers;
    }
    
    public boolean isComplete()
    {
        return getSize() != null && getSize().longValue() == getCompleteSize();
    }

}
