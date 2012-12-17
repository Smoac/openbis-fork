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

package ch.systemsx.cisd.common.jython.evaluator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

/**
 * @author anttil
 */
public class EvaluatorCacheTest
{

    private static String DUMMY_SCRIPT = "x=1\ny=2";

    private static String DUMMY_SCRIPT_2 = "x=2\ny=1";

    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.HOURS,
            new LinkedBlockingDeque<Runnable>());

    @Test
    public void sameThreadGetsAlwaysSameEvaluatorInstanceForSameScriptAndExpression()
            throws Exception
    {
        Evaluator evaluator1 = EvaluatorCache.getEvaluator("", null, DUMMY_SCRIPT);
        Evaluator evaluator2 = EvaluatorCache.getEvaluator("", null, DUMMY_SCRIPT);

        assertThat(evaluator1, is(sameInstance(evaluator2)));
    }

    @Test
    public void differenceInScriptsCausesDifferentEvaluatorInstancesToBeReturned() throws Exception
    {
        Evaluator evaluator1 = EvaluatorCache.getEvaluator("", null, DUMMY_SCRIPT);
        Evaluator evaluator2 = EvaluatorCache.getEvaluator("", null, DUMMY_SCRIPT_2);

        assertThat(evaluator1, is(not(sameInstance(evaluator2))));
    }

    @Test
    public void differenceInExpressionsCausesDifferentEvaluatorInstancesToBeReturned()
            throws Exception
    {
        Evaluator evaluator1 = EvaluatorCache.getEvaluator("get_something()", null, DUMMY_SCRIPT);
        Evaluator evaluator2 =
                EvaluatorCache.getEvaluator("get_something_else()", null, DUMMY_SCRIPT);

        assertThat(evaluator1, is(not(sameInstance(evaluator2))));
    }

    @Test
    public void differentThreadsGetDifferentEvaluatorsForSameScriptAndSameExpressions()
            throws Exception
    {
        Evaluator evaluator1 = EvaluatorCache.getEvaluator("", null, DUMMY_SCRIPT);
        Future<Evaluator> evaluator2 = executor.submit(new Callable<Evaluator>()
            {
                @Override
                public Evaluator call() throws Exception
                {
                    return EvaluatorCache.getEvaluator("", null, DUMMY_SCRIPT);
                }
            });

        assertThat(evaluator1, is(not(sameInstance(evaluator2.get()))));
    }

    @Test
    public void globalVariablesSetInPreviousRunAreClearedBeforeHandingOutAnEvaluatorInstance()
            throws Exception
    {
        Evaluator evaluator =
                EvaluatorCache
                        .getEvaluator("return_x_if_defined_else_0()", null, readFile("script.py"));
        evaluator.evalFunction("set_x", "3");

        assertThat(evaluator.evalAsString(), is("3"));
        evaluator =
                EvaluatorCache
                        .getEvaluator("return_x_if_defined_else_0()", null, readFile("script.py"));
        assertThat(evaluator.evalAsString(), is("0"));
    }

    private static String readFile(String name)
    {
        String filePath = "sourceTest/java/ch/systemsx/cisd/common/jython/evaluator/" + name;
        try
        {
            FileInputStream stream = new FileInputStream(new File(filePath));
            try
            {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                return Charset.defaultCharset().decode(bb).toString();
            } finally
            {
                stream.close();
            }
        } catch (IOException e)
        {
            throw new IllegalArgumentException("Could not read file " + filePath);
        }
    }
}
