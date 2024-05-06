package ch.ethz.sis.rdf.main;

import ch.ethz.sis.rdf.main.mappers.OntClassObject;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;

import java.util.*;

public class ClassCollector {

    private static void parseRestriction(Restriction restriction, OntClassObject ontClassObject) {
        if (restriction.isCardinalityRestriction()) {
            ontClassObject.addRestriction(restriction.getOnProperty(), restriction.asCardinalityRestriction());
            //System.out.println("   - Cardinality Restriction on " + restriction.getOnProperty().getURI() + " value: " + restriction.asCardinalityRestriction().getCardinality());
        } else if (restriction.isMinCardinalityRestriction()) {
            ontClassObject.addRestriction(restriction.getOnProperty(), restriction.asMinCardinalityRestriction());
            //System.out.println("   - Min Cardinality Restriction on " + restriction.getOnProperty().getURI() + " value: " + restriction.asMinCardinalityRestriction().getMinCardinality());
        } else if (restriction.isMaxCardinalityRestriction()) {
            ontClassObject.addRestriction(restriction.getOnProperty(), restriction.asMaxCardinalityRestriction());
            //System.out.println("   - Max Cardinality Restriction on " + restriction.getOnProperty().getURI() + " value: " + restriction.asMaxCardinalityRestriction().getMaxCardinality());
        } else if (restriction.isSomeValuesFromRestriction()) {
            SomeValuesFromRestriction svfRestriction = restriction.asSomeValuesFromRestriction();
            ontClassObject.addRestriction(restriction.getOnProperty(), restriction.asSomeValuesFromRestriction());
            //System.out.println("   - SomeValuesFrom Restriction on " + svfRestriction.getOnProperty().getURI());
            parseSomeValuesFromRestriction(svfRestriction, ontClassObject);
        } else if (restriction.isHasValueRestriction()) {
            ontClassObject.addRestriction(restriction.getOnProperty(), restriction.asHasValueRestriction());
            //System.out.println("   - HasValue Restriction on " + restriction.getOnProperty().getURI() + " with value: " + restriction.asHasValueRestriction().getHasValue());
        } else if (restriction.isAllValuesFromRestriction()) {
            ontClassObject.addRestriction(restriction.getOnProperty(), restriction.asAllValuesFromRestriction());
            //System.out.println("   - AllValuesFrom Restriction on " + restriction.getOnProperty().getURI() + " with value: " + restriction.asAllValuesFromRestriction().getAllValuesFrom());
        } else {
            throw new ConversionException("Unknown restriction type: " + restriction.getClass().getName());
            //System.out.println("   - Other Restriction: " + restriction);
        }
    }

    private static void parseSomeValuesFromRestriction(SomeValuesFromRestriction svfRestriction, OntClassObject ontClassObject) {
        RDFNode someValuesFrom = svfRestriction.getSomeValuesFrom();
        if (someValuesFrom.isAnon()) { // Directly handle anonymous cases
            OntClass someValuesClass = someValuesFrom.as(OntClass.class);
            // Recursively handle the anonymous class, be it union, intersection, etc.
            parseAnonymousClass(someValuesClass, ontClassObject);
        } else if (someValuesFrom.isURIResource()) {
            // Here, you handle URIResource cases, possibly adding them directly as restrictions
            ontClassObject.addRestriction(svfRestriction.getOnProperty(), svfRestriction.asSomeValuesFromRestriction());
            //System.out.println("     - Class URI Resource: " + someValuesFrom.asResource().getURI());
            // You might want to reflect this in classDetails, depending on your application logic
        } else {
            //System.out.println("     - Non-Class URI Resource (skipped): " + someValuesFrom.asResource().getURI());
            throw new ConversionException("Unknown some values from restriction type: " + svfRestriction.getClass().getName());
        }
    }

    private static void parseIntersection(IntersectionClass intersectionClass, OntClassObject ontClassObject) {
        intersectionClass.listOperands().forEachRemaining(operand -> {
            if (operand.isAnon()) {
                // Now, handle different types of anonymous superclasses
                parseAnonymousClass(operand, ontClassObject);
            } else {
                //System.out.println("Found UNKWON operand [" + operand + "] in intersection: " + intersectionClass);
                throw new ConversionException("Unknown intersection operand: " + operand.getClass().getName());
            }
        });
    }

