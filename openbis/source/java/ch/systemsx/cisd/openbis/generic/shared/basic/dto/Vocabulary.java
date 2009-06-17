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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.annotation.CollectionMapping;

/**
 * Controlled vocabulary.
 * 
 * @author Izabela Adamczyk
 */
public final class Vocabulary extends CodeWithRegistration<Vocabulary>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();

    private String description;

    private boolean managedInternally;

    private boolean internalNamespace;

    private boolean chosenFromList;

    private boolean uploadedFromFile; // transient

    private String urlTemplate;

    public Vocabulary()
    {
    }

    public final List<VocabularyTerm> getTerms()
    {
        return terms;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = VocabularyTerm.class)
    public final void setTerms(final List<VocabularyTerm> terms)
    {
        this.terms = terms;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public final boolean isManagedInternally()
    {
        return managedInternally;
    }

    public final void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public final boolean isInternalNamespace()
    {
        return internalNamespace;
    }

    public final void setInternalNamespace(final boolean internalNamespace)
    {
        this.internalNamespace = internalNamespace;
    }

    public boolean isChosenFromList()
    {
        return chosenFromList;
    }

    public void setChosenFromList(boolean chosenFromList)
    {
        this.chosenFromList = chosenFromList;
    }

    public String getURLTemplate()
    {
        return urlTemplate;
    }

    public void setURLTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    public boolean isUploadedFromFile()
    {
        return uploadedFromFile;
    }

    public void setUploadedFromFile(boolean uploadedFromFile)
    {
        this.uploadedFromFile = uploadedFromFile;
    }
}
