package ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;

public class AbstractGenerator
{

    public static void addModificationDate(DtoGenerator gen)
    {
        gen.addSimpleField(Date.class, "modificationDate");
    }

    public static void addRegistrationDate(DtoGenerator gen)
    {
        gen.addSimpleField(Date.class, "registrationDate");
    }

    public static void addCode(DtoGenerator gen)
    {
        gen.addSimpleField(String.class, "code");
    }

    public static void addModifier(DtoGenerator gen)
    {
        gen.addFetchedField(Person.class, "modifier", "Modifier", PersonFetchOptions.class);
    }

    public static void addExperiment(DtoGenerator gen)
    {
        gen.addFetchedField(Experiment.class, "experiment", "Experiment", ExperimentFetchOptions.class);
    }

    public static void addSample(DtoGenerator gen)
    {
        gen.addFetchedField(Sample.class, "sample", "Sample", SampleFetchOptions.class);
    }

    public static void addSpace(DtoGenerator gen)
    {
        gen.addFetchedField(Space.class, "space", "Space", SpaceFetchOptions.class);
    }

    public static void addRegistrator(DtoGenerator gen)
    {
        gen.addFetchedField(Person.class, "registrator", "Registrator", PersonFetchOptions.class);
    }

    public static void addTags(DtoGenerator gen)
    {
        gen.addPluralFetchedField("Set<Tag>", Set.class.getName(), "tags", "Tags", TagFetchOptions.class);
        gen.addClassForImport(Tag.class);
        gen.addClassForImport(Set.class);
    }

    public static void addProperties(DtoGenerator gen)
    {
        gen.addPluralFetchedFieldFromInterface("Map<String, String>", Map.class.getName(), "properties", "Properties", PropertyFetchOptions.class);
        gen.addClassForImport(Map.class);
        gen.addPluralFetchedFieldFromInterface("Map<String, Material>", Map.class.getName(), "materialProperties", "Material Properties",
                MaterialFetchOptions.class);
        gen.addClassForImport(Map.class);
        gen.addClassForImport(Material.class);
        gen.addImplementedInterface(IPropertiesHolder.class);
    }

    public static void addAttachments(DtoGenerator gen)
    {
        gen.addPluralFetchedField("List<Attachment>", List.class.getName(), "attachments", "Attachments", AttachmentFetchOptions.class);
        gen.addClassForImport(Attachment.class);
        gen.addClassForImport(List.class);
    }

    public static void addDescription(DtoGenerator gen)
    {
        gen.addSimpleField(String.class, "description");
    }
}
