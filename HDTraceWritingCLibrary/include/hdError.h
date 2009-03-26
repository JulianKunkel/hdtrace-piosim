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

/**
 * Enumeration for common errors in function.
 * hdT_* as well as hdS_* functions  can set errno to one of these values in
 * case of an error
 */
enum hdCommonError {
	/** Invalid argument */
	HD_INVALIDARG,
    /** Error while memory allocation */
    HD_MALLOC,
    /** Error while creating a file */
    HD_CREATEFILE,
};

/**
 * Enumeration for errors in trace functions.
 * hdT_* functions  can set errno to one of these values in case of an error
 */
enum hdTraceError {
	/** Example error */
	HDT_EXAMPLE
};

/**
 * Enumeration for errors in statistics functions.
 * hdS_* functions can set errno to one of these values in case of an error
 */
enum hdStatsError {
    /** Statistics group is not created correctly */
    HDS_NOGROUP,
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
