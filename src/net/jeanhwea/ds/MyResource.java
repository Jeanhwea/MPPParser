package net.jeanhwea.ds;

public class MyResource {
	
	private int		id;
	private String 	name;
	private int		uid;
	private long	cost;
	private long	max_unit;
	
	public MyResource() {
		id = -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public long getCost() {
		return cost;
	}

	public void setCost(long cost) {
		this.cost = cost;
	}

	public long getMaxUnit() {
		return max_unit;
	}

	public void setMaxUnit(long max_unit) {
		this.max_unit = max_unit;
	}

	@Override
	public String toString() {
		return "MyResource [name=" + name + ", uid=" + uid + "]";
	}
	
}
