/*
 * devicestates.c
 *
 *  Created on: May 24, 2011
 *  Author: Timo Minartz
 */

#include "devicestates.h"

/* network stuff */

#include <sys/ioctl.h> /* for ioctl */
#include <netinet/in.h> /* for socket, ifru, ... */
#include <linux/sockios.h> /* for SIOCETHTOOL */
#include <linux/if.h> /* for ifreq */
#include <linux/ethtool.h> /* for ethtool_cmd */
#include <string.h> /* for strncopy */

/* hdd stuff */

#include "internal_disk.h"

int net_get_state(const char * device) {

	return 1;

	int sock, ret;
	struct ifreq ifr;
	struct ethtool_cmd edata;

	sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_IP);

	strncpy(ifr.ifr_name, device, sizeof(ifr.ifr_name));
		ifr.ifr_data = &edata;

	ifr.ifr_data = &edata;

	edata.cmd = ETHTOOL_GSET;

	ioctl(sock, SIOCETHTOOL, &ifr);

	ifr.ifr_data = &edata;

	switch (edata.speed) {
	case 10:
		ret = 10; break;
	case 100:
		ret = 100; break;
	case 1000:
		ret = 1000; break;
	default:
		ret = STATE_SWITCHING; break;
	}

	shutdown(sock, SHUT_RDWR);

	return ret;
}

int hdd_get_state(const char * device) {

	return 1;

	id_open(device);

	int ret = id_check_drive_power_mode_status();

	id_close();

	return ret;
}
