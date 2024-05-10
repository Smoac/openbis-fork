package ch.ethz.sis.rdf.main;

import ch.ethz.sis.openbis.generic.OpenBISExtended;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.rdf.main.mappers.DatatypeMapper;
import ch.ethz.sis.rdf.main.mappers.ObjectPropertyMapper;
import ch.ethz.sis.rdf.main.mappers.OntClassObject;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.rdf.main.ClassCollector.collectClassDetails;

public class MainTest
{
    private static final String asURL = "http://localhost:8888/openbis/openbis";
    private static final String dssURL = "http://localhost:8889/datastore_server";

    private static final String serviceURL = asURL + IApplicationServerApi.SERVICE_URL;

    //private static final String URL = "https://openbis-sis-ci-sprint.ethz.ch/openbis/openbis" + IApplicationServerApi.SERVICE_URL;

    private static final int TIMEOUT = 30000;

    private static final String USER = "admin";

    private static final String PASSWORD = "changeit";


    public static void testPrintDatatypeMapper(OntModel model) {
        // Mapping of XSD datatypes to custom string types
        Map<String, List<String>> mappedDataTypes = DatatypeMapper.toOpenBISDataTypes(model);
        mappedDataTypes.forEach((k, v) -> System.out.println(k + " --> " + v));
        // doesn't catch https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNAttributeDatatype that is the superclass of SPHN datatypes

        /*
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasNSuffix --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasNPrefix --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasComment --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTime --> [TIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCodingSystemAndVersion --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasYear --> [YEAR]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasExposureDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasRecordDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTSuffix --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDischargeDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasBiobankName --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasExact --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasFreeText --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasIdentifier --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasEnd --> [DOUBLE]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasLastAdministrationDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasFirstAdministrationDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasEventDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasExtractionDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasOnsetDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasGenericName --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasName --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMonth --> [MONTH]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasReportDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasStart --> [DOUBLE]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTPrefix --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasStartDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasValue --> [DOUBLE, STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDeterminationDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasAdmissionDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasQualitativeResult --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMeasurementDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasReferenceAllele --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasEndDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCodingDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTemplateIdentifier --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasFirstRecordDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCollectionDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasAlternateAllele --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasScoringSystem --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasAssessmentDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasLastReactionDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMSuffix --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasVersion --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDay --> [DAY]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMPrefix --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasManifestationDateTime --> [DATETIME]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasUniformResourceIdentifier --> [STRING]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasIntervention --> [STRING]
        */
    }

