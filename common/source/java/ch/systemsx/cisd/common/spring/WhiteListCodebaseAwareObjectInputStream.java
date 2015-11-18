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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

    private static final Patterns whiteListPatterns = new Patterns();
    private static final Patterns blackListPatterns = new Patterns();

    static {
        addToWhiteListPatterns("char");
        addToWhiteListPatterns("\\[C");
        addToWhiteListPatterns("byte");
        addToWhiteListPatterns("\\[B");
        addToWhiteListPatterns("short");
        addToWhiteListPatterns("\\[S");
        addToWhiteListPatterns("int");
        addToWhiteListPatterns("\\[I");
        addToWhiteListPatterns("long");
        addToWhiteListPatterns("\\[J");
        addToWhiteListPatterns("float");
        addToWhiteListPatterns("\\[F");
        addToWhiteListPatterns("double");
        addToWhiteListPatterns("\\[D");
        addToWhiteListPatterns("boolean");
        addToWhiteListPatterns("\\[Z");
        addToWhiteListPatterns("org\\.springframework\\.remoting\\.support\\.RemoteInvocation");
        addToWhiteListPatterns("com\\.marathon\\.util\\.spring\\.StreamSupportingRemoteInvocation");
        addToWhiteListPatterns("com\\.marathon\\.util\\.spring\\.RemoteInvocationDecorator");
        addToWhiteListPatterns("java\\..*");
        addToWhiteListPatterns("org\\.python\\.core\\.Py.*");
        addToWhiteListPatterns("org\\.python27\\.core\\.Py.*");
        addToWhiteListPatterns("ch\\.ethz\\.sis\\..*");
        addToWhiteListPatterns("ch\\.systemsx\\.cisd\\..*");
        
        addToBlackListPatterns("org\\.python\\.core\\.PyClass.*");
        addToBlackListPatterns("org\\.python27\\.core\\.PyClass.*");
    }

    public static void addToWhiteListPatterns(String regex)
    {
        whiteListPatterns.addPattern(regex);
    }

    public static void addToBlackListPatterns(String regex)
    {
        blackListPatterns.addPattern(regex);
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
            if (whiteListPatterns.matches(className) == false || blackListPatterns.matches(className))
            {
                operationLog.error("Attempt to load class " + className);
                throw new IllegalArgumentException("Class not allowed to load: " + className);
            }
        }
    }
    
    private static final class Patterns
    {
        private final Set<String> patternsAsStrings = new HashSet<>();
        private final List<Pattern> patterns = new LinkedList<>();
        
        synchronized void addPattern(String pattern)
        {
            if (patternsAsStrings.contains(pattern) == false)
            {
                patterns.add(Pattern.compile(pattern));
                patternsAsStrings.add(pattern);
            }
        }
        
        synchronized boolean matches(String string)
        {
            for (Pattern pattern : patterns)
            {
                if (pattern.matcher(string).matches())
                {
                    return true;
                }
            }
            return false;
        }
    }
}
