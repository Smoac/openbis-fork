/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.etlserver.registrator.ITransactionalCommand;

/**
 * Reference to {@link ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack} kept for
 * backwards compatibility
 * 
 * @author Jakub Straszewski
 */
public class RollbackStack extends ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack
{

    public RollbackStack(File queue1File, File queue2File, Logger operationLog)
    {
        super(queue1File, queue2File, operationLog);
    }

    public RollbackStack(File queue1File, File queue2File)
    {
        super(queue1File, queue2File);
    }

    @SuppressWarnings("unused")
    private static class StackElement extends
            ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack.StackElement
    {
        private static final long serialVersionUID = 1L;

        protected StackElement(ITransactionalCommand command, int order)
        {
            super(command, order);
            System.out.println("The new stack element has been deserialized");
        }
    }
}
