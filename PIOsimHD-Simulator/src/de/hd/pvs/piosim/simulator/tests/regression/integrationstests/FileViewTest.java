package de.hd.pvs.piosim.simulator.tests.regression.integrationstests;

import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype.DimensionSpec;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype.Order;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.fileView.FileView;

public class FileViewTest {
	private void runTest(Datatype datatype, long displacement, long requestOffset, long requestBytes, long [] offsets, long[] accessed){
		final FileView view = new FileView(NamedDatatype.BYTE, datatype, displacement);

		final ListIO lio = new ListIO();

		view.createIOOperationWithDatatypeOffset(lio, requestOffset, requestBytes);

		long accessedBytes = 0;
		int pos = 0;
		for (SingleIOOperation op: lio.getIOOperations()){
			if(offsets[pos] != op.getOffset() || accessed[pos] != op.getAccessSize()){
				// print all:
				for (SingleIOOperation curOp: lio.getIOOperations()){
					System.out.println(curOp.getOffset() + " <" + curOp.getAccessSize() + ">");
				}
				System.out.println("Datatype size, extend: " + datatype.getSize() + " " + datatype.getExtend());

				throw new IllegalArgumentException("Error, wrong response (" + pos + "): "
						+ op.getOffset() + " <" + op.getAccessSize() + ">\n"
						+ "Request should look like: " + offsets[pos] + " <" + accessed[pos] + ">");
			}
			pos++;

			accessedBytes += op.getAccessSize();
		}

		if(requestBytes != accessedBytes){
			System.out.println("Datatype size, extend: " + datatype.getSize() + " " + datatype.getExtend());

			throw new IllegalArgumentException("accessed amount of Data != requested amount of Data: " + accessedBytes + " " + requestBytes);
		}
	}

	public void testVectorDatatype() {
		Datatype tint = NamedDatatype.CHAR;

		Datatype tvect = new VectorDatatype(tint, 5, 10, 20);

		runTest(tvect, 0, 0, 0, null, null);
		runTest(tvect, 0, 0, 5, new long[]{0}, new long[]{5});
		runTest(tvect, 0, 1, 5, new long[]{1}, new long[]{5});
		runTest(tvect, 0, 9, 5, new long[]{9, 20}, new long[]{1, 4});
		runTest(tvect, 0, 9, 11, new long[]{9, 20}, new long[]{1, 10});
		runTest(tvect, 0, 9, 21, new long[]{9, 20, 40}, new long[]{1, 10, 10});
		runTest(tvect, 0, 9, 29, new long[]{9, 20, 40, 60}, new long[]{1, 10, 10, 8});

		runTest(tvect, 99, 0, 5, new long[]{99}, new long[]{5});
		runTest(tvect, 99, 1, 5, new long[]{100}, new long[]{5});

		// test nested vector datatype:
		Datatype tvect2 = new VectorDatatype(tvect, 2, 1, 2);
		runTest(tvect2, 0, 0, 5, new long[]{0}, new long[]{5});

		runTest(tvect2, 0, 0, 110, new long[]{0,20,40,60,80,200,220,240,260,280,400}, new long[]{10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10});

		//(datatype, displacement, requestOffset, requestBytes, offsets, accessed)
	}

	public void testStructDatatype(){
		Datatype tint = NamedDatatype.CHAR;
		Datatype tvect = new VectorDatatype(tint, 5, 10, 20);

		StructDatatype struct = new StructDatatype();
		struct.appendType(NamedDatatype.LB, 0, 0);
		struct.appendType(NamedDatatype.INTEGER, 2, 2);

		struct.appendType(tvect, 20, 1);

		struct.appendType(NamedDatatype.INTEGER, 480, 1);
		struct.appendType(NamedDatatype.UB, 500, 1);

		runTest(struct, 0, 10, 6, new long[]{22}, new long[]{6});

		runTest(struct, 0, 10, 32, new long[]{22, 40, 60, 80}, new long[]{8, 10, 10, 4});

		runTest(struct, 0, 0, 66, new long[]{2, 20, 40, 60, 80, 100, 480, 502}, new long[]{8, 10, 10, 10, 10, 10, 4, 4});

		runTest(struct, 0, 2, 10, new long[]{4, 20}, new long[]{6, 4});

		runTest(struct, 0, 0, 62, new long[]{2, 20, 40, 60, 80, 100, 480}, new long[]{8, 10, 10, 10, 10, 10, 4});

		runTest(struct, 0, 0, 30, new long[]{2, 20, 40, 60}, new long[]{8, 10, 10, 2});

		runTest(struct, 0, 0, 10, new long[]{2, 20}, new long[]{8, 2});
	}

	public void testSubarrayDatatype(){
		// 10 x 5 Grid in which the inner 6x3 elements are interesting
		DimensionSpec [] dimSpec = new DimensionSpec[2];

		dimSpec[0] = new DimensionSpec(10, 6, 2);
		dimSpec[1] = new DimensionSpec(5, 3, 1);
		SubarrayDatatype subarray = new SubarrayDatatype(dimSpec, Order.MPI_ORDER_FORTRAN, NamedDatatype.BYTE);

		runTest(subarray, 0, 4, 2, new long[]{16} , new long[]{2});
		runTest(subarray, 0, 94, 2, new long[]{266} , new long[]{2});

		runTest(subarray, 0, 0, 4, new long[]{12} , new long[]{4});
		runTest(subarray, 0, 0, 7, new long[]{12,22} , new long[]{6,1});
		runTest(subarray, 0, 0, 18, new long[]{12,22,32} , new long[]{6,6,6});
		runTest(subarray, 0, 0, 36, new long[]{12,22,32,62,72,82} , new long[]{6,6,6,6,6,6});
		runTest(subarray, 0, 4, 32, new long[]{16,22,32,62,72,82} , new long[]{2,6,6,6,6,6});


		subarray = new SubarrayDatatype(dimSpec, Order.MPI_ORDER_C, NamedDatatype.BYTE);

		runTest(subarray, 0, 0, 4, new long[]{11,16} , new long[]{3,1});
		runTest(subarray, 0, 0, 7, new long[]{11,16,21} , new long[]{3,3,1});

		runTest(subarray, 0, 0, 18, new long[]{11,16,21,26,31,36} , new long[]{3,3,3,3,3,3});
		runTest(subarray, 0, 4, 2, new long[]{17} , new long[]{2});


		dimSpec[0] = new DimensionSpec(10, 5, 0);
		dimSpec[1] = new DimensionSpec(4, 2, 0);
		subarray = new SubarrayDatatype(dimSpec, Order.MPI_ORDER_FORTRAN, NamedDatatype.BYTE);
		runTest(subarray, 0, 0, 32, new long[]{0,10,40,50,80,90,120} , new long[]{5,5,5,5,5,5,2});
	}

	public static void main(String[] args) {
		FileViewTest t = new FileViewTest();
		t.testVectorDatatype();
		t.testStructDatatype();
		t.testSubarrayDatatype();
	}
}
