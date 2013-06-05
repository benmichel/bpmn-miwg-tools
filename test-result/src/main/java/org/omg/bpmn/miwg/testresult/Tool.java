package org.omg.bpmn.miwg.testresult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class Tool {

	/* Fields */
	@Attribute(name = "class", required = true)
	private String clazz = Tool.class.getSimpleName().toLowerCase();
	
	@Element(name = "h2", required = true)
	private String name;

	@ElementList(inline = true, required = true)
	private List<Test> tests;

	/* Constructors */

	public Tool() {
	}

	/**
	 * Constructor setting the name property (e.g. A.1.0-roundtrip) and adds a
	 * collection of test elements
	 * 
	 * @param name
	 */
	public Tool(String name, Collection<Test> tests) {
		this.setName(name);
		this.addTests(tests);
	}

	/* Business methods */

	/**
	 * Adds a test element.
	 * 
	 * @param test
	 */
	public void addTest(Test test) {
		this.getTests().add(test);
	}

	/**
	 * Adds all entries in tests.
	 * 
	 * @param tests
	 */
	public void addTests(Collection<Test> tests) {
		this.getTests().addAll(tests);
	}

	/**
	 * Returns a shadow copy of all test results.
	 * 
	 * @return {@link List} of {@link Test}
	 */
	public List<Test> getTestsCopy() {
		List<Test> t = new LinkedList<Test>();
		t.addAll(getTests());
		return t;
	}

	/* Getters & Setters */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private List<Test> getTests() {
		if (this.tests == null) {
			this.tests = new LinkedList<Test>();
		}
		return this.tests;
	}
}
