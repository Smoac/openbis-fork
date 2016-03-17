/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Vocabulary term perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.vocabulary.id.VocabularyTermPermId")
public class VocabularyTermPermId implements IVocabularyTermId, Serializable
{

    private static final long serialVersionUID = 1L;

    private String vocabularyCode;

    private String termCode;

    /**
     * @param vocabularyCode Vocabulary code, e.g. "MY_VOCABULARY"
     * @param termCode Vocabulary term code, e.g. "MY_TERM".
     */
    public VocabularyTermPermId(String vocabularyCode, String termCode)
    {
        setVocabularyCode(vocabularyCode != null ? vocabularyCode.toUpperCase() : null);
        setTermCode(termCode != null ? termCode.toUpperCase() : null);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private VocabularyTermPermId()
    {
        super();
    }

    public String getVocabularyCode()
    {
        return vocabularyCode;
    }

    private void setVocabularyCode(String vocabularyCode)
    {
        if (vocabularyCode == null)
        {
            throw new IllegalArgumentException("Vocabulary code cannot be null");
        }
        this.vocabularyCode = vocabularyCode;
    }

    public String getTermCode()
    {
        return termCode;
    }

    private void setTermCode(String termCode)
    {
        if (termCode == null)
        {
            throw new IllegalArgumentException("Term code cannot be null");
        }
        this.termCode = termCode;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vocabularyCode == null) ? 0 : vocabularyCode.hashCode());
        result = prime * result + ((termCode == null) ? 0 : termCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        VocabularyTermPermId other = (VocabularyTermPermId) obj;
        if (vocabularyCode == null)
        {
            if (other.vocabularyCode != null)
            {
                return false;
            }
        } else if (!vocabularyCode.equals(other.vocabularyCode))
        {
            return false;
        }
        if (termCode == null)
        {
            if (other.termCode != null)
            {
                return false;
            }
        } else if (!termCode.equals(other.termCode))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return getTermCode() + " (" + getVocabularyCode() + ")";
    }

}
