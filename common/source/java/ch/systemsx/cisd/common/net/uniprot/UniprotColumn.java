/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.net.uniprot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enum of all available columns in the Uniprot database.
 * 
 * @author Bernd Rinn
 */
public enum UniprotColumn
{
    CITATION("citation", "PubMed ID"), COMMENTS("comments", "Comments"), DATABASE("database",
            "Database"), DISEASE("comment(disease)", "Comment (disease)", "DISEASE: "), DOMAINS(
            "domains", "Domains"), DOMAIN("domain", "Domain"), EC("ec", "EC numbers"), ID("id",
            "Accession"), ENTRY_NAME("entry%20name", "Entry name"), EXISTENCE("existence",
            "Protein existence"), FAMILIES("families", "Protein family"), FEATURES("features",
            "Features"), FUNCTION("comment(function)", "Comment (function)", "FUNCTION: "), GENES(
            "genes", "Gene names"), GO("go", "Gene Ontology"), GO_ID("go-id", "Gene Ontology ID"),
    INTERPRO("interpro", "InterPro"), INTERACTOR("interactor", "Interacts with"), KEYWORDS(
            "keywords", "Keywords"), LAST_MODIFIED("last-modified", "Date of last modification"),
    LENGTH("length", "Length"), ORGANISM("organism", "Organism"), ORGANISM_ID("organism-id",
            "Organism ID"), PATHWAY("pathway", "Pathway"), PROTEIN_NAMES("protein%20names",
            "Protein names"), SCORE("score", "Score"), SEQUENCE("sequence", "Sequence"),
    SEQUENCE_SIMILARITIES("comment(similarity)", "Comment (similarity)", "SIMILARITY: "), STATUS(
            "reviewed", "Status"), SUBCELLULAR_LOCATIONS("subcellular%20locations",
            "Subcellular locations"), SUBUNIT_STRUCTURE("comment(subunit)", "Comment (subunit)",
            "SUBUNIT: "), TISSUE_SPECIFITY("comment(tissue_specificity)",
            "Comment (tissue_specificity)", "TISSUE SPECIFICITY: "), TAXON("taxon", "Taxon"),
    THREED("3d", "3D"), VERSION("version", "Version"),
    VIRUS_HOSTS("virus%20hosts", "Virus hosts\n");

    /**
     * The map from the column header in the Uniprot result set (in lower case characters) and the
     * {@link UniprotColumn}.
     */
    static final Map<String, UniprotColumn> columnMap;

    static
    {
        final Map<String, UniprotColumn> myColumnMap = new HashMap<String, UniprotColumn>();
        for (UniprotColumn col : values())
        {
            myColumnMap.put(col.getColumnHeader().toLowerCase(), col);
        }
        columnMap = Collections.unmodifiableMap(myColumnMap);
    }

    private final String fieldName;

    private final String columnHeader;

    private final String prefix;

    UniprotColumn(String fieldName, String columnHeader)
    {
        this.fieldName = fieldName;
        this.columnHeader = columnHeader;
        this.prefix = null;
    }

    UniprotColumn(String fieldName, String columnHeader, String prefix)
    {
        this.fieldName = fieldName;
        this.columnHeader = columnHeader;
        this.prefix = prefix;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getColumnHeader()
    {
        return columnHeader;
    }

    public String getPrefix()
    {
        return prefix;
    }
}