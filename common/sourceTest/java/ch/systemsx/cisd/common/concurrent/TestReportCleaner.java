package ch.systemsx.cisd.common.concurrent;

import java.util.HashSet;
import java.util.Set;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/*
 * TO-DO Clean Repeated success counts.
 * 
 * @author anttil & juanf
 */
public class TestReportCleaner extends TestListenerAdapter
{
    Set<ITestResult> failures = new HashSet<ITestResult>();

    // Set<ITestResult> successes = new HashSet<ITestResult>();
    //
    // @Override
    // public void onTestSuccess(ITestResult tr)
    // {
    // super.onTestSuccess(tr);
    // successes.add(tr);
    // }

    @Override
    public void onTestFailure(ITestResult tr)
    {
        super.onTestFailure(tr);

        RetryTen testRetryAnalyzer = (RetryTen) tr.getMethod().getRetryAnalyzer();
        System.out.println("COUNTER: " + testRetryAnalyzer.getCount());

        if (testRetryAnalyzer == null || testRetryAnalyzer.getCount() == 0)
        {
            failures.add(tr);
        }
        else
        {
            // TO-DO, Uncomment this to count the retries as successes.
            // tr.setStatus(ITestResult.SUCCESS);
            // tr.getTestContext().getFailedTests().removeResult(tr.getMethod());
            // this.getFailedTests().remove(tr);
            // Reporter.setCurrentTestResult(null);
        }
    }

    @Override
    public void onFinish(ITestContext testContext)
    {
        // for (Iterator<ITestResult> iter = this.getFailedTests().iterator(); iter.hasNext();)
        // {
        // ITestResult failedResult = iter.next();
        // if (!failures.contains(failedResult))
        // {
        // iter.remove();
        // }
        // }

        // Deletes retries counted as failures at the suite level
        for (ITestResult failedResult : testContext.getFailedTests().getAllResults())
        {
            if (!failures.contains(failedResult))
            {
                testContext.getFailedTests().removeResult(failedResult.getMethod());
            }
        }

        // Breaks the results when is a failure at the suite level
        // IResultMap failedResults = this.getTestContexts().get(0).getFailedTests();
        // for (ITestResult failedResult : failedResults.getAllResults())
        // {
        // if (!failures.contains(failedResult))
        // {
        // failedResults.removeResult(failedResult.getMethod());
        // }
        // }
    }

}
