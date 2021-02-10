package derek.android.ouyatest;


public class Options
{
    static private Options instance = new Options();

    static public Options getInstance()
    {
        return instance;
    }

    enum Level
    {
        FREEDOM,
        ALLEYWAY,
        BOXY,
    }
    private Level level = Level.FREEDOM;

    public Level getLevel()
    {
        return level;
    }

    public void setLevel( Level level )
    {
        this.level = level;
    }

    private Options()
    {
    }
}
