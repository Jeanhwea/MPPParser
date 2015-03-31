package net.jeanhwea;


import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.mpxj.Duration;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Relation;
import net.sf.mpxj.Resource;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpp.MPPReader;
import net.stixar.graph.BasicDigraph;
import net.stixar.graph.BasicNode;
import net.stixar.graph.Edge;
import net.stixar.graph.Node;
import net.stixar.graph.conn.Transitivity;

public class Reader {
	private ProjectFile         project_file;
	private MPPReader           mppreader;
	private BasicDigraph        dgraph;
	private Vector<MyTask>      v_tasks;
	private Vector<MyResource>  v_resources;
	
	private String dot_filename;
	
	// task uid to node id
	private Map<Integer, Integer> m_uid2nid;
	// task uid to MyTask reference
	private Map<Integer, MyTask>  m_uid2task;
	// task nid to MyTask reference
	private Map<Integer, MyTask> m_nid2task;
	
	
	private DotFileWriter dot_writer;
	
	public Reader() {
		project_file = new ProjectFile();
		mppreader = new MPPReader();
		dgraph = new BasicDigraph();
		v_tasks = new Vector<MyTask>();
		v_resources = new Vector<MyResource>();
		m_uid2nid = new HashMap<Integer, Integer>();
		m_uid2task = new HashMap<Integer, MyTask>();
		m_nid2task = new HashMap<Integer, MyTask>();
	}
	
	public ProjectFile getProjectFile() {
		return project_file;
	}

	public void setProjectFile(ProjectFile project_file) {
		this.project_file = project_file;
	}

	public MPPReader getMppreader() {
		return mppreader;
	}

	public void setMppreader(MPPReader mppreader) {
		this.mppreader = mppreader;
	}

	public BasicDigraph getDgraph() {
		return dgraph;
	}

	public void setDgraph(BasicDigraph dgraph) {
		this.dgraph = dgraph;
	}

	public Vector<MyTask> getTasks() {
		return v_tasks;
	}

	public void setTasks(Vector<MyTask> v_tasks) {
		this.v_tasks = v_tasks;
	}

	public Vector<MyResource> getResources() {
		return v_resources;
	}

	public void setResources(Vector<MyResource> v_resources) {
		this.v_resources = v_resources;
	}

	public String getDot_filename() {
		return dot_filename;
	}

	public void setDot_filename(String dot_filename) {
		this.dot_filename = dot_filename;
	}

	public void readFile(String filename) throws MPXJException {
		String[] path_list = filename.split("/");
		dot_filename = path_list[path_list.length-1].split("\\.")[0] + ".dot";
		project_file = mppreader.read(filename);
	}
	
