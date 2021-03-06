package com.cqu.settings;

import com.cqu.maxsum.AbstractRefiner;

import javax.swing.JDialog;

public class Settings {
	
	public static Settings settings=new Settings();
	
	private int communicationTimeInDPOPs;
	private int communicationNCCCInAdopts;
	private double BNBmergeADOPTboundArg;
	private int ADOPT_K;
	private int maxDimensionInMBDPOP;
	
	//蚁群算法参数设置
	//private int MaxCycle;
	private int countAnt;
	private int alpha;
	private int beta;
	private double rho;
	private double Min_tau;
	private double Max_tau;
	
	//BFSDPOP
	private int clusterRemovingChoice;
	
	//局部搜索算法参数设置
	private int cycleCountEnd;
	private double selectProbability;
	private double selectNewProbability;
	private double selectProbabilityA;
	private double selectProbabilityB;
	private double selectProbabilityC;
	private double selectProbabilityD;
	private int selectInterval;
	private int selectStepK1;
	private int selectStepK2;
	private int selectRound;
	private int T;
	private int Tmin;
	private double r;
	
	private boolean displayGraphFrame;

	//parameters for maxsum_ad
	private int stageSize;
	private int repeatTime;
	private boolean enableVP;
	private int vpTiming;

	//Parameter for local refine
	private int refineCycle;
	private boolean enableRefine;
	private String refineAlgorithm;

	//Parameters for Max-sum Probabilistic Value Propagation
	private int granularity;
	private double valuePropagationProbability;
	
	public Settings() {
		// TODO Auto-generated constructor stub
		this.communicationTimeInDPOPs=0;
		this.communicationNCCCInAdopts=0;
		this.BNBmergeADOPTboundArg=0.5;
		this.ADOPT_K=2;
		this.displayGraphFrame=true;
		this.maxDimensionInMBDPOP=3;
		
		//蚁群算法参数设置
		//this.MaxCycle = 100;
		this.countAnt = 2;
		this.alpha = 2;
		this.beta = 8;
		this.rho = 0.02;
		this.Min_tau = 0.1;
		this.Max_tau = 10;
		
		//BFSDPOP
		this.clusterRemovingChoice=0;
		
		this.cycleCountEnd = 20;
		this.selectProbability = 0.3;
		this.selectNewProbability = 0.5;
		this.selectInterval = 20;
		this.selectStepK1 = 20;
		this.selectStepK2 = 20;
		this.selectRound = 60;


		//local refine
		this.refineCycle = 30;
		this.enableRefine = false;
		this.refineAlgorithm = AbstractRefiner.REFINER_MGM;
		enableVP = false;
		enableRefine = false;
		valuePropagationProbability = 0.4;
	}
	
	public double getBNBmergeADOPTboundArg() {
		return BNBmergeADOPTboundArg;
	}

	public void setBNBmergeADOPTboundArg(double bNBmergeADOPTboundArg) {
		BNBmergeADOPTboundArg = bNBmergeADOPTboundArg;
	}
	
	public int getADOPT_K() {
		return ADOPT_K;
	}

	public void setADOPT_K(int aDOPT_K) {
		ADOPT_K = aDOPT_K;
	}

	public int getCountAnt() {
		return countAnt;
	}

