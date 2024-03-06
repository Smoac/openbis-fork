/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.utils;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.Arrays;

public class ExceptionUtils
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ExceptionUtils.class);

    public static RuntimeException create(IOperationContext context, Throwable t)
    {
        if (t instanceof UserFailureException)
        {
            return new UserFailureException(createMessage(context, t), t);
        }
        operationLog.error("||> ERROR", t);
        operationLog.error(Arrays.toString(t.getStackTrace()));
        return new RuntimeException(createMessage(context, t), t);
    }

    private static String createMessage(IOperationContext context, Throwable t)
    {
        String message = t.getMessage() == null ? t.toString() : t.getMessage();
        return message + " (Context: " + context.getProgressStack().toString() + ")";
    }

}
