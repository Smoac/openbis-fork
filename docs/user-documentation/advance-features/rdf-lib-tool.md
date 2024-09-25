RDF lib tool
===========================
https://openbis.readthedocs.io/en/latest/user-documentation/advance-features/rdf-lib-tool.html

`rdf-lib-tool` is a Java library that converts input RDF files into XLSX files that are easly ingested by openBIS. 

This tool supports various input and output formats and includes options for specifying credentials and other configurations.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Options](#options)
- [Examples](#examples)


## Installation

### Requirements

1. Java 17

To use `rdf-lib-tool`, you need to have Java 17 installed on your system. 

### Build the JAR

1. Download and set up the openBIS project (follow [building openBIS](#https://openbis.readthedocs.io/en/latest/software-developer-documentation/development-environment/installation-and-configuration-guide.html#building-openbis) guide)
2. Run the gradle task `buildRdfTool`
3. The JAR will be located under `lib-rdf/build/libs/lib-rdf-tool.jar`


## Usage

For the basic usage, this [turtle schema example](./files/schema.ttl) can be downloaded.

Open a terminal in the directiory where the `rdf-lib-tool.jar` is located or simply use the path to it instead `path/to/rdf-lib-tool.jar`.

To run the rdf-lib-tool, use the following command:

```bash
java -jar rdf-lib-tool.jar [options]
```

### Options
```bash
(mandatory) -i, --input-format <file>:  specifies the format of the input file (currently supports TTL).
(mandatory) -o, --output-format <file>: specifies the format of the output (currently supports XLSX, OPENBIS, OPENBIS-DEV).
-pid, --project: specifies the openBIS project identifier. Must be of the format '/{space}/{project}' e.g. '/DEFAULT/DEFAULT'
-u, --username <username>: specifies the username for authentication (needed for OPENBIS and OPENBIS-DEV output format options).
-p, --password <password>: specifies the password for authentication (needed for OPENBIS and OPENBIS-DEV output format options).
-v, --verbose: displays detailed information on the process.
-h, --help: displays the help message.
```

Other mandatory parameters must be provided based on what output format has been chosen:

1. **XLSX** requires `-i <input format> -o <output format> <path to input file> <path to output file>`

2. **OPENBIS** requires `-i <input format> -o <output format> <path to input file> -u <yourUsername> -p <AS openBIS URL>`

3. **OPENBIS-DEV** requires `-i <input format> -o <output format> <path to input file> -u <yourUsername> -p <AS openBIS URL> <DSS openBIS URL>`


## Examples

### Help Message
Display the help message to see all available options:

```bash
java -jar rdf-lib-tool.jar -h
```

### Basic Usage
Convert an RDF file in TTL format to an XLSX file:

```bash
java -jar lib-rdf-tool.jar -i TTL -o XLSX path/to/schema.ttl path/to/output.xlsx 
```

### Connect to openBIS
Import an RDF file in TTL format directly in to openBIS instances as `Sample Types`:
```bash
java -jar lib-rdf-tool.jar -i TTL -o OPENBIS path/to/schema.ttl -u yourUsername -p http://localhost:8888
```

For development environment add the DSS URL:
```bash
java -jar lib-rdf-tool.jar -i TTL -o OPENBIS-DEV path/to/schema.ttl -u yourUsername -p http://localhost:8888/openbis/openbis http://localhost:8889/datastore_server
```

In both cases a username and a password to login into the openBIS instance are required. 

The password will be inserted in a safe prompt.
