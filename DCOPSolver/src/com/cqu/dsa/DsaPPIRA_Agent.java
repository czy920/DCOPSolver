package com.cqu.dsa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.cyclequeue.AgentCycleAls;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

//Anytimg框架下，基于Anytime论文中第一种启发式优化，选择概率p的转换和重启机制
public class DsaPPIRA_Agent  extends AgentCycleAls{
	
	public final static String KEY_NCCC="KEY_NCCC";
	public final static int TYPE_VALUE_MESSAGE=0;
	
	private static int cycleCountEnd;
	private static double p1;
	private static double p2;
	private static int k1;
	private static int k2;
	private static int r;
	
	private double selectProbablity;
	private int nccc = 0;
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private HashMap<Integer, Integer> neighboursValueIndex;			//<neighbour 的 Index, neighbourValue 的  Index>
	
	public DsaPPIRA_Agent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO 自动生成的构造函数存根
	}
	
	protected void initRun() {
		super.initRun();

		cycleCountEnd = Settings.settings.getCycleCountEnd();
		p1 = Settings.settings.getSelectProbability();
		p2 = Settings.settings.getSelectNewProbability();
		k1 = Settings.settings.getSelectStepK1();
		k2 = Settings.settings.getSelectStepK2();
		r = Settings.settings.getSelectRound();
		
		selectProbablity = p1;
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		neighboursValueIndex=new HashMap<Integer, Integer>();
		neighboursQuantity=neighbours.length;
		for(int i=0; i<neighbours.length; i++)
			neighboursValueIndex.put((Integer)i, (Integer)0);
		sendValueMessages();
	}
	
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], DsaPPIRA_Agent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	
	@Override
	protected void disposeMessage(Message msg) {
		// TODO 自动生成的方法存根
		if(msg.getType() == DsaPPIRA_Agent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}
		else if(msg.getType() == DsaPPIRA_Agent.TYPE_ALSCOST_MESSAGE)
		{
			disposeAlsCostMessage(msg);
		}
		else if(msg.getType() == DsaPPIRA_Agent.TYPE_ALSBEST_MESSAGE)
		{
			disposeAlsBestMessage(msg);
		}else
			System.out.println("wrong!!!!!!!!");
	}
	
	
	public void disposeValueMessage(Message msg){
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursValueIndex.put((Integer)senderIndex, (Integer)msg.getValue());
		
		if(receivedQuantity==0){
			
		}
	}
	
	protected void allMessageDisposed() {
		if(cycleCount <= cycleCountEnd){
			cycleCount++;	
			if(cycleCount % (k1+k2) < k1)
				selectProbablity = p1;
			else
				selectProbablity = p2;
			localCost=localCost();
			AlsWork();
			
			if(cycleCount % r != 0){
				if(Math.random() < selectProbablity){
					int[] selectMinCost=new int[domain.length];
					for(int i=0; i<domain.length; i++){
						selectMinCost[i]=0;
					}
					for(int i=0; i<domain.length; i++){
						for(int j=0; j<neighbours.length; j++){
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex.get(j)];
						}					
					}				
					int selectValueIndex = 0;
					int selectOneMinCost = selectMinCost[0];
					for(int i = 1; i < domain.length; i++){
						if(selectOneMinCost >= selectMinCost[i] && selectMinCost[i] != valueIndex){
							selectOneMinCost = selectMinCost[i];
							selectValueIndex = i;
						}
					}
					if(selectOneMinCost <= localCost){
						valueIndex = selectValueIndex;
						sendValueMessages();
					}
					nccc++;
				}
			}
			else{
				valueIndex = (int) (Math.random() * domain.length);
				sendValueMessages();
			}
		}
		else
			AlsStopRunning();
	}
	
	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighbours.length; i++){
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex.get(i)];		
		}
		return localCostTemp;
	}
	
	protected void localSearchCheck(){
		while(msgQueue.size() == 0){
			try {
				Thread.sleep(1);
				System.out.println("!!! sleep(1) !!!!!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(msgQueue.isEmpty() == true){
			System.out.println("!!!!! IsEmpty Judged Wrong !!!!!");
		}
	}
	
	protected void runFinished(){
		super.runFinished();
		
		HashMap<String, Object> result=new HashMap<String, Object>();
		result.put(KEY_ID, this.id);
		result.put(KEY_NAME, this.name);
		result.put(KEY_VALUE, this.domain[valueIndex]);
		result.put(KEY_NCCC, this.nccc);
		result.put(KEY_BESTCOST, this.bestCost);
		result.put(KEY_BESTCOSTINCYCLE, bestCostInCycle);
		
		this.msgMailer.setResult(result);
		//System.out.println("Agent "+this.name+" stopped!");
	}
	
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO 自动生成的方法存根

		ResultCycleAls ret=new ResultCycleAls();
		int tag = 0;
		int totalCost=0;
		int ncccTemp = 0;
		for(Map<String, Object> result : results){
			
			//int id_=(Integer)result.get(KEY_ID);
			//String name_=(String)result.get(KEY_NAME);
			//int value_=(Integer)result.get(KEY_VALUE);
			
			if(ncccTemp < (Integer)result.get(KEY_NCCC))
				ncccTemp = (Integer)result.get(KEY_NCCC);
			if(tag == 0){
				ret.bestCostInCycle=(double[])result.get(KEY_BESTCOSTINCYCLE);
				totalCost = ((Integer)result.get(KEY_BESTCOST));
				tag = 1;
			}
			//String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			//System.out.println(displayStr);
		}
		
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost)+
				" nccc: "+Infinity.infinityEasy((int)ncccTemp));
		
		ret.nccc=(int)ncccTemp;
		ret.totalCost=(int)totalCost;
		return ret;
	}
	
	
	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		// TODO 自动生成的方法存根
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+DsaPPIRA_Agent.messageContent(msg);
	}
	
	
	public static String messageContent(Message msg){
		switch (msg.getType()) {
		case DsaPPIRA_Agent.TYPE_VALUE_MESSAGE:
		{
			int val=(Integer) msg.getValue();
			return "value["+val+"]";
		}case DsaPPIRA_Agent.TYPE_ALSCOST_MESSAGE:
		{
			int val=(Integer) msg.getValue();
			return "accumulativeCost["+val+"]";
		}case DsaPPIRA_Agent.TYPE_ALSBEST_MESSAGE:
		{
			int[] val=(int[]) msg.getValue();
			return "bestStep["+val[0]+", bestValue["+val[1]+"]";
		}
		default:
			return "unknown";
		}
	}
	
	
	@Override
	protected void messageLost(Message msg) {
		// TODO 自动生成的方法存根
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
		}
	}

}
