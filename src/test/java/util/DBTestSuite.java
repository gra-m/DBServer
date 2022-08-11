package util;

import org.junit.platform.suite.api.*;

/**
 * Created by Gra_m on 2022 08 02
 * https://howtodoinjava.com/junit5/junit5-test-suites-examples/
 *
 * testImplementation 'org.junit.platform:junit-platform-suite-engine:1.9.0'
 */

@SelectPackages({"fun.madeby.generic_server", "fun.madeby.generic_server.defrag",
		"fun.madeby.specific_server", "fun.madeby.specific_server.defrag"})

//@ExcludePackages("fun.madeby.defrag")

//@ExcludeClassNamePatterns({"^.*Defrag*.$"})

//@IncludeTags("production"); /@Exclude
//@SelectClasses
//@IncludeClassNamePatterns
//@ExcludeClassNamePatterns
// @Include/ExcludePackages
@Suite
@SuiteDisplayName("DBSpecificServer TestSuite")
public class DBTestSuite {

}
