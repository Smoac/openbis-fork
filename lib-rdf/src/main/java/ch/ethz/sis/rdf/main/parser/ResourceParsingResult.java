package ch.ethz.sis.rdf.main.parser;

import ch.ethz.sis.rdf.main.model.xlsx.SampleObject;

import java.util.List;
import java.util.Set;

public class ResourceParsingResult
{
    private final List<SampleObject> deletedObjects;

    private final List<SampleObject> unchangedObjects;

    private final List<SampleObject> editedObjects;

    private final Set<String> classesImported;

    private final List<String> propertiesImported;

    public ResourceParsingResult(List<SampleObject> deletedObjects,
            List<SampleObject> unchangedObjects,
            List<SampleObject> editedObjects, Set<String> classesImported,
            List<String> propertiesImported)
    {
        this.deletedObjects = deletedObjects;
        this.unchangedObjects = unchangedObjects;
        this.editedObjects = editedObjects;
        this.classesImported = classesImported;
        this.propertiesImported = propertiesImported;
    }

    public List<SampleObject> getDeletedObjects()
    {
        return deletedObjects;
    }

    public List<SampleObject> getEditedObjects()
    {
        return editedObjects;
    }

    public List<SampleObject> getUnchangedObjects()
    {
        return unchangedObjects;
    }

    public Set<String> getClassesImported()
    {
        return classesImported;
    }

    public List<String> getPropertiesImported()
    {
        return propertiesImported;
    }
}
