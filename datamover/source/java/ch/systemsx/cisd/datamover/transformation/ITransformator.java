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

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * Interface of the data transformator.
 * <p>
 * If DMV is restarted all files will be transformed once again because of recovery mechanism.
 * All transformations run in-place so for some of them it may be needed to implement additional
 * mechanism that ignores files that were already transformed.
 * 
 * @author Piotr Buczek
 */
public interface ITransformator
{

    /**
     * Transforms the directory/file pointed by <var>path</var>. The result of transformation will
     * be in the same path.
     * 
     * @return {@link Status} of the transformation. If transformation fails because of fatal errors
     *         and DMV should stop processing files it should contain error information. Otherwise
     *         it should be {@link Status#OK} (the transformer should take care of logging minor
     *         errors by itself).
     */
    Status transform(File path);
}
