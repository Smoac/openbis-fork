package ch.ethz.sis.rdf.main;

import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.PropertyTupleRDF;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;

public class ClassCollector {

    private static void parseRestriction(Restriction restriction, OntClassObject ontClassObject) {
        try {
            if (restriction.isCardinalityRestriction()) {
                addRestrictionSafely(ontClassObject, restriction.getOnProperty(), restriction.asCardinalityRestriction());
            } else if (restriction.isMinCardinalityRestriction()) {
                addRestrictionSafely(ontClassObject, restriction.getOnProperty(), restriction.asMinCardinalityRestriction());
            } else if (restriction.isMaxCardinalityRestriction()) {
                addRestrictionSafely(ontClassObject, restriction.getOnProperty(), restriction.asMaxCardinalityRestriction());
            } else if (restriction.isSomeValuesFromRestriction()) {
                SomeValuesFromRestriction svfRestriction = restriction.asSomeValuesFromRestriction();
                addRestrictionSafely(ontClassObject, restriction.getOnProperty(), svfRestriction);
                parseSomeValuesFromRestriction(svfRestriction, ontClassObject);
            } else if (restriction.isHasValueRestriction()) {
                addRestrictionSafely(ontClassObject, restriction.getOnProperty(), restriction.asHasValueRestriction());
            } else if (restriction.isAllValuesFromRestriction()) {
                addRestrictionSafely(ontClassObject, restriction.getOnProperty(), restriction.asAllValuesFromRestriction());
            } else {
                throw new ConversionException("Unknown restriction type: " + restriction.getClass().getName());
            }
        } catch (ConversionException e) {
            System.err.println("ConversionException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    private static void addRestrictionSafely(OntClassObject ontClassObject, Property onProperty, Restriction restriction) {
        if (onProperty.canAs(OntProperty.class)) {
            //ontClassObject.addRestriction(onProperty.as(OntProperty.class), restriction);
            ontClassObject.addRestriction(onProperty.getURI(), restriction);
        } else {
            System.err.println("Cannot convert node " + onProperty + " to OntProperty");
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
            //ontClassObject.addRestriction(svfRestriction.getOnProperty(), svfRestriction.asSomeValuesFromRestriction());
            ontClassObject.addRestriction(svfRestriction.getOnProperty().getURI(), svfRestriction.asSomeValuesFromRestriction());
            //System.out.println("     - Class URI Resource: " + someValuesFrom.asResource().getURI());
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

    private static void collectInstances(OntClass cls, OntClassObject classDetail){
        cls.listInstances().forEach(instance -> {
            if (instance.isAnon()) {
                classDetail.instances.add(instance.getURI());
            }
        });
    }

    private static void collectProperties(OntModel model, OntClass cls, OntClassObject classDetail) {
        if (!cls.isAnon()) { // Exclude anonymous classes for intersectionOf
            // Find all properties where the class is the domain
            StmtIterator propIterator = model.listStatements(null, RDFS.domain, (RDFNode) null);
            while (propIterator.hasNext()) {
                Statement stmt = propIterator.nextStatement();
                Property prop = stmt.getSubject().as(Property.class);
                Resource domain = stmt.getObject().asResource();

                if (isClassInDomain(cls, domain)) {
                    // Find the range of the property
                    StmtIterator rangeIterator = model.listStatements(prop, RDFS.range, (RDFNode) null);
                    while (rangeIterator.hasNext()) {
                        Statement rangeStmt = rangeIterator.nextStatement();
                        Resource range = rangeStmt.getObject().asResource();
                        if (range.canAs(UnionClass.class)) {
                            // If the range is a union class, process each operand
                            classDetail.propertyTuples.add(new PropertyTupleRDF(prop.getURI(), "SAMPLE"));
                            /*// If the range is a union class, process each operand
                            // To add multi ranged properties like hasCode [CODE] and hasCode [TERMINOLOGY]
                            UnionClass unionRange = range.as(UnionClass.class);
                            unionRange.listOperands().forEachRemaining(operand -> {
                                if (operand.isURIResource()) {
                                    classDetail.propertyTuples.add(new PropertyTupleRDF(prop.getURI(), operand.getLocalName().toUpperCase(Locale.ROOT)));
                                } else {
                                    classDetail.propertyTuples.add(new PropertyTupleRDF(prop.getURI(), "UNKNOWN"));
                                }
                            });*/
                        } else if (range.isURIResource()) {
                            // If the range is a single URI resource
                            classDetail.propertyTuples.add(new PropertyTupleRDF(prop.getURI(), "SAMPLE:"+range.getLocalName().toUpperCase(Locale.ROOT)));
                        } else {
                            classDetail.propertyTuples.add(new PropertyTupleRDF(prop.getURI(), "UNKNOWN"));
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the given class is included in the domain of a property.
     *
     * @param cls the class to check
     * @param domain the domain of the property
     * @return true if the class is in the domain, false otherwise
     */
    private static boolean isClassInDomain(OntClass cls, Resource domain) {
        if (domain.equals(cls)) {
            return true;
        } else if (domain.canAs(UnionClass.class)) {
            UnionClass unionClass = domain.as(UnionClass.class);
            return unionClass.listOperands().toList().contains(cls);
        }
        return false;
    }

    public static Map<String, OntClassObject> collectClassDetails(OntModel model){
        Map<OntClass, OntClassObject> classDetailsMap = new HashMap<>();

        model.listClasses().forEachRemaining(cls -> {
            if (!cls.isAnon()) { // Esclude anonymous classes for intersectionOf
                classDetailsMap.put(cls, new OntClassObject(cls));
            }
        });

        classDetailsMap.forEach((cls, classDetail) -> {
            collectProperties(model, cls, classDetail);
            collectInstances(cls, classDetail);
            //System.out.println("*** Class: " + cls);
            cls.listSuperClasses().forEachRemaining((superClass) -> {
                try {
                    // Check if the superclass is an anonymous class
                    if (superClass.isAnon()) {
                        // Now, handle different types of anonymous superclasses
                        parseAnonymousClass(superClass.as(OntClass.class), classDetail);
                    } else {
                        // Check if the superClass can be cast to OntClass
                        if (superClass.canAs(OntClass.class)) {
                            OntClass superOntClass = superClass.as(OntClass.class);
                            //System.out.println("* Not-Anon superClass: " + superOntClass);
                            classDetail.setSuperClass(superOntClass);
                        } else {
                            System.err.println("Cannot convert node " + superClass + " to OntClass");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error handling superClass " + superClass + ": " + e.getMessage());
                }
            });
        });

        Map<String, OntClassObject> classDetailsMapS = new HashMap<>();
        classDetailsMap.forEach((key, value) -> classDetailsMapS.put(key.getURI(), value));

        return classDetailsMapS;
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
