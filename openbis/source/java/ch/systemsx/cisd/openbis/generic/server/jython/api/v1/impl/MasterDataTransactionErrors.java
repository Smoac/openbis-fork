/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IAbstractType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;

/**
 * @author Kaloyan Enimanev
 */
public class MasterDataTransactionErrors
{
    public abstract static class TransactionError
    {
        private final Exception exception;

        TransactionError(Exception ex)
        {
            this.exception = ex;
        }

        Exception getException()
        {
            return exception;
        }

        abstract String getDescription();
    }

    private static class TypeRegistrationError extends TransactionError
    {
        private final IAbstractType type;

        TypeRegistrationError(Exception ex, IAbstractType type)
        {
            super(ex);
            this.type = type;
        }

        @Override
        String getDescription()
        {
            return String.format("Failed to register type '%s': %s", type.getCode(), getException()
                    .getMessage());
        }
    }

    private static class PropertyAssignmentError extends TransactionError
    {
        private final IPropertyAssignment propertyAssignment;

        PropertyAssignmentError(Exception ex, IPropertyAssignment propertyAssignment)
        {
            super(ex);
            this.propertyAssignment = propertyAssignment;
        }

        @Override
        String getDescription()
        {
            return String.format("Failed to assign property '%s' <-> '%s': %s",
                    propertyAssignment.getEntityTypeCode(),
                    propertyAssignment.getPropertyTypeCode(), getException().getMessage());
        }
    }

    private static class VocabularyRegistrationError extends TransactionError
    {
        private final IVocabulary vocabulary;

        VocabularyRegistrationError(Exception ex, IVocabulary vocabulary)
        {
            super(ex);
            this.vocabulary = vocabulary;
        }

        @Override
        String getDescription()
        {
            return String.format("Failed to register vocabulary '%s': %s", vocabulary.getCode(),
                    getException().getMessage());
        }
    }

    private List<TransactionError> errors =
            new ArrayList<MasterDataTransactionErrors.TransactionError>();

    public boolean hasErrors()
    {
        return errors.size() > 0;
    }

    public List<TransactionError> getErrors()
    {
        return Collections.unmodifiableList(errors);
    }

    public void addTypeRegistrationError(Exception ex, IAbstractType type)
    {
        TypeRegistrationError error = new TypeRegistrationError(ex, type);
        errors.add(error);
    }

    public void addPropertyAssignmentError(Exception ex, PropertyAssignment assignment)
    {
        PropertyAssignmentError error = new PropertyAssignmentError(ex, assignment);
        errors.add(error);

    }

    public void addVocabularyRegistrationError(Exception ex, IVocabulary vocabulary)
    {
        VocabularyRegistrationError error = new VocabularyRegistrationError(ex, vocabulary);
        errors.add(error);
    }
}
