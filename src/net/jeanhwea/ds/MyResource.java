package net.jeanhwea.ds;

public class MyResource {
	private String 	name;
	private int		uid;
	
	public MyResource() {
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

	@Override
	public String toString() {
		return "MyResource [name=" + name + ", uid=" + uid + "]";
	}
	
}
