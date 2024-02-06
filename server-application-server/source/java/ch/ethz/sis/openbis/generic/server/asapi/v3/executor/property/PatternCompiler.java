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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public final class PatternCompiler implements IPatternCompiler
{
    private static final String SEPARATOR = ",";

    private static final String DECIMAL_REGEXP = "(\\.\\d+)?";
    private static final String LEADING_ZERO_REGEXP = "0*";
    @Override
    public Pattern compilePattern(String pattern, String patternType)
    {
        if(patternType == null || patternType.trim().isEmpty()) {
            return null;
        }
        switch (patternType.trim().toUpperCase()) {
            case "PATTERN":
                return Pattern.compile(pattern);
            case "VALUES":
                return compileValues(pattern);
            case "RANGES":
                return compileRanges(pattern);
            default:
                throw new UserFailureException("Unknown pattern type specified!");
        }
    }

    private Pattern compileValues(String pattern){
        final String regex = "(?<!\\\\)" + Pattern.quote(SEPARATOR);
        String result = Stream.of(pattern.split(regex))
                .map(String::trim)
                .map(s -> s.substring(1, s.length()-2))
                .map(Pattern::quote)
                .reduce("", (a,b) -> a + "|" + b);
        return Pattern.compile(result);
    }


    private Pattern compileRanges(String pattern) {
        final String regex = "(?<!\\\\)" + Pattern.quote(SEPARATOR);
        String result = Stream.of(pattern.split(regex))
                .map(s -> s.replaceAll("\\s+", ""))
                .map(this::convertRange)
                .reduce("", (a,b) -> a + "|" + b);
        return Pattern.compile(result);
    }

    private Pair<Long> stringToPair(String range)
    {
        if(range.length() < 3)
        {
            // minimal range has at least 3 characters, e.g. 1-1
            throw new UserFailureException("Wrong range format: '" + range + "'");
        }

        char[] characters = range.toCharArray();
        boolean negative = false;
        int i = 0;
        for(;i<characters.length;i++)
        {
            if(characters[i] == '-' || (characters[i] >= '0' && characters[i] <= '9'))
            {
                break;
            }
        }
        // first number
        long first = 0L;
        negative = characters[i] == '-';
        if(negative) {
            i++;
        }
        if(i >= characters.length) {
            throw new UserFailureException("Wrong range format: '" + range + "'");
        }
        for(;i<characters.length;i++)
        {
            if(characters[i] >= '0' && characters[i] <= '9')
            {
                first *= 10;
                first += (characters[i] - '0');
            }
            else
            {
                break;
            }
        }
        if(negative)
        {
            first = -first;
        }
        if(i >= characters.length) {
            throw new UserFailureException("Wrong range format: '" + range + "'");
        }
        // scroll to '-' separator
        for(;i<characters.length;i++)
        {
            if(characters[i] == '-'){
                i++;
                break;
            }
        }
        if(i >= characters.length) {
            throw new UserFailureException("Wrong range format: '" + range + "'");
        }
        // second number
        long second = 0L;
        for(;i<characters.length;i++)
        {
            if(characters[i] == '-' || (characters[i] >= '0' && characters[i] <= '9'))
            {
                break;
            }
        }
        if(i >= characters.length) {
            throw new UserFailureException("Wrong range format: '" + range + "'");
        }
        negative = characters[i] == '-';
        if(negative) {
            i++;
        }
        if(i >= characters.length) {
            throw new UserFailureException("Wrong range format: '" + range + "'");
        }
        for(;i<characters.length;i++)
        {
            if(characters[i] >= '0' && characters[i] <= '9')
            {
                second *= 10;
                second += (characters[i] - '0');
            }
            else
            {
                break;
            }
        }
        if(negative)
        {
            second = -second;
        }
       return new Pair<>(first, second);
    }

    private String convertRange(String range) {
        if(range.charAt(0) == '[' && range.charAt(range.length()-1) == ']') {
            range = range.substring(1, range.length()-2);
        }

        for(char a : range.toCharArray())
        {

        }
        Pair<Long> pair = stringToPair(range);
        long from = pair.first;
        long to = pair.second;

        long min = Math.min(from, to);
        long max = Math.max(from, to);

        String result = "";
        if(max < 0L) {
            long swap = -min;
            min = -max;
            max = swap;
            List<String> patterns = splitToPatterns(min, max);
            result = joinRegexp(patterns, "-", DECIMAL_REGEXP);
        } else if(min < 0L)
        {
            if(max == 0L)
            {
                List<String> patterns = splitToPatterns(max, -min);
                result = String.format("(%s)|(0+)", joinRegexp(patterns, "-", DECIMAL_REGEXP));
            } else {
                List<String> patternsNegative = splitToPatterns(0L, -min);
                List<String> patternsPositive = splitToPatterns(0L, max);
                result = joinRegexp(patternsNegative, "-", DECIMAL_REGEXP);
                String result2 = joinRegexp(patternsPositive, LEADING_ZERO_REGEXP, DECIMAL_REGEXP);

                result = String.format("(%s)|(%s)", result, result2);

            }


        } else
        {
            List<String> patterns = splitToPatterns(min, max);
            result = joinRegexp(patterns, LEADING_ZERO_REGEXP, DECIMAL_REGEXP);
        }

        return result;
    }

    private String joinRegexp(List<String> parts, String prefix, String suffix)
    {
        StringBuilder builder = new StringBuilder(prefix);
        builder.append("(");
        int i=0;
        for(String part : parts)
        {
            if(i>0) {
                builder.append("|");
            }
            builder.append(part);
            i++;
        }
        builder.append(")");
        builder.append(suffix);
        return builder.toString();
    }

    private List<String> splitToPatterns(long minimum, long maximum){
        List<Long> ranges = splitToRanges(minimum, maximum);
        List<String> tokens = new ArrayList<>();
        long start = minimum;
        for(Long max : ranges)
        {
            Triple<String, List<Long>, Long> obj = rangeToPattern(String.valueOf(start), String.valueOf(max));
            String objs = obj.first + (obj.second.get(0) > 0 ?"{" + obj.second.get(0) + "}" : "");
            tokens.add(objs);
            start = max + 1;
        }
        return tokens;
    }

    private Triple<String, List<Long>, Long> rangeToPattern(String start, String stop)
    {
        if(start.equals(stop))
        {
            return new Triple<>(start, List.of(0L), 0L);
        }
        List<Pair<Character>> zipped = zip(start, stop);
        long digits = zipped.size();
        StringBuilder pattern = new StringBuilder();
        long count = 0;

        for (Pair<Character> digit : zipped)
        {
            if(digit.first == digit.second)
            {
                pattern.append(digit.first);
            } else if(digit.first != '0' || digit.second != '9')
            {
                pattern.append(toCharacterClass(digit.first, digit.second));
            } else {
                count ++;
            }
        }

        if(count > 0)
        {
            pattern.append("\\d");
        }
        return new Triple<>(pattern.toString(), List.of(count), digits);
    }

    private String toCharacterClass(Character a, Character b)
    {
        return "[" + a + ((b-a) == 1 ? "" : "-") + b + "]";
    }

    private List<Pair<Character>> zip(String a, String b)
    {
        List<Pair<Character>> result = new ArrayList<>();
        for(int i=0;i<a.length();i++)
        {
            result.add(new Pair<>(a.charAt(i), b.charAt(i)));
        }
        return result;
    }

    private static class Pair<T>
    {
        T first;
        T second;
        Pair(T a, T b)
        {
            first = a;
            second = b;
        }
    }

    private static class Triple<T, X, Y>
    {
        T first;
        X second;
        Y third;
        Triple(T a, X b, Y c)
        {
            first = a;
            second = b;
            third = c;
        }
    }

    private List<Long> splitToRanges(long min, long max)
    {
        long nines = 1;
        long zeros = 1;

        long stop = countNines(min, nines);
        Set<Long> stops = new HashSet<>();
        stops.add(max);

        while(min <= stop && stop <= max)
        {
            stops.add(stop);
            nines += 1;
            stop = countNines(min, nines);
        }

        stop = countZeros(max+1, zeros) - 1;

        while(min < stop && stop <= max)
        {
            stops.add(stop);
            zeros += 1;
            stop = countZeros(max +1, zeros) - 1;
        }

        return stops.stream().sorted().collect(Collectors.toList());
    }

    private long countNines(long min, long nines)
    {
        String value = String.valueOf(min);
        long index = value.length()-nines;
        StringBuilder builder = new StringBuilder(index>=0 ? value.substring(0, (int)index) : "");
        for(long i=0;i<nines;i++) {
            builder.append("9");
        }
        return Long.parseLong(builder.toString());
    }

    private long countZeros(long max, long zeros)
    {
        return (long)(max - (max % Math.pow(10, zeros)));
    }



}
