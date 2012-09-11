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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
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

    private final SampleUpdatesCollectionPredicate sampleUpdatesPredicate;

    private final SampleOwnerIdentifierPredicate sampleOwnerIdentifierPredicate;

    private final ExistingSpaceIdentifierPredicate experimentOwnerIdentifierPredicate;

    private final DataSetUpdatesCollectionPredicate dataSetUpdatesCollectionPredicate;

    public AtomicOperationsPredicate()
    {
        newExperimentPredicate = new NewExperimentPredicate();
        experimentUpdatesPredicate = new ExperimentUpdatesPredicate();
        newSamplePredicate = new NewSamplePredicate();
        sampleUpdatesPredicate = new SampleUpdatesCollectionPredicate();
        sampleOwnerIdentifierPredicate = new SampleOwnerIdentifierPredicate(true, true);
        experimentOwnerIdentifierPredicate = new ExistingSpaceIdentifierPredicate();
        dataSetUpdatesCollectionPredicate = new DataSetUpdatesCollectionPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        newExperimentPredicate.init(provider);
        experimentUpdatesPredicate.init(provider);
        newSamplePredicate.init(provider);
        sampleUpdatesPredicate.init(provider);
        sampleOwnerIdentifierPredicate.init(provider);
        experimentOwnerIdentifierPredicate.init(provider);
        dataSetUpdatesCollectionPredicate.init(provider);
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
                result = evaluateDataSetRegistrationsPredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateDataSetUpdatesPredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateSpaceRegistrations();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateMaterialRegistrations();
            }

            return result;
        }

        private Status evaluateSpaceRegistrations()
        {
            if (value.getSpaceRegistrations() != null && value.getSpaceRegistrations().size() > 0)
            {
                return isInstanceEtlServer(person);
            } else
            {
                return Status.OK;
            }
        }

        private Status evaluateMaterialRegistrations()
        {
            if (value.getMaterialRegistrations() != null
                    && value.getMaterialRegistrations().size() > 0)
            {
                return isInstanceEtlServer(person);
            } else
            {
                return Status.OK;
            }
        }

        private Status isInstanceEtlServer(PersonPE aPerson)
        {
            for (RoleAssignmentPE role : aPerson.getRoleAssignments())
            {
                if (role.getSpace() == null)
                {
                    RoleCode roleCode = role.getRole();
                    if (RoleCode.ADMIN.equals(roleCode) || RoleCode.ETL_SERVER.equals(roleCode))
                    {
                        return Status.OK;
                    }
                }
            }
            return Status.createError(false,
                    "None of method roles '[INSTANCE_ETL_SERVER, INSTANCE_ADMIN]' could be found in roles of user '"
                            + aPerson.getUserId() + "'.");
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
            return predicate.sampleUpdatesPredicate.doEvaluation(person, allowedRoles,
                    value.getSampleUpdates());
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

        private Status evaluateDataSetRegistrationsPredicate()
        {
            for (NewExternalData newExternalData : value.getDataSetRegistrations())
            {
                Status status =
                        evaluateDataSetPredicate(newExternalData.getSampleIdentifierOrNull(),
                                newExternalData.getExperimentIdentifierOrNull());
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
            }
            return Status.OK;
        }

        private Status evaluateDataSetUpdatesPredicate()
        {
            return predicate.dataSetUpdatesCollectionPredicate.doEvaluation(person, allowedRoles,
                    value.getDataSetUpdates());
        }

        private Status evaluateDataSetPredicate(SampleIdentifier sampleIdentifier,
                ExperimentIdentifier experimentIdentifier)
        {
            Status status;
            if (null != sampleIdentifier)
            {
                status =
                        predicate.sampleOwnerIdentifierPredicate.doEvaluation(person, allowedRoles,
                                sampleIdentifier);
            } else
            {
                status =
                        predicate.experimentOwnerIdentifierPredicate.doEvaluation(person,
                                allowedRoles, experimentIdentifier);
            }
            return status;
        }
    }
}
