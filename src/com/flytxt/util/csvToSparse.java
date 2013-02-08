/**
 * 
 */
package com.flytxt.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;


/**
 * Converts csv file to sparse file and then to scaled file.
 * <br>
 * Usage: <br>
				csvToSparse &lt;file_name&gt; -l &lt;lower range&gt; -s &lt;range file path&gt;
				<br>
				csvToSparse  &lt;file_name&gt; -r &lt;range file path&gt;  
				Output File is saved at the same location as input but with an extension .sparse.scale					
 * @author rahul.mishra
 *
 */
public class csvToSparse {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
				
		try {
			System.out.println("Start creating sparse file");
			createLibsvmFormat(args[0]);
			System.out.println("Start creating scale file");
			String[] scaleArray = new String[args.length];
			System.arraycopy(args, 1, scaleArray, 0,args.length-1);
			scaleArray[args.length-1] = args[0]+".sparse";
			svm_scale s = new svm_scale();			
			s.run(scaleArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Creates a sparse file from csv file assuming last field to be the label
	 * @param csvfilepath
	 * @throws IOException
	 * Output File is saved at the same location as input but with an extension .sparse	
	 */
	public static void createLibsvmFormat(String csvfilepath) throws IOException
	{
		BufferedReader input = new BufferedReader(new FileReader(csvfilepath));
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(csvfilepath+".sparse")));
		
		String line = null;
		String current = null;
		int index = 0;
		
		while(true)
		{
			line = input.readLine();
			if(line == null) break;
			
			StringTokenizer st = new StringTokenizer(line,",");
			
			//current = null;
			
			StringBuilder sb = new StringBuilder();
			index=0;
			while((current=st.nextToken()) != null)
			{
			 			 
			 if(!st.hasMoreTokens())
			 {
				 sb.insert(0, current);
				 break;
			 }
			 index++;
			 if(Double.parseDouble(current)!=0)
				 sb.append(" "+index+":"+current);
			}
		
				
			output.writeBytes(sb.toString()+"\n");			
			
								
		}
		
		input.close();
		output.close();
		System.out.println("Sparse File "+csvfilepath+".sparse created.");
		
	}

}
