package GBall.Shared;


import java.net.InetAddress;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class MsgData implements Comparable<MsgData>
{
	private JSONObject obj = new JSONObject();

	// used for debug purposes
	public final InetAddress m_address;
	public final int m_port;
	
	// used to translate between server time and local time.
	public static long m_offset = 0;
	
	// stamps this message with the offset to set it as server time
	private void stamp()
	{
		obj.put("timestamp", System.currentTimeMillis() + m_offset);
	}
	
	// translates the message timestamp from server time to local time.
	private void applyOffset()
	{
		obj.put("timestamp", getTimestamp() - m_offset);
	}
	
	public MsgData()
	{
		m_address = null;
		m_port = -1;
		stamp();
	}
	
	public MsgData(JSONObject o)
	{
		obj = o;
		m_address = null;
		m_port = -1;
		applyOffset();
	}
	
	public MsgData(String JSONString, InetAddress address, int port) throws ParseException
	{		
		JSONParser p = new JSONParser();
		obj = (JSONObject)p.parse(JSONString);
		
		m_address = address;
		m_port = port;
		applyOffset();
//		stamp();
	}
	
	public void setParameter(String key, int value)
	{
		obj.put(key, new Integer(value));
	}
	
	public void setParameter(String key, double value)
	{
		obj.put(key, new Double(value));
	}
	
	public void setParameter(String key, Vector2D value)
	{
		obj.put(key, value);
	}
	
	public void setParameter(String key, JSONObject value)
	{
		obj.put(key, value.toJSONString());
	}
	
	public JSONObject getJSONObj()
	{
		return obj;
	}
	
	public JSONObject getJSONObj(String key)
	{
		try
		{
			return (JSONObject)new JSONParser().parse((String) obj.get(key));
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public Vector2D getVector(String key)
	{
		JSONObject o = (JSONObject) obj.get(key);
		return (new Vector2D(o)) ;
	}
	
	public int getInt(String key)
	{
		return (int)((long) obj.get(key));
	}
	
	public double getDouble(String key) 
	{
		return (double) obj.get(key);
	}
	
	public long getTimestamp()
	{
		return (long) obj.get("timestamp");
	}
	
	//Makes instances of this class comparable which enables insertion in a priority queue	
	@Override
	public int compareTo(MsgData o)
	{
		if(o == null)
		{
			throw new NullPointerException();
		} else if(o.getClass() != this.getClass())
		{
			throw new ClassCastException();
		}
		
		if(getTimestamp() < o.getTimestamp())
		{
			return -1;
		} else if(getTimestamp() > o.getTimestamp())
		{
			return 1;
		} else
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
		return obj.toJSONString();
	}
	
	public String debugInfo()
	{
		return toString() + " " + m_address + ":" + m_port;
	}
}
