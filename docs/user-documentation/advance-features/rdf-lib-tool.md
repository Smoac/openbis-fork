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

To use `rdf-lib-tool`, you need to have Java 17 installed on your system. 

You can download the JAR file from the [? releases page](#).

### Download the JAR

1. Download the latest version of `rdf-lib-tool.jar` from the [releases page](#).
2. Save the JAR file to your desired directory.

## Usage

For the basic usage, this [turtle schema example](./files/schema.ttl) can be downloaded.

Open a terminal in the directiory where the `rdf-lib-tool.jar` is located or simply use the path to it instead `path/to/rdf-lib-tool.jar`.

To run the rdf-lib-tool, use the following command:

```bash
java -jar rdf-lib-tool.jar [options]
```

### Options
```bash
(mandatory) -i, --input-format <file>:  Specifies the format of the input file (currently supports TTL).
(mandatory) -o, --output-format <file>: Specifies the format of the output (currently supports XLSX, OPENBIS, OPENBIS-DEV).
-u, --username <username>: Specifies the username for authentication (needed for OPENBIS and OPENBIS-DEV output format options).
-p, --password <password>: Specifies the password for authentication (needed for OPENBIS and OPENBIS-DEV output format options).
-h, --help: Displays the help message.
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
java -jar lib-rdf-tool.jar -i TTL -o OPENBIS path/to/schema.ttl -u yourUsername -p http://localhost:8888/openbis/openbis
```

For development environment add the DSS URL:
```bash
java -jar lib-rdf-tool.jar -i TTL -o OPENBIS-DEV path/to/schema.ttl -u yourUsername -p http://localhost:8888/openbis/openbis http://localhost:8889/datastore_server
```

In both cases a username and a password to log in in the openBIS instance are required. 

The password will be inserted in a safe prompt.

