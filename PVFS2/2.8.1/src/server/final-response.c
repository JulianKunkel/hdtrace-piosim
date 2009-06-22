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
#include "pvfs2-internal.h"
#include "pint-perf-counter.h"

/* final-response state machine:
 * This is used as a nested state machine to perform two primary tasks:
 * - release the operation from the request scheduler
 * - send a response to the client
 */

/* PRECONDITIONS:
 * - the s_op->resp struct must be filled in with the response that
 *   needs to be sent (not yet encoded), with one exception:
 * - js_p->error_code must indicate the status value that you wish 
 *   to have set in the response structure
 * - if the operation has been scheduled, then the scheduled id must be
 *   stored in s_op->scheduled_id
 */

static void PINT_gossip_err_server_resp(
    struct PVFS_server_resp *resp);


static PINT_sm_action final_response_release(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_release;
static struct PINT_pjmp_tbl_s ST_release_pjtbl[];
static struct PINT_tran_tbl_s ST_release_trtbl[];

static PINT_sm_action final_response_send_resp(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_send_resp;
static struct PINT_pjmp_tbl_s ST_send_resp_pjtbl[];
static struct PINT_tran_tbl_s ST_send_resp_trtbl[];

static PINT_sm_action final_response_cleanup(
	struct PINT_smcb *smcb, job_status_s *js_p);

static struct PINT_state_s ST_cleanup;
static struct PINT_pjmp_tbl_s ST_cleanup_pjtbl[];
static struct PINT_tran_tbl_s ST_cleanup_trtbl[];

struct PINT_state_machine_s pvfs2_final_response_sm = {
	.name = "pvfs2_final_response_sm",
	.first_state = &ST_release
};

static struct PINT_state_s ST_release = {
	 .state_name = "release" ,
	 .parent_machine = &pvfs2_final_response_sm ,
	 .flag = SM_RUN ,
	 .action.func = final_response_release ,
	 .pjtbl = NULL ,
	 .trtbl = ST_release_trtbl 
};

static struct PINT_tran_tbl_s ST_release_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_send_resp }
};

static struct PINT_state_s ST_send_resp = {
	 .state_name = "send_resp" ,
	 .parent_machine = &pvfs2_final_response_sm ,
	 .flag = SM_RUN ,
	 .action.func = final_response_send_resp ,
	 .pjtbl = NULL ,
	 .trtbl = ST_send_resp_trtbl 
};

static struct PINT_tran_tbl_s ST_send_resp_trtbl[] = {
	{ .return_value = -1 ,
	 .next_state = &ST_cleanup }
};

static struct PINT_state_s ST_cleanup = {
	 .state_name = "cleanup" ,
	 .parent_machine = &pvfs2_final_response_sm ,
	 .flag = SM_RUN ,
	 .action.func = final_response_cleanup ,
	 .pjtbl = NULL ,
	 .trtbl = ST_cleanup_trtbl 
};

static struct PINT_tran_tbl_s ST_cleanup_trtbl[] = {
	{ .return_value = -1 ,
	 .flag = SM_RETURN }
};

# 58 "src/server/final-response.sm"


/* final_response_release()
 *
 * releases the operation from the request scheduler
 */
static PINT_sm_action final_response_release(
        struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    int ret = -1;
    job_id_t tmp_id;

    gossip_debug(GOSSIP_SERVER_DEBUG, 
                 "(s_op %p) %s (FR sm) state: release: (error_code = %d)\n",
                 s_op, PINT_map_server_op_to_string(s_op->req->op),
                 js_p->error_code);

    /* this seems a little odd, but since this is the first state of the
     * nested machine, now is the time to grab the error code if we
     * are going to use it
     */
    /* NOTE: we filter out any subsystem mask and make sure that this
     * shows up as a generic pvfs error on the client side
     */
    s_op->resp.status = -PVFS_ERROR_CODE(-js_p->error_code);

    /* catch cases in which the operation has not been scheduled */
    if (!s_op->scheduled_id)
    {
        js_p->error_code = 0;
        return SM_ACTION_COMPLETE;
    }

    ret = job_req_sched_release(
        s_op->scheduled_id, smcb, 0, js_p, &tmp_id, server_job_context);

    PINT_perf_count(PINT_server_pc, PINT_PERF_REQSCHED, 1, PINT_PERF_SUB);

    return ret;
}

/* final_response_send_resp()
 *
 * encodes and sends a response to the client
 */
