/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataTranslator
{
    private ExternalDataTranslator()
    {
    }
    
    public static DatasetDescription translateToDescription(ExternalData data)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDatasetCode(data.getCode());

        DataSet dataSet = data.tryGetAsDataSet();
        if (dataSet != null)
        {
            // TODO KE: 2011-05-06 make sure this other classes handle NULL correctly
            description.setDataSetLocation(dataSet.getLocation());
            description.setSpeedHint(dataSet.getSpeedHint());
        }
        description.setDataSetSize(data.getSize());
        DataSetType dataSetType = data.getDataSetType();
        if (dataSetType != null)
        {
            description.setDatasetTypeCode(dataSetType.getCode());
        }
        Experiment experiment = data.getExperiment();
        if (experiment != null)
        {
            description.setExperimentCode(experiment.getCode());
            description.setExperimentIdentifier(experiment.getIdentifier());
            Project project = experiment.getProject();
            if (project != null)
            {
                description.setProjectCode(project.getCode());
                Space space = project.getSpace();
                if (space != null)
                {
                    description.setSpaceCode(space.getCode());
                    DatabaseInstance instance = space.getInstance();
                    if (instance != null)
                    {
                        description.setDatabaseInstanceCode(instance.getCode());
                    }
                }
            }
            ExperimentType experimentType = experiment.getExperimentType();
            if (experimentType != null)
            {
                description.setExperimentTypeCode(experimentType.getCode());
            }
        }
        Sample sample = data.getSample();
        if (sample != null)
        {
            description.setSampleCode(sample.getCode());
            description.setSampleIdentifier(sample.getIdentifier());
            SampleType sampleType = sample.getSampleType();
            if (sampleType != null)
            {
                description.setSampleTypeCode(sampleType.getCode());
            }
        }
        return description;
    }

    public static List<ExternalData> translate(List<ExternalDataPE> list,
            String defaultDataStoreBaseURL, String baseIndexURL)
    {
        ArrayList<ExternalData> result = new ArrayList<ExternalData>(list.size());
        for (ExternalDataPE externalDataPE : list)
        {
            ExternalData data =
                    translate(externalDataPE, baseIndexURL, true,
                            ExperimentTranslator.LoadableFields.PROPERTIES);
            result.add(data);
        }
        return result;
    }

    public static ExternalData translate(ExternalDataPE externalDataPE, String baseIndexURL,
            final LoadableFields... withExperimentFields)
    {
        return translate(externalDataPE, baseIndexURL, true, withExperimentFields);
    }

    public static ExternalData translate(ExternalDataPE externalDataPE, String baseIndexURL,
            boolean loadSampleProperties, final LoadableFields... withExperimentFields)
    {
        ExternalData externalData = null;
        if (externalDataPE.isContainerDataSet())
        {
            externalData = translateContainerDataSetProperties(externalDataPE);
        } else
        {
            externalData = translateDataSetProperties(externalDataPE);
        }

        SamplePE sampleOrNull = externalDataPE.tryGetSample();
        ExperimentPE experiment = externalDataPE.getExperiment();
        externalData.setId(HibernateUtils.getId(externalDataPE));
        externalData.setCode(externalDataPE.getCode());
        externalData.setDataProducerCode(externalDataPE.getDataProducerCode());
        externalData.setDataSetType(DataSetTypeTranslator.translate(
                externalDataPE.getDataSetType(), new HashMap<PropertyTypePE, PropertyType>()));
        externalData.setDerived(externalDataPE.isDerived());
        externalData.setInvalidation(tryToGetInvalidation(sampleOrNull, experiment));
        externalData.setSize(externalDataPE.getSize());
        final Collection<ExternalData> parents = new HashSet<ExternalData>();
        externalData.setParents(parents);
        for (DataPE parentPE : externalDataPE.getParents())
        {
            parents.add(translateBasicProperties(parentPE));
        }
        setChildren(externalDataPE, externalData);
        externalData.setProductionDate(externalDataPE.getProductionDate());
        externalData.setModificationDate(externalDataPE.getModificationDate());
        externalData.setRegistrator(PersonTranslator.translate(externalDataPE.getRegistrator()));
        externalData.setRegistrationDate(externalDataPE.getRegistrationDate());
        externalData.setSample(sampleOrNull == null ? null : fillSample(new Sample(), sampleOrNull,
                loadSampleProperties));
        externalData.setDataStore(DataStoreTranslator.translate(externalDataPE.getDataStore()));
        externalData.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL,
                EntityKind.DATA_SET, externalData.getIdentifier()));
        setProperties(externalDataPE, externalData);
        externalData.setExperiment(ExperimentTranslator.translate(experiment, baseIndexURL,
                withExperimentFields));

        return externalData;
    }

    private static ExternalData translateContainerDataSetProperties(ExternalDataPE externalDataPE)
    {
        ContainerDataSet containerDataSet = new ContainerDataSet();
        setContainedDataSets(externalDataPE, containerDataSet);
        return containerDataSet;
    }

    private static ExternalData translateDataSetProperties(ExternalDataPE externalDataPE)
    {
        DataSet dataSet = new DataSet();
        dataSet.setComplete(BooleanOrUnknown.tryToResolve(externalDataPE.getComplete()));
        dataSet.setStatus(externalDataPE.getStatus());
        dataSet.setSpeedHint(externalDataPE.getSpeedHint());
        dataSet.setFileFormatType(TypeTranslator.translate(externalDataPE.getFileFormatType()));
        dataSet.setLocation(externalDataPE.getLocation());
        dataSet.setLocatorType(TypeTranslator.translate(externalDataPE.getLocatorType()));
        return dataSet;
    }

    private static void setProperties(ExternalDataPE externalDataPE, ExternalData externalData)
    {
        if (HibernateUtils.isInitialized(externalDataPE.getProperties()))
        {
            externalData.setDataSetProperties(EntityPropertyTranslator.translate(
                    externalDataPE.getProperties(), new HashMap<PropertyTypePE, PropertyType>()));
        } else
        {
            externalData.setDataSetProperties(new ArrayList<IEntityProperty>());
        }
    }

    private static Invalidation tryToGetInvalidation(SamplePE sampleOrNull, ExperimentPE experiment)
    {
        InvalidationPE invalidationOrNull;
        if (sampleOrNull != null)
        {
            invalidationOrNull = tryToGetInvalidationPE(sampleOrNull);
        } else
        {
            invalidationOrNull = tryToGetInvalidationPE(experiment);
        }
        return translateInvalidation(invalidationOrNull);
    }

    private static InvalidationPE tryToGetInvalidationPE(SamplePE sampleOrNull)
    {
        if (sampleOrNull != null)
        {
            return sampleOrNull.getInvalidation();
        } else
        {
            return null;
        }
    }

    private static InvalidationPE tryToGetInvalidationPE(ExperimentPE experiment)
    {
        if (experiment != null)
        {
            return experiment.getInvalidation();
        } else
        {
            return null;
        }
    }

    private static Invalidation translateInvalidation(InvalidationPE invalidationPE)
    {
        if (invalidationPE == null)
        {
            return null;
        }
        Invalidation result = new Invalidation();
        result.setReason(invalidationPE.getReason());
        result.setRegistrationDate(invalidationPE.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(invalidationPE.getRegistrator()));
        return result;
    }

    private static Sample fillSample(Sample sample, SamplePE samplePE, boolean loadSampleProperties)
    {
        sample.setId(HibernateUtils.getId(samplePE));
        sample.setPermId(samplePE.getPermId());
        SampleTranslator.setCodes(sample, samplePE);
        sample.setInvalidation(translateInvalidation(samplePE.getInvalidation()));
        sample.setSampleType(TypeTranslator.translate(samplePE.getSampleType()));
        sample.setIdentifier(samplePE.getSampleIdentifier().toString());
        sample.setRegistrationDate(samplePE.getRegistrationDate());
        sample.setRegistrator(PersonTranslator.translate(samplePE.getRegistrator()));
        sample.setSpace(GroupTranslator.translate(samplePE.getSpace()));
        if (loadSampleProperties)
        {
            sample.setProperties(EntityPropertyTranslator.translate(samplePE.getProperties(),
                    new HashMap<PropertyTypePE, PropertyType>()));
        }
        return sample;
    }

    private static void setChildren(ExternalDataPE externalDataPE, ExternalData externalData)
    {
        List<ExternalData> children = new ArrayList<ExternalData>();
        if (HibernateUtils.isInitialized(externalDataPE.getChildren()))
        {
            for (DataPE childPE : externalDataPE.getChildren())
            {
                children.add(translateBasicProperties(childPE));
            }
        }
        externalData.setChildren(children);
    }

    private static void setContainedDataSets(DataPE externalDataPE,
            ContainerDataSet containerDataSet)
    {
        List<ExternalData> containedDataSets = new ArrayList<ExternalData>();
        if (HibernateUtils.isInitialized(externalDataPE.getContainedDatas()))
        {
            for (DataPE childPE : externalDataPE.getContainedDatas())
            {
                containedDataSets.add(translateBasicProperties(childPE));
            }
        }
        containerDataSet.setContainedDataSets(containedDataSets);
    }

    /**
     * Creates an <var>externalData</var> from <var>dataPE</vra> an fills it with all data needed by
     * {@link IEntityInformationHolder}.
     */
    private static ExternalData translateBasicProperties(DataPE dataPE)
    {
        ExternalData result = new ExternalData();
        result.setId(HibernateUtils.getId(dataPE));
        result.setCode(dataPE.getCode());
        result.setDataSetType(DataSetTypeTranslator.translate(dataPE.getDataSetType(),
                new HashMap<PropertyTypePE, PropertyType>()));
        return result;
    }
}
