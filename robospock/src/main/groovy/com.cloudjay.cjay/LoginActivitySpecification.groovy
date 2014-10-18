package com.cloudjay.cjay

import com.cloudjay.cjay.activity.LoginActivity_
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class LoginActivitySpecification extends RoboSpecification {

	LoginActivity_ loginActivity = Robolectric.buildActivity(LoginActivity_).create().get()

//	def "should inflate views" () {
//		expect:
//		loginActivity.btnLogin
//		loginActivity.etEmail
//		loginActivity.etPassword
//	}
}