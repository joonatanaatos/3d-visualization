import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExampleSpec extends AnyFlatSpec with Matchers {
  "true" should "be true" in {
    true should be(true)
  }
}
