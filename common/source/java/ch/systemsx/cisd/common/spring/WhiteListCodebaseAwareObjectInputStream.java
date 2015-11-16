/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author Franz-Josef Elmer
 */
public class WhiteListCodebaseAwareObjectInputStream extends CodebaseAwareObjectInputStream
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, 
            WhiteListCodebaseAwareObjectInputStream.class);

    private static final List<Pattern> whiteListPatterns = new LinkedList<>();

    {
        addToWhiteListPatterns("byte");
        addToWhiteListPatterns("short");
        addToWhiteListPatterns("int");
        addToWhiteListPatterns("long");
        addToWhiteListPatterns("float");
        addToWhiteListPatterns("double");
        addToWhiteListPatterns("boolean");
        addToWhiteListPatterns("org\\.springframework\\.remoting\\.support\\.RemoteInvocation");
        addToWhiteListPatterns("com\\.marathon\\.util\\.spring\\.StreamSupportingRemoteInvocation");
        addToWhiteListPatterns("java\\..*");
        addToWhiteListPatterns("ch\\.ethz\\.sis\\..*");
        addToWhiteListPatterns("ch\\.systemsx\\.cisd\\..*");
    }

    private static void addToWhiteListPatterns(String regex)
    {
        whiteListPatterns.add(Pattern.compile(regex));
    }

    public WhiteListCodebaseAwareObjectInputStream(InputStream in, ClassLoader classLoader, boolean acceptProxyClasses) throws IOException
    {
        super(in, classLoader, acceptProxyClasses);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException
    {
        String className = classDesc.getName();
        assertMatchingClassName(className);
        return super.resolveClass(classDesc);
    }
    
    private void assertMatchingClassName(String className) throws ClassNotFoundException
    {
        if (className.startsWith("[L") && className.endsWith(";"))
        {
            assertMatchingClassName(className.substring(2, className.length() - 1));
        } else
        {
            for (Pattern pattern : whiteListPatterns)
            {
                if (pattern.matcher(className).matches())
                {
                    return;
                }
            }
            operationLog.error("Attempt to load class " + className);
            throw new IllegalArgumentException("Class not allowed to load: " + className);
        }
    }
}
