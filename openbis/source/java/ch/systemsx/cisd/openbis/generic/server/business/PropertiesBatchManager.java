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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertiesBean;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;

/**
 * Handles Managed Properties of batch uploads/updates.
 * 
 * @author Franz-Josef Elmer
 */
public class PropertiesBatchManager implements IPropertiesBatchManager
{

    private static class EvaluationContext
    {
        ManagedPropertyEvaluator evaluator;

        ScriptPE scriptPE;
    }

    private final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, getClass());

    public void manageProperties(SampleTypePE sampleType, List<NewSample> samples,
            PersonPE registrator)
    {
        Set<? extends EntityTypePropertyTypePE> sampleTypePropertyTypes =
                sampleType.getSampleTypePropertyTypes();

        managePropertiesBeans(samples, sampleTypePropertyTypes, registrator);
    }

    public void manageProperties(ExperimentTypePE experimentType,
            List<? extends NewBasicExperiment> experiments, PersonPE registrator)
    {
        Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes =
                experimentType.getExperimentTypePropertyTypes();

        managePropertiesBeans(experiments, entityTypePropertyTypes, registrator);
    }

    public void manageProperties(MaterialTypePE materialType, List<NewMaterial> newMaterials,
            PersonPE registrator)
    {
        Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes =
                materialType.getMaterialTypePropertyTypes();
        managePropertiesBeans(newMaterials, entityTypePropertyTypes, registrator);
    }

    private void managePropertiesBeans(List<? extends IPropertiesBean> propertiesBeans,
            Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes, PersonPE registrator)
    {
        Map<String, EvaluationContext> contexts = createEvaluationContexts(entityTypePropertyTypes);
        PropertiesBatchEvaluationErrors errors =
                new PropertiesBatchEvaluationErrors(registrator, propertiesBeans.size());

        int rowNumber = 0;
        for (IPropertiesBean propertiesBean : propertiesBeans)
        {
            rowNumber++;
            List<IEntityProperty> newProperties =
                    accumulateNewProperties(propertiesBean, rowNumber, contexts, errors);
            IEntityProperty[] newPropArray =
                    newProperties.toArray(new IEntityProperty[newProperties.size()]);
            propertiesBean.setProperties(newPropArray);
        }

        if (errors.hasErrors())
        {
            // send an email, so that actions can be taken to repair the script
            notificationLog.error(errors.constructErrorReportEmail());
            // inform the user that batch import has failed
            throw new UserFailureException(errors.constructUserFailureMessage());
        }
    }

    private List<IEntityProperty> accumulateNewProperties(IPropertiesBean propertiesBean,
            int rowNumber, Map<String, EvaluationContext> contexts,
            PropertiesBatchEvaluationErrors errors)
    {
        List<IEntityProperty> newProperties = new ArrayList<IEntityProperty>();

        Map<String, Map<String, String>> subColumnBindings =
                createColumnBindingsMap(propertiesBean.getProperties());
        for (Entry<String, Map<String, String>> entry : subColumnBindings.entrySet())
        {
            String code = entry.getKey();
            EvaluationContext evalContext = contexts.get(code);
            try
            {
                EntityProperty entityProperty =
                        evaluateManagedProperty(code, entry.getValue(), evalContext);
                newProperties.add(entityProperty);
            } catch (EvaluatorException ex)
            {
                Throwable cause = ex.getCause();
                if (cause instanceof ValidationException)
                {
                    throw new UserFailureException("Error in row " + rowNumber + ": "
                            + cause.getMessage());
                }
                errors.accumulateError(rowNumber, ex, code, evalContext.scriptPE);
            }
        }
        return newProperties;
    }

    private EntityProperty evaluateManagedProperty(String code, Map<String, String> bindings,
            EvaluationContext evalContext)
    {
        EntityProperty entityProperty = createNewEntityProperty(code);
        if (evalContext == null)
        {
            ManagedPropertyEvaluator.assertBatchColumnNames(code, Collections.<String> emptyList(),
                    bindings);
            entityProperty.setValue(bindings.get(""));
        } else
        {
            ManagedPropertyEvaluator evaluator = evalContext.evaluator;
            ManagedProperty managedProperty = new ManagedProperty();
            managedProperty.setPropertyTypeCode(code);
            evaluator.updateFromBatchInput(managedProperty, bindings);
            entityProperty.setValue(managedProperty.getValue());
        }
        return entityProperty;
    }

    private EntityProperty createNewEntityProperty(String code)
    {
        EntityProperty entityProperty = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        propertyType.setDataType(new DataType(DataTypeCode.VARCHAR));
        entityProperty.setPropertyType(propertyType);
        return entityProperty;
    }

    private Map<String, Map<String, String>> createColumnBindingsMap(IEntityProperty[] properties)
    {
        Map<String, Map<String, String>> subColumnBindings =
                new HashMap<String, Map<String, String>>();
        for (IEntityProperty property : properties)
        {
            String code = property.getPropertyType().getCode().toUpperCase();
            int indexOfColon = code.indexOf(':');
            String propertyCode = code;
            String subColumn = "";
            if (indexOfColon >= 0)
            {
                propertyCode = code.substring(0, indexOfColon);
                subColumn = code.substring(indexOfColon + 1);
            }
            Map<String, String> bindings = subColumnBindings.get(propertyCode);
            if (bindings == null)
            {
                bindings = new HashMap<String, String>();
                subColumnBindings.put(propertyCode, bindings);
            }
            bindings.put(subColumn, property.getValue());
        }
        return subColumnBindings;
    }

    private Map<String, EvaluationContext> createEvaluationContexts(
            Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes)
    {
        Map<String, EvaluationContext> result = new HashMap<String, EvaluationContext>();
        for (EntityTypePropertyTypePE entityTypePropertyType : entityTypePropertyTypes)
        {
            if (entityTypePropertyType.isManaged())
            {
                String propertyTypeCode = entityTypePropertyType.getPropertyType().getCode();
                String script = entityTypePropertyType.getScript().getScript();
                EvaluationContext context = new EvaluationContext();
                context.evaluator =
                        ManagedPropertyEvaluatorFactory.createManagedPropertyEvaluator(script);
                context.scriptPE = entityTypePropertyType.getScript();
                result.put(propertyTypeCode, context);
            }
        }
        return result;
    }
}
