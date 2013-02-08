package com.flytxt.churn;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.*;

/**
 * A prototype for churn prediction of the mobile subscribers using LibSVM [<a href="http://www.csie.ntu.edu.tw/~cjlin/libsvm/">http://www.csie.ntu.edu.tw/~cjlin/libsvm/</a>]
 * @author rahul.mishra
 *
 */
public class churntoy {

	
	/**
	 * Usage: <br>
				churntoy -i &lt;train_file_path&gt; -m &lt;model_file_path&gt;
				<br>
				churntoy -t &lt;test_file_path&gt; -m &lt;model_file_path&gt; -o &lt;output_file_path&gt;
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Better handling of input params
		System.out.println("\nUsage:\n\t"
				+"churntoy -i <train_file_path> -m <model_file_path>\n\t"
				+"churntoy -t <test_file_path> -m <model_file_path> -o <output_file_path>\n");		
		if((args[0]).equals("-i"))
		{			
			learnModel(args[1], args[3]);
		}
		else if((args[0]).equals("-t"))
		{	try {
				testConsensusModel(args[1], args[3], args[5]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			System.out.println("Wrong Usage");

	}
	
	/**
	 * This method creates a model from the training data and saves it into a file.
	 * 
	 * @param train_file	path of the training file. This needs to be in libsvm format. 
	 * @param model_file	path of the model file. Model needs to be persisted in this file for prediction usage. 
	 */
	public static void learnModel(String train_file, String model_file){
		
	    svm_parameter svmParameter = new svm_parameter();
	    svmParameter.svm_type = svm_parameter.C_SVC;
	    svmParameter.kernel_type = svm_parameter.RBF;
	    //svmParameter.nu = .25;
	    svmParameter.gamma = 16;
	    svmParameter.C = 16;
	    svmParameter.cache_size = 200;
	    svmParameter.eps = 0.001;
	    //double[] weight = {1,1};
	    //int[] weight_lbl = {1,0};
	    //svmParameter.weight = weight;
	    //svmParameter.weight_label = weight_lbl;
	    svmParameter.shrinking = 0;
	    
	    
	    svm_problem train_prob = null;
		try {
			train_prob = read_problem(train_file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    String chkParam = svm.svm_check_parameter(train_prob, svmParameter);
	    if( chkParam != null)
	    {
	    	System.out.println("Error in Parameter:: "+chkParam);
	    	System.exit(1);
	    }
	    svm_model svmModel = svm.svm_train(train_prob, svmParameter);
	    
	    try {
			svm.svm_save_model(model_file,svmModel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method predict the class based on a model and generates statistics like confusion matrix, F-measure, Precision, recall, and accuracy
	 *  for giving insights about quality of the model.
	 *  It dumps the predicted labels in the output_file 
	 * 
	 * @param test_file	path of the test file 
	 * @param model_file	path of the model_file [model is loaded from this file] 
	 * @param output_file	path of the output_file [predicted labels are output into this file]
	 */
	public static void testModel(String test_file, String model_file, String output_file) throws IOException
	{
		//TODO Error handling like fileNotFound
		BufferedReader input = new BufferedReader(new FileReader(test_file));
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output_file)));
		svm_model model = svm.svm_load_model(model_file);
		
			
		int TP_count = 0, FP_count = 0, FN_count = 0, TN_count = 0;
		
		while(true)
		{
			String line = input.readLine();
			if(line == null) break;
			
			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			int target = atoi(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			
			int v = (int)svm.svm_predict(model,x);
			output.writeBytes(v+"\n");
			
			if(target == 1)
				if(v == target)
					++TP_count;
				else
					++FN_count;
			else
				if(v == target)
					++TN_count;
				else
					++FP_count;
			
			int total = TP_count+TN_count+FP_count+FN_count;  
			/* 
			 * For progress when testing for larger files
			 */
			if(total>10000 && total%10000==0)
			{
				System.out.println("**** Temp output****");
				System.out.println("***********\n " +
						"Confusion Matrix : \n" +
							+TP_count+ "\t|\t" +
							+FP_count+ "\n" +
							+FN_count+ "\t|\t" +
							+TN_count);
			}
								
		}
		
		input.close();
		output.close();
		
		double correct = TP_count+TN_count;
		double total = TP_count+TN_count+FP_count+FN_count;
		System.out.println("*********************");
		System.out.println("**** Final output****");
		System.out.println("*********************");
		System.out.println("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");
		
		System.out.println("***********\n " +
			"Confusion Matrix : \n" +
				+TP_count+ "\t|\t" +
				+FP_count+ "\n" +
				+FN_count+ "\t|\t" +
				+TN_count);
							
		double precision = (double)TP_count/(TP_count+FP_count);
		double recall = (double)TP_count/(TP_count+FN_count);
		double F2 = 2*precision*recall/(precision+recall);
		System.out.println("Precision = "+precision+"\tRecall = "+recall+"\n F2="+F2);
		
		
		
	}
	
	/**
	 * 	
	 * This method predict the class based on 5 model using consensus approach and <br>
	 * generates statistics like confusion matrix, F-measure, Precision, recall, and accuracy
	 *  for giving insights about quality of the model.
	 *  It dumps the predicted labels in the output_file 
	 * 
	 * @param test_file	path of the test file 
	 * @param model_file	path of the model_file [model is loaded from this file] 
	 * @param output_file	path of the output_file [predicted labels are output into this file]
	 */

	public static void testConsensusModel(String test_file, String model_file, String output_file) throws IOException
	{
		//TODO Error handling like fileNotFound
		BufferedReader input = new BufferedReader(new FileReader(test_file));
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output_file)));
		svm_model[] model = new svm_model[5];
		for(int model_num =0; model_num<5;model_num++)
		{
			//TODO provide model names as input in the command line
			model[model_num] = svm.svm_load_model("D1_"+(model_num+1)+"_c16_g16.model");
		}
		
			
		int TP_count = 0, FP_count = 0, FN_count = 0, TN_count = 0;
		
		while(true)
		{
			String line = input.readLine();
			if(line == null) break;
			
			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			int target = atoi(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			
			
			int[] v_list = new int[5];
			for (int model_num=0; model_num<5;model_num++)
			{
			v_list[model_num] =  (int)svm.svm_predict(model[model_num],x);
			}
			int v = (sumall(v_list)<3)?0:1;
			output.writeBytes(v+"\n");
			
			if(target == 1)
				if(v == target)
					++TP_count;
				else
					++FN_count;
			else
				if(v == target)
					++TN_count;
				else
					++FP_count;
			
			int total = TP_count+TN_count+FP_count+FN_count;  
			/* 
			 * For progress when testing for larger files
			 */
			if(total>10000 && total%10000==0)
			{
				System.out.println("**** Temp output****");
				System.out.println("***********\n " +
						"Confusion Matrix : \n" +
							+TP_count+ "\t|\t" +
							+FP_count+ "\n" +
							+FN_count+ "\t|\t" +
							+TN_count);
			}
								
		}
		
		input.close();
		output.close();
		
		double correct = TP_count+TN_count;
		double total = TP_count+TN_count+FP_count+FN_count;
		System.out.println("*********************");
		System.out.println("**** Final output****");
		System.out.println("*********************");
		System.out.println("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");
		
		System.out.println("***********\n " +
			"Confusion Matrix : \n" +
				+TP_count+ "\t|\t" +
				+FP_count+ "\n" +
				+FN_count+ "\t|\t" +
				+TN_count);
							
		double precision = (double)TP_count/(TP_count+FP_count);
		double recall = (double)TP_count/(TP_count+FN_count);
		double F2 = 2*precision*recall/(precision+recall);
		System.out.println("Precision = "+precision+"\tRecall = "+recall+"\n F2="+F2);
		
		
		
	}
	private static int sumall(int[] v_list) {
		int sum=0;
		for(int count=0; count<v_list.length;count++)
		{
			sum+=v_list[count];
		}
		return sum;
	}

	/**
	 * This method converts each line of the input training file into svm nodes. 
	 * Further it collects all these nodes into svm_problem data structure
	 * 
	 * @param file_name	path of the training file 
	 */
	private static svm_problem read_problem(String file_name) throws IOException
	{
		svm_problem prob = new svm_problem();
		BufferedReader fp = new BufferedReader(new FileReader(file_name));
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();

		while(true)
		{
			String line = fp.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			vx.addElement(x);
		}

		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		fp.close();
		return prob;
	}

	private static double atof(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}


}
