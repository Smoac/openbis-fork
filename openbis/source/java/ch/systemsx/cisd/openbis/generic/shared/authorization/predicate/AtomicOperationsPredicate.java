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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * The predicate for the {@link AtomicEntityOperationDetails}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicOperationsPredicate extends AbstractPredicate<AtomicEntityOperationDetails>
{

    private final NewExperimentPredicate newExperimentPredicate;

    private final ExperimentUpdatesPredicate experimentUpdatesPredicate;

    private final NewSamplePredicate newSamplePredicate;

    private final SampleUpdatesPredicate sampleUpdatesPredicate;

    private final SampleOwnerIdentifierPredicate sampleOwnerIdentifierPredicate;

    private final ExistingSpaceIdentifierPredicate experimentOwnerIdentifierPredicate;

    public AtomicOperationsPredicate()
    {
        newExperimentPredicate = new NewExperimentPredicate();
        experimentUpdatesPredicate = new ExperimentUpdatesPredicate();
        newSamplePredicate = new NewSamplePredicate();
        sampleUpdatesPredicate = new SampleUpdatesPredicate();
        sampleOwnerIdentifierPredicate = new SampleOwnerIdentifierPredicate(true, true);
        experimentOwnerIdentifierPredicate = new ExistingSpaceIdentifierPredicate();
    }

    public void init(IAuthorizationDataProvider provider)
    {
        newExperimentPredicate.init(provider);
        experimentUpdatesPredicate.init(provider);
        newSamplePredicate.init(provider);
        sampleUpdatesPredicate.init(provider);
        sampleOwnerIdentifierPredicate.init(provider);
        experimentOwnerIdentifierPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "atomic entity operations";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            AtomicEntityOperationDetails value)
    {
        AtomicOperationsPredicateEvaluator evaluator =
                new AtomicOperationsPredicateEvaluator(this, person, allowedRoles, value);
        return evaluator.evaluate();
    }

    /**
     * A class to evaluate all the sub-predicates.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class AtomicOperationsPredicateEvaluator
    {
        private final AtomicOperationsPredicate predicate;

        private final PersonPE person;

        private final List<RoleWithIdentifier> allowedRoles;

        private final AtomicEntityOperationDetails value;

        private Status result = Status.OK;

        public AtomicOperationsPredicateEvaluator(AtomicOperationsPredicate predicate,
                PersonPE person, List<RoleWithIdentifier> allowedRoles,
                AtomicEntityOperationDetails value)
        {
            this.predicate = predicate;
            this.person = person;
            this.allowedRoles = allowedRoles;
            this.value = value;
        }

        public Status evaluate()
        {
            // Evaluate all the predicates, stopping if we find an operation that is not allowed
            if (result.equals(Status.OK))
            {
                result = evaluateExperimentUpdatePredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateNewExperimentPredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateSampleUpdatePredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateNewSamplePredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateDataSetsPredicate();
            }

            return result;
        }

        private Status evaluateExperimentUpdatePredicate()
        {
            for (ExperimentUpdatesDTO experimentUpdates : value.getExperimentUpdates())
            {
                Status status;

                status =
                        predicate.experimentUpdatesPredicate.doEvaluation(person, allowedRoles,
                                experimentUpdates);
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
            }
            return Status.OK;
        }

        private Status evaluateNewExperimentPredicate()
        {
            for (NewExperiment newExperiment : value.getExperimentRegistrations())
            {
                Status status;

                status =
                        predicate.newExperimentPredicate.doEvaluation(person, allowedRoles,
                                newExperiment);
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
            }
            return Status.OK;
        }

        private Status evaluateSampleUpdatePredicate()
        {
            for (SampleUpdatesDTO sampleUpdates : value.getSampleUpdates())
            {
                Status status =
                        predicate.sampleUpdatesPredicate.doEvaluation(person, allowedRoles,
                                sampleUpdates);
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
            }
            return Status.OK;
        }

        private Status evaluateNewSamplePredicate()
        {
            for (NewSample newSample : value.getSampleRegistrations())
            {
                Status status =
                        predicate.newSamplePredicate.doEvaluation(person, allowedRoles, newSample);
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
            }
            return Status.OK;
        }

        private Status evaluateDataSetsPredicate()
        {
            for (NewExternalData newExternalData : value.getDataSetRegistrations())
            {
                Status status;
                SampleIdentifier sampleIdentifier = newExternalData.getSampleIdentifierOrNull();
                if (null != sampleIdentifier)
                {
                    status =
                            predicate.sampleOwnerIdentifierPredicate.doEvaluation(person,
                                    allowedRoles, sampleIdentifier);
                } else
                {
                    ExperimentIdentifier experimentIdentifier =
                            newExternalData.getExperimentIdentifierOrNull();
                    status =
                            predicate.experimentOwnerIdentifierPredicate.doEvaluation(person,
                                    allowedRoles, experimentIdentifier);
                }
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
            }
            return Status.OK;
        }
    }
}
