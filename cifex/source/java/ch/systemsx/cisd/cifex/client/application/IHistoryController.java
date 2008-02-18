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

package ch.systemsx.cisd.cifex.client.application;

/**
 * Kind of history for pages.
 * 
 * @author Christian Ribeaud
 */
public interface IHistoryController
{

    /**
     * Sets the current page.
     * <p>
     * The old one (if not <code>null</code>) will become the previous page.
     * </p>
     */
    public void setCurrentPage(final Page page);

    /**
     * Returns the current Page.
     * 
     * @return <code>null</code> if no current page has been set till now.
     */
    public Page getCurrentPage();

    /**
     * Returns the previous page to the current one.
     * 
     * @return <code>null</code> if no current page has been set till now.
     */
    public Page getPreviousPage();

    //
    // Helper classes
    //

    public final static class Page
    {
        public final static Page MAIN_PAGE = new Page();

        public final static Page ADMIN_PAGE = new Page();

        public final static Page LOGIN_PAGE = new Page();

        public final static Page EDIT_PROFILE = new Page();

        public final static Page[] ALL_PAGES = new Page[]
            { MAIN_PAGE, ADMIN_PAGE, LOGIN_PAGE, EDIT_PROFILE };

        /** The page description. */
        private String description;

        private Page()
        {
        }

        public final String getDescription()
        {
            return description;
        }

        public final void setDescription(final String description)
        {
            this.description = description;
        }

        /** For given description returns the corresponding <code>Page</code>. */
        public final static Page getPageForDescription(final String description)
        {
            assert description != null : "Unspecifed description.";
            for (int i = 0; i < ALL_PAGES.length; i++)
            {
                final Page page = ALL_PAGES[i];
                if (page.getDescription().equals(description))
                {
                    return page;
                }
            }
            throw new IllegalArgumentException("No page found for description '" + description + "'.");
        }

        //
        // Object
        //

        public final String toString()
        {
            return getDescription();
        }
    }
}
