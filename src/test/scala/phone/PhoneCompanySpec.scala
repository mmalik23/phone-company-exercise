import org.scalatest.funsuite.AnyFunSuite
import com.phone.PhoneCompany
import org.scalatest.matchers.should.Matchers
import java.time.Duration
import com.phone.Record
import com.phone.InvalidRecord


class PhoneCompanySpec extends AnyFunSuite with Matchers {
    
    val pns = new PhoneCompany

    test("Can parse call logs file") {
        val read = pns.readFile("calls.log") 
        
        read.take(2) shouldBe List(
            "A 555-333-212 00:02:03",
            "A 555-433-242 00:06:41"
        )
        read.length shouldBe 15
    }

    test("Can parse log record") {
        pns.parseRawLog("lol rofl 00:02:03") shouldBe Right(Record("lol", "rofl", Duration.ofSeconds(123)))
    }

    test("Fail if there are too many rows") {
        pns.parseRawLog("lol rofl 00:02:03 sd") shouldBe Left(InvalidRecord("Too many space delimited columns"))
    }

    test("Fail if there are too few rows") {
        pns.parseRawLog("lol rofl") shouldBe Left(InvalidRecord("Too few space delimited columns"))
        pns.parseRawLog("") shouldBe Left(InvalidRecord("Too few space delimited columns"))
    }

    test("If the time window cannot be parsed") {
        pns.parseRawLog("lol rofl badduration") shouldBe Left(InvalidRecord("Invalid duration: badduration"))
    }

}
