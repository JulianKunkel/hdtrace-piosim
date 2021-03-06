/* -*- Mode: C; c-basic-offset:4 ; -*- */
/*
 *  (C) 2008 by Argonne National Laboratory.
 *      See COPYRIGHT in top-level directory.
 */

#ifndef SSH_H_INCLUDED
#define SSH_H_INCLUDED

#include "hydra_base.h"

/* Modern sshd servers don't like more than a certain number of ssh
 * connections from the same IP address per minute. If we exceed that,
 * the server assumes it's a hack-in attack, and does not accept any
 * more connections. So, we limit the number of ssh connections. */
#define SSH_LIMIT 8
#define SSH_LIMIT_TIME 15

#define older(a,b) \
    ((((a).tv_sec < (b).tv_sec) ||                                      \
      (((a).tv_sec == (b).tv_sec) && ((a).tv_usec < (b).tv_usec))) ? 1 : 0)

struct HYDT_bscd_ssh_time {
    char *hostname;
    struct timeval init_time[SSH_LIMIT];
    struct HYDT_bscd_ssh_time *next;
};

extern struct HYDT_bscd_ssh_time *HYDT_bscd_ssh_time;

HYD_status HYDT_bscd_ssh_store_launch_time(char *hostname);

#endif /* SSH_H_INCLUDED */
