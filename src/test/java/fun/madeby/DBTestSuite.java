package fun.madeby;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Created by Gra_m on 2022 08 02
 * https://howtodoinjava.com/junit5/junit5-test-suites-examples/
 *
 * testImplementation 'org.junit.platform:junit-platform-suite-engine:1.9.0'
 */

@SelectPackages({"fun.madeby.generic_server",
		"fun.madeby.specific_server"})

//@IncludeTags("production"); /@Exclude
//@SelectClasses
//@IncludeClassNamePatterns
//@ExcludeClassNamePatterns
// @Include/ExcludePackages
@Suite
@SuiteDisplayName("How sweet.. My first suite.")
public class DBTestSuite {

}
