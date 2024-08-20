/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.server.shuffling;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.logging.ISimpleLogger;

/**
 * Strategy of moving a data set to another share. Implementations of this interface should have a public constructor with an argument of type
 * {@link Properties}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSetMover
{
    /**
     * Moves the specified data set to the specified share. The data set is folder in the store its name is the data set code. The destination folder
     * is <code>share</code>. Its name is the share id. Share id and size will be updated on openBIS.
     * 
     * @param checksumProvider provides checksums of the source data set files that will be compared with calculated checksums of the destination data
     *            set files. If checksumProvider is null, then checksum verification is not performed.
     */
    public void moveDataSetToAnotherShare(File dataSetDirInStore, File share,
            IChecksumProvider checksumProvider, ISimpleLogger logger);
}
