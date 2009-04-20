/* WARNING: THIS FILE IS AUTOMATICALLY GENERATED FROM A .SM FILE.
 * Changes made here will certainly be overwritten.
 */

/* 
 * (C) 2001 Clemson University and The University of Chicago 
 *
 * See COPYING in top-level directory.
 */

#include <string.h>
#include <assert.h>

#include "server-config.h"
#include "pvfs2-server.h"
#include "pvfs2-attr.h"
#include "gossip.h"
#include "pvfs2-internal.h"

enum
{
    REMOVE_NEXT = 1
};


static PINT_sm_action setup_prelude(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_setup_prelude;
static struct PINT_pjmp_tbl_s ST_setup_prelude_pjtbl[];
static struct PINT_tran_tbl_s ST_setup_prelude_trtbl[];
static struct PINT_state_s ST_prelude;
static struct PINT_pjmp_tbl_s ST_prelude_pjtbl[];
static struct PINT_tran_tbl_s ST_prelude_trtbl[];

static PINT_sm_action setup_remove(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_setup_remove;
static struct PINT_pjmp_tbl_s ST_setup_remove_pjtbl[];
static struct PINT_tran_tbl_s ST_setup_remove_trtbl[];
static struct PINT_state_s ST_remove;
static struct PINT_pjmp_tbl_s ST_remove_pjtbl[];
static struct PINT_tran_tbl_s ST_remove_trtbl[];

static PINT_sm_action remove_complete(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_remove_complete;
static struct PINT_pjmp_tbl_s ST_remove_complete_pjtbl[];
static struct PINT_tran_tbl_s ST_remove_complete_trtbl[];

static PINT_sm_action release(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_release;
static struct PINT_pjmp_tbl_s ST_release_pjtbl[];
static struct PINT_tran_tbl_s ST_release_trtbl[];

static PINT_sm_action remove_next(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_remove_next;
static struct PINT_pjmp_tbl_s ST_remove_next_pjtbl[];
static struct PINT_tran_tbl_s ST_remove_next_trtbl[];
static struct PINT_state_s ST_response;
static struct PINT_pjmp_tbl_s ST_response_pjtbl[];
static struct PINT_tran_tbl_s ST_response_trtbl[];

static PINT_sm_action cleanup(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_cleanup;
static struct PINT_pjmp_tbl_s ST_cleanup_pjtbl[];
static struct PINT_tran_tbl_s ST_cleanup_trtbl[];

struct PINT_state_machine_s pvfs2_batch_remove_sm = {
	.name = "pvfs2_batch_remove_sm",
	.first_state = &ST_setup_prelude
};

static struct PINT_state_s ST_setup_prelude = {
	 .state_name = "setup_prelude" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_RUN ,
	 .action.func = setup_prelude ,
	 .pjtbl = NULL ,
	 .trtbl = ST_setup_prelude_trtbl 
};

static struct PINT_tran_tbl_s ST_setup_prelude_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_prelude }
};

static struct PINT_state_s ST_prelude = {
	 .state_name = "prelude" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_JUMP ,
	 .action.nested = &pvfs2_prelude_work_sm ,
	 .pjtbl = NULL ,
	 .trtbl = ST_prelude_trtbl 
};

static struct PINT_tran_tbl_s ST_prelude_trtbl[] = {
	{ .return_value = 0 ,
	 .next_state = &ST_setup_remove },
	{ .return_value = -1 ,
	 .next_state = &ST_release }
};

static struct PINT_state_s ST_setup_remove = {
	 .state_name = "setup_remove" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_RUN ,
	 .action.func = setup_remove ,
	 .pjtbl = NULL ,
	 .trtbl = ST_setup_remove_trtbl 
};

static struct PINT_tran_tbl_s ST_setup_remove_trtbl[] = {
	{ .return_value = 0 ,
	 .next_state = &ST_remove },
	{ .return_value = -1 ,
	 .next_state = &ST_remove_complete }
};

static struct PINT_state_s ST_remove = {
	 .state_name = "remove" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_JUMP ,
	 .action.nested = &pvfs2_remove_work_sm ,
	 .pjtbl = NULL ,
	 .trtbl = ST_remove_trtbl 
};

static struct PINT_tran_tbl_s ST_remove_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_remove_complete }
};

static struct PINT_state_s ST_remove_complete = {
	 .state_name = "remove_complete" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_RUN ,
	 .action.func = remove_complete ,
	 .pjtbl = NULL ,
	 .trtbl = ST_remove_complete_trtbl 
};

static struct PINT_tran_tbl_s ST_remove_complete_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_release }
};

static struct PINT_state_s ST_release = {
	 .state_name = "release" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_RUN ,
	 .action.func = release ,
	 .pjtbl = NULL ,
	 .trtbl = ST_release_trtbl 
};

static struct PINT_tran_tbl_s ST_release_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_remove_next }
};

