package org.omg.bpmn.miwg.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.omg.bpmn.miwg.api.AnalysisJob;
import org.omg.bpmn.miwg.api.AnalysisOutput;
import org.omg.bpmn.miwg.api.AnalysisRun;
import org.omg.bpmn.miwg.api.output.dom.ExceptionEntry;
import org.omg.bpmn.miwg.api.output.html.HTMLAnalysisOutputWriter;
import org.omg.bpmn.miwg.api.tools.AnalysisTool;
import org.omg.bpmn.miwg.schema.SchemaAnalysisTool;
import org.omg.bpmn.miwg.util.DOMFactory;
import org.omg.bpmn.miwg.xmlCompare.XmlCompareAnalysisTool;
import org.omg.bpmn.miwg.xpath.XpathAnalysisTool;
import org.w3c.dom.Document;

public class AnalysisFacade {

	public Collection<AnalysisRun> executeAnalysisJobs(
			Collection<AnalysisJob> jobs, String outputFolder) throws Exception {
		Collection<AnalysisRun> runs = new LinkedList<AnalysisRun>();

		for (AnalysisJob job : jobs) {
			AnalysisRun run = executeAnalysisJob(job);
			if (run != null) {
				runs.add(run);
			}
		}
		if (outputFolder != null)
			HTMLAnalysisOutputWriter.writeOverview(outputFolder, runs);

		return runs;
	}

	/**
	 * This is a pseudo tool which is used during the initialization of a job.
	 *
	 */
	private class Init implements AnalysisTool {

		@Override
		public String getName() {
			return "init";
		}
		
	}
	
	public AnalysisRun executeAnalysisJob(AnalysisJob job) throws Exception {
		System.out.println("Executing AnalysisJob '" + job.getName() + "' ...");

		XpathAnalysisTool xpathAnalysisTool = new XpathAnalysisTool();
		SchemaAnalysisTool xsdAnalysisTool = new SchemaAnalysisTool();
		XmlCompareAnalysisTool compareAnalysisTool = new XmlCompareAnalysisTool();

		InputStream referenceInputStream = null;
		InputStream actualInputStream = null;

		AnalysisOutput initOutput = new AnalysisOutput(job, new Init());

		Document actualDom = null;
		Document referenceDom = null;

		AnalysisRun run = new AnalysisRun(job);

		try {
			job.init();
			actualInputStream = job.getActualInput().getInputStream();
			actualDom = DOMFactory.getDocument(actualInputStream);

			if (actualInputStream != null)
				actualInputStream.close();

		} catch (Exception e) {
			initOutput.finding(new ExceptionEntry(
					"Exception during job preparation", e));
			return run;
		}
		finally {
			initOutput.close();
		}

		try {

			// Build the DOMs for the DOMAnalysisTools

			boolean hasReference;
			{
				hasReference = job.hasReference();
				if (hasReference) {
					referenceInputStream = job.getReferenceInput()
							.getInputStream();
					if (referenceInputStream != null) {
						referenceDom = DOMFactory
								.getDocument(referenceInputStream);
						// assert referenceDom != null;
						referenceInputStream.close();
					}
				}
			}

			// Build the InputStream for the XSD tool using the input stream
			actualInputStream = job.getActualInput().getInputStream();

			if (job.isEnableSchema()) {
				AnalysisOutput xsdResult = xsdAnalysisTool.analyzeStream(job,
						null, actualInputStream);
				run.addResult(xsdAnalysisTool, xsdResult);
			}

			if (job.isEnableXmlCompare()) {
				AnalysisOutput compareResult = compareAnalysisTool.analyzeDOM(
						job, referenceDom, actualDom);
				run.addResult(compareAnalysisTool, compareResult);
			}

			if (job.isEnableXpath()) {
				AnalysisOutput xpathResult = xpathAnalysisTool.analyzeDOM(job,
						referenceDom, actualDom);
				run.addResult(xpathAnalysisTool, xpathResult);
			}

			return run;
		} finally {
			try {
				if (referenceInputStream != null)
					referenceInputStream.close();
			} catch (IOException e) {
				;
			}
			try {
				if (actualInputStream != null)
					actualInputStream.close();
			} catch (IOException e) {
				;
			}
		}
	}
}