	public Vector<MyTask> loadTasks() {
		
		// add node to graph
		for (Task t : project_file.getAllTasks()) {
			MyTask my_task = new MyTask();
			BasicNode node = dgraph.genNode();
			my_task.setNid(node.nodeId());			
			my_task.setUid(t.getUniqueID());
			
			// add map item (k-v)
			m_uid2nid.put(my_task.getUid(), my_task.getNid());
			m_uid2task.put(my_task.getUid(), my_task);
			m_nid2task.put(my_task.getNid(), my_task);
			

			my_task.setName(t.getName());
			Duration work = t.getWork();
			if (work != null) {
				my_task.setDuration(work.getDuration());
				my_task.setUnit(work.getUnits().getName());
			} else {
				my_task.setDuration(0);
				my_task.setUnit("NA");
			}
			
			Integer level = t.getOutlineLevel();
			if (level != null) {
				my_task.setLevel(level);
			} else {
				my_task.setLevel(0);
			}
			
			String outline_number = t.getOutlineNumber();
			if (outline_number != null) {
				my_task.setOutline(outline_number);
			} else {
				my_task.setOutline("0");
			}
			
			v_tasks.add(my_task);
		}
		
		// add edge to graph
		for (Task t : project_file.getAllTasks()) {
			List<Relation> successors = t.getSuccessors();
			if (successors == null)
				continue;
			for (Relation rela : successors) {
				
				Integer nid_src, nid_des;
				nid_src = m_uid2nid.get(rela.getSourceTask().getUniqueID());
				nid_des = m_uid2nid.get(rela.getTargetTask().getUniqueID());
				if (nid_src == null) {
					System.err.println("can not map uid=" + rela.getSourceTask().getUniqueID() + " to nid");
					continue;
				}
				if (nid_des == null) {
					System.err.println("can not map uid=" + rela.getTargetTask().getUniqueID() + " to nid");
					continue;
				}
				
				BasicNode source, target;
				source = dgraph.node(nid_src);
				target = dgraph.node(nid_des);
				if (source == null) {
					System.err.println("node no found in dgraph!!!, bad nid=" + nid_src);
					continue;
				}
				if (target == null) {
					System.err.println("node no found in dgraph!!!, bad nid=" + nid_des);
					continue;
				}
				
				// add a edge to dgraph
				dgraph.genEdge(source, target);
			}
		}
		
		// doing transitivity reduction
		Transitivity.acyclicReduce(dgraph);

		// build tree structure
		for (Task t : project_file.getAllTasks()) {
			
			MyTask my_task = m_uid2task.get(t.getUniqueID());

			MyTask my_parent = null;
			Task parent = t.getParentTask();
			if (parent != null) {
				my_parent = m_uid2task.get(parent.getUniqueID());
			}
			my_task.setParent(my_parent);
			
			List<Task> children = t.getChildTasks();
			List<MyTask> my_children = my_task.getChildren();
			for (Task child : children) {
				MyTask my_child = m_uid2task.get(child.getUniqueID());
				my_children.add(my_child);
			}
		}
		
		// add edge caused by tree structure to dgraph
		for (Task t : project_file.getAllTasks()) {
			for (Task p = t.getParentTask(); p != null; p = p.getParentTask()) {
				Integer nid_src, nid_des;
				nid_src = m_uid2nid.get(p.getUniqueID());
				nid_des = m_uid2nid.get(t.getUniqueID());
				if (nid_src == null) {
					System.err.println("can not map uid=" + p.getUniqueID() + " to nid");
					continue;
				}
				if (nid_des == null) {
					System.err.println("can not map uid=" + t.getUniqueID() + " to nid");
					continue;
				}
				
				BasicNode source, target;
				source = dgraph.node(nid_src);
				target = dgraph.node(nid_des);
				if (source == null) {
					System.err.println("node no found in dgraph!!!, bad nid=" + nid_src);
					continue;
				}
				if (target == null) {
					System.err.println("node no found in dgraph!!!, bad nid=" + nid_des);
					continue;
				}
				
				// add a edge to dgraph
				dgraph.genEdge(source, target);

			}
		}
		
		// doing transitivity reduction
		Transitivity.acyclicReduce(dgraph);
		
		return v_tasks;
	}
	
	public Vector<MyResource> loadResources() {
		
		for (Resource r : project_file.getAllResources()) {
			MyResource my_resource = new MyResource();
			my_resource.setName(r.getName());
			my_resource.setUid(r.getUniqueID());
			v_resources.add(my_resource);
		}
		
		return v_resources;
	}
	
	public int removeZeroDurationTasks() {
		List<Task> l_tasks_to_remove = new LinkedList<Task>();
		
		for (Task t : project_file.getAllTasks()) {
			Duration work = t.getWork();
			if (work == null || work.getDuration() == 0) {
			  l_tasks_to_remove.add(t);
			}
		}
		
		for (Task t : l_tasks_to_remove) {
//			project_file.removeTask(t);
			delTask(t);
		}
		
		return l_tasks_to_remove.size();
	}
	
