#include <hdtraceLikwid.h>

int main(){
    int c;
    int coreNum = 0;

    // the rank of the process on this node: nodeRank_i < nodeRank_j  <=>  world_rank_i < world_rank_j, but enumerated from 0.
    int processNodeRank = 0;

    hdLikwid_init(coreNum);

    hdLikwidResults results;

    for(c=0; c < 1 ; c++){
        hdLikwid_start();

        int i;
        double a=1;
        double b=0;
        //for (i=0; i < (c+1)*100000 ; i++){
        //  a=b+i * a;
        //  b=a;
        //}

        sleep (1);

        hdLikwid_end(& results);

        printf("wc: %f - runtime: %f  ipc: %f  cpu: %f mem: %f remMem: %f scal: %f packed: %f sp: %f dp: %f \n", results.wallclocktime, results.runtime, results.IPC, results.clock, results.memBandwidth, results.remReadBW, results.sse_scalar, results.sse_packed, results.sse_sp, results.sse_dp);
    }

    hdLikwid_finalize();
    return 0;
}
