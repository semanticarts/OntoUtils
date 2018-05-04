/**
 * 
 */
package com.semanticarts.onto.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * @author thill
 *
 */
public class ExtractDefsCli {

    static UsefulHashSet uhs;
    static PrefixHashMap phm;

    static void printNodes(PrintWriter outputWriter, NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String vocabDef = eElement.getAttribute("rdf:about");
                if (vocabDef != null) {
                    if (phm != null) {
                        vocabDef = phm.replacePrefix(vocabDef);
                    }
                    String Useful = "";
                    if (uhs != null) {
                        if (uhs.isUseful(vocabDef))
                            Useful = "x";
                    }

                    // points from the vocabulary entity to the ontology entity
                    String isDefinedBy = "";

                    String def = ""; // skos:definition text
                    String label = ""; // skos:prefLabel or rdfs:label

                    // all skos:altLabels plus any extra skos:prefLabel or rdfs:label
                    LinkedList<String> AltLabels = new LinkedList<String>();

                    NodeList nl = eElement.getChildNodes();
                    for (int i = 0; nl.item(i) != null; ++i) {
                        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element eNode = (Element) nl.item(i);
                            if (eNode.getNodeName() == "rdfs:isDefinedBy") {
                                isDefinedBy = eNode.getAttribute("rdf:resource");
                                if (phm != null) {
                                    isDefinedBy = phm.replacePrefix(isDefinedBy);
                                }
                            } else if (eNode.getNodeName() == "skos:definition") {
                                def = eNode.getTextContent();
                                // double-up double quotes for Excel
                                def = def.replaceAll("\"", "\"\"");
                            } else if (
                                    eNode.getNodeName() == "rdfs:label" ||
                                    eNode.getNodeName() == "skos:prefLabel"
                                ) {
                                if (label.equals("")) {
                                    label = eNode.getTextContent();
                                } else {
                                    AltLabels.add(eNode.getTextContent());
                                }
                            } else if (eNode.getNodeName() == "skos:altLabel") {
                                AltLabels.add(eNode.getTextContent());
                            }
                        }
                    }
                    if (label.trim() != "" || def.trim() != "") {
                        outputWriter.println(
                            "\"" + vocabDef + "\"\t\"" + label + "\"\t\"" + isDefinedBy + "\"\t\"" +
                            Useful + "\"\t\"" + def + "\""
                        );
                    }
                    for (String s : AltLabels) {
                        outputWriter.println(
                            "\"" + vocabDef + "\"\t\"" + s + "\"\t\"" + "" + "\"\t\"" + Useful +
                            "\"\t\"" + "" + "\""
                        );
                    }
                }
            }
        }
    }

    /**
     * This program will read one or more RDF/XML files and extract labels and
     * definitions into a tab-delimited output file.
     * 
     * Entities of the following types are considered: owl:Class owl:ObjectProperty
     * skos:Concept
     *
     * For any of the above-listed entities, all of the following properties are
     * extracted:
     * 
     * the entity name, with prefix or as full IRI (see -p argument below)
     * rdfs:label, skos:prefLabel, skos:altLabel--all that are present
     * skos:definition--last one only rdfs:isDefinedBy--last one only
     *
     * The output file will be tab-delimited, with all strings enclosed in double
     * quotation marks, so that it is suitable for import into Excel. Enclosed
     * double-quotation marks will be doubled.
     * 
     * Output columns are as follows: entity name, with prefix or as full IRI label
     * entity that is the object of rdfs:isDefinedBy "x" if the entity is considered
     * useful (see -u option below) definition
     *
     * If an entity has more than one label, they will all be listed on separate
     * lines with the entity name repeated. The first skos:prefLabel or rdfs:label
     * will be listed first, followed by all remaining skos:prefLabel, rdfs:label,
     * and skos:altLabel values.
     *
     * @param args
     *            command-line arguments, as follows: -o output file pathname;
     *            default is stdout -u (optional) list of entities to be marked as
     *            useful -p (optional) list of tab-delimited pairs of namespace
     *            prefix and IRI <list of input file pathnames> (optional); default
     *            is stdin; filename of "-" is stdin
     * 
     *            By default, namespace references in the input file are expanded to
     *            IRIs. If a namespace prefix file is provided and an IRI is found
     *            in that file, it will be replaced in the output with the matching
     *            prefix.
     */
    public static void main(String[] args) {

        // Parse arguments.
        int i = 0;
        String arg;
        int nArgErrs = 0;
        PrintWriter outputWriter = new PrintWriter(System.out); // default output goes to stdout

        while (i < args.length && args[i].startsWith("-") && !args[i].equals("-")) {
            arg = args[i++];

            if (arg.equals("-o")) {
                if (i < args.length) {
                    String outputFileName = args[i++];
                    try {
                        File outputFile = new File(outputFileName);
                        outputWriter = new PrintWriter(outputFile);
                    } catch (FileNotFoundException e) {
                        System.err.println("Unable to open -o file" + outputFileName + ":");
                        System.err.println(e.getMessage());
                        ++nArgErrs;
                    }
                } else {
                    System.err.println("-o requires a filename");
                    ++nArgErrs;
                }
            } else if (arg.equals("-u")) {
                if (i < args.length) {
                    String usefulFileName = args[i++];
                    try {
                        File usefulFile = new File(usefulFileName);
                        BufferedReader br = new BufferedReader(new FileReader(usefulFile));
                        uhs = new UsefulHashSet(br);
                    } catch (IOException e) {
                        System.err.println("Unable to open -u file " + usefulFileName + ":");
                        System.err.println(e.getMessage());
                        ++nArgErrs;
                    }
                } else {
                    System.err.println("-u requires a filename");
                    ++nArgErrs;
                }
            } else if (arg.equals("-p")) {
                if (i < args.length) {
                    String prefixFileName = args[i++];
                    try {
                        File prefixFile = new File(prefixFileName);
                        BufferedReader br = new BufferedReader(new FileReader(prefixFile));
                        phm = new PrefixHashMap(br);
                    } catch (IOException e) {
                        System.err.println("Unable to open -p file " + prefixFileName + ":");
                        System.err.println(e.getMessage());
                        ++nArgErrs;
                    }
                } else {
                    System.err.println("-p requires a filename");
                    ++nArgErrs;
                }
            } else if (arg.equals("-h")) {
                ++nArgErrs; // Do nothing but print the help message. (++nArgErrs is a bit of a lie.)
            }
        }

        if (nArgErrs > 0) {
            System.err.println("Usage\n" + "-o output file pathname; default is stdout\r\n"
                    + "-u (optional) list of entities to be marked as useful\r\n"
                    + "-p (optional) list of tab-delimited pairs of namespace prefix and IRI\r\n"
                    + "<list of input file pathnames> (optional); default is stdin; filename of \"-\" is stdin\r\n");
            return;
        }

        try {
            outputWriter.println(
                "\"Vocabulary Entity\"\t\"Label\"\t\"Ontology Entity\"\t\"Useful?\"\t\"Definition\""
                );

            if (i == args.length) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setNamespaceAware(true); // .setExpandEntities(false); does nothing, unfortunately
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(System.in);
                doc.getDocumentElement().normalize();
                NodeList nList;

                nList = doc.getElementsByTagName("skos:Concept");
                printNodes(outputWriter, nList);
                nList = doc.getElementsByTagName("owl:ObjectProperty");
                printNodes(outputWriter, nList);
                nList = doc.getElementsByTagName("owl:Class");
                printNodes(outputWriter, nList);
            } else {
                InputStream is;
                while (i < args.length) {
                    arg = args[i++];
                    if (arg.equals("-")) {
                        is = System.in;
                    } else {
                        try {
                            is = new FileInputStream(arg);
                        } catch (FileNotFoundException e) {
                            System.err.println("Unable to open input file " + arg + ":");
                            System.err.println(e.getMessage());
                            continue;
                        }

                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        dbFactory.setNamespaceAware(true); // .setExpandEntities(false); does nothing, unfortunately
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(is);
                        doc.getDocumentElement().normalize();
                        NodeList nList;

                        nList = doc.getElementsByTagName("skos:Concept");
                        printNodes(outputWriter, nList);
                        nList = doc.getElementsByTagName("owl:ObjectProperty");
                        printNodes(outputWriter, nList);
                        nList = doc.getElementsByTagName("owl:Class");
                        printNodes(outputWriter, nList);
                    }
                }
            }

            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
