package org.codehaus.groovy.grails.orm.hibernate.validation

import org.codehaus.groovy.grails.orm.hibernate.AbstractGrailsHibernateTests

/**
 * @author Graeme Rocher
 * @since 1.0
 *
 * Created: Jan 10, 2008
 */
class InheritedConstraintsTests extends AbstractGrailsHibernateTests {

	protected void onSetUp() {
		gcl.parseClass '''
class InheritedConstraintsTestsParent {
	Long id
	Long version
	static constraints = {
		prop1(nullable: false, blank: false)
	}

	String prop1
}
class InheritedConstraintsTestsChild extends InheritedConstraintsTestsParent {
	Long id
	Long version

	static constraints = {
		other(nullable: true)
	}

	InheritedConstraintsTestsParentOther other
}
class InheritedConstraintsTestsParentOther {
	Long id
	Long version

	String prop
}
'''
	}

	void testSubClassConstraints() {
		def child = ga.getDomainClass("InheritedConstraintsTestsChild").newInstance()
		child.prop1 = "Property 1"

		// should pass as child constraint defines nullable field other
		assertTrue child.validate()
		assertEquals 0, child.errors.errorCount
	}
}
