package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.ImportUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertyAssignmentImportHelper extends BasicImportHelper
{

    private enum Attribute implements IAttribute {
        Version("Version", true),
        Code("Code", true),
        Mandatory("Mandatory", true),
        ShowInEditViews("Show in edit views", true),
        Section("Section", true),
        PropertyLabel("Property label", true),
        DataType("Data type", true),
        VocabularyCode("Vocabulary code", true),
        Description("Description", true),
        Metadata("Metadata", false),
        DynamicScript("Dynamic script", false),
        OntologyId("Ontology Id", false),
        OntologyVersion("Ontology Version", false),
        OntologyAnnotationId("Ontology Annotation Id", false);

        private final String headerName;

        private final boolean mandatory;

        Attribute(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }

    private final DelayedExecutionDecorator delayedExecutor;

    private ImportTypes importTypes;

    private Set<String> existingCodes;

    private EntityTypePermId permId;

    private AttributeValidator<Attribute> attributeValidator;

    public PropertyAssignmentImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
    {
        super(mode, options);
        this.delayedExecutor = delayedExecutor;
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.PROPERTY_TYPE;
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        return false;
    }

    @Override protected void createObject(Map<String, Integer> headers, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(headers, values, Attribute.Code);
        String mandatory = getValueByColumnName(headers, values, Attribute.Mandatory);
        String showInEditViews = getValueByColumnName(headers, values, Attribute.ShowInEditViews);
        String section = getValueByColumnName(headers, values, Attribute.Section);
        String script = getValueByColumnName(headers, values, Attribute.DynamicScript);

        PropertyAssignmentCreation creation = new PropertyAssignmentCreation();
        creation.setPropertyTypeId(new PropertyTypePermId(code));
        creation.setMandatory(Boolean.parseBoolean(mandatory));
        creation.setShowInEditView(Boolean.parseBoolean(showInEditViews));
        creation.setSection(section);
        if (script != null && !script.isEmpty())
        {
            creation.setPluginId(new PluginPermId(ImportUtils.getScriptName(code, script)));
        }

        if (!this.existingCodes.contains(creation.getPropertyTypeId().toString()))
        {
            // update property assignments
            ListUpdateValue newAssignments = new ListUpdateValue();
            newAssignments.add(creation);

            switch (importTypes)
            {
                case EXPERIMENT_TYPE:
                    ExperimentTypeUpdate experimentTypeUpdate = new ExperimentTypeUpdate();
                    experimentTypeUpdate.setTypeId(this.permId);
                    experimentTypeUpdate.setPropertyAssignmentActions(newAssignments.getActions());
                    delayedExecutor.updateExperimentType(experimentTypeUpdate, page, line);
                    break;
                case SAMPLE_TYPE:
                    SampleTypeUpdate sampleTypeUpdate = new SampleTypeUpdate();
                    sampleTypeUpdate.setTypeId(this.permId);
                    sampleTypeUpdate.setPropertyAssignmentActions(newAssignments.getActions());
                    delayedExecutor.updateSampleType(sampleTypeUpdate, page, line);
                    break;
                case DATASET_TYPE:
                    DataSetTypeUpdate dataSetTypeUpdate = new DataSetTypeUpdate();
                    dataSetTypeUpdate.setTypeId(this.permId);
                    dataSetTypeUpdate.setPropertyAssignmentActions(newAssignments.getActions());
                    delayedExecutor.updateDataSetType(dataSetTypeUpdate, page, line);
                    break;
            }
        }
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        // do only create
    }

    private Set<String> generateExistingCodes(IEntityTypeId permId)
    {
        switch (importTypes)
        {
            case EXPERIMENT_TYPE:
                ExperimentTypeFetchOptions experimentFetchOptions = new ExperimentTypeFetchOptions();
                experimentFetchOptions.withPropertyAssignments().withPropertyType();
                ExperimentType experimentType = delayedExecutor.getExperimentType(permId, experimentFetchOptions);
                return experimentType.getPropertyAssignments().stream().map(PropertyAssignment::getPropertyType).map(PropertyType::getCode)
                        .collect(Collectors.toSet());
            case SAMPLE_TYPE:
                SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
                sampleTypeFetchOptions.withPropertyAssignments().withPropertyType();
                SampleType sampleType = delayedExecutor.getSampleType(permId, sampleTypeFetchOptions);
                return sampleType.getPropertyAssignments().stream().map(PropertyAssignment::getPropertyType).map(PropertyType::getCode)
                        .collect(Collectors.toSet());
            case DATASET_TYPE:
                DataSetTypeFetchOptions dataSetTypeFetchOptions = new DataSetTypeFetchOptions();
                dataSetTypeFetchOptions.withPropertyAssignments().withPropertyType();
                DataSetType dataSetType = delayedExecutor.getDataSetType(permId, dataSetTypeFetchOptions);
                return dataSetType.getPropertyAssignments().stream().map(PropertyAssignment::getPropertyType).map(PropertyType::getCode)
                        .collect(Collectors.toSet());
            default:
                return new HashSet<>();
        }
    }

    @Override protected void validateHeader(Map<String, Integer> headers)
    {
        attributeValidator.validateHeaders(Attribute.values(), headers);
    }

    public void importBlock(List<List<String>> page, int pageIndex, int start, int end, ImportTypes importTypes)
    {
        this.importTypes = importTypes;

        Map<String, Integer> header = parseHeader(page.get(start), false);
        String code = getValueByColumnName(header, page.get(start + 1), Attribute.Code);

        switch (importTypes)
        {
            case EXPERIMENT_TYPE:
                this.permId = new EntityTypePermId(code, EntityKind.EXPERIMENT);
                break;
            case SAMPLE_TYPE:
                this.permId = new EntityTypePermId(code, EntityKind.SAMPLE);
                break;
            case DATASET_TYPE:
                this.permId = new EntityTypePermId(code, EntityKind.DATA_SET);
                break;
        }

        this.existingCodes = generateExistingCodes(this.permId);
        super.importBlock(page, pageIndex, start + 2, end);
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        throw new IllegalStateException("This method should have never been called.");
    }
}
