/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.etl;

import org.apache.commons.lang3.StringUtils;

/**
 * Describes an additional conversion.
 * 
 * @author Tomasz Pylak
 */
public enum MLConversionType
{
    EICML, FIAML, NONE;

    public static MLConversionType tryCreate(String conversion)
    {
        if (StringUtils.isBlank(conversion))
        {
            return MLConversionType.NONE;
        }
        for (MLConversionType ct : MLConversionType.values())
        {
            if (ct.name().equalsIgnoreCase(conversion))
            {
                return ct;
            }
        }
        return null;
    }
}