/* -*- Mode: C; c-basic-offset:4 ; -*- */
/*
 *  (C) 2008 by Argonne National Laboratory.
 *      See COPYRIGHT in top-level directory.
 */

#include "hydra_utils.h"
#include "bsci.h"
#include "bscu.h"
#include "bind.h"
#include "ll.h"

static int fd_stdin, fd_stdout, fd_stderr;

HYD_status HYDT_bscd_ll_launch_procs(char **args, struct HYD_node *node_list,
                                     int *control_fd, int enable_stdin,
                                     HYD_status(*stdout_cb) (void *buf, int buflen),
                                     HYD_status(*stderr_cb) (void *buf, int buflen))
{
    int idx, i, fd, total_procs, node_count;
    int *pid, *fd_list;
    char *targs[HYD_NUM_TMP_STRINGS], *node_list_str = NULL;
    char *path = NULL, *extra_arg_list = NULL, *extra_arg;
    struct HYD_node *node;
    struct HYDT_bind_cpuset_t cpuset;
    HYD_status status = HYD_SUCCESS;

    HYDU_FUNC_ENTER();

    /* We use the following priority order for the executable path:
     * (1) user-specified; (2) search in path; (3) Hard-coded
     * location */
    if (HYDT_bsci_info.bootstrap_exec)
        path = HYDU_strdup(HYDT_bsci_info.bootstrap_exec);
    if (!path)
        path = HYDU_find_full_path("poe");
    if (!path)
        path = HYDU_strdup("/usr/bin/poe");

    idx = 0;
    targs[idx++] = HYDU_strdup(path);

    if (HYDT_bscd_ll_user_node_list) {
        HYDU_ERR_SETANDJUMP(status, HYD_INTERNAL_ERROR,
                            "ll does not support user-defined host lists\n");
    }

    /* Check how many nodes are being passed for the launch */
    status = HYDTI_bscd_ll_query_node_count(&total_procs);
    HYDU_ERR_POP(status, "unable to query for the node count\n");

    node_count = 0;
    for (node = node_list; node; node = node->next)
        node_count++;

    if (total_procs != node_count)
        HYDU_ERR_SETANDJUMP(status, HYD_INTERNAL_ERROR,
                            "processes to be launched have to cover all nodes\n");


    MPL_env2str("HYDRA_LAUNCH_EXTRA_ARGS", (const char **) &extra_arg_list);
    if (extra_arg_list) {
        extra_arg = strtok(extra_arg_list, " ");
        while (extra_arg) {
            targs[idx++] = HYDU_strdup(extra_arg);
            extra_arg = strtok(NULL, " ");
        }
    }

    /* Fill in the remaining arguments */
    for (i = 0; args[i]; i++)
        targs[idx++] = HYDU_strdup(args[i]);

    /* Increase pid list to accommodate the new pid */
    HYDU_MALLOC(pid, int *, (HYD_bscu_pid_count + 1) * sizeof(int), status);
    for (i = 0; i < HYD_bscu_pid_count; i++)
        pid[i] = HYD_bscu_pid_list[i];
    HYDU_FREE(HYD_bscu_pid_list);
    HYD_bscu_pid_list = pid;

    /* Increase fd list to accommodate these new fds */
    HYDU_MALLOC(fd_list, int *, (HYD_bscu_fd_count + 3) * sizeof(int), status);
    for (i = 0; i < HYD_bscu_fd_count; i++)
        fd_list[i] = HYD_bscu_fd_list[i];
    HYDU_FREE(HYD_bscu_fd_list);
    HYD_bscu_fd_list = fd_list;

    /* append proxy ID as -1 */
    targs[idx++] = HYDU_int_to_str(-1);
    targs[idx++] = NULL;

    HYDT_bind_cpuset_zero(&cpuset);
    status = HYDU_create_process(targs, NULL,
                                 enable_stdin ? &fd_stdin : NULL, &fd_stdout,
                                 &fd_stderr, &HYD_bscu_pid_list[HYD_bscu_pid_count++], cpuset);
    HYDU_ERR_POP(status, "create process returned error\n");

    /* We don't wait for stdin to close */
    HYD_bscu_fd_list[HYD_bscu_fd_count++] = fd_stdout;
    HYD_bscu_fd_list[HYD_bscu_fd_count++] = fd_stderr;

    /* Register stdio callbacks for the spawned process */
    if (enable_stdin) {
        fd = STDIN_FILENO;
        status = HYDT_dmx_register_fd(1, &fd, HYD_POLLIN, &fd_stdin, HYDT_bscu_stdin_cb);
        HYDU_ERR_POP(status, "demux returned error registering fd\n");
    }

    status = HYDT_dmx_register_fd(1, &fd_stdout, HYD_POLLIN, stdout_cb, HYDT_bscu_inter_cb);
    HYDU_ERR_POP(status, "demux returned error registering fd\n");

    status = HYDT_dmx_register_fd(1, &fd_stderr, HYD_POLLIN, stderr_cb, HYDT_bscu_inter_cb);
    HYDU_ERR_POP(status, "demux returned error registering fd\n");

  fn_exit:
    if (node_list_str)
        HYDU_FREE(node_list_str);
    HYDU_free_strlist(targs);
    if (path)
        HYDU_FREE(path);
    HYDU_FUNC_EXIT();
    return status;

  fn_fail:
    goto fn_exit;
}
