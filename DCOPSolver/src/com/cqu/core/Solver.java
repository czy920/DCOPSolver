package com.cqu.core;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cqu.aco.PublicConstants;
import com.cqu.cyclequeue.AgentManagerCycle;
import com.cqu.cyclequeue.MessageMailerCycle;
import com.cqu.main.DOTrenderer;
import com.cqu.main.Debugger;
import com.cqu.parser.Problem;
import com.cqu.parser.ProblemParser;
import com.cqu.settings.Settings;
import com.cqu.tree.TreeGenerator;
import com.cqu.util.FileUtil;
import com.cqu.varOrdering.dfs.DFSgeneration;
import com.cqu.visualtree.GraphFrame;
import com.cqu.visualtree.TreeFrame;

public class Solver {
	
	private List<Result> results=new ArrayList<Result>();
	private List<Result> resultsRepeated;
	private String algorithmType = null;

	public void solve(String problemPath, String agentType, boolean showTreeFrame, boolean debug, EventListener el)
	{
		//parse problem xml
		String treeGeneratorType=null;
		if(agentType.startsWith("ACO")){
			PublicConstants.ACO_type = agentType;
		}
		if(agentType.equals("BFSDPOP")||agentType.equals("ALSDSA")||agentType.equals("ALS_DSA")||
				agentType.equals("DSA_PPIRA")||agentType.equals("DSA_SDP")||agentType.equals("ALSMLUDSA")||agentType.equals("ALSDSAMGM")||
				agentType.equals("ALSDSAMGMEVO")||agentType.equals("ALSDSADSAEVO")||agentType.equals("ALSDGA")||agentType.equals("ALSDGAFB")||
				agentType.equals("PDSALSDSA")||agentType.equals("PDSALSDSAN")||agentType.equals("PDSALSMGM")||agentType.equals("PDSALSMGM2")||
				agentType.equals("PDSDSASDP")||agentType.equals("ALS_GDBA"))
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_BFS;
		}else
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_DFS;
		}
		
		Problem problem=null;
		ProblemParser parser=new ProblemParser(problemPath, treeGeneratorType);
		problem=parser.parse();
		
		if(problem==null)
		{
			return;
		}
		
		if(Settings.settings.isDisplayGraphFrame()==true)
		{
			//display constraint graph
			GraphFrame graphFrame=new GraphFrame(problem.neighbourAgents);
			graphFrame.showGraphFrame();
			//new DOTrenderer ("Constraint graph", ProblemParser.toDOT(problemPath));
		}
		//display DFS tree，back edges not included
		if(showTreeFrame==true)
		{
			//TreeFrame treeFrame=new TreeFrame(DFSTree.toTreeString(problem.agentNames, problem.parentAgents, problem.childAgents));
			//treeFrame.showTreeFrame();
			new DOTrenderer ("DFS tree", DFSgeneration.dfsToString());
		}
		
		//set whether to print running data records
		Debugger.init(problem.agentNames);
		Debugger.debugOn=debug;
		
		//采用同步消息机制的算法
		if(agentType.equals("BNBADOPT")||agentType.equals("BDADOPT")||agentType.equals("ADOPT_K")||agentType.equals("SynAdopt1")||agentType.equals("SynAdopt2")||									
				agentType.equals("DSA_A")||agentType.equals("DSA_B")||agentType.equals("DSA_C")||agentType.equals("DSA_D")||agentType.equals("DSA_E")||agentType.equals("DSAN")||
				agentType.equals("MGM")||agentType.equals("MGM2")||agentType.equals("KOPT")||agentType.equals("ALSDSA")||
				agentType.equals("ALS_DSA")||agentType.equals("DSA_PPIRA")||agentType.equals("DSA_SDP")||agentType.equals("ALS_GDBA")||agentType.equals("ALSMLUDSA")||
				agentType.equals("ALSDSAMGM")||agentType.equals("ALSDSAMGMEVO")||agentType.equals("ALSDSADSAEVO")||agentType.equals("ALSDGA")||agentType.equals("ALSDGAFB")||
				agentType.equals("PDSALSDSA")||agentType.equals("PDSALSDSAN")||agentType.equals("PDSALSMGM")||agentType.equals("PDSALSMGM2")||
				agentType.equals("PDSDSAN")||agentType.equals("PDSMGM")||agentType.equals("PDSMGM2")||agentType.equals("PDSDSASDP")||
				agentType.equals("MAXSUM")||agentType.equals("ACO")||agentType.equals("ACO_tree")||agentType.equals("ACO_bf")||agentType.equals("ACO_phase")||
				agentType.equals("ACO_line")||agentType.equals("ACO_final")||agentType.equals("MAXSUMADVP")||agentType.equals("SBB")||agentType.equals("DGIBBS")
				||agentType.equals("MAXSUMCONVP")||agentType.equals("MAXSUMADSSVP")||agentType.equals("MAXSUMADPVP"))

		//if(agentType.equals("BNBADOPT")||agentType.equals("ADOPT"))
		{
			//construct agents
			AgentManagerCycle agentManagerCycle=new AgentManagerCycle(problem, agentType);
			MessageMailerCycle msgMailer=new MessageMailerCycle(agentManagerCycle);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			
			//先让MsgMailer启动并初始化完成，用while循环判断变量不行，会导致死循环，加一个sleep就行。
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			msgMailer.initWait();						//为避免出现Agent线程开始而Mailer未初始化完成而出现错误
			
			agentManagerCycle.startAgents(msgMailer);
		}
		//采用异步消息机制的算法
		else
		{
			//construct agents
			AgentManager agentManager=new AgentManager(problem, agentType);
			MessageMailer msgMailer=new MessageMailer(agentManager);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			agentManager.startAgents(msgMailer);
		}
	}
	
	public void batSolve(final String problemDir, final String agentType, final int repeatTimes, final EventListener el, final BatSolveListener bsl)
	{
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub

				final File[] files = new File(problemDir).listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File pathname) {
						// TODO Auto-generated method stub
						if(pathname.getName().endsWith(".xml")==true)
						{
							return true;
						}
						return false;
					}
				});
				
				AtomicBoolean problemSolved=new AtomicBoolean(false);
				int i=0;
				for(i=0;i<files.length;i++)
				{
					resultsRepeated=new ArrayList<Result>();
					int k=0;
					for(k=0;k<repeatTimes;k++)
					{
						batSolveEach(files[i].getPath(), agentType, problemSolved);
						
						synchronized (problemSolved) {
							while(problemSolved.get()==false)
							{
								try {
									problemSolved.wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									Thread.currentThread().interrupt();
									break;
								}
							}
							if(problemSolved.get()==true)
							{
								problemSolved.set(false);
							}
						}
						
						final int problemIndex=i;
						final int timeIndex=k;
						//refresh progress
						EventQueue.invokeLater(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								bsl.progressChanged(files.length, problemIndex, timeIndex);
							}
							
						});
					}
					if(k<repeatTimes)
					{
						break;
					}
					results.add(disposeRepeated(repeatTimes));
				}
				if(i>=files.length)
				{
					//write results to storage
					writeResultToStorage(problemDir);
					
					el.onFinished(null);
				}
			}
			
		}).start();
	}
	
	private void writeResultToStorage(String problemDir)
	{
		String path;
		if(problemDir.endsWith("\\"))
		{
			path=problemDir+"result";
		}else
		{
			path=problemDir+"\\result";
		}
		File f=new File(path);
		if(f.exists()==false)
		{
			f.mkdir();
		}
		
		Result rs=results.get(0);
		if(rs instanceof ResultAdopt)
		{
			String totalTime="";
			String totalCost="";
			String messageQuantity="";
			String nccc="";
			for(int i=0;i<results.size();i++)
			{
				ResultAdopt result=(ResultAdopt) results.get(i);
				totalTime+=result.totalTime+"\n";
				totalCost+=result.totalCost+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				nccc+=result.nccc+"\n";
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");
		}else if(rs instanceof ResultDPOP)
		{
			String totalTime="";
			String totalCost="";
			String messageQuantity="";
			String messageSizeMax="";
			String messageSizeAvg="";
			for(int i=0;i<results.size();i++)
			{
				ResultDPOP result=(ResultDPOP) results.get(i);
				totalTime+=result.totalTime+"\n";
				totalCost+=result.totalCost+"\n";
				messageQuantity+=Math.round(result.messageQuantity)+"\n";
				messageSizeMax+=Math.round(result.utilMsgSizeMax)+"\n";
				messageSizeAvg+=Math.round(result.utilMsgSizeAvg)+"\n";
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(messageSizeMax, path+"\\messageSizeMax.txt");
			FileUtil.writeString(messageSizeAvg, path+"\\messageSizeAvg.txt");
		}
		else if(rs instanceof ResultCycleAls){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
			String nccc="";
			String myTotalCostInCycle = "";
			String myBestCostInCycle = "";
			String myTimeCostInCycle = "";
			String myMessageQuantityInCycle = "";

			ResultCycleAls resultAvg = new ResultCycleAls();
			String totalTimeAvg="";
			String messageQuantityAvg="";
			String totalCostAvg="";
			String ncccAvg="";
			String myTotalCostInCycleAvg = "";
			String myTimeCostInCycleAvg = "";
			String myMessageQuantityInCycleAvg = "";
			String myBestCostInCycleAvg = "";

			for(int i=0;i<results.size();i++)
			{
				ResultCycleAls result=(ResultCycleAls) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				nccc+=result.nccc+"\n";
				myTotalCostInCycle = "";
				myBestCostInCycle = "";
				myTimeCostInCycle = "";
				myMessageQuantityInCycle = "";
				for(int j=0; j < result.totalCostInCycle.length; j++){
					myTotalCostInCycle += result.totalCostInCycle[j] + "\n";
					myTimeCostInCycle +=result.timeCostInCycle[j] + "\n";
					myMessageQuantityInCycle +=result.messageQuantityInCycle[j] + "\n";
				}
				for(int j = 0; j < result.bestCostInCycle.length; j++)
					myBestCostInCycle += result.bestCostInCycle[j] + "\n";
				FileUtil.writeString(myTotalCostInCycle, path+"\\totalCostInCycle_"+i+".txt");
				FileUtil.writeString(myBestCostInCycle, path+"\\bestCostInCycle_"+i+".txt");
				FileUtil.writeString(myTimeCostInCycle, path+"\\timeCostInCycle_"+i+".txt");
				FileUtil.writeString(myMessageQuantityInCycle, path+"\\messageQuantityInCycle_"+i+".txt");

				resultAvg.addAvg(result);
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");

			resultAvg.avg(results.size());
			totalTimeAvg+=resultAvg.totalTime+"\n";
			messageQuantityAvg+=resultAvg.messageQuantity+"\n";
			totalCostAvg+=resultAvg.totalCost+"\n";
			ncccAvg+=resultAvg.nccc+"\n";
			myTotalCostInCycleAvg = "";
			myTimeCostInCycleAvg = "";
			myMessageQuantityInCycleAvg = "";
			for(int j=0; j < resultAvg.totalCostInCycle.length; j++){
				myTotalCostInCycleAvg += resultAvg.totalCostInCycle[j] + "\n";
				myTimeCostInCycleAvg +=resultAvg.timeCostInCycle[j] + "\n";
				myMessageQuantityInCycleAvg +=resultAvg.messageQuantityInCycle[j] + "\n";
			}
			for(int j = 0; j < resultAvg.bestCostInCycle.length; j++)
				myBestCostInCycleAvg += resultAvg.bestCostInCycle[j] + "\n";
			FileUtil.writeString(totalTimeAvg, path+"\\totalTime_AVG.txt");
			FileUtil.writeString(messageQuantityAvg, path+"\\messageQuantity_AVG.txt");
			FileUtil.writeString(totalCostAvg, path+"\\totalCost_AVG.txt");
			FileUtil.writeString(ncccAvg, path+"\\nccc_AVG.txt");
			FileUtil.writeString(myTotalCostInCycleAvg, path+"\\totalCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myTimeCostInCycleAvg, path+"\\timeCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myMessageQuantityInCycleAvg, path+"\\messageQuantityInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myBestCostInCycleAvg, path+"\\bestCostInCycle_"+"AVG"+".txt");
		}
		else if(rs instanceof ResultCycle && !this.algorithmType.startsWith("ACO")){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
			String nccc="";
			String myTotalCostInCycle = "";
			String myTimeCostInCycle = "";
			String myMessageQuantityInCycle = "";

			ResultCycle resultAvg = new ResultCycle();
			String totalTimeAvg="";
			String messageQuantityAvg="";
			String totalCostAvg="";
			String ncccAvg="";
			String myTotalCostInCycleAvg = "";
			String myTimeCostInCycleAvg = "";
			String myMessageQuantityInCycleAvg = "";

			for(int i=0;i<results.size();i++)
			{
				ResultCycle result=(ResultCycle) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				nccc+=result.nccc+"\n";
				myTotalCostInCycle = "";
				myTimeCostInCycle = "";
				myMessageQuantityInCycle = "";
				for(int j=0; j < result.totalCostInCycle.length; j++){
					myTotalCostInCycle += result.totalCostInCycle[j] + "\n";
					myTimeCostInCycle +=result.timeCostInCycle[j] + "\n";
					myMessageQuantityInCycle +=result.messageQuantityInCycle[j] + "\n";
				}
				FileUtil.writeString(myTotalCostInCycle, path+"\\totalCostInCycle_"+i+".txt");
				FileUtil.writeString(myTimeCostInCycle, path+"\\timeCostInCycle_"+i+".txt");
				FileUtil.writeString(myMessageQuantityInCycle, path+"\\messageQuantityInCycle_"+i+".txt");

				resultAvg.addAvg(result);
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");

			resultAvg.avg(results.size());
			totalTimeAvg+=resultAvg.totalTime+"\n";
			messageQuantityAvg+=resultAvg.messageQuantity+"\n";
			totalCostAvg+=resultAvg.totalCost+"\n";
			ncccAvg+=resultAvg.nccc+"\n";
			myTotalCostInCycleAvg = "";
			myTimeCostInCycleAvg = "";
			myMessageQuantityInCycleAvg = "";
			for(int j=0; j < resultAvg.totalCostInCycle.length; j++){
				myTotalCostInCycleAvg += resultAvg.totalCostInCycle[j] + "\n";
				myTimeCostInCycleAvg +=resultAvg.timeCostInCycle[j] + "\n";
				myMessageQuantityInCycleAvg +=resultAvg.messageQuantityInCycle[j] + "\n";
			}
			FileUtil.writeString(totalTimeAvg, path+"\\totalTime_AVG.txt");
			FileUtil.writeString(messageQuantityAvg, path+"\\messageQuantity_AVG.txt");
			FileUtil.writeString(totalCostAvg, path+"\\totalCost_AVG.txt");
			FileUtil.writeString(ncccAvg, path+"\\nccc_AVG.txt");
			FileUtil.writeString(myTotalCostInCycleAvg, path+"\\totalCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myTimeCostInCycleAvg, path+"\\timeCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myMessageQuantityInCycleAvg, path+"\\messageQuantityInCycle_"+"AVG"+".txt");

		}
		//蚁群算法引入
		else if(rs instanceof ResultCycle && this.algorithmType.startsWith("ACO")){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
		
			String AnttotalCostInCycle = "";
			String AntbestCostInCycle = "";
			String myMessageQuantityInCycle = "";
			String myTimeCostInCycle = "";
			
			for(int i=0;i<results.size();i++)
			{
				ResultCycle result=(ResultCycle) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				
				AnttotalCostInCycle = "";
				AntbestCostInCycle = "";
				myMessageQuantityInCycle = "";
				myTimeCostInCycle = "";
				
				for(int j = 0; j < result.ant_totalCostInCyle.length; j++){
					AnttotalCostInCycle += result.ant_totalCostInCyle[j] +"\n";
					AntbestCostInCycle += result.ant_bestCostInCycle[j] + "\n";
					myMessageQuantityInCycle +=result.messageQuantityInCycle[j] + "\n";
					myTimeCostInCycle +=result.timeCostInCycle[j] + "\n";
				}
				FileUtil.writeString(AnttotalCostInCycle, path + "\\AnttotalCostInCycle_" + i + ".txt");
				FileUtil.writeString(AntbestCostInCycle, path + "\\AntbestCostInCycle_" + i + ".txt");
				FileUtil.writeString(myMessageQuantityInCycle, path+"\\messageQuantityInCycle_"+i+".txt");
				FileUtil.writeString(myTimeCostInCycle, path+"\\timeCostInCycle_"+i+".txt");
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
		}
		else{
			String totalTime="";
			String totalCost="";
			String messageQuantity="";
			for(int i=0;i<results.size();i++)
			{
				ResultDPOP result=(ResultDPOP) results.get(i);
				totalTime+=result.totalTime+"\n";
				totalCost+=result.totalCost+"\n";
				messageQuantity+=result.messageQuantity+"\n";
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
		}
	}
	
	private Result disposeRepeated(int repeatTimes)
	{
		Result rs=resultsRepeated.get(0);
		Result min;
		Result max;
		Result avg;
		if(rs instanceof ResultAdopt)
		{
			min=new ResultAdopt(rs);
			max=new ResultAdopt(rs);
			avg=new ResultAdopt();
		}else if(rs instanceof ResultDPOP)
		{
			min=new ResultDPOP(rs);
			max=new ResultDPOP(rs);
			avg=new ResultDPOP();
		}else if(rs instanceof ResultCycleAls)
		{
			min=new ResultCycleAls(rs);
			max=new ResultCycleAls(rs);
			avg=new ResultCycleAls();
		}else if(rs instanceof ResultCycle)
		{
			min=new ResultCycle(rs);
			max=new ResultCycle(rs);
			avg=new ResultCycle();
		}else
		{
			min=new Result(rs);
			max=new Result(rs);
			avg=new Result();
		}
		
//		avg.add(rs, repeatTimes-2);
//		for(int i=1;i<resultsRepeated.size();i++)
//		{
//			Result result=resultsRepeated.get(i);
//			min.min(result);
//			max.max(result);
//			avg.add(result, repeatTimes-2);
//		}
//		avg.minus(min, repeatTimes-2);
//		avg.minus(max, repeatTimes-2);
//		return avg;
		
//		writeTemp(resultsRepeated);
		
		avg.add(rs, repeatTimes);
		for(int i=1;i<resultsRepeated.size();i++)
		{
			Result result=resultsRepeated.get(i);
			avg.add(result, repeatTimes);
		}
		return avg;
	}
	
	private void writeTemp(List<Result> results)
	{
		String problemDir = "C:\\Users\\余浙鹏\\Desktop\\Result";
		String path;
		if(problemDir.endsWith("\\"))
		{
			path=problemDir+"result";
		}else
		{
			path=problemDir+"\\result";
		}
		File f=new File(path);
		if(f.exists()==false)
		{
			f.mkdir();
		}
		
		Result rs=results.get(0);
		if(rs instanceof ResultCycleAls){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
			String nccc="";
			String myTotalCostInCycle = "";
			String myBestCostInCycle = "";
			String myTimeCostInCycle = "";
			String myMessageQuantityInCycle = "";

			ResultCycleAls resultAvg = new ResultCycleAls();
			String totalTimeAvg="";
			String messageQuantityAvg="";
			String totalCostAvg="";
			String ncccAvg="";
			String myTotalCostInCycleAvg = "";
			String myTimeCostInCycleAvg = "";
			String myMessageQuantityInCycleAvg = "";
			String myBestCostInCycleAvg = "";

			for(int i=0;i<results.size();i++)
			{
				ResultCycleAls result=(ResultCycleAls) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				nccc+=result.nccc+"\n";
				myTotalCostInCycle = "";
				myBestCostInCycle = "";
				myTimeCostInCycle = "";
				myMessageQuantityInCycle = "";
				for(int j=0; j < result.totalCostInCycle.length; j++){
					myTotalCostInCycle += result.totalCostInCycle[j] + "\n";
					myTimeCostInCycle +=result.timeCostInCycle[j] + "\n";
					myMessageQuantityInCycle +=result.messageQuantityInCycle[j] + "\n";
				}
				for(int j = 0; j < result.bestCostInCycle.length; j++)
					myBestCostInCycle += result.bestCostInCycle[j] + "\n";
				FileUtil.writeString(myTotalCostInCycle, path+"\\totalCostInCycle_"+i+".txt");
				FileUtil.writeString(myBestCostInCycle, path+"\\bestCostInCycle_"+i+".txt");
				FileUtil.writeString(myTimeCostInCycle, path+"\\timeCostInCycle_"+i+".txt");
				FileUtil.writeString(myMessageQuantityInCycle, path+"\\messageQuantityInCycle_"+i+".txt");

				resultAvg.addAvg(result);
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");

			resultAvg.avg(results.size());
			totalTimeAvg+=resultAvg.totalTime+"\n";
			messageQuantityAvg+=resultAvg.messageQuantity+"\n";
			totalCostAvg+=resultAvg.totalCost+"\n";
			ncccAvg+=resultAvg.nccc+"\n";
			myTotalCostInCycleAvg = "";
			myTimeCostInCycleAvg = "";
			myMessageQuantityInCycleAvg = "";
			for(int j=0; j < resultAvg.totalCostInCycle.length; j++){
				myTotalCostInCycleAvg += resultAvg.totalCostInCycle[j] + "\n";
				myTimeCostInCycleAvg +=resultAvg.timeCostInCycle[j] + "\n";
				myMessageQuantityInCycleAvg +=resultAvg.messageQuantityInCycle[j] + "\n";
			}
			for(int j = 0; j < resultAvg.bestCostInCycle.length; j++)
				myBestCostInCycleAvg += resultAvg.bestCostInCycle[j] + "\n";
			FileUtil.writeString(totalTimeAvg, path+"\\totalTime_AVG.txt");
			FileUtil.writeString(messageQuantityAvg, path+"\\messageQuantity_AVG.txt");
			FileUtil.writeString(totalCostAvg, path+"\\totalCost_AVG.txt");
			FileUtil.writeString(ncccAvg, path+"\\nccc_AVG.txt");
			FileUtil.writeString(myTotalCostInCycleAvg, path+"\\totalCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myTimeCostInCycleAvg, path+"\\timeCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myMessageQuantityInCycleAvg, path+"\\messageQuantityInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myBestCostInCycleAvg, path+"\\bestCostInCycle_"+"AVG"+".txt");
		}
		else if(rs instanceof ResultCycle && !this.algorithmType.startsWith("ACO")){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
			String nccc="";
			String myTotalCostInCycle = "";
			String myTimeCostInCycle = "";
			String myMessageQuantityInCycle = "";

			ResultCycle resultAvg = new ResultCycle();
			String totalTimeAvg="";
			String messageQuantityAvg="";
			String totalCostAvg="";
			String ncccAvg="";
			String myTotalCostInCycleAvg = "";
			String myTimeCostInCycleAvg = "";
			String myMessageQuantityInCycleAvg = "";

			for(int i=0;i<results.size();i++)
			{
				ResultCycle result=(ResultCycle) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				nccc+=result.nccc+"\n";
				myTotalCostInCycle = "";
				myTimeCostInCycle = "";
				myMessageQuantityInCycle = "";
				for(int j=0; j < result.totalCostInCycle.length; j++){
					myTotalCostInCycle += result.totalCostInCycle[j] + "\n";
					myTimeCostInCycle +=result.timeCostInCycle[j] + "\n";
					myMessageQuantityInCycle +=result.messageQuantityInCycle[j] + "\n";
				}
				FileUtil.writeString(myTotalCostInCycle, path+"\\totalCostInCycle_"+i+".txt");
				FileUtil.writeString(myTimeCostInCycle, path+"\\timeCostInCycle_"+i+".txt");
				FileUtil.writeString(myMessageQuantityInCycle, path+"\\messageQuantityInCycle_"+i+".txt");

				resultAvg.addAvg(result);
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");

			resultAvg.avg(results.size());
			totalTimeAvg+=resultAvg.totalTime+"\n";
			messageQuantityAvg+=resultAvg.messageQuantity+"\n";
			totalCostAvg+=resultAvg.totalCost+"\n";
			ncccAvg+=resultAvg.nccc+"\n";
			myTotalCostInCycleAvg = "";
			myTimeCostInCycleAvg = "";
			myMessageQuantityInCycleAvg = "";
			for(int j=0; j < resultAvg.totalCostInCycle.length; j++){
				myTotalCostInCycleAvg += resultAvg.totalCostInCycle[j] + "\n";
				myTimeCostInCycleAvg +=resultAvg.timeCostInCycle[j] + "\n";
				myMessageQuantityInCycleAvg +=resultAvg.messageQuantityInCycle[j] + "\n";
			}
			FileUtil.writeString(totalTimeAvg, path+"\\totalTime_AVG.txt");
			FileUtil.writeString(messageQuantityAvg, path+"\\messageQuantity_AVG.txt");
			FileUtil.writeString(totalCostAvg, path+"\\totalCost_AVG.txt");
			FileUtil.writeString(ncccAvg, path+"\\nccc_AVG.txt");
			FileUtil.writeString(myTotalCostInCycleAvg, path+"\\totalCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myTimeCostInCycleAvg, path+"\\timeCostInCycle_"+"AVG"+".txt");
			FileUtil.writeString(myMessageQuantityInCycleAvg, path+"\\messageQuantityInCycle_"+"AVG"+".txt");

		}
	}
	
	
	
	
	
	private void batSolveEach(String problemPath, String algorithmType, final AtomicBoolean problemSolved)
	{
		String treeGeneratorType=null;
		this.algorithmType = algorithmType;
		if(algorithmType.startsWith("ACO")){
			PublicConstants.ACO_type = algorithmType;
		}
		if(algorithmType.equals("BFSDPOP")
				|| algorithmType.equals("ALS_DSA") || algorithmType.equals("DSA_PPIRA") || algorithmType.equals("DSA_SDP") || algorithmType.equals("ALS_GDBA")
				|| algorithmType.equals("ALSMLUDSA") || algorithmType.equals("ALSDSAMGM") || algorithmType.equals("ALSDSAMGMEVO") || algorithmType.equals("ALSDSADSAEVO")
				|| algorithmType.equals("ALSDGA") || algorithmType.equals("ALSDGAFB") || algorithmType.equals("PDSALSDSA") || algorithmType.equals("PDSALSDSAN") 
				|| algorithmType.equals("PDSALSMGM") || algorithmType.equals("PDSALSMGM2") || algorithmType.equals("PDSDSASDP"))
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_BFS;
		}else
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_DFS;
		}
		
		Problem problem=null;
		ProblemParser parser=new ProblemParser(problemPath, treeGeneratorType);
		problem=parser.parse();

		if(problem==null)
		{
			synchronized (problemSolved) {
				problemSolved.set(true);
			}
			return;
		}
		
		//set whether to print running data records
		Debugger.init(problem.agentNames);
		Debugger.debugOn=false;
		
		//start agents and MessageMailer
		EventListener el=new EventListener() {
			
			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinished(Object result) {
				// TODO Auto-generated method stub
				synchronized (problemSolved) {
					resultsRepeated.add((Result)result);
					problemSolved.set(true);
					problemSolved.notifyAll();
				}
			}
		};
		
		//采用同步消息机制的算法
		if(algorithmType.equals("BNBADOPT")||algorithmType.equals("ADOPT_K")||algorithmType.equals("BDADOPT")||algorithmType.equals("SynAdopt1")||algorithmType.equals("SynAdopt2")||
				algorithmType.equals("DSA_A")||algorithmType.equals("DSA_B")||algorithmType.equals("DSA_C")||algorithmType.equals("DSA_D")||algorithmType.equals("DSA_E")||algorithmType.equals("DSAN")||
				algorithmType.equals("MGM")||algorithmType.equals("MGM2")||
				algorithmType.equals("ALS_DSA")||algorithmType.equals("DSA_PPIRA")||algorithmType.equals("DSA_SDP")||algorithmType.equals("ALS_GDBA")||algorithmType.equals("ALSMLUDSA")||
				algorithmType.equals("ALSDSAMGM")||algorithmType.equals("ALSDSAMGMEVO")||algorithmType.equals("ALSDSADSAEVO")||algorithmType.equals("ALSDGA")||algorithmType.equals("ALSDGAFB")||
				algorithmType.equals("PDSALSDSA")||algorithmType.equals("PDSALSDSAN")||algorithmType.equals("PDSALSMGM")||algorithmType.equals("PDSALSMGM2")||
				algorithmType.equals("PDSDSAN")||algorithmType.equals("PDSMGM")||algorithmType.equals("PDSMGM2")||algorithmType.equals("PDSDSASDP")||
				algorithmType.equals("ACO")||algorithmType.equals("ACO_tree")||algorithmType.equals("ACO_bf")||algorithmType.equals("ACO_phase")||
				algorithmType.equals("ACO_line")||algorithmType.equals("ACO_final")||algorithmType.equals("ACO")||algorithmType.equals("ACO_tree")||algorithmType.equals("ACO_bf")||
				algorithmType.equals("ACO_phase")||algorithmType.equals("ACO_line")||algorithmType.equals("ACO_final") || algorithmType.equals("MAXSUMRS") || algorithmType.equals("MAXSUMAD")||algorithmType.equals("DGIBBS"))
		//if(algorithmType.equals("BNBADOPT")||algorithmType.equals("ADOPT"))
		{
			//construct agents
			AgentManagerCycle agentManagerCycle=new AgentManagerCycle(problem, algorithmType);
			MessageMailerCycle msgMailer=new MessageMailerCycle(agentManagerCycle);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			
			//先让MsgMailer启动并初始化完成，用while循环判断变量不行，会导致死循环，加一个sleep就行。
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			msgMailer.initWait();						//为避免出现Agent线程开始而Mailer未初始化完成而出现错误
			
			agentManagerCycle.startAgents(msgMailer);
		}
		//采用异步消息机制的算法
		else
		{
			//construct agents
			AgentManager agentManager=new AgentManager(problem, algorithmType);
			MessageMailer msgMailer=new MessageMailer(agentManager);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			agentManager.startAgents(msgMailer);
		}
	}
	
	public interface BatSolveListener
	{
		void progressChanged(int problemTotalCount, int problemIndex, int timeIndex);
	}

}
