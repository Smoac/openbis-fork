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

package ch.systemsx.cisd.datamover.transformation;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * {@link ITransformator} that does nothing but logging.
 * 
 * @author Piotr Buczek
 */
public class DummyTransformator implements ITransformator
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DummyTransformator.class);

    public Status transform(File path)
    {
        operationLog.info("Dummy transformation of '" + path.getAbsolutePath() + "'");
        return Status.OK;
    }
}
