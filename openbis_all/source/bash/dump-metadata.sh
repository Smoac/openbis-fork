#!/bin/bash

#
# dump-metadata.sh
#
# Dumps the metadata tables from an openBIS installation. The DB to dump and the file to save the
# the dump to are the required arguments for this script
#


DB=$1
FILE=$2

TABLES="-t controlled_vocabularies -t controlled_vocabulary_id_seq -t controlled_vocabulary_terms -t cvte_id_seq"
TABLES+=" -t data_set_types -t data_set_type_id_seq -t dstpt_id_seq -t data_set_type_property_types"
TABLES+=" -t experiment_types -t experiment_type_id_seq -t experiment_type_property_types -t etpt_id_seq"
TABLES+=" -t material_types -t material_type_id_seq -t material_type_property_types -t mtpt_id_seq"
TABLES+=" -t material_properties -t material_property_id_seq -t materials -t material_id_seq"
TABLES+=" -t property_types -t property_type_id_seq"

pg_dump --host localhost --port 5432 --username "postgres" --format plain --data-only --verbose --file $FILE $TABLES $DB

