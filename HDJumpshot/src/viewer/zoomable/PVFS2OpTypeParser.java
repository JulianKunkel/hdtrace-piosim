package viewer.zoomable;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Julian M. Kunkel
 *
 */
public class PVFS2OpTypeParser {
	private HashMap<Integer, String> mappingOp = new  HashMap<Integer, String> ();
	private HashMap<Integer, String> mappingEvent = new  HashMap<Integer, String> ();

	private static PVFS2OpTypeParser instance = new PVFS2OpTypeParser();

	public static PVFS2OpTypeParser getInstance(){
		return instance;
	}

	public void loadConfig(){
		loadConfig("pvfs2-server-ops.cfg");
	}

	public void loadConfig(String file){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			loadConfig(reader);

		}catch(Exception e){
			System.err.println("Warning cannot parse PVFS2 operations from file "
					+ file + " error was: " + e.getMessage());
		}
	}

	public void loadConfig(StringReader sreader){
		try{
			BufferedReader reader = new BufferedReader(sreader);
			loadConfig(reader);

		}catch(Exception e){
			System.err.println("Warning cannot parse PVFS2 operations " +
					" error was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadConfig(BufferedReader reader) throws IOException{
		StringBuffer buffer = new StringBuffer();
		while(reader.ready()){
			String line = reader.readLine();
			if (line == null) break;
			buffer.append( line + "\n");
		}

		String fileContent = buffer.toString();
		Pattern pattern =  Pattern.compile("enum[ \n\t]*PVFS_server_op[ \n\t]*\\{([^}]*)\\}", Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = pattern.matcher( fileContent );

		if(! m.find()){
			throw new IOException("Warning could not find operation enum in file, cannot parse PVFS2 operations" );
		}

		String enumString = m.group(1);
		if( enumString != null){
			pattern = Pattern.compile("PVFS_SERV_([^ =]*).*=[^0-9]*([0-9]+)");
			m  = pattern.matcher(enumString);
			while(m.find()){
				String type = m.group(1);
				Integer val = Integer.parseInt(m.group(2));

				mappingOp.put(val, type);
			}
		}else{
			throw new IOException("Warning could not find operation enum in file, cannot parse PVFS2 operations" );
		}

		pattern = Pattern.compile("enum[ \n\t]*PVFS_event_op[ \n\t]*\\{([^}]*)\\}", Pattern.DOTALL | Pattern.MULTILINE);
		m = pattern.matcher( fileContent );

		if(! m.find()){
			throw new IOException("Warning could not find event enum, cannot parse PVFS2 operations" );
		}

		enumString = m.group(1);
		if( enumString != null){
			pattern = Pattern.compile("PVFS_EVENT_([^ =]*).*=[^0-9]*([0-9]+)");
			m  = pattern.matcher(enumString);
			while(m.find()){
				String type = m.group(1);
				Integer val = Integer.parseInt(m.group(2));

				mappingEvent.put(val, type);
			}
		}else{
			throw new IOException("Warning could not find operation enum, cannot parse PVFS2 operations" );
		}

		reader.close();
		buffer = null;
	}

	private PVFS2OpTypeParser(){

	}

	public String getOperationType(Integer type){
		return mappingOp.get(type);
	}

	public String getEventType(Integer type){
		return mappingEvent.get(type);
	}

	/**
	 * Testfunction parses input file.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Operation TYPE:" +  PVFS2OpTypeParser.getInstance().getOperationType(1));
		System.out.println("Event TYPE:" +  PVFS2OpTypeParser.getInstance().getEventType(1));
	}

}
