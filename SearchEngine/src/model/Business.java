package model;

public class Business {
	private String name;
	private String url;
	private String snippet_text;
	private String snippet_url;
	private String display_address;
	
	public Business() {
		// TODO Auto-generated constructor stub
	}

	public Business(String name, String url, String snippet_text,
			String snippet_url, String display_address) {
		super();
		this.name = name;
		this.url = url;
		this.snippet_text = snippet_text;
		this.snippet_url = snippet_url;
		this.display_address = display_address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSnippet_text() {
		return snippet_text;
	}

	public void setSnippet_text(String snippet_text) {
		this.snippet_text = snippet_text;
	}

	public String getSnippet_url() {
		return snippet_url;
	}

	public void setSnippet_url(String snippet_url) {
		this.snippet_url = snippet_url;
	}

	public String getDisplay_address() {
		return display_address;
	}

	public void setDisplay_address(String display_address) {
		this.display_address = display_address;
	}
	
	
	
	
}
