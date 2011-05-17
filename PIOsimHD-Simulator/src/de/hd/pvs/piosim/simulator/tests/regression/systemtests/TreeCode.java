package de.hd.pvs.piosim.simulator.tests.regression.systemtests;


/**
 * In this class the code to generate binary communication trees is tested.
 *
 * @author julian
 */
public class TreeCode {

	static void printTree(final int clientRankInComm, int commSize){

		final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);

		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
		final int phaseStart = iterations - trailingZeros;
		int maxIter = iterations  - phaseStart - 1;

		System.out.println("test: " + clientRankInComm + " " + commSize);


		if(clientRankInComm != 0){
			// root rank is a special case...
			maxIter = maxIter + 1;
		}


		//		for (int iter = maxIter - 1 ; iter >= 0; iter--){
		for (int iter = 0 ; iter < maxIter; iter++){
			final int targetRank = (1<<iter | clientRankInComm);
			if (targetRank >= commSize )
				continue;

			System.out.println(" to leaf:" + targetRank );

			final int pow = (1<<(iter));
			int count = ( pow + targetRank ) > commSize ? commSize - targetRank : pow;

			System.out.println(targetRank + " " + count);
		}

		if(clientRankInComm != 0){
			int sendTo = (clientRankInComm ^ 1<<trailingZeros);
			System.out.println(" to root: " + sendTo);
		}
		System.out.println();
	}

	public static void main(String[] args) {
		for(int i=0; i < 7; i++)
			printTree(i, 7);

	}
}