	public void setCountAnt(int countAnt) {
		this.countAnt = countAnt;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getBeta() {
		return beta;
	}

	public void setBeta(int beta) {
		this.beta = beta;
	}

	public double getRho() {
		return rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}

	public double getMin_tau() {
		return Min_tau;
	}

	public void setMin_tau(double min_tau) {
		Min_tau = min_tau;
	}

	public double getMax_tau() {
		return Max_tau;
	}

	public void setMax_tau(double max_tau) {
		Max_tau = max_tau;
	}

	public int getCommunicationTimeInDPOPs() {
		return communicationTimeInDPOPs;
	}

	public void setCommunicationTimeInDPOPs(int communicationTimeInDPOPs) {
		this.communicationTimeInDPOPs = communicationTimeInDPOPs;
	}

	public int getCommunicationNCCCInAdopts() {
		return communicationNCCCInAdopts;
	}

	public void setCommunicationNCCCInAdopts(int communicationNCCCInAdopts) {
		this.communicationNCCCInAdopts = communicationNCCCInAdopts;
	}

	public boolean isDisplayGraphFrame() {
		return displayGraphFrame;
	}

	public void setDisplayGraphFrame(boolean displayGraphFrame) {
		this.displayGraphFrame = displayGraphFrame;
	}
	
	public int getMaxDimensionsInMBDPOP() {
		return maxDimensionInMBDPOP;
	}

	public void setMaxDimensionsInMBDPOP(int maxDimensionsInAgileDPOP) {
		this.maxDimensionInMBDPOP = maxDimensionsInAgileDPOP;
	}
	
	public int getClusterRemovingChoice() {
		return clusterRemovingChoice;
	}

	public void setClusterRemovingChoice(int clusterRemovingChoice) {
		this.clusterRemovingChoice = clusterRemovingChoice;
	}

	public int getCycleCountEnd() {
		return cycleCountEnd;
	}

	public void setCycleCount(int myCycleCountEnd) {
		cycleCountEnd = myCycleCountEnd;
	}
	
	public double getSelectProbability() {
		return selectProbability;
	}

	public void setSelectProbability(double mySelectProbability) {
		selectProbability = mySelectProbability;
	}
	
	public double getSelectNewProbability() {
		return selectNewProbability;
	}

	public void setSelectNewProbability(double mySelectNewProbability) {
		selectNewProbability = mySelectNewProbability;
	}

	public double getSelectProbabilityA() {
		return selectProbabilityA;
	}

	public void setSelectProbabilityA(double mySelectProbabilityA) {
		selectProbabilityA = mySelectProbabilityA;
	}

	public double getSelectProbabilityB() {
		return selectProbabilityB;
	}

	public void setSelectProbabilityB(double mySelectProbabilityB) {
		selectProbabilityB = mySelectProbabilityB;
	}

	public double getSelectProbabilityC() {
		return selectProbabilityC;
	}

	public void setSelectProbabilityC(double mySelectProbabilityC) {
		selectProbabilityC = mySelectProbabilityC;
	}

	public double getSelectProbabilityD() {
		return selectProbabilityD;
	}

	public void setSelectProbabilityD(double mySelectProbabilityD) {
		selectProbabilityD = mySelectProbabilityD;
	}
	
	public int getSelectInterval() {
		return selectInterval;
	}

	public void setSelectInterval(int mySelectInterval) {
		selectInterval = mySelectInterval;
	}
	
	public int getSelectStepK1() {
		return selectStepK1;
	}

	public void setSelectStepK1(int mySelectStepK1) {
		selectStepK1 = mySelectStepK1;
	}

	public int getSelectStepK2() {
		return selectStepK2;
	}

	public void setSelectStepK2(int mySelectStepK2) {
		selectStepK2 = mySelectStepK2;
	}

	public int getSelectRound() {
		return selectRound;
	}

	public void setSelectRound(int mySelectRound) {
		selectRound = mySelectRound;
	}
	
	public int getT() {
		return T;
	}

	public void setT(int myT) {
		T = myT;
	}
	
	public int getTmin() {
		return Tmin;
	}

	public void setTmin(int myTmin) {
		Tmin = myTmin;
	}
	
	public double getR() {
		return r;
	}

	public void setR(double myR) {
		r = myR;
	}
	
	public static Settings showSettingsDialog()
	{
		DialogSettings dialog = new DialogSettings(settings);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setModal(true);
		dialog.setVisible(true);
		return settings;
	}

	public int getRefineCycle() {
		return refineCycle;
	}

	public void setRefineCycle(int refineCycle) {
		this.refineCycle = refineCycle;
	}

	public void setEnableRefine(boolean enableRefine) {
		this.enableRefine = enableRefine;
	}

	public boolean isEnableRefine() {
		return enableRefine;
	}

	public void setGranularity(int granularity) {
		this.granularity = granularity;
	}

	public int getGranularity() {
		return granularity;
	}

	public void setVpTiming(int vpTiming) {
		this.vpTiming = vpTiming;
	}

	public int getVpTiming() {
		return vpTiming;
	}

	public void setValuePropagationProbability(double valuePropagationProbability) {
		this.valuePropagationProbability = valuePropagationProbability;
	}

	public double getValuePropagationProbability() {
		return valuePropagationProbability;
	}

	public String getRefineAlgorithm() {
		return refineAlgorithm;
	}

	public void setRefineAlgorithm(String refineAlgorithm) {
		this.refineAlgorithm = refineAlgorithm;
	}

	public void setStageSize(int stageSize) {
		this.stageSize = stageSize;
	}



	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}


	public void setEnableVP(boolean enableVP) {
		this.enableVP = enableVP;
	}

	public boolean isEnableVP() {
		return enableVP;
	}

	public int getStageSize() {
		// TODO Auto-generated method stub
		return stageSize;
	}

	public int getRepeatTime() {
		// TODO Auto-generated method stub
		return repeatTime;
	}
}
