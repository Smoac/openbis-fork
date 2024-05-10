package ch.ethz.sis.rdf.main;

import ch.ethz.sis.openbis.generic.OpenBISExtended;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.rdf.main.mappers.OntClassObject;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import org.apache.commons.cli.*;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.Console;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static ch.ethz.sis.rdf.main.ClassCollector.collectClassDetails;

public class RDFCommandLine
{

    public static void main(String[] args)
    {
        Options options = createOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try
        {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("help"))
            {
                formatter.printHelp(
                        "java -jar lib-rdf-tool.jar -i <TTL> -o <XLSX, OPENBIS, OPENBIS-DEV> <TTL input file path> [<XLSX output file path>] [[[-u <username> -p] <openBIS AS URL>] <openBIS DSS URL>]",
                        options);
                return;
            }
            validateAndExecute(cmd);
        } catch (ParseException e)
        {
            System.out.println(e.getMessage());
            formatter.printHelp(
                    "java -jar lib-rdf-tool.jar -i <TTL> -o <XLSX, OPENBIS, OPENBIS-DEV> <TTL input file path> [<XLSX output file path>] [[[-u <username> -p] <openBIS AS URL>] <openBIS DSS URL>]",
                    options);
            System.exit(1);
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static Options createOptions()
    {
        Options options = new Options();

        Option input = Option.builder("i")
                .longOpt("input")
                .hasArgs()
                .numberOfArgs(1)
                .argName("format")
                .desc("Input format, supported: \n \t- TTL, turtle file (an input ttl file must be provided)")
                .required()
                .build();
        options.addOption(input);

        Option output = Option.builder("o")
                .longOpt("output")
                .hasArgs()
                .numberOfArgs(1)
                .argName("format")
                .desc("Output option, supported: \n \t- XLSX, return a XLSX file (an output XLSX file must be provided)\n" +
                        " - OPENBIS, the entities are stored directly in an openBIS instance (username, password and AS openbis URL must be provided)\n " +
                        " - OPENBIS-DEV, the entities are stored directly in an openBIS development instance (username, password, AS openbis URL and DSS openBIS URL must be provided)")
                .required()
                .build();
        options.addOption(output);

        Option user = new Option("u", "user", true, "openBIS instance user login");
        options.addOption(user);

        Option password = new Option("p", "password", false, "openBIS user password (will be prompted)");
        options.addOption(password);

        Option help = new Option("h", "help", false, "Display this help message");
        options.addOption(help);

        return options;
    }

    private static String getPassword(CommandLine cmd)
    {
        char[] password = null;
        if (cmd.hasOption("password"))
        {
            Console console = System.console();
            if (console == null)
            {
                System.out.println("No console available");
                System.exit(1);
            } else
            {
                password = console.readPassword("Enter password: ");
            }
        }
        assert password != null;
        return String.valueOf(password);
    }

    private static void validateAndExecute(CommandLine cmd) throws IOException
    {
        String inputFormatValue = cmd.getOptionValue("input");
        String outputFormatValue = cmd.getOptionValue("output");
        String inputFilePath = null;
        String username = null;
        String password = null;
        String openbisASURL = null;
        String openBISDSSURL = null;

        String[] remainingArgs = cmd.getArgs();
        //Arrays.stream(remainingArgs).forEach(System.out::println);
        switch (outputFormatValue.toUpperCase())
        {
            case "XLSX":
                if (remainingArgs.length != 2)
                {
                    throw new IllegalArgumentException(
                            "For XLSX output, specify the input and output file path. \n " +
                                    "Usage: java -jar lib-rdf-tool.jar -i <input format> -o XLSX <<input format> file path> <XLSX file path>");
                }
                inputFilePath = remainingArgs[0];
                String outputFilePath = remainingArgs[1];
                System.out.println("Handling XLSX: " + inputFormatValue + " -> " + outputFormatValue);
                handleXlsxOutput(inputFormatValue, inputFilePath, outputFilePath);
                break;
            case "OPENBIS":
                if (remainingArgs.length != 2 && !cmd.hasOption("username") && !cmd.hasOption("password"))
                {
                    throw new IllegalArgumentException("For OPENBIS output, specify input file path, username, password and AS openBIS URL. \n " +
                            "Usage: java -jar lib-rdf-tool.jar -i <input format> -o OPENBIS <<input format> file path> -u <username> -p <openBIS AS URL>");
                }
                username = cmd.getOptionValue("user");
                password = getPassword(cmd);
                inputFilePath = remainingArgs[0];
                openbisASURL = remainingArgs[1];

                System.out.println("Handling OPENBIS: " + inputFormatValue + " -> " + outputFormatValue);
                System.out.println("Connect to openBIS instance " + openbisASURL + " with username[" + username + "] and password[" + new String(
                        password) + "]");
                handleOpenBISOutput(inputFormatValue, inputFilePath, openbisASURL, username, new String(password));
                break;
            case "OPENBIS-DEV":
                if (remainingArgs.length != 3 && !cmd.hasOption("username") && !cmd.hasOption("password"))
                {
                    throw new IllegalArgumentException(
                            "For OPENBIS output, specify input file path, username, password, AS openBIS URL and DSS openBIS URL. \n " +
                                    "Usage: java -jar lib-rdf-tool.jar -i <input format> -o OPENBIS-DEV <<input format> file path> -u <username> -p <openBIS AS URL> <openBIS DSS URL>");
                }
                username = cmd.getOptionValue("user");
                password = getPassword(cmd);
                inputFilePath = remainingArgs[0];
                openbisASURL = remainingArgs[1];
                openBISDSSURL = remainingArgs[2];

                System.out.println("Handling OPENBIS-DEV: " + inputFormatValue + " -> " + outputFormatValue);
                handleOpenBISDevOutput(inputFormatValue, inputFilePath, openbisASURL, openBISDSSURL, username, new String(password));
                break;
            default:
                throw new IllegalArgumentException("Unsupported output type: " + outputFormatValue.toUpperCase());
        }
    }

    private static void handleXlsxOutput(String inputFormatValue, String inputFilePath, String outputFilePath) throws IOException
    {
        System.out.println("Handling XLSX from [" + inputFilePath + "] to [" + outputFilePath + "]");

        System.out.println("Creating Ontology Model...");

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        if (inputFormatValue.equals("TTL")) {
            //model.read(inputFilePath, "TURTLE");
            RDFDataMgr.read(model, inputFilePath, Lang.TTL);
        } else {
            throw new IllegalArgumentException("Unsupported input format: " + inputFormatValue);
        }

        // Collect and map all RDF classes in JAVA obj
        System.out.println("Collecting RDF classes...");
        Map<OntClass, OntClassObject> classDetailsMap = collectClassDetails(model);

        // Write model to an Excel file (apache POI dependency)
        System.out.println("Writing XLSX file...");
        ExcelWriter.createExcelFile(model, classDetailsMap.values(), outputFilePath);

        System.out.println("Done. Excel at " + outputFilePath);
    }

    //private static final String asURL = "http://localhost:8888/openbis/openbis";
    //private static final String dssURL = "http://localhost:8889/datastore_server";
    private static final int TIMEOUT = 30000;

    private static void handleOpenBISOutput(String inputFormatValue, String inputFilePath, String openbisASURL, String username, String password)
            throws IOException
    {
        String tempFileOutput = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/output.xlsx";
        handleXlsxOutput(inputFormatValue, inputFilePath, tempFileOutput);

        System.out.println(
                "Connect to openBIS instance " + openbisASURL + " with username[" + username + "]"); //and password[" + new String(password) +"]");
        String serviceURL = openbisASURL + IApplicationServerApi.SERVICE_URL;

        // 4. Call openBIS V3 API executeImport with the Excel file
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, serviceURL, TIMEOUT);
        String sessionToken = v3.login(username, password);
        System.out.println("sessionToken: " + sessionToken);

        // Call excel import
        UncompressedImportData uncompressedImportData = new UncompressedImportData();
        uncompressedImportData.setFormat(ImportFormat.XLS);
        uncompressedImportData.setFile(Files.readAllBytes(Path.of(tempFileOutput)));

        ImportOptions importOptions = new ImportOptions();
        importOptions.setMode(ImportMode.UPDATE_IF_EXISTS);

        OpenBISExtended openBIS = new OpenBISExtended(openbisASURL, null, TIMEOUT);

        openBIS.executeImport(uncompressedImportData, importOptions, sessionToken);
    }

