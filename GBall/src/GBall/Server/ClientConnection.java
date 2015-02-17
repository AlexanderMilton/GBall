package GBall.Server;

import java.net.InetAddress;

public class ClientConnection
{
	private final InetAddress m_address;
	private final int m_port;

	public ClientConnection(InetAddress adr, int port)
	{
		m_address = adr;
		m_port = port;
	}
	
	public boolean testAddress(InetAddress test)
	{
		return m_address.equals(test);
	}
	
	public boolean testPort(int test)
	{
		return (m_port == test);
	}

}
