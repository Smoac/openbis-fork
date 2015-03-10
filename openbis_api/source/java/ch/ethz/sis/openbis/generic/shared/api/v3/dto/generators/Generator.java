package ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.Complete;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.FileFormatType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.LocatorType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.ExternalDataFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.FileFormatTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.LocatorTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;

public class Generator extends AbstractGenerator
{

    private static DtoGenerator createSampleGenerator()
    {
        DtoGenerator gen = new DtoGenerator("sample", "Sample", SampleFetchOptions.class);

        gen.addSimpleField(SamplePermId.class, "permId");
        gen.addSimpleField(SampleIdentifier.class, "identifier");
        addCode(gen);
        addRegistrationDate(gen);
        addModificationDate(gen);

        gen.addFetchedField(SampleType.class, "type", "Sample type", SampleTypeFetchOptions.class);
        addSpace(gen);
        addExperiment(gen);
        addProperties(gen);
        gen.addFetchedField("List<Sample>", List.class.getName(), "parents", "Parents", SampleFetchOptions.class);
        gen.addFetchedField("List<Sample>", List.class.getName(), "children", "Children", SampleFetchOptions.class);
        gen.addFetchedField(Sample.class, "container", "Container sample", SampleFetchOptions.class);
        gen.addFetchedField("List<Sample>", List.class.getName(), "contained", "Contained samples", SampleFetchOptions.class);
        gen.addFetchedField("List<DataSet>", List.class.getName(), "dataSets", "Data sets", DataSetFetchOptions.class);
        gen.addClassForImport(DataSet.class);
        addTags(gen);
        addRegistrator(gen);
        addModifier(gen);
        addAttachments(gen);

        gen.setToStringMethod("\"Sample \" + permId");

        return gen;
    }

    private static DtoGenerator createSampleTypeGenerator()
    {
        // type.getContainerHierarchyDepth();
        // type.getGeneratedFromHierarchyDepth();
        // the two fields are ignored as they are hints for old api translations

        // potentially missing fields:

        // type.getSampleTypePropertyTypes();
        // type.getValidationScript();

        DtoGenerator gen = new DtoGenerator("sample", "SampleType", SampleTypeFetchOptions.class);

        gen.addSimpleField(EntityTypePermId.class, "permId");
        addCode(gen);
        addDescription(gen);
        gen.addBooleanField("listable");
        gen.addBooleanField("subcodeUnique");
        gen.addBooleanField("autoGeneratedCode");
        gen.addBooleanField("showParentMetadata");

        gen.addStringField("generatedCodePrefix");
        addModificationDate(gen);

        return gen;
    }

    private static DtoGenerator createAttachmentGenerator()
    {
        DtoGenerator gen = new DtoGenerator("attachment", "Attachment", AttachmentFetchOptions.class);

        gen.addStringField("fileName");
        gen.addStringField("title");
        addDescription(gen);
        gen.addStringField("permlink");
        gen.addStringField("latestVersionPermlink");
        gen.addSimpleField(Integer.class, "version");
        addRegistrationDate(gen);

        addRegistrator(gen);
        gen.addFetchedField(Attachment.class, "previousVersion", "Previous version of attachment ", AttachmentFetchOptions.class);

        gen.addFetchedField(byte[].class, "content", "Content", EmptyFetchOptions.class);

        gen.setToStringMethod("\"Attachment \" + fileName + \":\" + version");

        return gen;
    }

