/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.afs.manager.operation;

import static ch.ethz.sis.afs.exception.AFSExceptions.MD5NotMatch;
import static ch.ethz.sis.afs.exception.AFSExceptions.PathIsDirectory;

import java.io.IOException;
import java.util.Arrays;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.Operation;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.shared.io.IOUtils;

public abstract class AbstractModificationOperationExecutor<O extends Operation> implements OperationExecutor<O>
{

    protected void prepare(final Transaction transaction, final O operation, final String source, final byte[] data, final byte[] dataHash)
            throws IOException
    {
        // 1. Check that if the file exists, is not a directory
        boolean exists = IOUtils.exists(source);
        if (exists) {
            File existingFile = IOUtils.getFile(source);
            if (existingFile.getDirectory()) {
                AFSExceptions.throwInstance(PathIsDirectory, OperationName.Write.name(), source);
            }
        }

        // 2. Validate new data
        byte[] md5Hash = IOUtils.getMD5(data);
        if (!Arrays.equals(md5Hash, dataHash)) {
            AFSExceptions.throwInstance(MD5NotMatch, OperationName.Write.name(), source);
        }

        // 3. Create temporary file if it has not been created already
        String tempFilePath = OperationExecutor.getTempPath(transaction, source);
        if (!IOUtils.exists(tempFilePath)) {
            IOUtils.createDirectories(IOUtils.getParentPath(tempFilePath));
            if (!exists) {
                IOUtils.createFile(tempFilePath);
            } else {
                IOUtils.copy(source, tempFilePath);
            }
        }

        // 4. Flush bytes
        doWrite(operation, tempFilePath);
    }

    protected abstract void doWrite(final O operation, final String tempFilePath) throws IOException;

    protected boolean commit(final Transaction transaction, final String source) throws Exception
    {
        String tempFilePath = OperationExecutor.getTempPath(transaction, source);
        if (!IOUtils.exists(source))
        {
            IOUtils.createDirectories(IOUtils.getParentPath(source));
        }
        if (IOUtils.exists(tempFilePath))
        {
            IOUtils.move(tempFilePath, source);
        }
        return true;
    }

}
