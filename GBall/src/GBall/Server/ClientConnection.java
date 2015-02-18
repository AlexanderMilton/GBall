package GBall.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ClientConnection
{
	private final InetAddress m_address;
	private final int m_port;
	private final DatagramSocket m_socket;

	public ClientConnection(InetAddress adr, int port, DatagramSocket socket)
	{
		m_address = adr;
		m_port = port;
		m_socket = socket;
	}
	
	public void sendMessage(String JSONString)
	{
		byte[] buf = JSONString.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, m_address, m_port);
		try
		{
			m_socket.send(packet);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
