package ch.ethz.sis.rdf.main;

import ch.ethz.sis.rdf.main.model.rdf.ModelRDF;
import ch.ethz.sis.rdf.main.parser.RDFReader;
import ch.ethz.sis.rdf.main.xlsx.ExcelImportMessage;
import ch.ethz.sis.rdf.main.xlsx.write.XLSXWriter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.apache.commons.cli.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Path;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RDFCommandLine {

    //private static final String openBISURL = "http://localhost:8888";

    private static final String openBISURL = "https://openbis-sis-ci-sprint.ethz.ch";

    private static final String asURL = "http://localhost:8888/openbis/openbis";

    private static final String dssURL = "http://localhost:8889/datastore_server";

    private static final String helperCommand = "java -jar lib-rdf-tool.jar -i <TTL> -o <XLSX, OPENBIS, OPENBIS-DEV> <TTL input file path> [<XLSX output file path>] -pid <project identifier> [[[-u <username> -p] <openBIS AS URL>] <openBIS DSS URL>]";

    //!!! DEV_MODE is used only for pure dev turn it to FALSE for PRODUCTION !!!
    private static final boolean DEV_MODE = false;
    private static final String OPENBIS_HOME = "/home/mdanaila/Projects/rdf/openbis/";
    private static final String TEMP_OUTPUT_XLSX = OPENBIS_HOME + "lib-rdf/test-data/sphn-data-small/output.xlsx";

    public static void main(String[] args) {

        if (DEV_MODE)
        {
            runTestCases();
        } else {
            Options options = createOptions();

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd = null;

            try {
                cmd = parser.parse(options, args);
                if (cmd.hasOption("help"))
                {
                    formatter.printHelp(helperCommand, options);
                    return;
                }
                validateCommandLine(cmd);
                executeCommandLine(cmd);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp(helperCommand, options);
                System.exit(1);
            }
        }
    }

    private static void runTestCases()
    {
        final String USERNAME = "admin";
        final String PASSWORD = "changeit";
        final String PROJECT_ID = "/DEFAULT/SPHN";
        //final String PROJECT_ID = "/DEFAULT/PREMISE";
        //final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/herbie/new_binder_comp_1.0.0.ttl";
        //final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/link-ml/smallMaterialMLinfo.owl.ttl";
        //final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/sphn-data/mockdata.ttl";
        //final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/sphn-data-small/mockdata_allergy.ttl";
        //final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/sphn-model/rdf_schema_sphn_dataset_release_2024_2.ttl";
        final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/sphn-model/rdf_schema_sphn_dataset_release_2024_2_with_data.ttl";
        //final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/sphn-model/sphn_rdf_schema_2023_2.ttl";
        //final String TTL_FILE_PATH = OPENBIS_HOME + "lib-rdf/test-data/sphn-model/sphn_rdf_schema_2023_2_with_data.ttl";

        //handleXlsxOutput("TTL", TTL_FILE_PATH,
        //        TEMP_OUTPUT_XLSX,
        //        PROJECT_ID,
        //        false);

        //handleOpenBISOutput("TTL", TTL_FILE_PATH,
        //        openBISURL,
        //        USERNAME, PASSWORD,
        //        PROJECT_ID,
        //        false);

        handleOpenBISDevOutput("TTL", TTL_FILE_PATH,
                asURL, dssURL,
                USERNAME, PASSWORD,
                PROJECT_ID,
                false);
    }

    //TODO: add flag -d for dependecies list of files or zip
    //TODO: change -i to take and process a list of files or zip
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
                        " - OPENBIS, the entities are stored directly in an openBIS instance (username, password and openbis URL must be provided)\n " +
                        " - OPENBIS-DEV, the entities are stored directly in an openBIS development instance (username, password, AS openbis URL and DSS openBIS URL must be provided)")
                .required()
                .build();
        options.addOption(output);

        Option user = new Option("u", "user", true, "openBIS instance user login");
        options.addOption(user);

        Option password = new Option("p", "password", false, "openBIS user password (will be prompted)");
        options.addOption(password);

        Option project = new Option("pid", "project", true, "openBIS project identifier");
        options.addOption(project);

        Option verbose = new Option("v", "verbose", false, "Display verbose output");
        options.addOption(verbose);

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

    private static void validateCommandLine(CommandLine cmd)
    {
        if (cmd.hasOption("project"))
            if (!validateProjectIdentifier(cmd.getOptionValue("project")))
                throw new IllegalArgumentException("Project identifier not valid! PID must follow the openBIS standard e.g. /DEFAULT/DEFAULT ");
        String outputFormatValue = cmd.getOptionValue("output");
        String[] remainingArgs = cmd.getArgs();
        //Arrays.stream(remainingArgs).forEach(System.out::println);
        switch (outputFormatValue.toUpperCase())
        {
            case "XLSX":
                if (remainingArgs.length != 2)
                {
                    throw new IllegalArgumentException(
                            "For XLSX output, specify the input and output file path. \n " +
                                    "Usage: java -jar lib-rdf-tool.jar -i <input format> -o XLSX <file path> <XLSX output file path> \n");
                }
                break;
            case "OPENBIS":
                if (remainingArgs.length != 2 && !cmd.hasOption("username") && !cmd.hasOption("password"))
                {
                    throw new IllegalArgumentException("For OPENBIS output, specify input file path, username, password and openBIS URL. \n " +
                            "Usage: java -jar lib-rdf-tool.jar -i <input format> -o OPENBIS <file path> -u <username> -p <openBIS URL> \n");
                }
                break;
            case "OPENBIS-DEV":
                if (remainingArgs.length != 3 && !cmd.hasOption("username") && !cmd.hasOption("password"))
                {
                    throw new IllegalArgumentException(
                            "For OPENBIS-DEV output, specify input file path, username, password, AS openBIS URL and DSS openBIS URL. \n " +
                                    "Usage: java -jar lib-rdf-tool.jar -i <input format> -o OPENBIS-DEV <file path> -u <username> -p <openBIS AS URL> <openBIS DSS URL> \n");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported output type: " + outputFormatValue.toUpperCase());
        }
    }

    public static boolean validateProjectIdentifier(String projectIdentifier)
    {
        String PROJECT_IDENTIFIER_PATTERN = "^/[a-zA-Z]+/[a-zA-Z]+$";
        Pattern pattern = Pattern.compile(PROJECT_IDENTIFIER_PATTERN);
        Matcher matcher = pattern.matcher(projectIdentifier);

        return matcher.matches();
    }

    private static void executeCommandLine(CommandLine cmd)
    {
        String inputFormatValue = cmd.getOptionValue("input");
        String outputFormatValue = cmd.getOptionValue("output");
        String inputFilePath = null;
        String username = null;
        String password = null;
        String openbisASURL = null;
        String openBISDSSURL = null;
        String projectIdentifier = cmd.getOptionValue("project");
        boolean verbose = cmd.hasOption("verbose");

        String[] remainingArgs = cmd.getArgs();
        //Arrays.stream(remainingArgs).forEach(System.out::println);
        switch (outputFormatValue.toUpperCase())
        {
            case "XLSX":
                inputFilePath = remainingArgs[0];
                String outputFilePath = remainingArgs[1];
                System.out.println("Handling: " + inputFormatValue + " -> " + outputFormatValue);
                handleXlsxOutput(inputFormatValue, inputFilePath, outputFilePath, projectIdentifier, verbose);
                break;
            case "OPENBIS":
                username = cmd.getOptionValue("user");
                password = "changeit";
                inputFilePath = remainingArgs[0];
                openbisASURL = remainingArgs[1];

                System.out.println("Handling: " + inputFormatValue + " -> " + outputFormatValue);
                System.out.println("Connect to openBIS instance " + openbisASURL + " with username[" + username + "]"); // and password[" + new String(password) + "]");
                handleOpenBISOutput(inputFormatValue, inputFilePath, openbisASURL, username, new String(password), projectIdentifier, verbose);
                break;
            case "OPENBIS-DEV":
                username = cmd.getOptionValue("user");
                password = getPassword(cmd);
                inputFilePath = remainingArgs[0];
                openbisASURL = remainingArgs[1];
                openBISDSSURL = remainingArgs[2];

                System.out.println("Handling: " + inputFormatValue + " -> " + outputFormatValue);
                handleOpenBISDevOutput(inputFormatValue, inputFilePath, openbisASURL, openBISDSSURL, username, new String(password), projectIdentifier, verbose);
                break;
            default:
                throw new IllegalArgumentException("Unsupported output type: " + outputFormatValue.toUpperCase());
        }
    }

    private static void handleXlsxOutput(String inputFormatValue, String inputFilePath, String outputFilePath, String projectIdentifier, boolean verbose)
    {
        System.out.println("Reading Ontology Model...");

        RDFReader rdfReader = new RDFReader();
        ModelRDF modelRDF = rdfReader.read(inputFilePath, inputFormatValue, verbose);

        // Collect and map all RDF classes in JAVA obj
        System.out.println("Collecting RDF classes...");

        // Write model to an Excel file (apache POI dependency)
        System.out.println("Writing XLSX file...");
        XLSXWriter XLSXWriter = new XLSXWriter();
        XLSXWriter.write(modelRDF, outputFilePath, projectIdentifier);

        System.out.println("XLSX created successfully!");
    }

    private static void handleOpenBISOutput(String inputFormatValue, String inputFilePath, String openbisURL, String username, String password, String projectIdentifier, boolean verbose)
    {
        Path tempFile = Utils.createTemporaryFile();
        String tempFileOutput = tempFile.toString();

        if (DEV_MODE)
        {
            //change to your local path
            tempFileOutput = TEMP_OUTPUT_XLSX;
            tempFile = Path.of(tempFileOutput);
        }

        System.out.println("Created temporary XLSX output file: " + tempFileOutput);
        handleXlsxOutput(inputFormatValue, inputFilePath, tempFileOutput, projectIdentifier, verbose);

        System.out.println(
                "Connect to openBIS instance " + openbisURL + " with username[" + username + "]"); //and password[" + new String(password) +"]");

        var importer = new Importer(openbisURL, username, password, tempFile);

        int maxRetries = 3000;
        boolean shouldTry = true;
        int numRetries = 0;
        List<ExcelImportMessage> messageList = new ArrayList<>();


        while (shouldTry){
            try
            {
                numRetries++;
                importer.connect(tempFile);
                shouldTry = false;
            } catch (UserFailureException e)
            {

                ExcelImportMessage message = ExcelImportMessage.from(e);
                if (message == null)
                {
                    shouldTry = false;
                } else
                {
                    messageList.add(message);
                    shouldTry = numRetries < maxRetries;
                    if (!shouldTry)
                    {
                        throw e;
                    }
                    deleteRow(tempFile, message.getSheet(), message.getLine());
                }
            }
        }
        printMessageList(messageList);
    }

    private static void printMessageList(List<ExcelImportMessage> messageList)
    {
        if (messageList.isEmpty())
        {
            return;
        }
        System.out.println("File was imported, individual entries had problems");
        messageList.forEach(x -> {
            System.out.println(
                    "Sheet" + x.getSheet() + " row: " + x.getLine() + " message: " + x.getMessage());
        });

    }

    private static void deleteRow(Path path, int sheet, int row)
    {
        try
        {
            FileInputStream fis = new FileInputStream(new File(path.toUri()));
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet curSheet = wb.getSheetAt(sheet - 1);
            curSheet.removeRow(curSheet.getRow(row - 1));
            if (row - 1 < curSheet.getLastRowNum())
            {
                curSheet.shiftRows(row, curSheet.getLastRowNum(), -1);
            }

            wb.write(new FileOutputStream(path.toFile()));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    private static void handleOpenBISDevOutput(String inputFormatValue, String inputFilePath, String openbisASURL, String openBISDSSURL,
            String username, String password, String projectIdentifier, boolean verbose) {

        Path tempFile = Utils.createTemporaryFile();
        String tempFileOutput = tempFile.toString();
        if (DEV_MODE)
        {
            //change to your local path
            tempFileOutput = TEMP_OUTPUT_XLSX;
            tempFile = Path.of(tempFileOutput);
        }
        System.out.println("Created temporary XLSX output file: " + tempFileOutput);
        handleXlsxOutput(inputFormatValue, inputFilePath, tempFileOutput, projectIdentifier, verbose);

        System.out.println("Connect to openBIS-DEV instance AS[" + openbisASURL + "] DSS[" + openBISDSSURL + "] with username[" + username + "]"); //and password[" + new String(password) +"]");

        new Importer(openbisASURL, openBISDSSURL, username, password, tempFile).connect(tempFile);
    }
}
