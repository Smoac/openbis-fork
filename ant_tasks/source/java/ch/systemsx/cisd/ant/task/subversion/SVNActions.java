/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.ant.common.StringUtils;
import ch.systemsx.cisd.ant.task.subversion.SVNUtilities.ProcessInfo;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * A class that provides a wrapper for actions performed on a subversion path by wrapping the subversion command line
 * client. It requires the <code>svn<code> executable to be on the <var>PATH</var>.
 *
 * @author Bernd Rinn
 */
class SVNActions implements ISVNActions
{
    private final ISimpleLogger logger;

    SVNActions(ISimpleLogger logger)
    {
        assert logger != null;

        this.logger = logger;
    }

    public String cat(String pathOrUrl) throws SVNException
    {
        assert pathOrUrl != null;
        assert checkUrlOrAbsolutePath(pathOrUrl) : "'" + pathOrUrl
                + "' is neither a URL nor an absolute path.";

        final ProcessInfo subversionProcessInfo =
                SVNUtilities.subversionCommand(logger, false, "cat", pathOrUrl);
        return StringUtils.join(subversionProcessInfo.getLines(), OSUtilities.LINE_SEPARATOR);
    }

    public List<String> list(String pathOrUrl) throws SVNException
    {
        assert pathOrUrl != null;
        assert checkUrlOrAbsolutePath(pathOrUrl) : "'" + pathOrUrl
                + "' is neither a URL nor an absolute path.";

        final ProcessInfo subversionProcessInfo =
                SVNUtilities.subversionCommand(logger, "list", pathOrUrl);
        return subversionProcessInfo.getLines();
    }

    public void mkdir(String pathOrUrl, String logMessage) throws SVNException
    {
        assert pathOrUrl != null;
        assert checkUrlOrAbsolutePath(pathOrUrl) : "'" + pathOrUrl
                + "' is neither a URL nor an absolute path.";

        SVNUtilities.subversionCommand(logger, "mkdir", "--message", logMessage, pathOrUrl);
    }

    public void copy(String sourcePathOrUrl, String sourceRevision, String destinationPathOrUrl,
            String logMessage) throws SVNException
    {
        assert sourcePathOrUrl != null;
        assert checkUrlOrAbsolutePath(sourcePathOrUrl) : "'" + sourcePathOrUrl
                + "' is neither a URL nor an absolute path.";
        assert destinationPathOrUrl != null;
        assert checkUrlOrAbsolutePath(destinationPathOrUrl) : "'" + destinationPathOrUrl
                + "' is neither a URL nor an absolute path.";

        if (SVNUtilities.HEAD_REVISION.equals(sourceRevision))
        {
            SVNUtilities.subversionCommand(logger, "copy", "--message", logMessage,
                    sourcePathOrUrl, destinationPathOrUrl);
        } else
        {
            SVNUtilities.subversionCommand(logger, "copy", "--message", logMessage, "--revision",
                    sourceRevision, sourcePathOrUrl, destinationPathOrUrl);
        }
    }

    public SVNInfoRecord info(String pathOrUrl)
    {
        assert pathOrUrl != null;
        assert checkUrlOrAbsolutePath(pathOrUrl) : "'" + pathOrUrl
                + "' is neither a URL nor an absolute path.";

        final ProcessInfo subversionProcessInfo =
                SVNUtilities.subversionCommand(logger, "info", "-R", pathOrUrl);
        final SVNInfoRecord infoRecord = new SVNInfoRecord();
        new SVNInfoRecordExtractor().fillInfoRecord(infoRecord, subversionProcessInfo);
        return infoRecord;
    }

    public List<SVNItemStatus> status(String pathOrUrl)
    {
        assert pathOrUrl != null;
        assert checkUrlOrAbsolutePath(pathOrUrl) : "'" + pathOrUrl
                + "' is neither a URL nor an absolute path.";

        final ProcessInfo subversionProcessInfo =
                SVNUtilities.subversionCommand(logger, "status", pathOrUrl);
        final List<SVNItemStatus> status = new ArrayList<SVNItemStatus>();
        for (String line : subversionProcessInfo.getLines())
        {
            status.add(new SVNItemStatus(line.charAt(0), line.substring(7)));
        }
        return status;
    }

    private boolean checkUrlOrAbsolutePath(String pathOrUrl)
    {
        if (new File(pathOrUrl).isAbsolute())
        {
            return true;
        }
        if (pathOrUrl.indexOf("://") > 0)
        {
            return true;
        }
        return false;
    }

    public boolean isMuccAvailable()
    {
        return SVNUtilities.isMuccAvailable();
    }

    public void mucc(String logMessage, String... args) throws SVNException
    {
        assert logMessage != null;
        assert args.length > 0;

        SVNUtilities.subversionMuccCommand(logger, logMessage, args);
    }

}
