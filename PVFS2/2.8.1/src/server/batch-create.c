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

static struct PINT_state_s ST_prelude;
static struct PINT_pjmp_tbl_s ST_prelude_pjtbl[];
static struct PINT_tran_tbl_s ST_prelude_trtbl[];

static PINT_sm_action batch_create_create(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_create;
static struct PINT_pjmp_tbl_s ST_create_pjtbl[];
static struct PINT_tran_tbl_s ST_create_trtbl[];
static struct PINT_state_s ST_final_response;
static struct PINT_pjmp_tbl_s ST_final_response_pjtbl[];
static struct PINT_tran_tbl_s ST_final_response_trtbl[];

static PINT_sm_action batch_create_cleanup(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_cleanup;
static struct PINT_pjmp_tbl_s ST_cleanup_pjtbl[];
static struct PINT_tran_tbl_s ST_cleanup_trtbl[];

struct PINT_state_machine_s pvfs2_batch_create_sm = {
	.name = "pvfs2_batch_create_sm",
	.first_state = &ST_prelude
};

static struct PINT_state_s ST_prelude = {
	 .state_name = "prelude" ,
	 .parent_machine = &pvfs2_batch_create_sm ,
	 .flag = SM_JUMP ,
	 .action.nested = &pvfs2_prelude_sm ,
	 .pjtbl = NULL ,
	 .trtbl = ST_prelude_trtbl 
};

static struct PINT_tran_tbl_s ST_prelude_trtbl[] = {
	{ .return_value = 0 ,
	 .next_state = &ST_create },
	{ .return_value = -1 ,
	 .next_state = &ST_final_response }
};

static struct PINT_state_s ST_create = {
	 .state_name = "create" ,
	 .parent_machine = &pvfs2_batch_create_sm ,
	 .flag = SM_RUN ,
	 .action.func = batch_create_create ,
	 .pjtbl = NULL ,
	 .trtbl = ST_create_trtbl 
};

static struct PINT_tran_tbl_s ST_create_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_final_response }
};

static struct PINT_state_s ST_final_response = {
	 .state_name = "final_response" ,
	 .parent_machine = &pvfs2_batch_create_sm ,
	 .flag = SM_JUMP ,
	 .action.nested = &pvfs2_final_response_sm ,
	 .pjtbl = NULL ,
	 .trtbl = ST_final_response_trtbl 
};

static struct PINT_tran_tbl_s ST_final_response_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_cleanup }
};

static struct PINT_state_s ST_cleanup = {
	 .state_name = "cleanup" ,
	 .parent_machine = &pvfs2_batch_create_sm ,
	 .flag = SM_RUN ,
	 .action.func = batch_create_cleanup ,
	 .pjtbl = NULL ,
	 .trtbl = ST_cleanup_trtbl 
};

static struct PINT_tran_tbl_s ST_cleanup_trtbl[] = {
	{ .return_value = -1 ,

	 .flag = SM_TERM }
};

# 46 "src/server/batch-create.sm"



/*
 * Function: batch_create_create
 *
 * Params:   server_op *s_op, 
 *           job_status_s* js_p
 *
 * Pre:      None
 *
 * Post:     None
 *
 * Returns:  int
 *
 * Synopsis: Create a dataspace.
 */
static int batch_create_create(
        struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    int ret = -1;
    job_id_t i;

    if(s_op->req->u.batch_create.object_count < 1)
    {
        js_p->error_code = -PVFS_EINVAL;
        return(SM_ACTION_COMPLETE);
    }

    s_op->resp.u.batch_create.handle_count 
        = s_op->req->u.batch_create.object_count;

    /* allocate some space to hold the handles we create */
    s_op->resp.u.batch_create.handle_array = 
        malloc(s_op->req->u.batch_create.object_count * sizeof(PVFS_handle));
    if(!s_op->resp.u.batch_create.handle_array)
    {
        js_p->error_code = -PVFS_ENOMEM;
        return(SM_ACTION_COMPLETE);
    }

    ret = job_trove_dspace_create_list(
        s_op->req->u.batch_create.fs_id,
        &s_op->req->u.batch_create.handle_extent_array,
        s_op->resp.u.batch_create.handle_array,
        s_op->req->u.batch_create.object_count,
        s_op->req->u.batch_create.object_type,
        NULL,
        TROVE_SYNC,
        smcb,
        0,
        js_p,
        &i,
        server_job_context,
        s_op->req->hints);

     return(ret);
}

/*
 * Function: batch_create_cleanup
 *
 * Params:   server_op *b, 
 *           job_status_s* js_p
 *
 * Pre:      None
 *
 * Post:     None
 *
 * Returns:  int
 *
 * Synopsis: free memory and return
 *           
 */
static int batch_create_cleanup(
        struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    int i;

    if(s_op->resp.status == 0)
    {
        for(i=0; i<s_op->resp.u.batch_create.handle_count; i++)
        {
            gossip_debug(
                GOSSIP_SERVER_DEBUG, "Batch created: %llu\n",
                llu(s_op->resp.u.batch_create.handle_array[i]));
        }
    }

    if(s_op->resp.u.batch_create.handle_array)
    {
        free(s_op->resp.u.batch_create.handle_array);
    }

    return(server_state_machine_complete(smcb));
}

struct PINT_server_req_params pvfs2_batch_create_params =
{
    .string_name = "batch_create",
    .perm = PINT_SERVER_CHECK_NONE,
    .access_type = PINT_server_req_modify,
    .state_machine = &pvfs2_batch_create_sm
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
