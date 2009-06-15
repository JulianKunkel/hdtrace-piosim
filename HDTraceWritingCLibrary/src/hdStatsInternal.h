
#ifndef HDSTATS_INTERNAL_H_
#define HDSTATS_INTERNAL_H_


/* ************************************************************************* *
 *                            TYPE DEFINITIONS                               *
 * ************************************************************************* */

/**
 * Type of usage for the group buffer
 */
enum _hdStatsBufferType {
	/** Buffer for creating group header */
	HDS_HEADER_BUFFER,//!< HDS_HEADER_BUFFER
	/** Buffer for collection values for entry */
	HDS_ENTRY_BUFFER  //!< HDS_ENTRY_BUFFER
};

/**
 * @internal
 * Structure representing one statistics group.
 *
 * Do not use directly, use \ref hdStatsGroup instead
 */
struct _hdStatsGroup {
	/**
	 * Name of the group
	 */
	char *name;

    /**
     * File descriptor of the statistics group file
     */
	int fd;

    /**
     * Filename of the statistics group file (for error output only)
     */
    char *tracefile;

    /**
     * Current type of \a buffer
     */
    enum _hdStatsBufferType btype;

    /**
     * Offset for buffer to write next byte
     */
    int offset;

    /**
     * Size of the header and initial time stamp
     */
    off_t headerLength;

    /**
     * Length that an entry should have
     */
    size_t entryLength;

    /**
     * Types of the defined values (for error checking)
     * '-1' terminated in @ref hdS_commitGroup
     */
    hdStatsValueType *valueTypes;

    /**
     * Index of the next value to write (for error checking)
     */
    int nextValueIdx;

    /**
     * True if string values are defined
     * => reduced error checking
     */
    unsigned int hasString : 1;

    /**
     * True if the group is committed (for error checking)
     */
    unsigned int isCommitted : 1;

    /**
     * True if the group is enabled to trace
     */
    unsigned int isEnabled : 1;

    /**
     * Buffer for creating header and collecting entries
     */
    char *buffer;
};


#endif /* HDSTATS_INTERNAL_H_ */
