package org.apache.stratos.cep.extension;

public class DataReceiverConfig {

	private Integer port;
	private Integer securePort;
	private String host;
	
	public DataReceiverConfig() {
		
	}
	
	public DataReceiverConfig(int port, int securePort, String host) {
		this.port = port;
		this.securePort = securePort;
		this.host = host;
	}
	
	
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getSecurePort() {
		return securePort;
	}

	public void setSecurePort(Integer securePort) {
		this.securePort = securePort;
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	
}
