/**
 * @file hdError.h
 *
 * Declaration of the enumerations for error reporting
 *
 * @date 25.03.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version 0.1
 */

#ifndef HDERROR_H_
#define HDERROR_H_

#define HD_MIN_ERRNO_VALUE  800000

/**
 * Enumeration for common errors in function.
 * hdT_* as well as hdS_* functions  can set errno to one of these values in
 * case of an error
 */
enum hdCommonError {
	/** Invalid argument */
	HD_ERR_INVALID_ARGUMENT = HD_MIN_ERRNO_VALUE,
    /** Error while memory allocation */
    HD_ERR_MALLOC,
    /** Error due to buffer overflow */
    HD_ERR_BUFFER_OVERFLOW,
    /** Error while getting system time */
    HD_ERR_GET_TIME,
    /** Error while creating a file */
    HD_ERR_CREATE_FILE,
    /** Error while creating a file */
	HD_ERR_WRITE_FILE,
    /** Error while creating a file */
	HD_ERR_CLOSE_FILE,
	/** Timeout occured */
	HD_ERR_TIMEOUT,
	/** Error with unknown cause */
	HD_ERR_UNKNOWN
};

/**
 * Enumeration for errors in trace functions.
 * hdT_* functions  can set errno to one of these values in case of an error
 */
enum hdTraceError {
	/** Example error */
	HDT_EXAMPLE = HD_MIN_ERRNO_VALUE + 100
};

/**
 * Enumeration for errors in statistics functions.
 * hdS_* functions can set errno to one of these values in case of an error
 */
enum hdStatsError {
    /** Statistics group is not created correctly */
    HDS_NOGROUP = HD_MIN_ERRNO_VALUE + 200,
    /** Name is empty that is not allowed */
    HDS_EMPTYNAME,
    /** Statistics group already committed */
    HDS_ISCOMMITED,
    /** Value has not the expected length */
    HDS_WRONGLENGTH,
    /** Value has not the expected type */
    HDS_WRONGTYPE
};


#endif /* HDERROR_H_ */
