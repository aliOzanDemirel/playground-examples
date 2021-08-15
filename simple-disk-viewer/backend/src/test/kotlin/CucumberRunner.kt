import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["classpath:features"],
    plugin = ["pretty", "html:build/cucumber"],
    glue = ["steps"]
)
class CucumberRunner