static struct PINT_state_s ST_remove_next = {
	 .state_name = "remove_next" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_RUN ,
	 .action.func = remove_next ,
	 .pjtbl = NULL ,
	 .trtbl = ST_remove_next_trtbl 
};

static struct PINT_tran_tbl_s ST_remove_next_trtbl[] = {
	{ .return_value = REMOVE_NEXT ,
	 .next_state = &ST_setup_prelude },
	{ .return_value = -1 ,
	 .next_state = &ST_response }
};

static struct PINT_state_s ST_response = {
	 .state_name = "response" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_JUMP ,
	 .action.nested = &pvfs2_final_response_sm ,
	 .pjtbl = NULL ,
	 .trtbl = ST_response_trtbl 
};

static struct PINT_tran_tbl_s ST_response_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_cleanup }
};

static struct PINT_state_s ST_cleanup = {
	 .state_name = "cleanup" ,
	 .parent_machine = &pvfs2_batch_remove_sm ,
	 .flag = SM_RUN ,
	 .action.func = cleanup ,
	 .pjtbl = NULL ,
	 .trtbl = ST_cleanup_trtbl 
};

static struct PINT_tran_tbl_s ST_cleanup_trtbl[] = {
	{ .return_value = -1 ,

	 .flag = SM_TERM }
};

# 83 "src/server/batch-remove.sm"


static PINT_sm_action setup_prelude(
    struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    assert(s_op);

    /* get the object to remove, the access and scheduling policies */
    s_op->target_fs_id = s_op->req->u.batch_remove.fs_id;
    s_op->target_handle =
        s_op->req->u.batch_remove.handles[s_op->u.batch_remove.handle_index];

    s_op->access_type = PINT_server_req_get_access_type(s_op->req);
    s_op->sched_policy = PINT_server_req_get_sched_policy(s_op->req);

    js_p->error_code = 0;
    return SM_ACTION_COMPLETE;
}

static PINT_sm_action setup_remove(
    struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    struct PINT_server_op *remove_op;
    int ret;

    remove_op = malloc(sizeof(*remove_op));
    if(!remove_op)
    {
        js_p->error_code = -PVFS_ENOMEM;
        return SM_ACTION_COMPLETE;
    }
    memset(remove_op, 0, sizeof(*remove_op));

    remove_op->u.remove.fs_id = s_op->target_fs_id;
    remove_op->u.remove.handle = s_op->target_handle;

    ret = PINT_sm_push_frame(smcb, 0, remove_op);
    if(ret < 0)
    {
        js_p->error_code = ret;
    }
    return SM_ACTION_COMPLETE;
}

static PINT_sm_action remove_complete(
    struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *remove_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    struct PINT_server_op *s_op;
    int error_code;
    int task_id;
    int remaining;

    s_op = PINT_sm_pop_frame(smcb, &task_id, &error_code, &remaining);

    free(remove_op);

    if(error_code != 0)
    {
        s_op->u.batch_remove.error_code = error_code;
        return SM_ACTION_COMPLETE;
    }

    return SM_ACTION_COMPLETE;
}

static PINT_sm_action release(
    struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    job_id_t tmp_id;
    int ret;

    /* we need to release the scheduled remove request on the target
     * handle.  The schedule call occurred in the prelude_work sm */

    if(!s_op->scheduled_id)
    {
        return SM_ACTION_COMPLETE;
    }

    if(js_p->error_code)
    {
        s_op->u.batch_remove.error_code = js_p->error_code;
    }

    ret = job_req_sched_release(s_op->scheduled_id, smcb, 0, js_p, &tmp_id,
                                server_job_context);
    s_op->scheduled_id = 0;
    return ret;
}

static PINT_sm_action remove_next(
    struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);

    if(s_op->u.batch_remove.error_code != 0)
    {
        js_p->error_code = s_op->u.batch_remove.error_code;
        return SM_ACTION_COMPLETE;
    }

    if(js_p->error_code != 0)
    {
        return SM_ACTION_COMPLETE;
    }

    s_op->u.batch_remove.handle_index++;
    if(s_op->u.batch_remove.handle_index < s_op->req->u.batch_remove.handle_count)
    {
        js_p->error_code = REMOVE_NEXT;
    }

    return SM_ACTION_COMPLETE;
}


static int cleanup(
        struct PINT_smcb *smcb, job_status_s *js_p)
{
    return(server_state_machine_complete(smcb));
}

struct PINT_server_req_params pvfs2_batch_remove_params =
{
    .string_name = "batch_remove",
    .perm = PINT_SERVER_CHECK_NONE,
    .access_type = PINT_server_req_modify,
    .state_machine = &pvfs2_batch_remove_sm
};


/*
 * Local variables:
 *  mode: c
 *  c-indent-level: 4
 *  c-basic-offset: 4
 * End:
 *
 * vim: ft=c ts=8 sts=4 sw=4 expandtab
 */
