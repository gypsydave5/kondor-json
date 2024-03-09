package com.ubertob.kondor.outcome

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ConvenienceMethodsTest {


    @Test
    fun `asOutcome transforms values into Outcomes`() {

        val okStr = "All ok"
        okStr.asOutcome({ contains("ok") }, ::Err).expectSuccess()

        val errStr = "There is an error!"
        errStr.asOutcome({ contains("error").not() }, ::Err).expectFailure()

    }

    @Test
    fun `failIf fails the outcome if a predicate is true`() {
        val error = getUser(100)
            .failIf({ it.name.length > 6 }, { Err("${it.name} is too long!") })
            .expectFailure()

        expectThat(error.msg).isEqualTo("user100 is too long!")
    }

    @Test
    fun `failUnless fails the outcome if a predicate is false`() {
        val error = getUser(100)
            .failUnless({ name.length <= 6 }, { Err("${it.name} is too long!") })
            .expectFailure()

        expectThat(error.msg).isEqualTo("user100 is too long!")
    }


    @Test
    fun `combine successes`() {
        val a = 2.asSuccess()
        val b = 3.asSuccess()

        val combined = a.combine(b)

        expectThat(combined).isEqualTo(Success(Pair(2, 3)))
    }


    @Test
    fun `castOrFail return failure for wrong subcasting`() {
        val person: BaseOutcome<Person> = getUser(12)

        val interested: Outcome<OutcomeError, Interested> =
            person.castOrFail { value -> Err("Cannot cast $value to Interested") }

        val failure = interested.expectFailure()
        expectThat(failure.msg).isEqualTo("Cannot cast User(name=user12, email=12@example.com) to Interested")
    }

}