static PINT_sm_action final_response_send_resp(
        struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
    int ret = -1;
    job_id_t tmp_id;
    struct server_configuration_s *user_opts = get_server_config_struct();
    
    gossip_debug(
        GOSSIP_SERVER_DEBUG, 
	"(s_op %p) %s (FR sm) state: send_resp (status = %d)\n",
	s_op, PINT_map_server_op_to_string(s_op->req->op),
	s_op->resp.status);

    if (js_p->error_code != 0)
    {
        gossip_lerr("Error: req_sched_release() failure; continuing...\n");
    }

    ret = PINT_encode(&s_op->resp, PINT_ENCODE_RESP, &(s_op->encoded),
                      s_op->addr, s_op->decoded.enc_type);
    if (ret < 0)
    {
        gossip_lerr("Error: PINT_encode() failure.\n");
        PINT_gossip_err_server_resp(&s_op->resp);

        js_p->error_code = ret;
        return 1;
    }

    /* send the response */
    ret = job_bmi_send_list(
        s_op->addr, s_op->encoded.buffer_list, s_op->encoded.size_list,
        s_op->encoded.list_count, s_op->encoded.total_size, s_op->tag,
        s_op->encoded.buffer_type, 0, smcb, 0, js_p, &tmp_id,
        server_job_context, user_opts->server_job_bmi_timeout,
        s_op->req->hints);

    return ret;
}


/* final_response_cleanup()
 *
 * cleans up resources allocated while in this nested machine.  Right now 
 * that just means releasing the encoding of the response
 */
static PINT_sm_action final_response_cleanup(
        struct PINT_smcb *smcb, job_status_s *js_p)
{
    struct PINT_server_op *s_op = PINT_sm_frame(smcb, PINT_FRAME_CURRENT);
//    if (s_op->smToken){
//    hdR_start(s_op->smToken,"final_response_cleanup",0,NULL,NULL);
//    hdR_end(s_op->smToken,0,NULL,NULL);
//    }
    char status_string[64] = {0};

    gossip_debug(GOSSIP_SERVER_DEBUG, 
                 "(s_op %p) %s (FR sm) state: cleanup\n",
                 s_op, PINT_map_server_op_to_string(s_op->req->op));

    PVFS_strerror_r(s_op->resp.status, status_string, 64);
    PINT_ACCESS_DEBUG(s_op, GOSSIP_ACCESS_DEBUG, "finish (%s)\n", status_string);

    PINT_encode_release(&s_op->encoded, PINT_ENCODE_RESP);

    js_p->error_code = 0;
    return SM_ACTION_COMPLETE;
}

static void PINT_gossip_err_server_resp(
    struct PVFS_server_resp *resp)
{
    if (resp)
    {
        if (resp->op >= 0 && resp->op < PVFS_SERV_NUM_OPS)
        {
            gossip_err("Server Response %p is of type: %s\n",
                       resp, PINT_map_server_op_to_string(resp->op));
        }
        else
        {
            gossip_err("Server Response %p is of type: UNKNOWN (op %d)\n",
                       resp, resp->op);
        }

        switch(resp->op)
        {
            case PVFS_SERV_GETATTR:
            {
                PVFS_object_attr *attr = &resp->u.getattr.attr;
                switch(attr->objtype)
                {
                    case PVFS_TYPE_METAFILE:
                    {
                        int i = 0;
                        gossip_err(
                            "METAFILE [ dist is %p, dist_size is %d,\n\t"
                            "   dfile_array is %p, dfile_count is %d ]\n",
                            attr->u.meta.dist, attr->u.meta.dist_size,
                            attr->u.meta.dfile_array, 
                            attr->u.meta.dfile_count);

                        if (attr->u.meta.dfile_array)
                        {
                            for(i = 0; i < attr->u.meta.dfile_count; i++)
                            {
                                gossip_err("   DATA HANDLE[%d] is %llu\n", i,
                                           llu(attr->u.meta.dfile_array[i]));
                            }
                        }
                    }
                    break;
                    case PVFS_TYPE_DATAFILE:
                        gossip_err("DATAFILE [ size is %lld ]\n",
                                   lld(attr->u.data.size));
                        break;
                    case PVFS_TYPE_SYMLINK:
                        gossip_err(
                            "SYMLINK [ target is %s ; len is %d ]\n",
                            attr->u.sym.target_path,
                            attr->u.sym.target_path_len);
                        break;
                    case PVFS_TYPE_DIRECTORY:
                        gossip_err("DIRECTORY [ n/a ]\n");
                        break;
                    case PVFS_TYPE_DIRDATA:
                        gossip_err("DIRDATA [ n/a ]\n");
                        break;
                    case PVFS_TYPE_INTERNAL:
                        gossip_err("INTERNAL [ n/a ]\n");
                        break;
                    case PVFS_TYPE_NONE:
                        gossip_err("NONE [ n/a ]\n");
                        break;
                }
            }
            break;
            default:
                gossip_err("FIXME: unimplemented resp type to print\n");
                break;
        }
    }
}

/*
 * Local variables:
 *  mode: c
 *  c-indent-level: 4
 *  c-basic-offset: 4
 * End:
 *
 * vim: ft=c ts=8 sts=4 sw=4 expandtab
 */
