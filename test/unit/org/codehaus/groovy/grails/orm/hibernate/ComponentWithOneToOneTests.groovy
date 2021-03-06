package org.codehaus.groovy.grails.orm.hibernate

/**
 * @author Graeme Rocher
 * @since 1.0
 *
 * Created: Nov 19, 2007
 */
class ComponentWithOneToOneTests extends AbstractGrailsHibernateTests {

	protected void onSetUp() {
		gcl.parseClass '''
import grails.persistence.*

@Entity
class Unit {
	String name
	String abbreviation
}

@Entity
class Measurement {
	Unit unit
	BigDecimal value
	Boolean approximation
}

@Entity
class BatchAction {
	Measurement sample
	Measurement sample2
	String name
	static embedded = ['sample','sample2']
}
'''
	}

	void testEmbeddedComponentWithOne2One() {
		def unitClass = ga.getDomainClass("Unit").clazz
		def mClass = ga.getDomainClass("Measurement").clazz
		def bClass = ga.getDomainClass("BatchAction").clazz

		def u = unitClass.newInstance(name:"metres",abbreviation:"m")
		def u2 = unitClass.newInstance(name:"centimetres",abbreviation:"cm")

		u.save()
		u2.save()

		def m1 = mClass.newInstance(value:1.1, unit:u, approximation:true)
		def m2 = mClass.newInstance(value:2.4, unit:u2, approximation:false)

		m1.save()
		m2.save()

		def action = bClass.newInstance(sample:m1, sample2:m2, name:"test")

		action.save()
		session.flush()
		session.clear()

		action = bClass.get(1)
		assertNotNull action

		assertEquals 1.1, action.sample.value
		assertEquals "metres", action.sample.unit.name
		assertEquals 2.4, action.sample2.value
		assertEquals "centimetres", action.sample2.unit.name
	}
}
