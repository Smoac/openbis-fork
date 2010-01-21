/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.clientnonjs;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper.OpenInitialTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class UrlParamsHelperTest extends AssertJUnit
{

    private Mockery context;

    private IViewContext<?> viewContext;

    private ICommonClientServiceAsync commonService;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        viewContext = context.mock(IViewContext.class);
        commonService = context.mock(ICommonClientServiceAsync.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOpenInitialViewerTab()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));

                    one(commonService).getEntityInformationHolder(with(EntityKind.SAMPLE),
                            with("20100104150239401-871"),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        final UrlParamsHelper urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initializeUrlParametersForTest("entity=SAMPLE&permId=20100104150239401-871");

        IDelegatedAction action = urlParamsHelper.getOpenInitialTabAction();
        action.execute();

        context.assertIsSatisfied();
    }

    @Test
    public void testOpenInitialViewerTabWithIncompleteLink()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));
                }

            });

        // State used by the two sub-tests
        UrlParamsHelper urlParamsHelper;
        UrlParamsHelper.OpenInitialTabAction action;

        // No permId supplied
        urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initializeUrlParametersForTest("entity=SAMPLE");

        action = (OpenInitialTabAction) urlParamsHelper.getOpenInitialTabAction();
        try
        {
            action.openInitialTabUnderExceptionHandlerForTest();
            fail("A URL with no permId should result in an exception.");
        } catch (UserFailureException expected)
        {
            // Do nothing -- this should happen
        }

        // No entity supplied
        urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initializeUrlParametersForTest("permId=20100104150239401-871");

        action = (OpenInitialTabAction) urlParamsHelper.getOpenInitialTabAction();
        try
        {
            action.openInitialTabUnderExceptionHandlerForTest();
            fail("A URL with no entity should result in an exception.");
        } catch (UserFailureException expected)
        {
            // Do nothing -- this should happen
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testOpenInitialViewerTabWithInvalidEntity()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));
                }

            });

        UrlParamsHelper urlParamsHelper;
        UrlParamsHelper.OpenInitialTabAction action;

        urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initializeUrlParametersForTest("entity=JUNK");

        action = (OpenInitialTabAction) urlParamsHelper.getOpenInitialTabAction();
        try
        {
            action.openInitialTabUnderExceptionHandlerForTest();
            fail("A URL with invalid entity should result in an exception.");
        } catch (UserFailureException expected)
        {
            // Do nothing -- this should happen
        }

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOpenInitialSearchTab()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));

                    one(commonService).listSamples(
                            with(Expectations.any(ListSampleDisplayCriteria.class)),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        final UrlParamsHelper urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initializeUrlParametersForTest("searchEntity=SAMPLE&code=CL1");

        IDelegatedAction action = urlParamsHelper.getOpenInitialTabAction();
        action.execute();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOpenInitialSearchTabWithoutCode()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));

                    one(commonService).listSamples(
                            with(Expectations.any(ListSampleDisplayCriteria.class)),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        final UrlParamsHelper urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initializeUrlParametersForTest("searchEntity=SAMPLE");

        IDelegatedAction action = urlParamsHelper.getOpenInitialTabAction();
        action.execute();

        context.assertIsSatisfied();
    }

    @Test
    public void testOpenInitialSearchTabWithDataset()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));
                }

            });

        final UrlParamsHelper urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initializeUrlParametersForTest("searchEntity=DATA_SET");

        OpenInitialTabAction action =
                (OpenInitialTabAction) urlParamsHelper.getOpenInitialTabAction();

        try
        {
            action.openInitialTabUnderExceptionHandlerForTest();
            fail("Only SAMPLE is supported by the search link mechanism right now.");
        } catch (UserFailureException expected)
        {
            // Do nothing -- this should happen
        }

        context.assertIsSatisfied();
    }
}
