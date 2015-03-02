package GBall.Client;


public class GBMain
{
	public static void main(String[] argc)
	{
		// if a parameter is submitted, assume it's a serveraddress and tell the game to connect to it
		if(argc.length > 0)
		{
			World.SERVERIP = argc[0];
		}
		World.getInstance().process();
	}
}