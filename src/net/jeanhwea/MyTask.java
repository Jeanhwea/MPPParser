package net.jeanhwea;

import java.util.LinkedList;
import java.util.List;

public class MyTask {


	private int         nid;    // node id
	private int         uid;
	private String      name;
	private double      duration;
	private String      unit;   // unit of duration
	private int         level;  // out line level
	private String      outline;
	
	private MyTask		 parent;
	private List<MyTask> children;
	
	
	public MyTask() {
		parent = null;
		children = new LinkedList<MyTask>();
	}


	public int getNid() {
		return nid;
	}


	public void setNid(int nid) {
		this.nid = nid;
	}


	public int getUid() {
		return uid;
	}


	public void setUid(int uid) {
		this.uid = uid;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public double getDuration() {
		return duration;
	}


	public void setDuration(double duration) {
		this.duration = duration;
	}


	public String getUnit() {
		return unit;
	}


	public void setUnit(String unit) {
		this.unit = unit;
	}


	public int getLevel() {
		return level;
	}


	public void setLevel(int level) {
		this.level = level;
	}


	public String getOutline() {
		return outline;
	}


	public void setOutline(String outline) {
		this.outline = outline;
	}


	public MyTask getParent() {
		return parent;
	}


	public void setParent(MyTask parent) {
		this.parent = parent;
	}


	public List<MyTask> getChildren() {
		return children;
	}


	public void setChildren(List<MyTask> children) {
		this.children = children;
	}


	@Override
	public String toString() {
		return "MyTask [nid=" + nid + ", uid=" + uid + ", name=" + name
				+ ", duration=" + duration + unit + ", level="
				+ level + ", outline=" + outline + "]";
	}


}
