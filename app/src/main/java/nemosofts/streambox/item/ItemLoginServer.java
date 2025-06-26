package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemLoginServer implements Serializable {

	private final Boolean isXui;
	private final String url;
	private final String port;
	private final String httpsPort;
	private final String serverProtocol;

	/**
	 * Constructs an ItemLoginServer object.
	 *
	 * @param xui A boolean value indicating whether the XUI (UI configuration) is enabled
	 * @param url The URL of the server
	 * @param port The port used for regular communication
	 * @param httpsPort The port used for HTTPS communication
	 * @param serverProtocol The protocol used by the server (e.g., HTTP, HTTPS)
	 */

    public ItemLoginServer(Boolean xui, String url, String port,
						   String httpsPort, String serverProtocol) {
        this.isXui = xui;
        this.url = url;
        this.port = port;
        this.httpsPort = httpsPort;
        this.serverProtocol = serverProtocol;
    }

	public Boolean getXui() {
		return isXui;
	}

	public String getUrl() {
		return url;
	}

	public String getPort() {
		return port;
	}

	public String getHttpsPort() {
		return httpsPort;
	}

	public String getServerProtocol() {
		return serverProtocol;
	}
}