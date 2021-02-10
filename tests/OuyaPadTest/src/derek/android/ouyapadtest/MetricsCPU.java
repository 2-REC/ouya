package derek.android.ouyapadtest;

import java.io.IOException;
import java.io.RandomAccessFile;


public class MetricsCPU
{
    private long[] mUser;
    private long[] mSystem;
    private long[] mTotal;

    public MetricsCPU()
    {
        mUser = new long[] {0, 0, 0, 0};
        mSystem = new long[] {0, 0, 0, 0};
        mTotal = new long[] {0, 0, 0, 0};
    }

    public double readUsage( String label, int index )
    {
        try
        {
            RandomAccessFile reader = new RandomAccessFile( "/proc/stat", "r" );
            try
            {
                String line;
                while ( ( line = reader.readLine() ) != null )
                {
                    if ( line.startsWith( label ) )
                    {
                        return updateStats( line.trim().split( "[ ]+" ), index );
                    }
                }
            }
            catch ( IOException ex )
            {
            	//read file issue
            }
            reader.close();
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }

        return 0;
    }
    
    private double updateStats( String[] segs, int index )
    {
        // user = user + nice
        long user = Long.parseLong( segs[ 1 ] ) + Long.parseLong( segs[ 2 ] );
        // system = system + intr + soft_irq
        long system = Long.parseLong( segs[ 3 ] ) + Long.parseLong( segs[ 6 ] ) + Long.parseLong( segs[ 7 ] );
        // total = user + system + idle + io_wait
        long total = user + system + Long.parseLong( segs[ 4 ] ) + Long.parseLong( segs[ 5 ] );

        if ( ( mTotal[ index ] != 0 ) || ( total >= mTotal[ index ] ) )
        {
            long duser = user - mUser[ index ];
            long dsystem = system - mSystem[ index ];
            long dtotal = total - mTotal[ index ];
            return ( double )( duser + dsystem ) * 100.0 / dtotal;                
        }
        mUser[ index ] = user;
        mSystem[ index ] = system;
        mTotal[ index ] = total;
        
        return 0.0;
    }
}