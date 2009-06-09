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

package ch.systemsx.cisd.cifex.server.trigger;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * An interface used by an {@link ITrigger} implementation to issue commands to the CIFEX server.
 * 
 * @author Bernd Rinn
 */
public interface ITriggerConsole extends IMailClient
{

    /**
     * Returns the pending requests of the user that created this request. Note that the current
     * request is not contained.
     */
    public List<ITriggerRequest> getPendingRequests();

    /**
     * Returns the pending requests of the user that created this request which match the given
     * <var>fileNameWildCard</var>. Note that the current request is not contained.
     */
    public List<ITriggerRequest> getPendingRequests(String fileNameWildCard);

    /**
     * Returns all pending requests. Note that the current request is not contained. 
     * <p>
     * <i>Note: most likely you want to use {@link #getPendingRequests()}</i> instead.
     */
    public List<ITriggerRequest> getAllPendingRequests();

    /**
     * Returns all pending requests which match the given <var>fileNameWildCard</var>. Note that the
     * current request is not contained.
     * <p>
     * <i>Note: most likely you want to use {@link #getPendingRequests(String)}</i> instead.
     */
    public List<ITriggerRequest> getAllPendingRequests(String fileNameWildCard);

    /**
     * Uploads the <var>file</var> for the <var>recipients</var> with a default comment. The mime
     * type will be inferred from the file name.
     */
    public void upload(File file, String[] recipients);

    /**
     * Uploads the <var>file</var> for the <var>recipients</var> with the given <var>comment</var>.
     * The mime type will be inferred from the file name.
     */
    public void upload(File file, String[] recipients, String comment);

    /**
     * Uploads the <var>file</var> with <var>mimeType</var> for the <var>recipients</var> with the
     * given <var>comment</var>.
     */
    public void upload(File file, String mimeType, String[] recipients, String comment);

}
