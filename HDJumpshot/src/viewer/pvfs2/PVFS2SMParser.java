package viewer.pvfs2;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Julian M. Kunkel
 *
 */
public class PVFS2SMParser {
	protected class SMData{
		public Integer sm_number;
		public String  sm_name;
		public HashMap<Integer, String> state_names = new HashMap<Integer, String>();

		@Override
		public boolean equals(Object obj) {
			if ( obj instanceof SMData) {
				return ((SMData) obj).sm_number == this.sm_number;
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return this.sm_number;
		}
	};

	private HashMap<Integer, SMData> mappingSM = new  HashMap<Integer, SMData> ();

	private static PVFS2SMParser instance = new PVFS2SMParser();

	public static PVFS2SMParser getInstance(){
		return instance;
	}

	public void loadConfig(){
		loadConfig("pvfs2-sm.conf");
	}

	public void loadConfig(StringReader sreader){
		try{
			BufferedReader reader = new BufferedReader(sreader);
			loadConfig(reader);

		}catch(Exception e){
			System.err.println("Warning cannot parse PVFS2 sms error was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void loadConfig(String file){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			loadConfig(reader);
		}catch(Exception e){
			System.err.println("Warning cannot parse PVFS2 sms from file "
					+ file +" error was: " + e.getMessage());
		}
	}

	private void loadConfig(BufferedReader reader) throws IOException{
		while(reader.ready()){
			int cur_sm_number_in_line = 0;
			String line = reader.readLine();
			if(line == null ) break;

			Pattern pattern =  Pattern.compile("([^:]*):([^:]*):(.*)", Pattern.DOTALL);
			Matcher m = pattern.matcher( line );

			if(! m.find()){
				throw new IOException("Warning could not find valid sm line cannot parse SM in line " + line );
			}

			String sm_ids = m.group(1).trim();
			String sm_names = m.group(2).trim();
			String sm_states = m.group(3).trim();

			ArrayList<Integer> sm_number_list = new ArrayList<Integer>();
			ArrayList<String> sm_name_list = new ArrayList<String>();

			StringTokenizer tok = new StringTokenizer(sm_ids, " ");
			while(tok.hasMoreTokens()){
				sm_number_list.add( Integer.parseInt( tok.nextToken()));
			}

			tok = new StringTokenizer(sm_names, " ");
			while(tok.hasMoreTokens()){
				String cur = tok.nextToken();
				/* strip pvfs2-... prefix from name */
				sm_name_list.add( cur.substring(7, cur.length()-1) );
			}

			tok = new StringTokenizer(sm_states, " ");
			int old_value = -1;
			SMData cur_SM = new SMData();
			cur_SM.sm_name = sm_name_list.get(cur_sm_number_in_line);
			cur_SM.sm_number = sm_number_list.get(cur_sm_number_in_line);

			mappingSM.put(cur_SM.sm_number , cur_SM);

			while(tok.hasMoreTokens()){
				Integer value =  Integer.parseInt( tok.nextToken());
				String state_name = tok.nextToken();
				state_name = state_name.substring(1, state_name.length()-1);
				if (value < old_value){ /* parse the next sm */
					cur_sm_number_in_line++;
					cur_SM = new SMData();
					cur_SM.sm_name = sm_name_list.get(cur_sm_number_in_line);
					cur_SM.sm_number = sm_number_list.get(cur_sm_number_in_line);
					mappingSM.put(cur_SM.sm_number , cur_SM);
				}
				cur_SM.state_names.put(value, state_name);
				old_value = value;
			}


		}
		reader.close();
	}


	private PVFS2SMParser(){

	}

	public String getStateMaschineName(Integer sm){
		return mappingSM.get(sm).sm_name;
	}

	public String getStateName(Integer sm, Integer state){
		return mappingSM.get(sm).state_names.get(state);
	}

	/**
	 * Testfunction parses input file.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(" SM 14 name:" +  PVFS2SMParser.getInstance().getStateMaschineName(14) +
				"\n SM 90 name:" + PVFS2SMParser.getInstance().getStateMaschineName(90));
		System.out.println("State name:" +  PVFS2SMParser.getInstance().getStateName(90,2));
	}

}

