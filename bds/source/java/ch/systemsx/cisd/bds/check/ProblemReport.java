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

package ch.systemsx.cisd.bds.check;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows gathering error and warning messages.
 * 
 * @author Izabela Adamczyk
 */
public class ProblemReport
{
    class Problem
    {
        final String message;

        final Severity severity;

        public Problem(final String message, final Severity level)
        {
            this.message = message;
            this.severity = level;
        }

        @Override
        public String toString()
        {
            if (Severity.ERROR.equals(this.severity))
            {
                return "ERROR: " + message;
            } else
            {
                return "WARNING: " + message;
            }
        }

    }

    private enum Severity
    {
        ERROR, WARNING
    }

    private final List<Problem> problems = new ArrayList<Problem>();

    public boolean noProblemsFound()
    {
        return problems.size() == 0;
    }

    public int numberOfProblems()
    {
        return problems.size();
    }

    public void error(final String error)
    {
        assert error != null;
        problems.add(new Problem(error, Severity.ERROR));
    }

    public void warning(final String error)
    {
        assert error != null;
        problems.add(new Problem(error, Severity.WARNING));
    }

    @Override
    public final String toString()
    {
        final StringBuilder builder = new StringBuilder();
        for (final Problem problem : problems)
        {
            builder.append(problem + "\n");
        }
        return builder.toString();
    }

}
