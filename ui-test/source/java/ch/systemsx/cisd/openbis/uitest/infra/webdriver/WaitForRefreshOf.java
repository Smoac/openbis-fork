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

package ch.systemsx.cisd.openbis.uitest.infra.webdriver;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Predicate;

import ch.systemsx.cisd.openbis.uitest.widget.Refreshable;

/**
 * @author anttil
 */
public class WaitForRefreshOf<T> extends FluentWait<Refreshable>
{

    private final String state;

    private Action<T> action;

    public WaitForRefreshOf(Refreshable widget)
    {
        super(widget);
        this.state = widget.getState();
    }

    @SuppressWarnings("hiding")
    public WaitForRefreshOf<T> after(Action<T> action)
    {
        this.action = action;
        return this;
    }

    public T withTimeoutOf(int seconds)
    {
        T result = action.execute();
        if (action.shouldWait(result))
        {
            withTimeout(seconds, TimeUnit.SECONDS)
                    .pollingEvery(100, TimeUnit.MILLISECONDS)
                    .until(new Predicate<Refreshable>()
                        {
                            @Override
                            public boolean apply(Refreshable widget)
                            {
                                return !state.equals(widget.getState());
                            }
                        });
        }
        return result;
    }
}
