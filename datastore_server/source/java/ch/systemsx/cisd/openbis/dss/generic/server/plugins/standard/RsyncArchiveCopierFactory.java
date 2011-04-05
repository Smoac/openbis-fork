/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.Serializable;

import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;

/**
 * {@link IPathCopierFactory} that is more reliable than {@link RsyncCopierFactory} when it comes to
 * deciding which files to transfer. {@link IPathCopier} created by {@link RsyncCopierFactory} uses
 * "--append" flag causing files that are bigger in destination than in source to be ignored.
 * {@link IPathCopier} created by this factory is supposed to ignore only those files that have same
 * sizes and modification times.
 */
public final class RsyncArchiveCopierFactory implements Serializable, IPathCopierFactory
{
    private static final long serialVersionUID = 1L;

    public IPathCopier create(File rsyncExecutable, File sshExecutableOrNull)
    {
        // TODO 2011-04-05, Piotr Buczek: should we use --no-whole-file?
        return new RsyncCopier(rsyncExecutable, sshExecutableOrNull, "--archive", "--delete",
                "--inplace");
    }
}