    private static DtoGenerator createExperimentGenerator()
    {
        DtoGenerator gen = new DtoGenerator("experiment", "Experiment", ExperimentFetchOptions.class);

        gen.addSimpleField(ExperimentPermId.class, "permId");
        gen.addSimpleField(ExperimentIdentifier.class, "identifier");
        addCode(gen);
        addRegistrationDate(gen);
        addModificationDate(gen);

        gen.addFetchedField(ExperimentType.class, "type", "Experiment type", ExperimentTypeFetchOptions.class);
        gen.addFetchedField(Project.class, "project", "Project", ProjectFetchOptions.class);

        gen.addFetchedField("List<DataSet>", List.class.getName(), "dataSets", "Data sets", DataSetFetchOptions.class);
        gen.addClassForImport(DataSet.class);

        gen.addFetchedField("List<Sample>", List.class.getName(), "samples", "Samples", SampleFetchOptions.class);
        gen.addClassForImport(Sample.class);

        addProperties(gen);
        addTags(gen);
        addRegistrator(gen);
        addModifier(gen);
        addAttachments(gen);

        gen.setToStringMethod("\"Experiment \" + permId");

        return gen;
    }

    private static DtoGenerator createExperimentTypeGenerator()
    {
        DtoGenerator gen = new DtoGenerator("experiment", "ExperimentType", ExperimentTypeFetchOptions.class);

        gen.addSimpleField(EntityTypePermId.class, "permId");
        addCode(gen);
        addDescription(gen);
        addModificationDate(gen);

        // TODO add property definitions

        // TODO add validation script
        return gen;
    }

