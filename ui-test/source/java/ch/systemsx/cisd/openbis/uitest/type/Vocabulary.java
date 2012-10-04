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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import ch.systemsx.cisd.openbis.uitest.layout.VocabularyBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.VocabularyBrowser;

/**
 * @author anttil
 */
public class Vocabulary implements Browsable<VocabularyBrowser>
{
    private final String code;

    private String description;

    private Set<String> terms;

    private String url;

    Vocabulary(String code, String description, Set<String> terms, String url)
    {
        this.code = code;
        this.description = description;
        this.terms = terms;
        this.url = url;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    public Set<String> getTerms()
    {
        return terms;
    }

    public String getUrl()
    {
        return url;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setTerms(Set<String> terms)
    {
        this.terms = terms;
    }

    void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public Collection<String> getColumns()
    {
        return Arrays.asList("Code", "Description", "URL Template");
    }

    @Override
    public String toString()
    {
        return "Vocabulary " + this.code + ": " + this.terms;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Vocabulary)
        {
            return ((Vocabulary) o).getCode().equals(code);
        }
        return false;
    }

    @Override
    public VocabularyBrowserLocation getBrowserLocation()
    {
        return new VocabularyBrowserLocation();
    }
}
