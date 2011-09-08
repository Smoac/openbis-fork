/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * A map that maps capabilities to roles.
 * 
 * @author Bernd Rinn
 */
class CapabilityMap
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CapabilityMap.class);

    private final Map<String, RoleWithHierarchy> capMap = new HashMap<String, RoleWithHierarchy>();

    @SuppressWarnings("unchecked")
    private final static List<String> readLines(File file)
    {
        if (file.exists() == false)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("No file '%s", file.getPath()));
            }
            return Collections.emptyList();
        }
        try
        {
            return FileUtils.readLines(file);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    CapabilityMap(File file)
    {
        this(readLines(file), file.getPath());
    }

    CapabilityMap(List<String> lines, String filePath)
    {
        for (String line : lines)
        {
            final String trimmed = line.trim();
            if (trimmed.length() == 0 || trimmed.startsWith("#"))
            {
                continue;
            }
            final String[] splitted = StringUtils.split(trimmed);
            if (splitted.length != 2)
            {
                operationLog.warn(String.format("Ignoring mal-formed line '%s' in %s.", trimmed,
                        filePath));
                continue;
            }
            final String methodName =
                    (splitted[0].endsWith(":") ? splitted[0].substring(0, splitted[0].length() - 1)
                            : splitted[0]);
            final String roleName = splitted[1];
            try
            {
                final RoleWithHierarchy role = RoleWithHierarchy.valueOf(roleName);
                capMap.put(methodName, role);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format("Add to map: '%s' -> %s", methodName, role));
                }
            } catch (IllegalArgumentException ex)
            {
                operationLog.warn(String.format(
                        "Ignoring mal-formed line '%s' in %s [role '%s' doesn't exist].", trimmed,
                        filePath, roleName));
            }
        }
    }

    RoleWithHierarchy tryGetRole(Method m)
    {
        final String methodName = m.getDeclaringClass().getSimpleName() + "." + m.getName();
        final RoleWithHierarchy roleOrNull = capMap.get(methodName);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Request: '%s' -> %s", methodName, roleOrNull));
        }
        return roleOrNull;
    }
}