    private static void parseUnion(UnionClass unionClass, OntClassObject ontClassObject) {
        List<String> operands = new ArrayList<>();
        for(int i=0; i<unionClass.getOperands().size(); i++) {
            //System.out.println(unionClass.getOperands().get(i).as(Restriction.class));
            RDFNode item = unionClass.getOperands().get(i);
            // Check if the current item can be a Restriction and process it
            if (item.canAs(OntClass.class)) {
                OntClass ontClass = item.as(OntClass.class);
                if (item.isAnon()) {
                    //System.out.println("Item at index " + i + " is Anon. " + item);
                    parseAnonymousClass(ontClass, ontClassObject);
                }
                // Now, you have OntClass restriction, and you can process it
                //System.out.println("     - Found OntClass operand ["+ontClass+"] at index " + i + " in union: "+unionClass);
                operands.add(ontClass.getURI());
            } else  {
                //System.out.println("     - Found not-OntClass operand [" + item + "] at index " +i+ " in union: " + unionClass);
                operands.add(item.toString());
            }
        }
        ontClassObject.addUnion(unionClass, operands);
    }

    private static void parseAnonymousClass(OntClass anonClass, OntClassObject ontClassObject) {
        //TODO implement missing classes
        if (anonClass.isRestriction()) {
            Restriction restriction = anonClass.asRestriction();
            //System.out.println("- Restriction: " + restriction);
            parseRestriction(restriction, ontClassObject);
        } else if (anonClass.isUnionClass()) {
            UnionClass unionClass = anonClass.asUnionClass();
            //System.out.println("- Union Of: " + unionClass.getOperands().size());
            parseUnion(unionClass, ontClassObject);
        } else if (anonClass.isIntersectionClass()) {
            IntersectionClass intersectionClass = anonClass.asIntersectionClass();
            //System.out.println("- Intersection Of: " + intersectionClass);
            parseIntersection(intersectionClass, ontClassObject);
        } else if (anonClass.isComplementClass()) {
            ComplementClass complementClass = anonClass.asComplementClass();
            //System.out.println("- Complemented Of: " + complementClass);
            throw new ConversionException("Complement class [" + complementClass + "] is not implemented!");
        } else if (anonClass.isEnumeratedClass()) {
            EnumeratedClass enumeratedClass = anonClass.asEnumeratedClass();
            //System.out.println("- Enumerated: " + enumeratedClass);
            throw new ConversionException("Enumerated class [" + enumeratedClass + "] is not implemented!");
        }else {
            //System.out.println("- Anonymous Superclass (Complex Class Expression): " + anonClass);
            throw new ConversionException("Anonymous Superclass (Complex Class Expression) ["+anonClass+"] is not implemented!");
            // Handle other complex expressions as needed
        }
    }

    public static Map<OntClass, OntClassObject> collectClassDetails(OntModel model){
        Map<OntClass, OntClassObject> classDetailsMap = new HashMap<>();

        model.listClasses().forEachRemaining(cls -> {
            if (!cls.isAnon()) { // Esclude anonymous classes for intersectionOf
                classDetailsMap.put(cls, new OntClassObject(cls));
            }
        });

        classDetailsMap.forEach((cls, classDetail) -> {
            //System.out.println("*** Class: " + cls);
            cls.listSuperClasses().forEachRemaining((superClass)-> {
                // Check if the superclass is an anonymous class
                if (superClass.isAnon()) {
                    // Now, handle different types of anonymous superclasses
                    parseAnonymousClass(superClass, classDetail);
                } else {
                    //System.out.println("* Not-Anon superCLass: " + superClass);
                    classDetail.setSuperClass(superClass);
                }
            });
        });

        return classDetailsMap;
    }

   /* private static void processDomain(RDFNode domain, OntProperty property, Map<OntClass, ClassDetails> classDetailsMap, OntModel model) {
        if (domain.isResource()) {
            Resource res = domain.asResource();
            // Check if the resource is of type owl:Class before casting
            if (model.contains(res, RDF.type, OWL.Class)) {
                OntClass ontClass = res.as(OntClass.class);
                ClassDetails details = classDetailsMap.getOrDefault(ontClass, new ClassDetails(ontClass));
                details.addProperty(property);
                classDetailsMap.putIfAbsent(ontClass, details);
            }
            *//*if (res.canAs(OntClass.class)) {
                OntClass ontClass = res.as(OntClass.class);
                ClassDetails details = classDetailsMap.get(ontClass);
                if (details != null) {
                    details.addProperty(property);
                }
            }*//*
        }
    }

    private static void processInheritance(OntClass subCls, OntClass superCls, Map<OntClass, ClassDetails> classDetailsMap) {
        ClassDetails superClassDetails = classDetailsMap.get(superCls);
        ClassDetails subClassDetails = classDetailsMap.get(subCls);

        if (superClassDetails != null && subClassDetails != null) {
            // Copy all properties from superClassDetails to subClassDetails, avoiding duplicates
            for (OntProperty superProperty : superClassDetails.properties) {
                if (!subClassDetails.properties.contains(superProperty)) {
                    subClassDetails.addProperty(superProperty);
                }
            }
        }
    }*/
}
