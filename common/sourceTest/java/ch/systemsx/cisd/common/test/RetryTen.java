package ch.systemsx.cisd.common.test;

import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

/**
 * Retry10 extension to allow access the counter variable.
 * 
 * @author anttil & juanf
 */
public final class RetryTen extends RetryAnalyzerCount
{
    public RetryTen()
    {
        setCount(10);
    }

    public int getCount()
    {
        return super.getCount();
    }

    //
    // RetryAnalyzerCount
    //

    @Override
    public final boolean retryMethod(final ITestResult result)
    {
        return true;
    }

}