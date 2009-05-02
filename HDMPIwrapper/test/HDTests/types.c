/**
 * \file types.c
 *
 * This program can be used to test the logging capabilities of
 * the MPI wrapper.
 *
 * The program creates and uses some MPI datatypes which should
 * be logged by the wrapper.
 *
 * \author Paul Mueller <pmueller@ix.urz.uni-heidelberg.de>
 */

#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int rank;
int size;


int darray_2d_c_test1(void)
{
    MPI_Datatype darray;
    int array[9]; /* initialized below */
    int array_size[2] = {3, 3};
    int array_distrib[2] = {MPI_DISTRIBUTE_BLOCK, MPI_DISTRIBUTE_BLOCK};
    int array_dargs[2] = {MPI_DISTRIBUTE_DFLT_DARG, MPI_DISTRIBUTE_DFLT_DARG};
    int array_psizes[2] = {3, 3};
    int i, rank, err, errs = 0, sizeoftype;

    /* pretend we are each rank, one at a time */
    for (rank=0; rank < 9; rank++) {
        /* set up buffer */
        for (i=0; i < 9; i++) {
            array[i] = i;
        }
        /* set up type */
        err = MPI_Type_create_darray(9 /* size */, rank, 2 /* dims */, array_size, array_distrib, array_dargs, array_psizes, MPI_ORDER_C, MPI_INT, &darray);
        if (err != MPI_SUCCESS) {
            errs++;
            fprintf(stderr, "error in MPI_Type_create_darray call; aborting after %d errors\n", errs); fflush(stderr);
            return errs;
        }
		else
		{
			printf("darray created\n");
		}


        MPI_Type_commit(&darray);
		return;

        MPI_Type_size(darray, &sizeoftype);
        if (sizeoftype != sizeof(int)) {
            errs++;
            fprintf(stderr, "size of type = %d; should be %d\n", sizeoftype, (int) sizeof(int));fflush(stderr);
            return errs;
        }

        err = pack_and_unpack((char *) array, 1, darray, 9*sizeof(int));

        for (i=0; i < 9; i++) {
            if ((i == rank) && (array[i] != rank)) {
                errs++;
                fprintf(stderr, "array[%d] = %d; should be %d\n", i, array[i], rank);fflush(stderr);
            }
            else if ((i != rank) && (array[i] != 0)) {
                errs++;
                fprintf(stderr, "array[%d] = %d; should be %d\n", i, array[i], 0);fflush(stderr);
            }
        }
        MPI_Type_free(&darray);
    }
    return errs;
}

int main (int argc, char** argv)
{

	MPI_Init(&argc, &argv);

	darray_2d_c_test1();

	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &size);

	MPI_Datatype type1, type2, type3, type4, type5, type6;
	int size;

	int blens[2] = {4, 5};
	MPI_Aint inds[2] = {6, 7};
	MPI_Datatype oldtypes[2] = {MPI_INT, MPI_DOUBLE};

	int disps[3] = {1, 23, 42};

	MPI_Type_struct(2, blens, inds, oldtypes, &type6);
	MPI_Type_commit(&type6);

	int a_of_sizes[2] = {2, 3};
	int a_of_subsizes[2] = {1, 2};
	int a_of_starts[2] = {1, 1};

//	MPI_Type_create_indexed_block(3, 2, disps, type6, &type5);
	MPI_Type_create_subarray(2, a_of_sizes, a_of_subsizes, a_of_starts, MPI_ORDER_C, type6, &type5);
	MPI_Type_commit(&type5);

	MPI_Type_create_resized(type5, 10, 20, &type4);
	MPI_Type_commit(&type4);

	MPI_Type_contiguous(3, type4, &type3);
	MPI_Type_commit(&type3);

	int indices[5] = {1, 10, 20, 30, 40};
	int lens[5] = {5, 3, 3, 3, 5};

	MPI_Type_hindexed(5, lens, indices, type3, &type2);
	MPI_Type_commit(&type2);

	MPI_Type_vector(5, 6, 7, type2, &type1);
	MPI_Type_commit(&type1);

	MPI_Type_free(&type1);
	MPI_Type_vector(5, 6, 7, type2, &type1);
	MPI_Type_commit(&type1);

	MPI_Type_free(&type1);
	MPI_Type_vector(5, 6, 7, type2, &type1);
	MPI_Type_commit(&type1);


	MPI_Type_size(type1, &size);
	printf("size=%d\n", size);
	//size = 10000;
	char *send = malloc(size);
	MPI_Bcast(send, 1, type1, 0, MPI_COMM_WORLD);

	MPI_Finalize();

	free(send);

	return 0;
}
