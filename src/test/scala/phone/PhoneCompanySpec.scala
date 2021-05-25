import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.duration._
import com.phone._


class PhoneCompanySpec extends AnyFreeSpec with Matchers {
    
    val pns = new PhoneCompany

    "readFile" - {
        "Can parse call logs file" in {

            val read = pns.readFile("calls.log") 
        
            read.take(2) shouldBe List(
                "A 555-333-212 00:02:03",
                "A 555-433-242 00:06:41"
            )
            read.length shouldBe 15

        }
    }

    "parseRawLog" - {
       "Can parse log record" in {
            pns.parseRawLog("lol rofl 00:02:03") shouldBe Right(Record("lol", "rofl", 123.seconds))
        }

       "Fail if there are too many rows" in {
            pns.parseRawLog("lol rofl 00:02:03 sd") shouldBe Left(InvalidRecord("Too many space delimited columns"))
        }

       "Fail if there are too few rows" in {
            pns.parseRawLog("lol rofl") shouldBe Left(InvalidRecord("Too few space delimited columns"))
            pns.parseRawLog("") shouldBe Left(InvalidRecord("Too few space delimited columns"))
        }

       "If the time window cannot be parsed" in {
            pns.parseRawLog("lol rofl badduration") shouldBe Left(InvalidRecord("Invalid duration: badduration"))
        }
    }

    "durationToCharge" - {
        "If duration <= 3m charge is fixed 0.05p per pence " in {
            pns.durationToCharge(3.minutes) shouldBe HundrethOfAPence(5 * 3 * 60)
        }

        "If duration > 3m charge is fixed 0.05p per pence for the first three minutes then 0.03p for rest " in {
            pns.durationToCharge(3.minutes.plus(1.second)) shouldBe HundrethOfAPence(5 * 3 * 60 + 3)
        }

        "When the duration is zero return 0 price" in {
            pns.durationToCharge(0.seconds) shouldBe HundrethOfAPence(0)
        }
    }

    "calculateCostPerCustomer" - {

        val bob = "bob"

        val wendy = "wendy"
        val lauren = "lauren"

        "If there is only one record then the cost is zero because of promotion" in {
            pns.calculateCostPerCustomer(List(
                Record(bob, wendy, 1.minutes)
            )) shouldBe Map(bob -> HundrethOfAPence(0))
        }

        "If there are two records for the same number then the cost is zero" in {
            pns.calculateCostPerCustomer(List(
                Record(bob, wendy, 1.minutes),
                Record(bob, wendy, 1.minutes)
            )) shouldBe Map(bob -> HundrethOfAPence(0))
        }

        "If there are calls to multiple numbers remove the one which cost the most" in {
            val firstCall = 3.minutes
            val secondCall = 2.seconds
            val thirdCall =  firstCall.plus(3.second)
            pns.calculateCostPerCustomer(List(
                Record(bob, wendy, firstCall),
                Record(bob, wendy, secondCall),
                Record(bob, lauren, firstCall.plus(3.second)),
            )) shouldBe Map(bob -> HundrethOfAPence(909))
        }

        "If a customer has two numbers the same amount of time, take one of them out of the calculation" in {
            val call = 3.minutes
            pns.calculateCostPerCustomer(List(
                Record(bob, lauren, call),
                Record(bob, wendy, call)
            )) shouldBe Map(bob -> HundrethOfAPence(900))
        }

        "Finds the total for each customer" in {
            pns.calculateCostPerCustomer(List(
                Record(bob, wendy, 5.second),
                Record(bob, lauren, 2.second),
                Record(lauren, wendy, 1.second),
                Record(lauren, bob, 3.second),
            )) shouldBe Map(bob -> HundrethOfAPence(10), lauren -> HundrethOfAPence(5))
        }
    }

}