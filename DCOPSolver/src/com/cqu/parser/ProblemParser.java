package com.cqu.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import com.cqu.aco.PublicConstants;
import com.cqu.bfsdpop.CEAllocatorFactory;
import com.cqu.bfsdpop.CrossEdgeAllocator;
import com.cqu.heuristics.MostConnectedHeuristic;
import com.cqu.main.DOTrenderer;
import com.cqu.settings.Settings;
import com.cqu.tree.BFSTree;
import com.cqu.tree.TreeGenerator;
import com.cqu.util.XmlUtil;
import com.cqu.varOrdering.dfs.DFSgeneration;
import com.cqu.varOrdering.priority.PriorityGeneration;

public class ProblemParser {
	
	private static final String PRESENTATION="presentation";
	private static final String FORMAT="format";
	private static final String TYPE="type";
	
	private static final String FORMAT_DISCHOCO="XDisCSP 1.0";
	private static final String FORMAT_FRODO="XCSP 2.1_FRODO";
	
	private static final String TYPE_DCOP="DCOP";
	private static final String TYPE_GRAPH_COLORING="DisCSP";
	
	private String xmlPath;
	private String problemBenchmark;//For SensorNetwork
	private String problemFormat;
	private String problemType;
	private String treeGeneratorType;
	protected Element root;
	
	public ProblemParser(String xmlPath, String treeGeneratorType) {
		// TODO Auto-generated constructor stub
		this.xmlPath=xmlPath;
		this.treeGeneratorType=treeGeneratorType;
	}
	public ProblemParser(String xmlPath){
		this.xmlPath = xmlPath;
		Document doc = XmlUtil.openXmlDocument(xmlPath);
		this.root = doc.getRootElement();
	}

