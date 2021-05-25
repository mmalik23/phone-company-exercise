import org.scalatest.funsuite.AnyFunSuite
import com.phone.PhoneCompany
import org.scalatest.matchers.should.Matchers


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
}
