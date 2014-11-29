package org.omg.bpmn.miwg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.omg.bpmn.miwg.AnalysisResultSimpleXmlPojos.Output;
import org.omg.bpmn.miwg.AnalysisResultSimpleXmlPojos.TestResults;
import org.omg.bpmn.miwg.xmlCompare.Variant;
import org.omg.bpmn.miwg.xmlCompare.XmlCompareAnalysisTool;
import org.w3c.dom.Document;

public class XMLCompareTest {

    private static final String TOOL_ID = "Yaoqiang BPMN Editor 2.2.18";
    private static final String TEST_ID = "A.1.0";
    private static final String VARIANT = "roundtrip";

    @Test
    public void testFolderInput() {
        String refFolderPath = "target" + File.separator + "test-suite"
                + File.separator + "Reference";
        String testFolderPath = "target" + File.separator + "test-suite"
                + File.separator + TOOL_ID;
        try {
            String results = XmlCompareAnalysisTool.runXmlCompareTest(refFolderPath,
                    testFolderPath,
                    Variant.roundtrip, null);
            assertTrue(results.contains("data-test=\"A.1.0.bpmn\""));
            assertTrue(results.contains("data-test=\"A.2.0.bpmn\""));
            assertTrue(results.contains("data-test=\"A.3.0.bpmn\""));
            assertTrue(results.contains("data-test=\"A.4.0.bpmn\""));
            assertTrue(results.contains("data-test=\"B.1.0.bpmn\""));
            assertTrue(results.contains("data-test=\"B.2.0.bpmn\""));
            FileUtils.writeStringToFile(new File("target",
                    "testFolderInput.testOutput.html"), results);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getClass() + ":" + e.getMessage());
        }
    }

    @Test
    public void testDomInputs() {
        InputStream bpmnStream = null;
        InputStream compareStream = null;
        try {
            bpmnStream = getClass().getResourceAsStream(
                    "/Reference/" + TEST_ID + ".bpmn");
            assertNotNull(bpmnStream);
            compareStream = getClass().getResourceAsStream(
                    "/" + TOOL_ID + "/" + TEST_ID + "-" + VARIANT + ".bpmn");
            assertNotNull(compareStream);
            Collection<? extends Output> significantDifferences = new XmlCompareAnalysisTool()
                    .analyzeDOM(null, readDom(bpmnStream), readDom(compareStream), null).output;
            System.out.println("Found " + significantDifferences.size()
                    + " diffs.");
            for (Output output : significantDifferences) {
                System.out.println("..." + output.getDescription());
            }

            assertEquals(33, significantDifferences.size());

            TestResults results = new TestResults();
            org.omg.bpmn.miwg.AnalysisResultSimpleXmlPojos.Test test = results.addTool(TOOL_ID)
                    .addTest(TEST_ID, VARIANT);
            test.addAll(significantDifferences);
            final File f = new File(new File(new File("target", "xml-compare"),
                    TOOL_ID), TOOL_ID + "-" + TEST_ID + ".html");
            results.writeResultFile(f);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getClass() + ":" + e.getMessage());
        } finally {
            try {
                bpmnStream.close();
            } catch (Exception e) {
                ;
            }
            try {
                compareStream.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    private Document readDom(InputStream bpmnStream) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(bpmnStream);
    }

}
