/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.WriteOperation;
import lombok.NonNull;

import java.io.IOException;

public class WriteOperationExecutor extends AbstractModificationOperationExecutor<WriteOperation> {

    //
    // Singleton
    //

    private static final WriteOperationExecutor instance;

    static {
        instance = new WriteOperationExecutor();
    }

    protected WriteOperationExecutor() {
    }

    public static WriteOperationExecutor getInstance() {
        return instance;
    }

    //
    // Operation
    //


    @Override
    public boolean prepare(@NonNull final Transaction transaction, @NonNull final WriteOperation operation) throws Exception {
        prepare(transaction, operation, operation.getSource(), operation.getData(), operation.getMd5Hash());
        return true;
    }

    protected void doWrite(final WriteOperation operation, final String tempFilePath) throws IOException
    {
        IOUtils.write(tempFilePath, operation.getOffset(), operation.getData());
    }

    @Override
    public boolean commit(final @NonNull Transaction transaction, final WriteOperation operation) throws Exception {
        return super.commit(transaction, operation.getSource());
    }

}
