package GBall.Client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import GBall.Shared.Const;
import GBall.Shared.KeyConfig;

public class InputListener implements KeyListener
{
	private final KeyConfig m_keyConfig;
	
	private int m_rotation = 0;
	private double m_acceleration = 0;
	
	public InputListener(KeyConfig kc)
	{
		m_keyConfig = kc;
		World.getInstance().addKeyListener(this);
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		try
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				System.exit(0);
			} else if (e.getKeyCode() == m_keyConfig.rightKey())
			{
				m_rotation = 1;
			} else if (e.getKeyCode() == m_keyConfig.leftKey())
			{
				m_rotation = -1;
			} else if (e.getKeyCode() == m_keyConfig.accelerateKey())
			{
				m_acceleration = Const.SHIP_MAX_ACCELERATION;
			}
		} catch (Exception x)
		{
			System.err.println(x);
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		try
		{
			if (e.getKeyCode() == m_keyConfig.rightKey() && m_rotation == 1)
			{
				m_rotation = 0;
			} else if (e.getKeyCode() == m_keyConfig.leftKey() && m_rotation == -1)
			{
				m_rotation = 0;
			} else if (e.getKeyCode() == m_keyConfig.accelerateKey())
			{
				m_acceleration = 0f;
			}
		} catch (Exception x)
		{
			System.out.println(x);
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}
	
	public int getRotation()
	{
		return m_rotation;
	}
	
	public double getAcceleration()
	{
		return m_acceleration;
	}
}
