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

package ch.systemsx.cisd.common.filesystem;

import java.io.IOException;

/**
 * A roles that allows to close resource. Like {@link java.io.Closeable} but doesn't throw an
 * {@link IOException} but instead an unchecked exception.
 * 
 * @author Bernd Rinn
 */
public interface ICloseable
{

    /**
     * Closes the resource.
     */
    public void close();

}