    public static void testPrintObjectPrepertyMapper(OntModel model) {
        // Mapping of relationship object properties to respective objects
        Map<String, List<String>> mappedObjectProperty = ObjectPropertyMapper.toObjects(model);
        mappedObjectProperty.forEach((k, v) -> System.out.println(k + " --> " + v));
        // doesn't catch https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNAttributeDatatype that is the superclass of SPHN datatypes

        /*
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDeathDate --> [https://biomedit.ch/rdf/sphn-ontology/sphn#DeathDate]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasComparator --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasExposureDuration --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasBodySite --> [https://biomedit.ch/rdf/sphn-ontology/sphn#BodySite]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTargetLocation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Location]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasOriginLocation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Location]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasZygosityCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasProgressionType --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasProgressionBodySite --> [https://biomedit.ch/rdf/sphn-ontology/sphn#BodySite]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCalculationMethod --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCurrentLocation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Location]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDiastolicPressure --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCareHandling --> [https://biomedit.ch/rdf/sphn-ontology/sphn#CareHandling]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMeasurementMethod --> [https://biomedit.ch/rdf/sphn-ontology/sphn#MeasurementMethod]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTypeCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasFrequency --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasPhysiologicState --> [https://biomedit.ch/rdf/sphn-ontology/sphn#PhysiologicState]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDataFile --> [https://biomedit.ch/rdf/sphn-ontology/sphn#DataFile]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasRegularityCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasLocation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Location]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasFormatCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDrug --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Drug]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasLowerLimit --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasReasonToStopCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasManifestationSeverityCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasChromosomalLocation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#ChromosomalLocation]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasProductCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasReference --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Reference]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTestKit --> [https://biomedit.ch/rdf/sphn-ontology/sphn#LabAnalyzer]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasStatusCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasGenomicPosition --> [https://biomedit.ch/rdf/sphn-ontology/sphn#GenomicPosition]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasManufacturedDoseForm --> [https://biomedit.ch/rdf/sphn-ontology/sphn#PharmaceuticalDoseForm]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasProtein --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Protein]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasFractionsNumber --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDischargeLocation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Location]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasReferenceRange --> [https://biomedit.ch/rdf/sphn-ontology/sphn#ReferenceRange]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasOxygenFlowRate --> [https://biomedit.ch/rdf/sphn-ontology/sphn#DrugAdministrationEvent]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasOutput --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Electrocardiogram]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasUnit --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Unit]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasQualitativeResultCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDuration --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasQuantitativeResult --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasVerificationStatusCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasInsertionPoint --> [https://biomedit.ch/rdf/sphn-ontology/sphn#BodySite]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasManifestationDuration --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasFixationType --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasPrimaryContainer --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasRadiationQuantity --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTranscript --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Transcript]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasManifestationCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasOutcome --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTimePattern --> [https://biomedit.ch/rdf/sphn-ontology/sphn#TimePattern]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasReactionTypeCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasAdministrationRouteCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasExposureRouteCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDataDetermination --> [https://biomedit.ch/rdf/sphn-ontology/sphn#DataDetermination]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasIndicationToStart --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Diagnosis, https://biomedit.ch/rdf/sphn-ontology/sphn#Intent]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCriteria --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasStartCytobandCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDataProviderInstitute --> [https://biomedit.ch/rdf/sphn-ontology/sphn#DataProviderInstitute]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasIntent --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Intent]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasRelativeTemporalityCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasInstrument --> [https://biomedit.ch/rdf/sphn-ontology/sphn#LabAnalyzer]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCertaintyCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasInactiveIngredient --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Substance]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasManifestationBodySite --> [https://biomedit.ch/rdf/sphn-ontology/sphn#BodySite]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasLaterality --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Laterality]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasAllergen --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Allergen]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasConsequences --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasSubjectPseudoIdentifier --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SubjectPseudoIdentifier]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasSystolicPressure --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMethod --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasNotation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#VariantNotation]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMedicalDevice --> [https://biomedit.ch/rdf/sphn-ontology/sphn#MedicalDevice]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMaterialTypeCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasRank --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasEndCytobandCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMethodCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMorphologyCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTumorPurity --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasMeanPressure --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCoordinateConvention --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTherapeuticArea --> [https://biomedit.ch/rdf/sphn-ontology/sphn#TherapeuticArea]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasQuantity --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasDrugQuantity --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasSeverityCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasUpperLimit --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasGene --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Gene]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasTopographyCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasSample --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Sample]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasOxygenEquipment --> [https://biomedit.ch/rdf/sphn-ontology/sphn#MedicalDevice]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasAlleleOriginCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasLabTest --> [https://biomedit.ch/rdf/sphn-ontology/sphn#LabTest]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasGeneticVariation --> [https://biomedit.ch/rdf/sphn-ontology/sphn#GeneticVariation]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasRestingPoint --> [https://biomedit.ch/rdf/sphn-ontology/sphn#BodySite]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasActiveIngredient --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Substance]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasChromosome --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Chromosome]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasResult --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasOrganism --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Organism]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasScoringSystemCode --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Terminology, https://biomedit.ch/rdf/sphn-ontology/sphn#Code]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasSpecialtyName --> [https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasAdministrativeCase --> [https://biomedit.ch/rdf/sphn-ontology/sphn#AdministrativeCase]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasNumberOfLeads --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Quantity]
            https://biomedit.ch/rdf/sphn-ontology/sphn#hasSubjectAge --> [https://biomedit.ch/rdf/sphn-ontology/sphn#Age]
        */
    }

