/*
 * Copyright 2007 ETH Zuerich, CISD.
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

package ch.systemsx.cisd.ant.task.subversion;

import java.io.File;

import ch.systemsx.cisd.common.logging.ISimpleLogger;

/**
 * A class that provides a checkout function for subversion, using the subversion command line client. It requires the
 * <code>svn<code> to be in the <var>PATH</var>.
 * <p>
 * This command works on a subversion repository. 
 *
 * @author Bernd Rinn
 */
class SVNCheckout implements ISVNCheckout
{
    private final ISimpleLogger logger;

    private final File directoryToCheckout;

    SVNCheckout(ISimpleLogger logger, String directoryToCheckout)
    {
        assert logger != null;
        this.logger = logger;

        assert directoryToCheckout != null;
        this.directoryToCheckout = new File(directoryToCheckout);
        assert this.directoryToCheckout.isDirectory();
    }

    public void checkout(String url, String projectName, String revision) throws SVNException
    {
        assert url != null && url.indexOf("://") > 0;
        assert projectName != null && projectName.indexOf('/') == -1;
        assert revision != null;
        assert SVNUtilities.HEAD_REVISION.equals(revision) || Integer.parseInt(revision) >= 0;

        final String workingCopyPath = new File(directoryToCheckout, projectName).getAbsolutePath();
        SVNUtilities.subversionCommand(logger, "checkout", "--revision", revision, String.format(
                "%s@%s", url, revision), workingCopyPath);
    }

    public String getDirectoryToCheckout()
    {
        return directoryToCheckout.getAbsolutePath();
    }

}
