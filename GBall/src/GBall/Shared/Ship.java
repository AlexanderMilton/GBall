package GBall.Shared;

import java.awt.Color;


public class Ship extends GameEntity// implements KeyListener
{

	/**
	 * 
	 */
	protected Color m_color;
	
	private int rotation = 0; // Set to 1 when rotating clockwise, -1 when
								// rotating counterclockwise
	private boolean braking = false;

	public Ship(final Vector2D position, final Vector2D speed, final Vector2D direction, final int col, final int id)
	{
		super(position, speed, direction, Const.SHIP_MAX_ACCELERATION, Const.SHIP_MAX_SPEED, Const.SHIP_FRICTION, id);
		if(col == 0)
		{
			m_color = Const.TEAM1_COLOR;
		} else
		{
			m_color = Const.TEAM2_COLOR;
		}
	}
	
	@Override
	public void setRotation(int r)
	{
		
		rotation = (int)Math.signum(r); 
	}
	
	@Override
	public int getRotation()
	{
		return rotation;
	}

	@Override
	public void move()
	{
		if (rotation != 0)
		{
			rotate(rotation * Const.SHIP_ROTATION);
			scaleSpeed(Const.SHIP_TURN_BRAKE_SCALE);
		}
		if (braking)
		{
			scaleSpeed(Const.SHIP_BRAKE_SCALE);
			setAcceleration(0);
		}
		super.move();
	}

	@Override
	public void render(java.awt.Graphics g)
	{
		g.setColor(m_color);
		g.drawOval((int) getPosition().getX() - Const.SHIP_RADIUS, (int) getPosition().getY() - Const.SHIP_RADIUS, Const.SHIP_RADIUS * 2, Const.SHIP_RADIUS * 2);

		g.drawLine((int) getPosition().getX(), (int) getPosition().getY(), (int) (getPosition().getX() + getDirection().getX() * Const.SHIP_RADIUS), (int) (getPosition().getY() + getDirection()
				.getY() * Const.SHIP_RADIUS));
	}

	@Override
	public boolean givesPoints()
	{
		return false;
	}

	@Override
	public double getRadius()
	{
		return Const.SHIP_RADIUS;
	}

	public int getID()
	{
		return m_ID;
	}
}