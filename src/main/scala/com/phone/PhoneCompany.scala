package com.phone

import scala.concurrent.duration._
import java.time.LocalTime
import scala.util.{ Try, Failure, Success }

case class Record(customerId: String, number: String, durations: FiniteDuration)
case class HundrethOfAPence(value: Long) {
    def add(second: HundrethOfAPence) = HundrethOfAPence(second.value + value)
}

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

   def durationToCharge(duration: FiniteDuration): HundrethOfAPence = {
    val threeMinutes = 3.minutes
    if (duration <= threeMinutes) HundrethOfAPence(duration.toSeconds * 5)
    else HundrethOfAPence(
        threeMinutes.toSeconds * 5 + duration.minus(threeMinutes).toSeconds * 3
    )
   }

   def calculateCostPerCustomer(records: List[Record]): Map[String, HundrethOfAPence] = records
    .groupBy(_.customerId)
    .view
    .mapValues(records => 
        records
            .groupBy(_.number)
            .values
            .map(recordsByNumberCalled => 
                recordsByNumberCalled.map( r => durationToCharge(r.durations))
                .toVector
                .fold(HundrethOfAPence(0))(_.add(_))   
             )
            .toList
            .sortBy(_.value)
            .dropRight(1)
            .fold(HundrethOfAPence(0))(_.add(_))   
  
    ).toMap
}