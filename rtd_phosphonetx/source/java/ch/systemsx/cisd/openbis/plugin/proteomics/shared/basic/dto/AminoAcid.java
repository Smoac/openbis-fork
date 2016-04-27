/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto;

/**
 * Amino acid symbols and names.
 *
 * @author Franz-Josef Elmer
 */
public enum AminoAcid
{
    A("Alanine", "Ala"),
    R("Arginine", "Arg"),
    N("Asparagine", "Asn"),
    D("Aspartic acid", "Asp"),
    C("Cysteine", "Cys"),
    E("Glutamic acid", "Glu"),
    Q("Glutamine", "Gln"),
    G("Glycine", "Gly"),
    H("Histidine", "His"),
    I("Isoleucine", "Ile"),
    L("Leucine", "Leu"),
    K("Lysine", "Lys"),
    M("Methionine", "Met"),
    F("Phenylalanine", "Phe"),
    P("Proline", "Pro"),
    S("Serine", "Ser"),
    T("Threonine", "Thr"),
    W("Tryptophan", "Trp"),
    Y("Tyrosine", "Tyr"),
    V("Valine", "Val");

    private final String name;

    private final String longSymbol;

    private AminoAcid(String name, String longSymbol)
    {
        this.name = name;
        this.longSymbol = longSymbol;
    }

    public String getName()
    {
        return name;
    }

    public String getLongSymbol()
    {
        return longSymbol;
    }

}