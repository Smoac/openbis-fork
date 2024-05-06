package ch.ethz.sis.rdf.main;

import ch.ethz.sis.openbis.generic.OpenBISExtended;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.rdf.main.mappers.OntClassObject;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static ch.ethz.sis.rdf.main.ClassCollector.collectClassDetails;

public class Main
{
    private static final String asURL = "http://localhost:8888/openbis/openbis";
    private static final String dssURL = "http://localhost:8889/datastore_server";

    private static final String serviceURL = asURL + IApplicationServerApi.SERVICE_URL;

    //private static final String URL = "https://openbis-sis-ci-sprint.ethz.ch/openbis/openbis" + IApplicationServerApi.SERVICE_URL;

    private static final int TIMEOUT = 30000;

    private static final String USER = "admin";

    private static final String PASSWORD = "changeit";

    public static void main(String[] args) throws IOException
    {

        // 1. Read RDF file with Apache Jena to have an in-memory model (Jena dependency)
        // https://mvnrepository.com/artifact/org.apache.jena/jena-core/5.0.0 -> Move what we need to our ivy-repository

        //String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/sphn_rdf_schema.ttl";
        String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/new_binder_comp_1.0.0.ttl";
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(filePath, "TURTLE");


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
}
