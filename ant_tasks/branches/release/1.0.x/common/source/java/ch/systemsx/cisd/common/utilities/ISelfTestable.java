/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

/**
 * A role that can perform a self test.
 * 
 * @author Bernd Rinn
 */
public interface ISelfTestable
{

    /**
     * Checks this <code>ISelfTestable</code>. Any failure needs to be properly logged.
     * 
     * @throws RuntimeException If the self test fails.
     */
    public void check();

}