    public static void testPrintClassCollector(OntModel model) {
        Map<OntClass, OntClassObject> classDetailsMap = ch.ethz.sis.rdf.main.ClassCollector.collectClassDetails(model);

        classDetailsMap.values().forEach(System.out::println);
        System.out.println(classDetailsMap.values().size());

        /*
            Class: https://biomedit.ch/rdf/sphn-ontology/sphn#LabTest
            SuperClass: https://biomedit.ch/rdf/sphn-ontology/sphn#SPHNConcept
            Label: Lab Test
            Comment: lab test information including information elements provided by LOINC, instrument and test kit
            SKOS Definition: lab test information including information elements provided by LOINC, instrument and test kit
            SKOS Note: -
            Restrictions:
             - on https://biomedit.ch/rdf/sphn-ontology/sphn#hasCode:  MinCardinalityRestriction [1] MaxCardinalityRestriction [1] SomeValuesFromRestriction [https://biomedit.ch/rdf/sphn-resource/loinc/LOINC] SomeValuesFromRestriction [https://biomedit.ch/rdf/sphn-resource/loinc/LOINC]
             - on https://biomedit.ch/rdf/sphn-ontology/sphn#hasInstrument:  MinCardinalityRestriction [0] MaxCardinalityRestriction [1]
             - on https://biomedit.ch/rdf/sphn-ontology/sphn#hasTestKit:  MinCardinalityRestriction [0] MaxCardinalityRestriction [1]
        */
    }

    public static String testWriteToXlsx(OntModel model) throws IOException {
        // Collect and map all RDF classes in JAVA obj
        Map<OntClass, OntClassObject> classDetailsMap = collectClassDetails(model);

        String fileName = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/output.xlsx";
        ExcelWriter.createExcelFile(model, classDetailsMap.values(), fileName);

        return fileName;
    }

    public static void testWorkflow(OntModel model) throws IOException {
        // 1. Read RDF file with Apache Jena to have an in-memory model (Jena dependency)
        // https://mvnrepository.com/artifact/org.apache.jena/jena-core/5.0.0 -> Move what we need to our ivy-repository

        // 2. Create openBIS model from RDF (openbis batteries dependency)
        // https://sissource.ethz.ch/openbis/openbis-public/openbis-ivy/-/tree/main/openbis/openbis-v3-api-batteries-included/6.5.0?ref_type=heads

        // Collect and map all RDF classes in JAVA obj
        Map<OntClass, OntClassObject> classDetailsMap = collectClassDetails(model);


        // 3. Write model to an Excel file (apache POI dependency)

        //String fileName = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/output.xlsx";
        String fileName = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/output2.xlsx";
        ExcelWriter.createExcelFile(model, classDetailsMap.values(), fileName);


        // https://sissource.ethz.ch/openbis/openbis-public/openbis-ivy/-/tree/main/apache/poi/5.2.5
        // 4. Call openBIS V3 API executeImport with the Excel file

        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, serviceURL, TIMEOUT);
        String sessionToken = v3.login(USER, PASSWORD);
        System.out.println("sessionToken: " + sessionToken);

        // Call excel import
        UncompressedImportData uncompressedImportData = new UncompressedImportData();
        uncompressedImportData.setFormat(ImportFormat.XLS);
        uncompressedImportData.setFile(Files.readAllBytes(Path.of(fileName)));

        ImportOptions importOptions = new ImportOptions();
        importOptions.setMode(ImportMode.UPDATE_IF_EXISTS);

        OpenBISExtended openBIS = new OpenBISExtended(asURL, dssURL, TIMEOUT);

        openBIS.executeImport(uncompressedImportData, importOptions, sessionToken);
    }

    public static void main(String[] args) throws IOException
    {
        String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/sphn_rdf_schema.ttl";
        // String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/new_binder_comp_1.0.0.ttl";
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        //model.read(filePath, "TURTLE");
        RDFDataMgr.read(model, filePath, Lang.TTL);

        //testPrintDatatypeMapper(model);
        //testPrintObjectPrepertyMapper(model);
        //testPrintClassCollector(model);
        //testWriteToXlsx(model);
        testWorkflow(model);
    }
}
