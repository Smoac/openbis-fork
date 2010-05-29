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

package ch.systemsx.cisd.cifex.client.application.utils;

/**
 * Some utilities for <code>Object</code>.
 * 
 * @author Bernd Rinn
 */
public class ObjectUtils
{

    /**
     * Returns the string representation of <var>obj</var>, or an empty string, if
     * <code>obj==null</code>.
     */
    public static String toString(Object obj)
    {
        return obj == null ? "" : obj.toString();
    }

}
