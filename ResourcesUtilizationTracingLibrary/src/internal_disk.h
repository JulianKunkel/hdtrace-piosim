/*
 * internal_disk.h
 *
 *  Created on: Mar 17, 2011
 *  Author: Timo Minartz
 */

#ifndef INTERNAL_DISK_H_
#define INTERNAL_DISK_H_

#define STATE_UNKNOWN -1
#define STATE_SWITCHING 0
#define STATE_STANDBY 1
#define STATE_IDLE 2
#define STATE_ACTIVE_IDLE 3
#define STATE_SPINDOWN 4
#define STATE_SPINUP 5

/**
 * Opens the internal device
 * @param devname Device to open
 * @return 0 or 1 in case of error
 */
int id_open(const char * devname);

/**
 * Closes the internal device
 */
void id_close();

/**
 * Performs a hdparm -Y on the internal device
 * @return 0 or 3 in case of error
 */
int id_put_drive_to_sleep();

/**
 * Performs a hdparm -y on the internal device
 * @return 0 or 2 in case of error
 */
int id_put_drive_in_standby();

/**
 * Returns the actual power state of the device performing a hdparm -C on the internal device
 * The power state is one out of: NVcache_spindown, NVcache_spinup, idle, active/idle
 * @return -1 in case of error
 */
int id_check_drive_power_mode_status();

/**
 * Performs a hdparm -F followed by a hdparm -f which flushes the drives write cache and the buffer cache for device on exit
 * @return 0 or errno in case of error
 */
int id_flush();

#endif /* INTERNAL_DISK_H_ */
