/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.PlateGeometryContainer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.internal.authorization.ScreeningExperimentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Contains implementations of the screening public API calls.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContentQueryResult.class,
            PlateGeometryContainer.class })
public class ScreeningApiImpl
{
    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    public ScreeningApiImpl(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
    }

    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates)
    {
        FeatureVectorDatasetLoader datasetRetriever = createDatasetRetriever(plates);
        List<FeatureVectorDatasetReference> result =
                datasetRetriever.getFeatureVectorDatasetReferences();

        return result;
    }

    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates)
    {
        return createDatasetRetriever(plates).getImageDatasetReferences();
    }

    public List<ImageDatasetReference> listRawImageDatasets(List<? extends PlateIdentifier> plates)
    {
        return createDatasetRetriever(plates).getRawImageDatasetReferences();
    }

    public List<ImageDatasetReference> listSegmentationImageDatasets(
            List<? extends PlateIdentifier> plates)
    {
        return createDatasetRetriever(plates).getSegmentationImageDatasetReferences();
    }

    private FeatureVectorDatasetLoader createDatasetRetriever(List<? extends PlateIdentifier> plates)
    {
        return new FeatureVectorDatasetLoader(session, businessObjectFactory,
                session.tryGetHomeGroupCode(), new HashSet<PlateIdentifier>(plates));
    }

    public List<Plate> listPlates()
    {
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);

        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSampleType(loadPlateType());
        criteria.setIncludeSpace(true);
        criteria.setSpaceCode(null);
        criteria.setExcludeWithoutExperiment(true);

        final List<Sample> samples = sampleLister.list(new ListOrSearchSampleCriteria(criteria));
        return asPlates(samples);
    }

    public List<Plate> listPlates(ExperimentIdentifier experiment)
    {
        final TechId experimentId = getExperimentTechId(experiment);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        final ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(experimentId);

        final List<Sample> samples = sampleLister.list(new ListOrSearchSampleCriteria(criteria));
        return asPlates(samples);
    }

    private static List<Plate> asPlates(List<Sample> samples)
    {
        final List<Plate> plates = new ArrayList<Plate>();
        for (Sample sample : samples)
        {
            if (ScreeningConstants.DEFAULT_PLATE_SAMPLE_TYPE_CODE.equals(sample.getEntityType()
                    .getCode()))
            {
                plates.add(asPlate(sample));
            }
        }
        Collections.sort(plates, new Comparator<Plate>()
            {
                public int compare(Plate o1, Plate o2)
                {
                    return o1.getAugmentedCode().compareTo(o2.getAugmentedCode());
                }
            });
        return plates;
    }

    private static Plate asPlate(Sample sample)
    {
        final Experiment experiment = sample.getExperiment();
        final Project project = experiment.getProject();
        final Space sampleSpace = sample.getSpace();
        final String sampleSpaceCode = (sampleSpace != null) ? sampleSpace.getCode() : null;
        final Space experimentSpace = project.getSpace();
        final ExperimentIdentifier experimentId =
                new ExperimentIdentifier(experiment.getCode(), project.getCode(),
                        experimentSpace.getCode(), experiment.getPermId());
        return new Plate(sample.getCode(), sampleSpaceCode, sample.getPermId(), experimentId);
    }

    private SampleType loadPlateType()
    {
        ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        SampleTypePE plateTypePE =
                sampleTypeDAO
                        .tryFindSampleTypeByCode(ScreeningConstants.DEFAULT_PLATE_SAMPLE_TYPE_CODE);
        if (plateTypePE == null)
        {
            throw new Error(
                    "The database has not been initialized properly for screening, sample type '"
                            + ScreeningConstants.DEFAULT_PLATE_SAMPLE_TYPE_CODE + "' not found.");
        }
        return SampleTypeTranslator.translate(plateTypePE, null);
    }

    public List<ExperimentIdentifier> listExperiments()
    {
        final List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        final List<ExperimentIdentifier> experimentIds = asExperimentIdentifiers(experiments);
        sortByAugmentedCode(experimentIds);
        return experimentIds;
    }

    private void sortByAugmentedCode(final List<ExperimentIdentifier> experimentIds)
    {
        Collections.sort(experimentIds, new Comparator<ExperimentIdentifier>()
            {
                public int compare(ExperimentIdentifier o1, ExperimentIdentifier o2)
                {
                    return o1.getAugmentedCode().compareTo(o2.getAugmentedCode());
                }
            });
    }

    public List<ExperimentIdentifier> listExperiments(String userId)
    {
        final PersonPE user = daoFactory.getPersonDAO().tryFindPersonByUserId(userId);
        if (user == null)
        {
            return Collections.emptyList();
        }
        final List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        final List<ExperimentIdentifier> experimentIds = asExperimentIdentifiers(experiments);
        filterForUser(user, experimentIds);
        sortByAugmentedCode(experimentIds);
        return experimentIds;
    }

    private void filterForUser(final PersonPE user, final List<ExperimentIdentifier> experimentIds)
    {
        final ScreeningExperimentValidator validator = new ScreeningExperimentValidator();
        final Iterator<ExperimentIdentifier> it = experimentIds.iterator();
        while (it.hasNext())
        {
            final ExperimentIdentifier id = it.next();
            if (validator.doValidation(user, id) == false)
            {
                it.remove();
            }
        }
    }

    private static List<ExperimentIdentifier> asExperimentIdentifiers(List<ExperimentPE> experiments)
    {
        final List<ExperimentIdentifier> experimentIds = new ArrayList<ExperimentIdentifier>();
        for (ExperimentPE experiment : experiments)
        {
            experimentIds.add(asExperimentIdentifier(experiment));
        }
        return experimentIds;
    }

    private static ExperimentIdentifier asExperimentIdentifier(ExperimentPE experiment)
    {
        final ExperimentIdentifier experimentId =
                new ExperimentIdentifier(experiment.getCode(), experiment.getProject().getCode(),
                        experiment.getProject().getSpace().getCode(), experiment.getPermId());
        return experimentId;
    }

    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes)
    {
        IExternalDataBO externalDataBO = businessObjectFactory.createExternalDataBO(session);
        List<IDatasetIdentifier> identifiers = new ArrayList<IDatasetIdentifier>();
        for (String datasetCode : datasetCodes)
        {
            identifiers.add(getDatasetIdentifier(externalDataBO, datasetCode));
        }
        return identifiers;
    }

    private IDatasetIdentifier getDatasetIdentifier(IExternalDataBO externalDataBO,
            String datasetCode)
    {
        externalDataBO.loadByCode(datasetCode);
        ExternalDataPE externalData = externalDataBO.getExternalData();
        if (externalData == null)
        {
            throw UserFailureException.fromTemplate("Dataset '%s' does not exist", datasetCode);
        }
        return new DatasetIdentifier(datasetCode, externalData.getDataStore().getDownloadUrl());
    }

    static class DatasetReferenceHolder
    {
        final List<ImageDatasetReference> imageDatasets = new ArrayList<ImageDatasetReference>();

        final List<FeatureVectorDatasetReference> featureVectorDatasets =
                new ArrayList<FeatureVectorDatasetReference>();
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(
            ExperimentIdentifier experimentIdentifier, MaterialIdentifier materialIdentifier,
            boolean findDatasets)
    {
        final MaterialPE materialOrNull =
                daoFactory.getMaterialDAO().tryFindMaterial(
                        new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier(
                                materialIdentifier.getMaterialCode(), materialIdentifier
                                        .getMaterialTypeIdentifier().getMaterialTypeCode()));
        if (materialOrNull == null)
        {
            throw UserFailureException.fromTemplate("Material '%s' does not exist",
                    materialIdentifier.getAugmentedCode());
        }
        final List<WellContent> wellContents;
        final ExperimentIdentifier fullExperimentIdentifier =
                getExperimentIdentifierFromDB(experimentIdentifier);
        wellContents =
                WellContentLoader.loadOnlyMetadata(session, businessObjectFactory, daoFactory,
                        new TechId(materialOrNull.getId()), fullExperimentIdentifier.getPermId());
        if (findDatasets)
        {
            final Set<Plate> plates = extractPlates(wellContents, fullExperimentIdentifier);
            final FeatureVectorDatasetLoader datasetRetriever =
                    new FeatureVectorDatasetLoader(session, businessObjectFactory,
                            session.tryGetHomeGroupCode(), plates);
            final List<ImageDatasetReference> imageDatasets =
                    datasetRetriever.getImageDatasetReferences();
            final List<FeatureVectorDatasetReference> featureVectorDatasets =
                    datasetRetriever.getFeatureVectorDatasetReferences();

            return asPlateWellReferences(fullExperimentIdentifier, wellContents,
                    createPlateToDatasetsMap(imageDatasets, featureVectorDatasets));
        } else
        {
            return asPlateWellReferences(fullExperimentIdentifier, wellContents,
                    Collections.<String, DatasetReferenceHolder> emptyMap());
        }
    }

    private static Set<Plate> extractPlates(final List<WellContent> wellContents,
            final ExperimentIdentifier fullExperimentIdentifier)
    {
        final Set<Plate> plates = new HashSet<Plate>(wellContents.size());
        for (WellContent wellContent : wellContents)
        {
            plates.add(asPlate(fullExperimentIdentifier, wellContent));
        }
        return plates;
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        final MaterialPE materialOrNull =
                daoFactory.getMaterialDAO().tryFindMaterial(
                        new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier(
                                materialIdentifier.getMaterialCode(), materialIdentifier
                                        .getMaterialTypeIdentifier().getMaterialTypeCode()));
        if (materialOrNull == null)
        {
            throw UserFailureException.fromTemplate("Material '%s' does not exist",
                    materialIdentifier.getAugmentedCode());
        }
        final List<WellContent> wellContent =
                WellContentLoader.loadOnlyMetadata(session, businessObjectFactory, daoFactory,
                        new TechId(materialOrNull.getId()));

        if (findDatasets)
        {
            final Set<Plate> plates = new HashSet<Plate>(wellContent.size());
            for (WellContent w : wellContent)
            {
                plates.add(asPlate(w));
            }
            final FeatureVectorDatasetLoader datasetRetriever =
                    new FeatureVectorDatasetLoader(session, businessObjectFactory,
                            session.tryGetHomeGroupCode(), plates);
            final List<ImageDatasetReference> imageDatasets =
                    datasetRetriever.getImageDatasetReferences();
            final List<FeatureVectorDatasetReference> featureVectorDatasets =
                    datasetRetriever.getFeatureVectorDatasetReferences();

            return asPlateWellReferences(wellContent,
                    createPlateToDatasetsMap(imageDatasets, featureVectorDatasets));
        } else
        {
            return asPlateWellReferences(wellContent,
                    Collections.<String, DatasetReferenceHolder> emptyMap());
        }
    }

    public List<WellIdentifier> listPlateWells(PlateIdentifier plateIdentifier)
    {
        TechId plateSampleId = getSampleTechId(plateIdentifier);
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        ListSampleCriteria criteria = ListSampleCriteria.createForContainer(plateSampleId);
        List<Sample> samples = sampleLister.list(new ListOrSearchSampleCriteria(criteria));
        return asWellIdentifiers(samples, plateIdentifier);
    }

    private TechId getSampleTechId(PlateIdentifier plateIdentifier)
    {
        if (plateIdentifier.getPermId() != null)
        {
            return TechId.create(loadSampleByPermId(plateIdentifier.getPermId()));
        } else
        {
            SampleIdentifier sampleIdentifier = createSampleIdentifier(plateIdentifier);
            ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
            sampleBO.loadBySampleIdentifier(sampleIdentifier);
            return TechId.create(sampleBO.getSample());
        }
    }

    public ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample getWellSample(
            WellIdentifier wellIdentifier)
    {
        Sample sample = loadSampleByPermId(wellIdentifier.getPermId());
        return Translator.translate(sample);
    }

    private Sample loadSampleByPermId(String permId)
    {
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySamplePermId(permId);
        SamplePE samplePE = sampleBO.getSample();
        return translate(samplePE);
    }

    private Sample translate(SamplePE sample)
    {
        return SampleTranslator.translate(sample, session.getBaseIndexURL());
    }

    private static SampleIdentifier createSampleIdentifier(PlateIdentifier plate)
    {
        if (plate.getPlateCode() == null)
        {
            return null;
        }

        SampleOwnerIdentifier owner;
        String spaceCodeOrNull = plate.tryGetSpaceCode();
        if (spaceCodeOrNull != null)
        {
            SpaceIdentifier space =
                    new SpaceIdentifier(DatabaseInstanceIdentifier.HOME, spaceCodeOrNull);
            owner = new SampleOwnerIdentifier(space);
        } else
        {
            owner = new SampleOwnerIdentifier(DatabaseInstanceIdentifier.createHome());
        }
        return SampleIdentifier.createOwnedBy(owner, plate.getPlateCode());
    }

    private List<WellIdentifier> asWellIdentifiers(List<Sample> samples,
            PlateIdentifier plateIdentifier)
    {
        final List<WellIdentifier> wells = new ArrayList<WellIdentifier>();
        for (Sample sample : samples)
        {
            wells.add(asWellIdentifier(sample, plateIdentifier));
        }
        Collections.sort(wells, new Comparator<WellIdentifier>()
            {
                public int compare(WellIdentifier o1, WellIdentifier o2)
                {
                    WellPosition p1 = o1.getWellPosition();
                    WellPosition p2 = o2.getWellPosition();
                    int result =
                            Integer.valueOf(p1.getWellRow()).compareTo(
                                    Integer.valueOf(p2.getWellRow()));
                    if (result == 0)
                    {
                        result =
                                Integer.valueOf(p1.getWellColumn()).compareTo(
                                        Integer.valueOf(p2.getWellColumn()));
                    }
                    return result;
                }
            });
        return wells;
    }

    private static WellIdentifier asWellIdentifier(Sample sample, PlateIdentifier plateIdentifier)
    {
        // need to translate location to position to use it in API
        WellLocation location =
                ScreeningUtils.tryCreateLocationFromMatrixCoordinate(sample.getSubCode());
        if (location == null)
        {
            new IllegalArgumentException(String.format(
                    "Failed to retrieve location of sample '%s'.", sample.getCode()));
            return null;
        }
        WellPosition position = new WellPosition(location.getRow(), location.getColumn());
        return new WellIdentifier(plateIdentifier, position, sample.getPermId());
    }

    public List<PlateWellMaterialMapping> listPlateMaterialMapping(
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        final List<PlateWellMaterialMapping> result =
                new ArrayList<PlateWellMaterialMapping>(plates.size());
        final IScreeningQuery query = createScreeningQuery();
        for (PlateIdentifier plate : plates)
        {
            result.add(toPlateWellMaterialMapping(plate, materialTypeIdentifierOrNull,
                    getPlateGeometry(query, plate),
                    getPlateMapping(query, plate, materialTypeIdentifierOrNull)));
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    // NOTE: this method will return duplicated wells if there are two materials connected to one
    // well
    private PlateWellMaterialMapping toPlateWellMaterialMapping(
            PlateIdentifier plateIdentifier,
            MaterialTypeIdentifier materialTypeIdentifierOrNull,
            PlateGeometryContainer plateGeometryContainer,
            DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContentQueryResult> wellContentList)
    {
        final Geometry plateGeometry =
                Geometry.createFromPlateGeometryString(plateGeometryContainer.plate_geometry);
        final PlateIdentifier finalPlateIdentifier =
                new PlateIdentifier(plateGeometryContainer.plate_code,
                        plateGeometryContainer.space_code, plateGeometryContainer.perm_id);
        final PlateWellMaterialMapping result =
                new PlateWellMaterialMapping(finalPlateIdentifier, plateGeometry, 1);
        if (materialTypeIdentifierOrNull != null)
        {
            for (ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContentQueryResult wellContent : wellContentList)
            {
                final WellLocation location =
                        ScreeningUtils.tryCreateLocationFromMatrixCoordinate(wellContent.well_code);
                final String materialContentCode = wellContent.material_content_code;
                result.getMaterialsForWell(location.getRow(), location.getColumn()).add(
                        new MaterialIdentifier(materialTypeIdentifierOrNull, materialContentCode));
            }
        } else
        {
            final Map<String, MaterialTypeIdentifier> map =
                    new HashMap<String, MaterialTypeIdentifier>();
            for (ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContentQueryResult wellContent : wellContentList)
            {
                MaterialTypeIdentifier typeId = map.get(wellContent.material_content_type_code);
                if (typeId == null)
                {
                    typeId = new MaterialTypeIdentifier(wellContent.material_content_type_code);
                    map.put(typeId.getMaterialTypeCode(), typeId);
                }
                final WellLocation location =
                        ScreeningUtils.tryCreateLocationFromMatrixCoordinate(wellContent.well_code);
                final String materialContentCode = wellContent.material_content_code;
                result.getMaterialsForWell(location.getRow(), location.getColumn()).add(
                        new MaterialIdentifier(typeId, materialContentCode));
            }
        }
        return result;
    }

    private PlateGeometryContainer getPlateGeometry(IScreeningQuery query, PlateIdentifier plate)
    {
        final PlateGeometryContainer plateGeometryContainer;
        if (plate.getPermId() == null)
        {
            plateGeometryContainer =
                    query.tryGetPlateGeometry(plate.tryGetSpaceCode(), plate.getPlateCode());
            if (plateGeometryContainer == null)
            {
                throw new IllegalArgumentException("No plate with code '" + plate.tryGetSpaceCode()
                        + "/" + plate.getPlateCode() + "' found.");
            }
            plateGeometryContainer.space_code = plate.tryGetSpaceCode();
            plateGeometryContainer.plate_code = plate.getPlateCode();
        } else
        {
            plateGeometryContainer = query.tryGetPlateGeometry(plate.getPermId());
            if (plateGeometryContainer == null)
            {
                throw new IllegalArgumentException("No plate with perm id '" + plate.getPermId()
                        + "' found.");
            }
            plateGeometryContainer.perm_id = plate.getPermId();
        }
        return plateGeometryContainer;
    }

    private DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContentQueryResult> getPlateMapping(
            IScreeningQuery query, PlateIdentifier plate,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        if (materialTypeIdentifierOrNull != null)
        {
            if (plate.getPermId() == null)
            {
                return query.getPlateMappingForMaterialType(plate.tryGetSpaceCode(),
                        plate.getPlateCode(), materialTypeIdentifierOrNull.getMaterialTypeCode());
            } else
            {
                return query.getPlateMappingForMaterialType(plate.getPermId(),
                        materialTypeIdentifierOrNull.getMaterialTypeCode());
            }
        } else
        {
            if (plate.getPermId() == null)
            {
                return query.getPlateMapping(plate.tryGetSpaceCode(), plate.getPlateCode());
            } else
            {
                return query.getPlateMapping(plate.getPermId());
            }
        }

    }

    private IScreeningQuery createScreeningQuery()
    {
        final Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        return QueryTool.getQuery(connection, IScreeningQuery.class);
    }

    private ExperimentIdentifier getExperimentIdentifierFromDB(
            ExperimentIdentifier experimentIdentifierFromUser)
    {
        if (experimentIdentifierFromUser.getPermId() != null)
        {
            final ExperimentPE experimentPE =
                    daoFactory.getExperimentDAO().tryGetByPermID(
                            experimentIdentifierFromUser.getPermId());
            if (experimentPE == null)
            {
                throw UserFailureException.fromTemplate("Experiment '%s' not found",
                        experimentIdentifierFromUser.getPermId());
            }
            return asExperimentIdentifier(experimentPE);
        } else
        {
            final String spaceCode =
                    SpaceCodeHelper.getSpaceCode(session.tryGetHomeGroupCode(),
                            experimentIdentifierFromUser.getSpaceCode());
            if (StringUtils.isEmpty(spaceCode))
            {
                throw new UserFailureException(
                        "Space code is empty but there are no experiments outside a space, "
                                + "use null to denote your home space.");
            }
            final ProjectPE projectPE =
                    daoFactory.getProjectDAO().tryFindProject(null, spaceCode,
                            experimentIdentifierFromUser.getProjectCode());
            if (projectPE == null)
            {
                throw UserFailureException.fromTemplate("Project '%s' in space '%s' not found",
                        experimentIdentifierFromUser.getProjectCode(), spaceCode);
            }
            final ExperimentPE experimentPE =
                    daoFactory.getExperimentDAO().tryFindByCodeAndProject(projectPE,
                            experimentIdentifierFromUser.getExperimentCode());
            if (experimentPE == null)
            {
                throw UserFailureException.fromTemplate(
                        "Experiment '%s' in project '%s', space '%s' not found",
                        experimentIdentifierFromUser.getExperimentCode(),
                        experimentIdentifierFromUser.getProjectCode(), spaceCode);
            }
            return asExperimentIdentifier(experimentPE);
        }

    }

    private TechId getExperimentTechId(ExperimentIdentifier experimentIdentifierFromUser)
    {
        if (experimentIdentifierFromUser.getPermId() != null)
        {
            final ExperimentPE experimentPE =
                    daoFactory.getExperimentDAO().tryGetByPermID(
                            experimentIdentifierFromUser.getPermId());
            if (experimentPE == null)
            {
                throw UserFailureException.fromTemplate("Experiment '%s' not found",
                        experimentIdentifierFromUser.getPermId());
            }
            return new TechId(experimentPE.getId());
        } else
        {
            final String spaceCode =
                    SpaceCodeHelper.getSpaceCode(session.tryGetHomeGroupCode(),
                            experimentIdentifierFromUser.getSpaceCode());
            if (StringUtils.isEmpty(spaceCode))
            {
                throw new UserFailureException(
                        "Space code is empty but there are no experiments outside a space, "
                                + "use null to denote your home space.");
            }
            final ProjectPE projectPE =
                    daoFactory.getProjectDAO().tryFindProject(null, spaceCode,
                            experimentIdentifierFromUser.getProjectCode());
            if (projectPE == null)
            {
                throw UserFailureException.fromTemplate("Project '%s' in space '%s' not found",
                        experimentIdentifierFromUser.getProjectCode(), spaceCode);
            }
            final ExperimentPE experimentPE =
                    daoFactory.getExperimentDAO().tryFindByCodeAndProject(projectPE,
                            experimentIdentifierFromUser.getExperimentCode());
            if (experimentPE == null)
            {
                throw UserFailureException.fromTemplate(
                        "Experiment '%s' in project '%s', space '%s' not found",
                        experimentIdentifierFromUser.getExperimentCode(),
                        experimentIdentifierFromUser.getProjectCode(), spaceCode);
            }
            return new TechId(experimentPE.getId());
        }
    }

    private static Map<String, DatasetReferenceHolder> createPlateToDatasetsMap(
            List<ImageDatasetReference> imageDatasets,
            List<FeatureVectorDatasetReference> featureVectorDatasets)
    {
        final Map<String, DatasetReferenceHolder> map =
                new HashMap<String, DatasetReferenceHolder>();
        for (ImageDatasetReference dataset : imageDatasets)
        {
            DatasetReferenceHolder reference = map.get(dataset.getPlate().getPermId());
            if (reference == null)
            {
                reference = new DatasetReferenceHolder();
                map.put(dataset.getPlate().getPermId(), reference);
            }
            reference.imageDatasets.add(dataset);
        }
        for (FeatureVectorDatasetReference dataset : featureVectorDatasets)
        {
            DatasetReferenceHolder reference = map.get(dataset.getPlate().getPermId());
            if (reference == null)
            {
                reference = new DatasetReferenceHolder();
                map.put(dataset.getPlate().getPermId(), reference);
            }
            reference.featureVectorDatasets.add(dataset);
        }
        return map;
    }

    private static Plate asPlate(ExperimentIdentifier experimentIdentifier, WellContent wellContent)
    {
        return new Plate(wellContent.getPlate().getCode(), experimentIdentifier.getSpaceCode(),
                wellContent.getPlate().getPermId(), experimentIdentifier);
    }

    private static Plate asPlate(WellContent wellContent)
    {
        return new Plate(wellContent.getPlate().getCode(), wellContent.getExperiment()
                .getSpaceCode(), wellContent.getPlate().getPermId(), asExperiment(wellContent));
    }

    private static ExperimentIdentifier asExperiment(WellContent wellContent)
    {
        return new ExperimentIdentifier(wellContent.getExperiment().getCode(), wellContent
                .getExperiment().getProjectCode(), wellContent.getExperiment().getSpaceCode(),
                wellContent.getExperiment().getPermId());
    }

    private static PlateWellReferenceWithDatasets asPlateWellReference(
            ExperimentIdentifier experimentIdentifier, WellContent wellContent,
            Map<String, DatasetReferenceHolder> plateToDatasetsMap)
    {
        final Plate plate = asPlate(experimentIdentifier, wellContent);
        WellLocation location = wellContent.tryGetLocation();
        final WellPosition wellPosition = new WellPosition(location.getRow(), location.getColumn());
        final DatasetReferenceHolder datasetReferences = plateToDatasetsMap.get(plate.getPermId());
        if (datasetReferences == null)
        {
            return new PlateWellReferenceWithDatasets(plate, wellPosition);
        } else
        {
            return new PlateWellReferenceWithDatasets(plate, wellPosition,
                    datasetReferences.imageDatasets, datasetReferences.featureVectorDatasets);
        }
    }

    private static List<PlateWellReferenceWithDatasets> asPlateWellReferences(
            ExperimentIdentifier experimentIdentifer, List<WellContent> wellContents,
            Map<String, DatasetReferenceHolder> plateToDatasetsMap)
    {
        final List<PlateWellReferenceWithDatasets> plateWellReferences =
                new ArrayList<PlateWellReferenceWithDatasets>();
        for (WellContent wellContent : wellContents)
        {
            plateWellReferences.add(asPlateWellReference(experimentIdentifer, wellContent,
                    plateToDatasetsMap));
        }
        Collections.sort(plateWellReferences, new Comparator<PlateWellReferenceWithDatasets>()
            {
                public int compare(PlateWellReferenceWithDatasets o1,
                        PlateWellReferenceWithDatasets o2)
                {
                    return (o1.getExperimentPlateIdentifier().getAugmentedCode() + ":" + o1
                            .getWellPosition()).compareTo(o2.getExperimentPlateIdentifier()
                            .getAugmentedCode() + ":" + o2.getWellPosition());
                }
            });
        return plateWellReferences;
    }

    private static List<PlateWellReferenceWithDatasets> asPlateWellReferences(
            List<WellContent> wellContents, Map<String, DatasetReferenceHolder> plateToDatasetsMap)
    {
        final List<PlateWellReferenceWithDatasets> plateWellReferences =
                new ArrayList<PlateWellReferenceWithDatasets>();
        for (WellContent wellContent : wellContents)
        {
            plateWellReferences.add(asPlateWellReference(asExperiment(wellContent), wellContent,
                    plateToDatasetsMap));
        }
        Collections.sort(plateWellReferences, new Comparator<PlateWellReferenceWithDatasets>()
            {
                public int compare(PlateWellReferenceWithDatasets o1,
                        PlateWellReferenceWithDatasets o2)
                {
                    return (o1.getExperimentPlateIdentifier().getAugmentedCode() + ":" + o1
                            .getWellPosition()).compareTo(o2.getExperimentPlateIdentifier()
                            .getAugmentedCode() + ":" + o2.getWellPosition());
                }
            });
        return plateWellReferences;
    }

}