	public int removeNoneLeafTasks() {
		List<Task> l_tasks_to_remove = new LinkedList<Task>();
		
		for (Task t : project_file.getAllTasks()) {
			List<Task> cld = t.getChildTasks();
			if (!cld.isEmpty()) {
			  l_tasks_to_remove.add(t);
			}
		}
		
		for (Task t : l_tasks_to_remove) {
			delTask(t);
		}
		
		return l_tasks_to_remove.size();
	}
	
	/**
	 * delete a task from project_file, we will make this in 3 steps --->
	 * 		step 1: modify the predecessors of the task;
	 * 		step 2: modify the successors of the task;
	 * 		step 3: remove the task.
	 * 
	 * @param task_to_remove
	 */
	private void delTask(Task task_to_remove) {
		
//		System.out.println("del task " + task_to_remove.getName());
				
		List<Relation> l_pred_to_modify = task_to_remove.getPredecessors(); // relation list of predecessors of current to-removed task
		List<Relation> l_succ_to_modify = task_to_remove.getSuccessors();   // relation list of successors of current to-removed task

		//step 1: modify predecessors before delete the task
		if (l_pred_to_modify != null) {
			for (Relation rela_pred : l_pred_to_modify) {
				List<Relation> l_succ_of_pred = rela_pred.getTargetTask().getSuccessors();
				Relation rela_to_remove = null;
				if (l_succ_of_pred != null) {
					for (Relation a_succ : l_succ_of_pred) {
						if (a_succ.getTargetTask().equals(task_to_remove))
							rela_to_remove = a_succ;
					}
					if (l_succ_to_modify != null) {
						Task task_src, task_des;
						task_src = rela_pred.getTargetTask();
						for (Relation rela : l_succ_to_modify) {
							task_des = rela.getTargetTask();
							Relation new_relation = new Relation(task_src, task_des, null, null);
							l_succ_of_pred.add(new_relation);
						}
					}
				}
				if (rela_to_remove != null) {
					l_succ_of_pred.remove(rela_to_remove);
				} else {
					System.err.println("Pred have no succ" + rela_pred);
				}
			}
		}
		
		//step 2: modify successors before delete the task
		if (l_succ_to_modify != null) {
			for (Relation rela_succ : l_succ_to_modify) {
				List<Relation> l_pred_of_succ = rela_succ.getTargetTask().getPredecessors();
				Relation rela_to_remove = null;
				if (l_pred_of_succ != null) {
					for (Relation a_pred : l_pred_of_succ) {
						if (a_pred.getTargetTask().equals(task_to_remove))
							rela_to_remove = a_pred;
					}
					if (l_pred_to_modify != null) {
						Task task_src, task_des;
						task_src = rela_succ.getTargetTask();
						for (Relation rela : l_pred_to_modify) {
							task_des = rela.getTargetTask();
							Relation new_relation = new Relation(task_src, task_des, null, null);
							l_pred_of_succ.add(new_relation);
						}
					}
				}
				if (rela_to_remove != null) {
					l_pred_of_succ.remove(rela_to_remove);
				} else {
					System.err.println("Succ have no pred" + rela_succ);
				}
			}
		}
		
		//step 3: remove the task
		project_file.removeTask(task_to_remove);
	}

	public void printEdges() {
		for (Edge e : dgraph.edges()) {
			System.out.println(e.source()+"->"+e.target());
		}
		System.out.println("dgraph.edgeSize()="+dgraph.edgeSize());
	}
	
	public void printNodes() {
		for ( Node n: dgraph.nodes()) {
			System.out.println(n);
		}
	}
	
	public void printTasks() {
		for (MyTask mt: v_tasks) {
			System.out.println(mt);
		}
	}
	
	public MyTask getTaskByNid(int nid) {
		return m_nid2task.get(nid);
	}
	
	public void genDotFile() throws IOException {
//		dot_writer = new DotFileWriter(dot_filename);
		dot_writer = new DotFileWriter(dot_filename, this);
		dot_writer.write(dgraph);
	}
	
}
