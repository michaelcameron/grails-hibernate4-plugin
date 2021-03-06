package org.codehaus.groovy.grails.orm.hibernate.validation

import org.codehaus.groovy.grails.orm.hibernate.AbstractGrailsHibernateTests

/**
* @author Graeme Rocher
* @since 1.0
*
* Created: Jan 18, 2008
*/
class CascadingValidationWithInheritanceTests extends AbstractGrailsHibernateTests {

	protected void onSetUp() {
		gcl.parseClass '''
import grails.persistence.*

@Entity
class CascadingValidationWithInheritanceTestsBook {
	String name
	List authors
	static hasMany = [authors: CascadingValidationWithInheritanceTestsAuthor]
	static constraints = {
		name(blank: false)
		authors(validator: {authors ->
			def names = new HashSet()
			names.addAll(authors.name)
			if (names.size() < authors.size()) {
				return 'names.unique'
			}
		})
	}

	String toString() {"Book[$name]"}
}

@Entity
class CascadingValidationWithInheritanceTestsTechBook extends CascadingValidationWithInheritanceTestsBook {

	static hasMany = [subjects: CascadingValidationWithInheritanceTestsTechSubject]
}
@Entity
class CascadingValidationWithInheritanceTestsAuthor {

	String name
}
@Entity
class CascadingValidationWithInheritanceTestsTechSubject {

	String name
}
'''
	}

	void testBooksMustHaveUniqueAuthors() {
		def bookClass = ga.getDomainClass("CascadingValidationWithInheritanceTestsBook").clazz
		def book = bookClass.newInstance(name: 'Spook Country')
		book.addToAuthors(name: 'William Gibson')
		book.addToAuthors(book.authors[0])

		assertFalse book.validate()
		assertTrue book.errors.hasFieldErrors('authors')
		assertEquals(['names.unique'], book.errors.getFieldErrors('authors').code)
	}

	void testTechBookCanHaveAuthorsAndSubjects() {
		def bookClass = ga.getDomainClass("CascadingValidationWithInheritanceTestsBook").clazz
		def techBookClass = ga.getDomainClass("CascadingValidationWithInheritanceTestsTechBook").clazz
		def book = techBookClass.newInstance(name: 'The Definitive Guide to Grails')
		book.addToAuthors(name: 'Graeme Rocher')
		book.addToAuthors(name: 'Guillaume Laforge')
		book.addToAuthors(name: 'Dierk K\u00f6nig')
		book.addToSubjects(name: 'Grails')
		book.addToSubjects(name: 'Groovy')

		assertTrue book.validate()
		assertNotNull book.save(flush: true)
		session.clear()

		book = bookClass.findByName('The Definitive Guide to Grails')
		assertEquals(['Dierk K\u00f6nig', 'Graeme Rocher', 'Guillaume Laforge'], book.authors.name.sort {it})
		assertEquals(['Grails', 'Groovy'], book.subjects.name.sort {it})
	}

	void testTechBooksMustHaveUniqueAuthors() {
		def techBookClass = ga.getDomainClass("CascadingValidationWithInheritanceTestsTechBook").clazz
		def book = techBookClass.newInstance(name: 'Groovy In Action')
		book.addToAuthors(name: 'Dierk K\u00f6nig')
		book.addToAuthors(book.authors[0])
		book.addToSubjects(name: 'Groovy')

		assertFalse 'Validation should have failed as unique constraint on Book.authors is violated', book.validate()
		assertTrue book.errors.hasFieldErrors('authors')
		assertEquals(['names.unique'], book.errors.getFieldErrors('authors').code)
	}
}
