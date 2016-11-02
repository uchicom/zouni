package javax.servlet.http;

public class Cookie implements Cloneable {
	private String comment;
	private String domain;
	private int maxAge;
	private String path;
	private boolean secure;
	private String name;
	private String value;
	private int version;
	public Cookie(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getComment() {
		return comment;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getDomain() {
		return domain;
	}
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
	public int getMaxAge() {
		return maxAge;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	public boolean getSecure() {
		return secure;
	}
	public String getName() {
		return name;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public Object clone() {
		return new Cookie(name, value);
	}
}
