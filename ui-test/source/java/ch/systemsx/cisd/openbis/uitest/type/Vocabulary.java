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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;

/**
 * @author anttil
 */
public class Vocabulary implements Browsable
{

    private String code;

    private String description;

    private Set<String> terms;

    private String url;

    public Vocabulary()
    {
        this.code = UUID.randomUUID().toString();
        this.description = "";
        this.terms = new HashSet<String>();
        this.terms.add("term1");
        this.url = "http://invalid.com/${term}";
    }

    public String getCode()
    {
        return code;
    }

    public Vocabulary setCode(String code)
    {
        this.code = code;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public Vocabulary setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public Set<String> getTerms()
    {
        return terms;
    }

    public Vocabulary setTerms(Set<String> terms)
    {
        this.terms = terms;
        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public Vocabulary setUrl(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public boolean isRepresentedBy(Map<String, String> row)
    {
        return this.code.equalsIgnoreCase(row.get("Code"));
    }

    @Override
    public String toString()
    {
        return "Vocabulary " + this.code + ": " + this.terms;
    }
}
