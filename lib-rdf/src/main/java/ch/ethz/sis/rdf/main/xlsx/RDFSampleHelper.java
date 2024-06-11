package ch.ethz.sis.rdf.main.xlsx;

public class RDFSampleHelper {
    public enum Attribute { // implements IAttribute {
        $("$", false),
        Identifier("Identifier", false),
        Code("Code", false),
        Space("Space", false),
        Project("Project", false),
        Experiment("Experiment", false),
        //AutoGenerateCode("Auto generate code", false),
        Parents("Parents", false),
        Children("Children", false),
        Name("Name", false);

        private final String headerName;

        private final boolean mandatory;

        Attribute(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }
}
