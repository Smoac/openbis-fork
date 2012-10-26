/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;

/**
 * @author Franz-Josef Elmer
 */
public class TransferAuthorizationAnnotations
{
    private static final String AUTHORIZATION_GUARD_ANNOTATION = "@"
            + AuthorizationGuard.class.getSimpleName() + "(";

    private static final Set<String> METHOD_AUTHORIZATION_ANNOTATIONS = new HashSet<String>(
            Arrays.asList(RolesAllowed.class.getSimpleName(), Capability.class.getSimpleName(),
                    ReturnValueFilter.class.getSimpleName()));

    private static final class MethodAnnotation
    {
        private List<String> lines = new ArrayList<String>();

        private String annotationName;

        private boolean finished;

        MethodAnnotation(String firstLine)
        {
            add(firstLine);
            int indexOfStart = firstLine.indexOf('@');
            int indexOfOpenParanthesis = firstLine.indexOf('(');
            if (indexOfOpenParanthesis < 0)
            {
                indexOfOpenParanthesis = firstLine.length();
                finished = true;
            }
            annotationName = firstLine.substring(indexOfStart + 1, indexOfOpenParanthesis);
        }

        void add(String line)
        {
            lines.add(line);
            finished = line.trim().endsWith(")");
        }

        boolean isFinishedAnnotation()
        {
            return finished;
        }

        boolean isAuthorizationAnnotation()
        {
            return METHOD_AUTHORIZATION_ANNOTATIONS.contains(annotationName);
        }
    }

    private static final class MethodWithAnnotations
    {
        private final List<MethodAnnotation> annotations = new ArrayList<MethodAnnotation>();

        private final List<String> signatureLines = new ArrayList<String>();

        private String joinedSignatureLines = "";

        private String canonicalSignature;

        void addAnnotation(MethodAnnotation methodAnnotation)
        {
            annotations.add(methodAnnotation);
        }

        void addMethodSignatureLine(String line)
        {
            final String methodSignatureLine;
            if (line.lastIndexOf(';') >= 0)
            {
                methodSignatureLine = line.substring(0, line.lastIndexOf(';'));
            } else
            {
                methodSignatureLine = line;
            }
            signatureLines.add(methodSignatureLine);
            joinedSignatureLines += methodSignatureLine.trim();
        }

        String getSignature()
        {
            if (canonicalSignature == null)
            {
                canonicalSignature = getCanonicalSignature(joinedSignatureLines);
            }
            return canonicalSignature;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof MethodWithAnnotations ? getSignature().equals(
                    ((MethodWithAnnotations) obj).getSignature()) : false;
        }

        @Override
        public int hashCode()
        {
            return getSignature().hashCode();
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(getSignature()).append(":\n");
            for (MethodAnnotation methodAnnotation : annotations)
            {
                appendLines(builder, methodAnnotation.lines);
            }
            appendLines(builder, signatureLines);
            return builder.toString();
        }

        public boolean hasAuthorizationAnnotations()
        {
            return annotations.isEmpty() == false;
        }

        public List<String> getAllLines()
        {
            List<String> allLines = new ArrayList<String>();
            for (MethodAnnotation methodAnnotation : annotations)
            {
                allLines.addAll(methodAnnotation.lines);
            }
            allLines.addAll(signatureLines);
            return allLines;
        }

    }

    private static void appendLines(StringBuilder builder, List<String> lines)
    {
        for (String line : lines)
        {
            builder.append(line).append('\n');
        }
    }

    private static String getCanonicalSignature(String joinedSignatureLines)
    {
        int parametersStartIndex = joinedSignatureLines.indexOf('(');
        if (parametersStartIndex < 0)
        {
            throw new IllegalArgumentException("Invalid signature: " + joinedSignatureLines);
        }
        int parametersEndIndex = joinedSignatureLines.lastIndexOf(')');
        String[] splittedBeginning =
                joinedSignatureLines.substring(0, parametersStartIndex).split(" ");
        String methodName = splittedBeginning[splittedBeginning.length - 1];
        StringBuilder builder = new StringBuilder();
        builder.append(methodName).append('(');
        String[] splittedParameters =
                joinedSignatureLines.substring(parametersStartIndex + 1, parametersEndIndex).split(
                        ",");
        CommaSeparatedListBuilder parametersBuilder = new CommaSeparatedListBuilder();
        for (String parameter : splittedParameters)
        {
            if (parameter.trim().length() == 0)
            {
                continue;
            }
            String modifiedParameter = removeAuthorizationGuardAnnotation(parameter);
            StringTokenizer stringTokenizer = new StringTokenizer(modifiedParameter);
            String firstToken = stringTokenizer.nextToken();
            parametersBuilder.append(firstToken.equals("final") ? stringTokenizer.nextToken()
                    : firstToken);
        }
        return builder.append(parametersBuilder).append(')').toString();

    }

    private static final class MethodsBuilder
    {
        private Map<String, MethodWithAnnotations> methods =
                new LinkedHashMap<String, MethodWithAnnotations>();

        private MethodWithAnnotations currentMethod = new MethodWithAnnotations();

        private MethodAnnotation methodAnnotation;

        private boolean insideBody;

        private boolean insideMethodSignature;

        boolean addLine(String line)
        {
            if (insideBody == false)
            {
                insideBody = line.startsWith("{");
                return false;
            }
            if (insideMethodSignature == false)
            {
                return handleAnnotationLine(line);
            }
            return handleSignatureLine(line);
        }

