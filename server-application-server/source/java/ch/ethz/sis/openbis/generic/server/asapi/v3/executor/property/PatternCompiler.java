/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public final class PatternCompiler implements IPatternCompiler
{
    private static final String SEPARATOR = ",";
    @Override
    public String compilePattern(String pattern, String patternType)
    {
        if(patternType == null || patternType.trim().isEmpty()) {
            return null;
        }
        switch (patternType.trim().toUpperCase()) {
            case "PATTERN":
                return pattern;
            case "VALUES":
                return compileValues(pattern);
            case "RANGES":
                return compileRanges(pattern);
            default:
                throw new UserFailureException("Unknown pattern type specified!");
        }
    }

    private String compileValues(String pattern){
        final String regex = "(?<!\\\\)" + Pattern.quote(SEPARATOR);
        String result = Stream.of(pattern.split(regex))
                .map(String::trim)
                .map(Pattern::quote)
                .reduce("", (a,b) -> a + "|" + b);
        return "VALUES:" + result;
    }


    private String compileRanges(String pattern) {
        final String regex = "(?<!\\\\)" + Pattern.quote(SEPARATOR);
        String result = Stream.of(pattern.split(regex))
                .map(String::trim)
                .map(this::convertRange)
                .reduce("", (a,b) -> a + "|" + b);
        return "RANGES:" + result;
    }

    private String convertRange(String range) {
        if(range.charAt(0) == '[' && range.charAt(range.length()-1) == ']') {
            return range;
        }
        return "[" + range + "]";
    }
}