    private static void handleOpenBISDevOutput(String inputFormatValue, String inputFilePath, String openbisASURL, String openBISDSSURL,
            String username, String password)
            throws IOException
    {

        String tempFileOutput = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/output.xlsx";
        handleXlsxOutput(inputFormatValue, inputFilePath, tempFileOutput);

        System.out.println(
                "Connect to openBIS-DEV instance AS[" + openbisASURL + "] DSS[" + openBISDSSURL + "] with username[" + username + "]"); //and password[" + new String(password) +"]");
        String serviceURL = openbisASURL + IApplicationServerApi.SERVICE_URL;

        // 4. Call openBIS V3 API executeImport with the Excel file
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, serviceURL, TIMEOUT);
        String sessionToken = v3.login(username, password);
        System.out.println("sessionToken: " + sessionToken);

        // Call excel import
        UncompressedImportData uncompressedImportData = new UncompressedImportData();
        uncompressedImportData.setFormat(ImportFormat.XLS);
        uncompressedImportData.setFile(Files.readAllBytes(Path.of(tempFileOutput)));

        ImportOptions importOptions = new ImportOptions();
        importOptions.setMode(ImportMode.UPDATE_IF_EXISTS);

        OpenBISExtended openBIS = new OpenBISExtended(openbisASURL, openBISDSSURL, TIMEOUT);

        openBIS.executeImport(uncompressedImportData, importOptions, sessionToken);
    }
}
