/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.deletion;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.DeletedObject;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DeletionValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;

/**
 * @author pkupczyk
 */
public class DeletionTranslator extends
        AbstractCachingTranslator<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion, Deletion, DeletionFetchOptions>
{

    private IDAOFactory daoFactory;

    public DeletionTranslator(TranslationContext translationContext, DeletionFetchOptions fetchOptions, IDAOFactory daoFactory)
    {
        super(translationContext, fetchOptions);
        this.daoFactory = daoFactory;
    }

    @Override
    protected boolean shouldTranslate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion input)
    {
        DeletionValidator validator = new DeletionValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));
        return validator.doValidation(getTranslationContext().getSession().tryGetPerson(), input);
    }

    @Override
    protected Deletion createObject(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion input)
    {
        Deletion deletion = new Deletion();
        deletion.setId(new DeletionTechId(input.getId()));
        deletion.setReason(input.getReason());
        deletion.setFetchOptions(new DeletionFetchOptions());
        return deletion;
    }

    @Override
    protected void updateObject(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion input, Deletion output, Relations relations)
    {
        if (getFetchOptions().hasDeletedObjects())
        {
            output.getFetchOptions().fetchDeletedObjects();

            if (input.getDeletedEntities() != null)
            {
                List<DeletedObject> deletedObjects = new ArrayList<DeletedObject>(input.getDeletedEntities().size());

                for (IEntityInformationHolderWithIdentifier deletedEntity : input.getDeletedEntities())
                {
                    DeletedObject deletedObject = new DeletedObject();

                    switch (deletedEntity.getEntityKind())
                    {
                        case EXPERIMENT:
                            deletedObject.setId(new ExperimentPermId(deletedEntity.getPermId()));
                            break;
                        case SAMPLE:
                            deletedObject.setId(new SamplePermId(deletedEntity.getPermId()));
                            break;
                        case DATA_SET:
                            deletedObject.setId(new DataSetPermId(deletedEntity.getPermId()));
                            break;
                        case MATERIAL:
                            deletedObject.setId(new MaterialPermId(deletedEntity.getCode(), deletedEntity.getEntityType().getCode()));
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown entity kind: " + deletedEntity.getEntityKind());
                    }

                    deletedObjects.add(deletedObject);
                }

                output.setDeletedObjects(deletedObjects);
            }
        }
    }
}
