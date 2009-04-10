/**
 * @file tests.h
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.5
 */

#ifndef TESTS_H_
#define TESTS_H_


#define TEST_BEGIN(name) \
	printf("%s: Start test \"%s\"\n", __FUNCTION__, name);

#define TEST_PASSED \
	printf("%s: Passed\n", __FUNCTION__);


#endif /* TESTS_H_ */