        private boolean handleAnnotationLine(String line)
        {
            if (line.trim().startsWith("@"))
            {
                methodAnnotation = new MethodAnnotation(line);
                if (methodAnnotation.isAuthorizationAnnotation())
                {
                    currentMethod.addAnnotation(methodAnnotation);
                }
                return methodAnnotation.isAuthorizationAnnotation();
            }
            if (methodAnnotation != null && methodAnnotation.isFinishedAnnotation() == false)
            {
                methodAnnotation.add(line);
                return methodAnnotation.isAuthorizationAnnotation();
            }
            return handleSignatureLine(line);
        }

        private boolean handleSignatureLine(String line)
        {
            currentMethod.addMethodSignatureLine(line);
            insideMethodSignature = line.trim().endsWith(";") == false;
            if (insideMethodSignature == false)
            {
                if (currentMethod.hasAuthorizationAnnotations())
                {
                    methods.put(currentMethod.getSignature(), currentMethod);
                }
                currentMethod = new MethodWithAnnotations();
            }
            return false;
        }

        Map<String, MethodWithAnnotations> getMethods()
        {
            return methods;
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage " + TransferAuthorizationAnnotations.class.getName()
                    + " <interface source code file> <implementation source code file>");
            System.exit(1);
        }
        File interfaceSourceCodeFile = new File(args[0]);
        File implementationSourceCodeFile = new File(args[1]);
        List<String> interfaceSourceCode = FileUtilities.loadToStringList(interfaceSourceCodeFile);
        List<String> implementationSourceCode =
                FileUtilities.loadToStringList(implementationSourceCodeFile);

        Map<String, MethodWithAnnotations> methods =
                parseAndRemoveAuthorizationAnnotations(interfaceSourceCode);
        List<String> newInterfaceSourceCode =
                removeAuthorizationGuardAnnotations(interfaceSourceCode);
        List<String> newImplementationSourceCode =
                injectAuthorizationAnnotations(implementationSourceCode, methods);
        writeBack(interfaceSourceCodeFile, newInterfaceSourceCode);
        writeBack(implementationSourceCodeFile, newImplementationSourceCode);
    }

    private static List<String> injectAuthorizationAnnotations(
            List<String> implementationSourceCode, Map<String, MethodWithAnnotations> methods)
    {
        List<String> result = new ArrayList<String>();
        List<String> methodSignature = new ArrayList<String>();
        boolean insideBody = false;
        for (String line : implementationSourceCode)
        {
            if (isEmptyLineOrJavadocOrComment(line))
            {
                result.add(line);
                continue;
            }
            if (insideBody == false)
            {
                insideBody = line.startsWith("{");
                result.add(line);
                continue;
            }
            if (methodSignature.isEmpty())
            {
                if (line.startsWith("    public "))
                {
                    methodSignature.add(line);
                } else
                {
                    result.add(line);
                }
                continue;
            }
            if (line.trim().startsWith("{") == false)
            {
                methodSignature.add(line);
                continue;
            }
            String canonicalSignature = getCanonicalSignature(methodSignature);
            MethodWithAnnotations method = methods.get(canonicalSignature);
            result.addAll(method == null ? methodSignature : method.getAllLines());
            methodSignature.clear();
            result.add(line);
        }
        return result;
    }

    private static String getCanonicalSignature(List<String> signatureLines)
    {
        StringBuilder builder = new StringBuilder();
        for (String line : signatureLines)
        {
            builder.append(line.trim());
        }
        return getCanonicalSignature(builder.toString());
    }

    private static Map<String, MethodWithAnnotations> parseAndRemoveAuthorizationAnnotations(
            List<String> interfaceSourceCode)
    {
        MethodsBuilder builder = new MethodsBuilder();
        for (Iterator<String> iterator = interfaceSourceCode.iterator(); iterator.hasNext();)
        {
            String line = iterator.next();
            if (isEmptyLineOrJavadocOrComment(line))
            {
                continue;
            }
            boolean remove = builder.addLine(line);
            if (remove)
            {
                iterator.remove();
            }
        }
        return builder.getMethods();
    }

    private static List<String> removeAuthorizationGuardAnnotations(List<String> interfaceSourceCode)
    {
        List<String> result = new ArrayList<String>();
        for (String line : interfaceSourceCode)
        {
            String modifiedLine = removeAuthorizationGuardAnnotation(line);
            if (modifiedLine.trim().length() > 0 || line.indexOf('@') < 0)
            {
                result.add(modifiedLine);
            }
        }
        return result;
    }

    private static String removeAuthorizationGuardAnnotation(String line)
    {
        int indexOfAuthorizationGuardAnnotation = line.indexOf(AUTHORIZATION_GUARD_ANNOTATION);
        if (indexOfAuthorizationGuardAnnotation < 0)
        {
            return line;
        }
        int indexOfAnnotationEnd = line.indexOf(")", indexOfAuthorizationGuardAnnotation);
        if (indexOfAnnotationEnd < 0)
        {
            throw new IllegalArgumentException("Multiline annotation?: " + line);
        }
        return line.substring(0, indexOfAuthorizationGuardAnnotation)
                + line.substring(indexOfAnnotationEnd + 1);
    }

    private static boolean isEmptyLineOrJavadocOrComment(String line)
    {
        String trimmedLine = line.trim();
        return trimmedLine.length() == 0 || trimmedLine.startsWith("//")
                || trimmedLine.startsWith("/*") || trimmedLine.startsWith("/**")
                || trimmedLine.startsWith("*");
    }

    private static void writeBack(File file, List<String> sourceCode)
    {
        StringBuilder builder = new StringBuilder();
        for (String line : sourceCode)
        {
            builder.append(line).append('\n');
        }
        FileUtilities.writeToFile(file, builder.toString());
    }
}
