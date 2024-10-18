package ch.ethz.sis.rdf.main.parser;

import ch.ethz.sis.rdf.main.ClassCollector;
import ch.ethz.sis.rdf.main.mappers.DatatypeMapper;
import ch.ethz.sis.rdf.main.mappers.NamedIndividualMapper;
import ch.ethz.sis.rdf.main.mappers.ObjectPropertyMapper;
import ch.ethz.sis.rdf.main.model.rdf.ModelRDF;
import ch.ethz.sis.rdf.main.model.rdf.OntClassExtension;
import ch.ethz.sis.rdf.main.model.xlsx.SampleObject;
import ch.ethz.sis.rdf.main.model.xlsx.SamplePropertyType;
import ch.ethz.sis.rdf.main.model.xlsx.SampleType;
import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceParsingResult
{
    private final List<SampleObject> deletedObjects;

    private final List<SampleObject> unchangedObjects;

    private final List<SampleObject> editedObjects;

    public ResourceParsingResult(List<SampleObject> deletedObjects,
            List<SampleObject> unchangedObjects,
            List<SampleObject> editedObjects)
    {
        this.deletedObjects = deletedObjects;
        this.unchangedObjects = unchangedObjects;
        this.editedObjects = editedObjects;
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
}
