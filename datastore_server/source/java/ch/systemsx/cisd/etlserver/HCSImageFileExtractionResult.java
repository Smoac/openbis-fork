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
package ch.systemsx.cisd.etlserver;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.bds.hcs.Channel;
import ch.systemsx.cisd.bds.storage.IFile;

/**
 * Class which contains the extraction process results.
 */
public final class HCSImageFileExtractionResult
{

    /** The duration of the process. */
    private final long duration;

    /** The total number of files found. */
    private final int totalFiles;

    /** The invalid files found. */
    private final List<IFile> invalidFiles;

    /** The channels found. */
    private final Set<Channel> channels;

    public HCSImageFileExtractionResult(final long duration, final int totalFiles,
            final List<IFile> invalidFiles, final Set<Channel> channels)
    {
        this.duration = duration;
        this.totalFiles = totalFiles;
        this.invalidFiles = invalidFiles;
        this.channels = channels;
    }

    /**
     * Returns the duration of the process.
     */
    public final long getDuration()
    {
        return duration;
    }

    /**
     * Returns the total number of files found.
     */
    public final int getTotalFiles()
    {
        return totalFiles;
    }

    /**
     * Returns the invalid files found.
     */
    public final List<IFile> getInvalidFiles()
    {
        return invalidFiles;
    }

    /**
     * Returns the channels found.
     */
    public final Set<Channel> getChannels()
    {
        return channels;
    }
}