	public Problem parse()
	{
		Problem problem = new Problem();
		
		Document doc=XmlUtil.openXmlDocument(xmlPath);
		
		String docURL=doc.getBaseURI();
		String[] docName=docURL.split("/");
		System.out.println(docName[docName.length-1]);
		
		Element root=doc.getRootElement();
		if(parsePresentation(root.getChild(PRESENTATION))==false)
		{
			System.out.println("parsePresentation()=false");
			return null;
		}
		
		ContentParser parser = null;
		if(problemFormat.equals(FORMAT_DISCHOCO) && problemBenchmark.equals("GSensorDCSP")){
			parser = new ParserSensorNetwork(root,problemType);
		}else if(problemFormat.equals(FORMAT_DISCHOCO)){
			parser=new ParserGeneral(root, problemType);
		}else if(problemFormat.equals(FORMAT_FRODO))
		{
			parser=new ParserMeetingScheduling(root, problemType);
		}else
		{
			parser=null;
		}
		
		if(parser!=null)
		{
			parser.parseContent(problem);
			//this.generateAgentProperty(problem);
			
			this.generateCommunicationStructure(problem);
			return problem;
		}else
		{
			return null;
		}
	}
	//获取agentID,将不必要的domain排除
	private void resetVariableDomain(String relationName, int variableID, Problem problem)
	{
		String variablePair = problem.VariableRelation.get(relationName);
		String valuePair = problem.VariableValue.get(relationName);
		
		String[] splitVariable = variablePair.split("\\s+");
		//1 2 1 8 2 4
		/*for(String each : splitVariable) {
		    System.out.print("'" + each + "'");
		}
		System.out.println();*/
		String[] splitValue = valuePair.split("\\s+");
		/*for(String each : splitValue) {
		    System.out.print("'" + each + "'");
		}
		System.out.println();*/
		int leftVariable = Integer.parseInt(splitVariable[0]);
		int rightVariable = Integer.parseInt(splitVariable[1]);
		
		//System.out.println("leftVariable： " + leftVariable);
		//System.out.println("rightVariable： " + rightVariable);
		
		if (!problem.variableDomains.containsKey(variableID))
		{
			problem.variableDomains.put(variableID, new HashSet<Integer>() );
		}

		if (leftVariable >= variableID) 
		{
			problem.variableDomains.get(variableID).add(Integer.parseInt(splitValue[0]));
		}else
		{
			problem.variableDomains.get(variableID).add(Integer.parseInt(splitValue[1]));
		}
	}
	private void generateAgentProperty(Problem problem)
	{
		for (Map.Entry<Integer, String[]> entry : problem.agentConstraintCosts.entrySet())
		{
			if(entry.getValue().length == problem.domains.get(problem.agentDomains.get(entry.getKey())).length - 1)
			{
				for (int i = 0; i < entry.getValue().length; i++)
				{
					String relationName = entry.getValue()[i] ;
					resetVariableDomain(relationName, entry.getKey(), problem);	
				}
			}else
			{
				for (int i = 0; i < entry.getValue().length; i++)
				{
					int[] domain = problem.domains.get(problem.agentDomains.get(entry.getKey()));
					/*if (!problem.variableDomains.containsKey(entry.getKey()))
					{
						problem.variableDomains.put(entry.getKey(), new HashSet<Integer>());
					}
					for (int each : domain)
					{
						problem.variableDomains.get(entry.getKey()).add(each);
					}*/
					problem.VariableDomains.put(entry.getKey(), domain);
				}
			}
		}
		/*
		 * 需要对其进行排序
		 */
		/*for (Map.Entry<Integer, Integer> entry : problem.agentProperty.entrySet())
		{
			System.out.println("key: " + entry.getKey() + ", value: " + entry.getValue());
		}*/
		
		for (Map.Entry<Integer, Set<Integer> > entry : problem.variableDomains.entrySet())
		{
			int[] domains = new int[entry.getValue().size()];
			System.out.print("variable: " + entry.getKey() + ", domain: ");
			Iterator<Integer> iter = entry.getValue().iterator();
			for (int i = 0; iter.hasNext(); i++)
			{
			    domains[i] = iter.next();
				System.out.print(domains[i] + " ");
			}
			System.out.println();
			problem.VariableDomains.put(entry.getKey(), domains);
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void generateCommunicationStructure(Problem problem)
	{
		TreeGenerator treeGenerator;
		if(treeGeneratorType.equals(TreeGenerator.TREE_GENERATOR_TYPE_DFS))
		{
			//treeGenerator=new DFSTree(problem.neighbourAgents);
			treeGenerator = new DFSgeneration(problem.neighbourAgents);
		}else
		{
			treeGenerator=new BFSTree(problem.neighbourAgents);
		}
		//DFSgeneration.setRootHeuristics(new MostConstributionHeuristic(problem));
		//DFSgeneration.setNextNodeHeuristics(new MostConstributionHeuristic(problem));
		DFSgeneration.setRootHeuristics(new MostConnectedHeuristic(problem));
		DFSgeneration.setNextNodeHeuristics(new MostConnectedHeuristic(problem));
		treeGenerator.generate();
		
		PriorityGeneration varOrdering = new PriorityGeneration(problem.neighbourAgents);
		if(PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[0])){	
			varOrdering.setRootHeuristics(new MostConnectedHeuristic(problem));
			varOrdering.setNextNodeHeuristics(new MostConnectedHeuristic(problem));
			varOrdering.generate();
		}else{
			varOrdering.generate(treeGenerator);
		}
		

		problem.highNodes = varOrdering.getHighNodes();
		problem.lowNodes = varOrdering.getLowNodes();
		problem.priorities = varOrdering.getPriorities();
		problem.maxPriority = varOrdering.getMaxPriority();
		problem.minPriority = varOrdering.getMinPriority();
		problem.allNodes = varOrdering.getAllNodes();
		
		problem.agentLevels=treeGenerator.getLevels();
		for(Integer level:problem.agentLevels.values())
			if(problem.treeDepth<(level+1))problem.treeDepth=level+1;
		problem.pseudoHeight=treeGenerator.getHeight();
		problem.parentAgents=treeGenerator.getParents();
		problem.childAgents=treeGenerator.getChildren();
		problem.allParentAgents=treeGenerator.getAllParents();
		problem.allChildrenAgents=treeGenerator.getAllChildren();
		
		if(treeGeneratorType.equals(TreeGenerator.TREE_GENERATOR_TYPE_BFS))
		{
			CrossEdgeAllocator allocator;
			if(Settings.settings.getClusterRemovingChoice()==1)
			{
				allocator=CEAllocatorFactory.getCrossEdgeAllocator("CEAllocatorB", problem);
			}else
			{
				allocator=CEAllocatorFactory.getCrossEdgeAllocator("CEAllocatorA", problem);
			}
			allocator.allocate();
			problem.crossConstraintAllocation=allocator.getConsideredConstraint();
		}
	}
	
	private boolean parsePresentation(Element element)
	{
		if(element==null)
		{
			return false;
		}
		problemFormat = element.getAttributeValue(FORMAT);
		problemBenchmark = element.getAttributeValue("benchmark");
//		System.out.println(problemBenchmark);
		if(problemFormat.equals(FORMAT_DISCHOCO))
		{
			problemType=element.getAttributeValue(TYPE);
			if(problemType.equals(TYPE_DCOP)==true||problemType.equals(TYPE_GRAPH_COLORING)==true)
			{
				return true;
			}else
			{
				return false;
			}
		}else if(problemFormat.equals(FORMAT_FRODO))
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	public static void main (String[] args) throws Exception{
		new DOTrenderer ("Constraint graph", ProblemParser.toDOT("ins.xml"));
		//System.out.println(ProblemParser.toDOT("random_Max-DisCSP.xml"));
	}
	//display constraint graph
	public static String toDOT (String path) throws Exception{
		StringBuilder out = new StringBuilder ("graph {\n\tnode [shape = \"circle\"];\n");
		
		
		ProblemParser parser = new ProblemParser (path);
		for (String agent : parser.getAgents())
		{
			out.append("\tsubgraph cluster_" + agent + " {\n");
			
			out.append("\t\tlabel = " + agent + ";\n");
			
			for (String var : parser.getVariables(agent)){
				out.append("\t\t" + var);
				
				out.append(";\n");
			}
			out.append("\t}\n");
		}
		out.append("\n");
		for (String var : parser.getVariables())
		{
			for (String neighbor : parser.getNeighborVars(var))
			{
				if (var.compareTo(neighbor) >= 0)
				{
					out.append("\t" + var + "--" + neighbor + ";\n");
				}
			}
		}
		out.append("}\n");
		return out.toString();
	}
	public Set<String> getAgents(){
		Set<String> agents = new HashSet<String>();
		for(Element var : this.root.getChild("agents").getChildren())
		{
			agents.add(var.getAttributeValue("name"));
		}
			
		return agents;
	}
	public String getOwner (String var)
	{
		for(Element varElmt :this.root.getChild("variables").getChildren())
			if(varElmt.getAttributeValue("name").equals(var))
				return varElmt.getAttributeValue("agent");
		assert false : "Unknown variable '" + var + "'" ;
		return null;
	}
	public Set<String> getVariables()
	{
		Set<String> out = new HashSet<String> ();
		for(Element varElmt : this.root.getChild("variables").getChildren())
			if(! "random".equals(varElmt.getAttributeValue("type")))
				out.add(varElmt.getAttributeValue("name"));
		
		return out;
	}
	public Set<String> getVariables(String owner)
	{
		Set<String> out = new HashSet<String>();
		if(owner != null)
		{
			for (Element var : this.root.getChild("variables").getChildren())
				if(owner.equals(var.getAttributeValue("agent")))
					out.add(var.getAttributeValue("name"));
		}else
			for (Element var : this.root.getChild("variables").getChildren())
				if(var.getAttributeValue("agent") == null)
					out.add(var.getAttributeValue("name"));
		return out;
	}
	public HashSet<String> getNeighborVars(String var)
	{
		HashSet<String> out = new HashSet<String>();
		LinkedList<String> pending = new LinkedList<String>();
		pending.add(var);
		HashSet<String> done = new HashSet<String> ();
		do{
			String var2 = pending.poll();
			if (!done.add(var2))
				continue;
			for (Element constraint : this.root.getChild("constraints").getChildren()){
				String[] scope = constraint.getAttributeValue("scope").trim().split("\\s+");
				Arrays.sort(scope);
				if (Arrays.binarySearch(scope, var2) >=0 ){
					for (String neighbor : scope)
					{
						if (! this.isRandom(neighbor))
							out.add(neighbor);
					}
				}
			}
		}while(!pending.isEmpty());
		out.remove(var);
		return out;
	}
	public boolean isRandom (String var) {
		for (Element varElmt : this.root.getChild("variables").getChildren())
			if (var.equals(varElmt.getAttributeValue("name")))
				return new String ("random").equals(varElmt.getAttributeValue("type"));
		
		return false;
	}
}
