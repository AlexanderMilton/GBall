package GBall.Client;

import java.awt.Color;
import java.awt.Graphics;

import GBall.Shared.Const;
import GBall.Shared.MsgData;
import GBall.Shared.Ship;
import GBall.Shared.Vector2D;

public class SurrogateShip extends Ship
{
	// surrogate position, speed, direction etc.
	private final Vector2D m_surPosition;
	private final Vector2D m_surSpeed;
	private final Vector2D m_surDirection;
	private int m_surRotation = 0;
	private double m_surAcceleration = 0.0;
	private long m_surLastUpdate = System.currentTimeMillis();

	public SurrogateShip(Vector2D position, Vector2D speed, Vector2D direction, int col, int id)
	{
		super(position, speed, direction, col, id);
		m_surPosition = new Vector2D(position);
		m_surSpeed = new Vector2D(speed);
		m_surDirection = new Vector2D(direction);
	}

	@Override
	public void setRotation(int r)
	{
		m_surRotation = r;
	}

	@Override
	public void setAcceleration(double a)
	{
		if (a > m_maxAcceleration)
		{
			m_surAcceleration = m_maxAcceleration;
		} else if (a < (-m_maxAcceleration))
		{
			m_surAcceleration = -m_maxAcceleration;
		} else
			m_surAcceleration = a;
	}

	@Override
	public void move()
	{
//		Vector2D prevPos = new Vector2D(m_surPosition);
		if (m_surRotation != 0)
		{
			m_surDirection.rotate(m_surRotation * Const.SHIP_ROTATION);
			scaleSpeed(Const.SHIP_TURN_BRAKE_SCALE, m_surSpeed);
		}

		long currTime = System.currentTimeMillis();
		double delta = (double) (currTime - m_surLastUpdate) / 1000.0;
		if (m_surAcceleration > 0)
		{
			changeSpeed(m_surDirection.multiplyOperator(m_surAcceleration * delta), m_surSpeed);
		} else
		{
			scaleSpeed(m_friction, m_surSpeed);
		}
		m_surPosition.add(m_surSpeed.multiplyOperator(delta));

		m_surLastUpdate = currTime;

		checkRealState();

//		prevPos.subtract(m_surPosition);
		// System.out.println(prevPos + " " + m_surAcceleration + " " +
		// m_surRotation + " : " + delta);
	}

	private void checkRealState()
	{
		long stateAge = System.currentTimeMillis() - m_lastUpdateTime;
		float f = 0.3f;
		if (stateAge > 0)
		{
			f = 10.0f / (float) stateAge;
			// System.out.println(f + " " + stateDiff + " " + stateAge);
		}

		double deltaX = Math.abs(m_surPosition.getX() - super.getPosition().getX());
		double deltaY = Math.abs(m_surPosition.getY() - super.getPosition().getY());

		boolean changed = false;
		// System.out.println(deltaX + " " + deltaY);
		if (deltaX > Const.SURROGATE_MAX_DIFFERENCE || deltaY > Const.SURROGATE_MAX_DIFFERENCE)
		{
			m_surPosition.set(super.getPosition());
			m_surDirection.set(super.getDirection());
			m_surSpeed.set(super.getSpeed());
			changed = true;
		} else
		{
			if (deltaX < Const.SURROGATE_MIN_DIFFERENCE)
			{
				m_surPosition.setX(super.getPosition().getX());
				changed = true;
			}
			if (deltaY < Const.SURROGATE_MIN_DIFFERENCE)
			{
				m_surPosition.setY(super.getPosition().getY());
				changed = true;
			}
			if (m_surDirection.dotProduct(super.getDirection()) > 0.9)
			{
				m_surDirection.set(super.getDirection());
			} else
			{
				m_surDirection.lerp(super.getDirection(), f);
			}
//			System.out.println(m_surDirection.dotProduct(super.getDirection()));
		}

		/*
		 * if(m_surDirection.dotProduct(super.getDirection()) < 0.5) {
		 * m_surDirection.set(super.getDirection()); }
		 */
		if (!changed)
		{
			m_surPosition.lerp(super.getPosition(), f);
			m_surSpeed.lerp(super.getSpeed(), f);
		}

		// System.out.println(m_surSpeed.dotProduct(super.getSpeed()) + "\n"
		// + m_surSpeed + " " + super.getSpeed());
	}

	@Override
	public Vector2D getPosition()
	{
		// System.out.println(m_surPosition + " " + super.getPosition() + " : "
		// + m_surAcceleration + " " + m_surRotation);
		return m_surPosition;
		// return super.getPosition();
	}

	@Override
	public Vector2D getDirection()
	{
		return m_surDirection;
	}

	@Override
	public void deflectX()
	{
		m_surSpeed.setX(-m_surSpeed.getX());
	}

	@Override
	public void deflectY()
	{
		m_surSpeed.setY(-m_surSpeed.getY());
	}

	@Override
	public void render(Graphics g)
	{
		super.render(g);
		g.setFont(Const.SCORE_FONT);
		g.setColor(Color.white);
		int diff = (int) (m_surLastUpdate - m_lastUpdateTime);
		g.drawString(new Integer(diff).toString(), 50, 50 + (m_ID * 25));
	}

	@Override
	public void setState(MsgData msg)
	{
		super.setState(msg);
		checkRealState();
		if (!m_surPosition.equals(super.getPosition()))
		{
//			System.out.println(m_surPosition + " " + m_surDirection + " " + m_surSpeed);
		}
	}
}
