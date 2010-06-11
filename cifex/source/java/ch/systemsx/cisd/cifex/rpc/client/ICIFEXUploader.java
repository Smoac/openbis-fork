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

import java.util.List;

/**
 * Interface for uploading operations in CIFEX.
 * 
 * @author Bernd Rinn
 */
public interface ICIFEXUploader extends ICIFEXOperation
{

    /**
     * Uploads the specified files for the specified recipients.
     * 
     * @param recipientsOrNull Comma or space-separated list of e-mail addresses or user ID's in the
     *            form <code>id:<i>user ID</i></code>. If it is null or an empty string, the files
     *            will not be shared with any user.
     * @param comment Optional comment added to the outgoing e-mails. Can be an empty string.
     */
    public void upload(List<FileWithOverrideName> files, String recipientsOrNull, String comment);

}