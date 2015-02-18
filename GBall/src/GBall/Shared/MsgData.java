package GBall.Shared;


import java.net.InetAddress;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class MsgData implements Comparable<MsgData>
{

	/**
	 * 
	 */
	private JSONObject obj = new JSONObject();

//	public Vector2D m_position;
//	public Vector2D m_initialPosition;
//	public Vector2D m_initialDirection;
//	public Vector2D m_speed;
//	public Vector2D m_direction; // Should always be unit vector; determines the
//									// object's facing
//	
//	public int m_rotation;
//	public double m_acceleration;
//	public long m_timestamp;
//	
//	public MsgData m_prevMsg = null;

	public final InetAddress m_address;
	public final int m_port;
	
	private void stamp()
	{
		obj.put("timestamp", System.currentTimeMillis());
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
	}
	
	public MsgData(String JSONString, InetAddress address, int port) throws ParseException
	{		
		JSONParser p = new JSONParser();
		obj = (JSONObject)p.parse(JSONString);
		
		m_address = address;
		m_port = port;
		stamp();
	}
	
	public void setPrevMessage(JSONObject pObj)
	{
		obj.put("prevMsg", pObj);
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
			// TODO Auto-generated catch block
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

//	public MsgData(Vector2D position, Vector2D initialPosition, Vector2D initialDirection, Vector2D speed, Vector2D direction)
//	{
//
//		m_position = position;
//		m_initialPosition = initialPosition;
//		m_initialDirection = initialDirection;
//		m_speed = speed;
//		m_direction = direction;
//		m_timestamp = System.currentTimeMillis();
//	}
	
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
//		String string = m_position + ", "
//						+ m_initialPosition + ", "
//						+ m_initialDirection + ", "
//						+ m_speed + ", "
//						+ m_direction + ", "
//						+ m_rotation + ", "
//						+ m_acceleration + ", "
//						+ m_timestamp + " : ";
//		if(m_prevMsg != null)
//		{
//			string += m_prevMsg;
//		}
//		else
//		{
//			string += "null";
//		}
//		return string;
		
		return obj.toJSONString();
	}
	
	public String debugInfo()
	{
		return toString() + " " + m_address + ":" + m_port;
	}
}
