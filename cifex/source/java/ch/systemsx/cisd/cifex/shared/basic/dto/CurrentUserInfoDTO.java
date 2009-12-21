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
 * An extension of the {@link UserInfoDTO} for the currently logged in user.
 *
 * @author Bernd Rinn
 */
public final class CurrentUserInfoDTO extends UserInfoDTO
{

    private static final long serialVersionUID = 1L;
    
    private boolean hasFilesForDownload;

    public boolean hasFilesForDownload()
    {
        return hasFilesForDownload;
    }

    public void setHasFilesForDownload(boolean hasFilesForDownload)
    {
        this.hasFilesForDownload = hasFilesForDownload;
    }
    
}
