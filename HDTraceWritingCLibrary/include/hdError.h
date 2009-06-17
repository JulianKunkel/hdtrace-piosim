/**
 * @file hdError.h
 *
 * Declaration of the enumerations for error reporting
 *
 * @ifnot api_only
 *  @ingroup hdError
 * @endif
 *
 * @date 10.04.2009
 * @author Stephan Krempel <stephan.krempel@gmx.de>
 * @version \$Id$
 */

#ifndef HDERROR_H_
#define HDERROR_H_

#define HD_MIN_ERRNO_VALUE  800000
/**
 * @addtogroup hdError HDTrace Errors
 */

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
    /** Error while writing a file */
	HD_ERR_WRITE_FILE,
    /** Error while closing a file */
	HD_ERR_CLOSE_FILE,
	/** Timeout occurred */
	HD_ERR_TIMEOUT,
	/** Trace is disabled */
	HD_ERR_TRACE_DISABLED,
	/** function may not be called in this context */
	HD_ERR_INVALID_CONTEXT,
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
	/** Statistics group's commit state is not the needed */
	HDS_ERR_GROUP_COMMIT_STATE = HD_MIN_ERRNO_VALUE + 200,
	/** One of the arguments has an unexpected value */
	HDS_ERR_UNEXPECTED_ARGVALUE,
	/** State of the current entry is wrong for requested action */
	HDS_ERR_ENTRY_STATE
};

/******** @cond api_only ********/
/**
 * @enum hdCommonError
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_INVALID_ARGUMENT
 * @ingroup hdError
 */
/**
 * @var HD_ERR_MALLOC
 * @ingroup hdError
 */
/**
 * @var HD_ERR_BUFFER_OVERFLOW
 * @ingroup hdError
 */
/**
 * @var HD_ERR_GET_TIME
 * @ingroup hdError
 */
/**
 * @var HD_ERR_CREATE_FILE
 * @ingroup hdError
 */
/**
 * @var HD_ERR_WRITE_FILE
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_CLOSE_FILE
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_TIMEOUT
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_TRACE_DISABLED
 * @ingroup hdError
 */
/**
 * @var	HD_ERR_UNKNOWN
 * @ingroup hdError
 */
/**
 * @enum hdStatsError
 * @ingroup hdError
 */
/**
 * @var HDS_ERR_GROUP_COMMIT_STATE
 * @ingroup hdError
 */
/**
 * @var HDS_ERR_UNEXPECTED_ARGVALUE
 * @ingroup hdError
 */
/**
 * @var HDS_ERR_ENTRY_STATE
 * @ingroup hdError
 */
/** @endcond **/


#endif /* HDERROR_H_ */
