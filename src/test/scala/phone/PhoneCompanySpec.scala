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
}