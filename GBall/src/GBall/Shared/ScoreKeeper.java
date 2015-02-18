package GBall.Shared;

import java.awt.Graphics;

public class ScoreKeeper
{
	private static class ScoreKeeperSingletonHolder
	{
		public static final ScoreKeeper instance = new ScoreKeeper();
	}

	public static ScoreKeeper getInstance()
	{
		return ScoreKeeperSingletonHolder.instance;
	}

	private int m_team1Score;
	private int m_team2Score;

	public void changeScores(int deltaTeam1, int deltaTeam2)
	{
		m_team1Score += deltaTeam1;
		m_team2Score += deltaTeam2;
	}

	private ScoreKeeper()
	{
		m_team1Score = 0;
		m_team2Score = 0;
	}
	
	public void setScore(Vector2D v)
	{
		if(v.getX() < 0 || v.getY() < 0)
		{
			return;
		}
		
		m_team1Score = (int)v.getX();
		m_team2Score = (int)v.getY();
	}
	
	public Vector2D getScore()
	{
		return new Vector2D(m_team1Score, m_team2Score);
	}

	public void render(Graphics g)
	{
		g.setFont(Const.SCORE_FONT);
		g.setColor(Const.TEAM1_COLOR);
		g.drawString(new Integer(m_team1Score).toString(), (int) Const.TEAM1_SCORE_TEXT_POSITION.getX(), (int) Const.TEAM1_SCORE_TEXT_POSITION.getY());

		g.setColor(Const.TEAM2_COLOR);
		g.drawString(new Integer(m_team2Score).toString(), (int) Const.TEAM2_SCORE_TEXT_POSITION.getX(), (int) Const.TEAM2_SCORE_TEXT_POSITION.getY());
	}
}