    private static DtoGenerator createDataSetGenerator()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "DataSet", DataSetFetchOptions.class);

        gen.addSimpleField(DataSetPermId.class, "permId");

        addCode(gen);
        gen.addDateField("accessDate");

        gen.addBooleanField("derived");
        gen.addBooleanField("placeholder");

        gen.addFetchedField("List<DataSet>", List.class.getName(), "parents", "Parents", DataSetFetchOptions.class);
        gen.addFetchedField("List<DataSet>", List.class.getName(), "children", "Children", DataSetFetchOptions.class);
        gen.addFetchedField("List<DataSet>", List.class.getName(), "containers", "Container data sets", DataSetFetchOptions.class);
        gen.addFetchedField("List<DataSet>", List.class.getName(), "contained", "Contained data sets", DataSetFetchOptions.class);
        gen.addFetchedField(ExternalData.class, "externalData", "External data", ExternalDataFetchOptions.class);
        addTags(gen);

        gen.addFetchedField(DataSetType.class, "type", "Data Set type", DataSetTypeFetchOptions.class);
        // add data set type
        // add data store
        addModificationDate(gen);
        addModifier(gen);
        addRegistrationDate(gen);
        addRegistrator(gen);

        addExperiment(gen);
        addSample(gen);
        addProperties(gen);

        return gen;
    }

    private static DtoGenerator createDataSetTypeGenerator()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "DataSetType", DataSetTypeFetchOptions.class);

        gen.addSimpleField(EntityTypePermId.class, "permId");
        gen.addSimpleField(DataSetKind.class, "kind");
        addCode(gen);
        addDescription(gen);
        addModificationDate(gen);

        // TODO add property definitions

        // TODO add validation script
        return gen;
    }

    private static DtoGenerator createExternalDataGenerator()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "ExternalData", ExternalDataFetchOptions.class);

        gen.addStringField("shareId");
        gen.addStringField("location");
        gen.addSimpleField(Long.class, "size");
        gen.addFetchedField(VocabularyTerm.class, "storageFormat", "Storage format vocabulary term", VocabularyTermFetchOptions.class);
        gen.addFetchedField(FileFormatType.class, "fileFormatType", "File Format Type", FileFormatTypeFetchOptions.class);
        gen.addFetchedField(LocatorType.class, "locatorType", "Locator Type", LocatorTypeFetchOptions.class);
        gen.addSimpleField(Complete.class, "complete");
        gen.addSimpleField(ArchivingStatus.class, "status");
        gen.addBooleanField("presentInArchive");
        gen.addBooleanField("storageConfirmation");
        gen.addSimpleField(Integer.class, "speedHint");

        return gen;
    }

    private static DtoGenerator createFileFormatType()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "FileFormatType", FileFormatTypeFetchOptions.class);

        gen.addStringField("code");
        gen.addStringField("description");

        return gen;
    }

    private static DtoGenerator createLocatorType()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "LocatorType", LocatorTypeFetchOptions.class);

        gen.addStringField("code");
        gen.addStringField("description");

        return gen;
    }

    private static DtoGenerator createVocabulary()
    {
        DtoGenerator gen = new DtoGenerator("vocabulary", "Vocabulary", VocabularyFetchOptions.class);

        addCode(gen);
        addDescription(gen);
        addRegistrationDate(gen);
        addRegistrator(gen);
        addModificationDate(gen);

        return gen;
    }

    private static DtoGenerator createVocabularyTerm()
    {
        DtoGenerator gen = new DtoGenerator("vocabulary", "VocabularyTerm", VocabularyTermFetchOptions.class);

        addCode(gen);
        gen.addStringField("label");
        addDescription(gen);
        gen.addSimpleField(Long.class, "ordinal");
        gen.addBooleanField("official");
        gen.addFetchedField(Vocabulary.class, "vocabulary", "Vocabulary", VocabularyFetchOptions.class);
        addRegistrationDate(gen);
        addRegistrator(gen);
        addModificationDate(gen);

        return gen;
    }

    private static DtoGenerator createPersonGenerator()
    {
        DtoGenerator gen = new DtoGenerator("person", "Person", PersonFetchOptions.class);

        gen.addStringField("userId");
        gen.addStringField("firstName");
        gen.addStringField("lastName");
        gen.addStringField("email");
        addRegistrationDate(gen);
        gen.addBooleanField("active");

        addSpace(gen);
        addRegistrator(gen);

        return gen;
    }

    private static DtoGenerator createProjectGenerator()
    {
        DtoGenerator gen = new DtoGenerator("project", "Project", ProjectFetchOptions.class);

        gen.addSimpleField(ProjectPermId.class, "permId");
        gen.addSimpleField(ProjectIdentifier.class, "identifier");
        addCode(gen);
        addDescription(gen);
        addRegistrationDate(gen);
        addModificationDate(gen);

        addSpace(gen);
        addRegistrator(gen);
        addModifier(gen);

        return gen;
    }

    private static DtoGenerator createSpaceGenerator()
    {
        DtoGenerator gen = new DtoGenerator("space", "Space", SpaceFetchOptions.class);

        gen.addSimpleField(SpacePermId.class, "permId");
        addCode(gen);
        addDescription(gen);
        addRegistrationDate(gen);

        addRegistrator(gen);

        return gen;
    }

    private static DtoGenerator createTagGenerator()
    {
        DtoGenerator gen = new DtoGenerator("tag", "Tag", TagFetchOptions.class);

        gen.addSimpleField(TagPermId.class, "permId");
        gen.addStringField("code");
        addDescription(gen);
        gen.addSimpleField(Boolean.class, "private", "isPrivate");
        addRegistrationDate(gen);

        gen.addFetchedField(Person.class, "owner", "Owner", PersonFetchOptions.class);

        return gen;
    }

    public static void main(String[] args) throws FileNotFoundException
    {
        List<DtoGenerator> list = new LinkedList<DtoGenerator>();
        list.add(createDataSetGenerator());
        list.add(createDataSetTypeGenerator());
        list.add(createAttachmentGenerator());
        list.add(createExperimentGenerator());
        list.add(createExperimentTypeGenerator());
        list.add(createPersonGenerator());
        list.add(createProjectGenerator());
        list.add(createSampleGenerator());
        list.add(createSampleTypeGenerator());
        list.add(createSpaceGenerator());
        list.add(createTagGenerator());

        for (DtoGenerator gen : list)
        {
            System.out.print("Generating api classes for " + gen + "...");
            gen.generateDTO();
            gen.generateFetchOptions();
            gen.generateDTOJS();
            gen.generateFetchOptionsJS();
            System.out.println("done");
        }
    }
}
