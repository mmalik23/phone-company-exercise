package com.phone

import scala.concurrent.duration._
import java.time.LocalTime
import scala.util.Try
import scala.util.Failure
import scala.util.Success

case class Record(customerId: String, number: String, durations: FiniteDuration)

final case class InvalidRecord(msg: String) extends Throwable
class PhoneCompany {

    def readFile(resource: String): List[String] = scala.io.Source.fromResource(resource).getLines().toList

    def parseRawLog(record: String): Either[InvalidRecord, Record] = {

        def toDuration(duration: String) = Try {
            LocalTime.parse(duration)
        } match {
            case Failure(_) => Left(InvalidRecord(s"Invalid duration: $duration"))
            case Success(time) => Right(time.toSecondOfDay().seconds)
        }
        
        record.split(" ") match {
            case Array(a, b, c) => toDuration(c).map(Record(a, b, _))
            case a if a.length > 3 => Left(InvalidRecord("Too many space delimited columns"))
            case _ => Left(InvalidRecord("Too few space delimited columns"))
        }
    }
}