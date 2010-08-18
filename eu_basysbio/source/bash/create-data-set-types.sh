#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Usage: $0 <base url> <user id>"
    exit 1
fi

url=$1/openbis
user=$2
read -s -p "Password: " password

jar=openbis-admin-console/openbis-admin-console.jar

types=`echo METABOLITE_LCMS_ABS \
            METABOLITE_LCMS_REL \
            PHYS_RFR \
            TF_ACTIVITY \
            QUANTIFIED_PEPTIDES \
            PROTEIN_LCMS \
            PROTEIN_2DE_COMBINED_LCMS \
            NIMBLEGEN_RNA_1_COLOR_BSUBT1 \
            NIMBLEGEN_RNA_1_COLOR_BSUBT1_MEDIAN_NORM \
            NIMBLEGEN_RNA_1_COLOR_BSUBT1_QQ_NORM \
            AGILENT_RNA_2_COLOR \
            ABSOLUTE_PROTEIN_NUMBERS_GFP \
            ABSOLUTE_PROTEIN_CONCENTRATION_AQUA_2D \
            2D_GEL_SPOT_VOL \
            PHYS_OD600
            `
for type in $types;do
  java -Dopenbis.type=$type -jar $jar $url create-data-set-type.txt $user $password
done