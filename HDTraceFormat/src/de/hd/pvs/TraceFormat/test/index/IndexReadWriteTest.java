package de.hd.pvs.TraceFormat.test.index;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.hd.pvs.TraceFormat.index.IndexCreator;
import de.hd.pvs.TraceFormat.index.IndexReader;
import de.hd.pvs.TraceFormat.index.IndexWriter;
import de.hd.pvs.TraceFormat.index.IndexReader.IndexData;
import de.hd.pvs.TraceFormat.util.Epoch;

public class IndexReadWriteTest{
	
	@Before
	public void WriteIndex() throws IOException{
		IndexWriter tmp = new IndexWriter("/tmp/idx");
		
		for (int i=0; i < 10000; i++){
			tmp.writeNextEntry(new Epoch(i*10, 0), (i*10));
		}
		
		tmp.finalize();	
	}
	
	@Test
	public void ReadExactTime() throws IOException {		
		IndexReader reader = new IndexReader("/tmp/idx"); //home/julian/workspace/PIOsimHD/HDTraceConverter/Example/test_0_0_stat_Energy.idx
		
		for(int i=0; i < 100; i++){
			IndexData data =  reader.getFirstInfoWithTime(new Epoch(i*10, 0));
			
			org.junit.Assert.assertNotNull(data);
			org.junit.Assert.assertEquals( i*10.0, data.getNextTime().getDouble(), 0.0);
			org.junit.Assert.assertEquals(i*10,data.getPosition());
		}
		
		reader.close();
	}

	@Test
	public void ReadAfterTime() throws IOException {		
		IndexReader reader = new IndexReader("/tmp/idx"); //home/julian/workspace/PIOsimHD/HDTraceConverter/Example/test_0_0_stat_Energy.idx
		
		for(int i=0; i < 100; i++){
			IndexData data =  reader.getFirstInfoWithTime(new Epoch(i*10, 100));
			
			org.junit.Assert.assertNotNull(data);
			org.junit.Assert.assertEquals((i+1)*10, data.getNextTime().getDouble(),  0);
			org.junit.Assert.assertEquals(i*10, data.getPosition());
		}
		
		reader.close();
	}
	
	@Test
	public void ReadBeforeTime() throws IOException {		
		IndexReader reader = new IndexReader("/tmp/idx"); //home/julian/workspace/PIOsimHD/HDTraceConverter/Example/test_0_0_stat_Energy.idx
		
		IndexData data =  reader.getFirstInfoWithTime(new Epoch(-5, 0));
		org.junit.Assert.assertNotNull(data);
		org.junit.Assert.assertEquals(0.0, data.getNextTime().getDouble(), 0.0);
		org.junit.Assert.assertEquals(0, data.getPosition());
		
		for(int i=1; i < 100; i++){
			data =  reader.getFirstInfoWithTime(new Epoch(i*10 - 1, 0));
			
			org.junit.Assert.assertNotNull(data);
			org.junit.Assert.assertEquals(i*10.0, data.getNextTime().getDouble(), 0.0);
			org.junit.Assert.assertEquals((i-1)*10, data.getPosition());
		}
		
		reader.close();
	}

	@Test
	public void ReadTooLargeTime() throws IOException {		
		IndexReader reader = new IndexReader("/tmp/idx"); //home/julian/workspace/PIOsimHD/HDTraceConverter/Example/test_0_0_stat_Energy.idx
		
		IndexData data =  reader.getFirstInfoWithTime(new Epoch(1000000, 0));
		org.junit.Assert.assertNull(data);
	}
	
	
	@Test
	public void CreateTraceFileIndex() throws Exception{
		IndexCreator creator = new IndexCreator();
		
		creator.createIndexForTraceFile("../HDTraceConverter/Example/test_0_0.xml");
	}
	
	@Test
	public void CreateTraceStatisticFileIndex() throws Exception{
		IndexCreator creator = new IndexCreator();
	
		creator.createIndexForStatisticFile("../HDTraceConverter/Example/test.xml", "Energy", 0, 0);
	}
}
