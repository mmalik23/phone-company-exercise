package com.phone
class PhoneCompany {

    def readFile(resource: String): List[String] = scala.io.Source.fromResource(resource).getLines().toList
}