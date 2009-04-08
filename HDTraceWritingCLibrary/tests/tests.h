/**
 * @file tests.h
 *
 *  Created on: 08.04.2009
 *      Author: Stephan Krempel <stephan.krempel@gmx.de>
 */

#ifndef TESTS_H_
#define TESTS_H_


#define TEST_BEGIN(name) \
	printf("%s: Start test \"%s\"\n", __FUNCTION__, name);

#define TEST_PASSED \
	printf("%s: Passed\n", __FUNCTION__);


#endif /* TESTS_H_ */
