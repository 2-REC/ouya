package derek.android.ouyapadtest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import tv.ouya.console.api.OuyaController;


public class ControllerTestActivity extends Activity
{
    private OuyaPlotFPS m_plot = null;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );
        OuyaController.init( this );

        m_plot = ( OuyaPlotFPS )findViewById( R.id.ouyaPlotFPS1 );
        m_plot.m_fpsText = ( TextView )findViewById( R.id.fpsText );
        m_plot.m_cpu1Text = ( TextView )findViewById( R.id.cpu1Text );
        m_plot.m_cpu2Text = ( TextView )findViewById( R.id.cpu2Text );
        m_plot.m_cpu3Text = ( TextView )findViewById( R.id.cpu3Text );
        m_plot.m_cpu4Text = ( TextView )findViewById( R.id.cpu4Text );
        m_plot.m_keyDownText = ( TextView )findViewById( R.id.keyDownTime );
        m_plot.m_keyUpText = ( TextView )findViewById( R.id.keyUpTime );
        m_plot.m_genericMotionText = (TextView )findViewById( R.id.genericMotionTime );
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();   	

        OuyaPlotFPS plot = ( OuyaPlotFPS )findViewById( R.id.ouyaPlotFPS1 );
        plot.Quit();
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        m_plot.m_keyDownTime = ( System.nanoTime() / 1000000000.0 ) - ( event.getEventTime() / 1000.0 );
        View controllerView = getControllerView( event );
        controllerView.setVisibility( View.VISIBLE );
        return controllerView.onKeyDown( keyCode, event );
    }

    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event )
    {
        m_plot.m_keyUpTime = ( System.nanoTime() / 1000000000.0 ) - ( event.getEventTime() / 1000.0 );
        View controllerView = getControllerView( event );
        controllerView.setVisibility( View.VISIBLE );
        return controllerView.onKeyUp( keyCode, event );
    }

    @Override
    public boolean onGenericMotionEvent( MotionEvent event )
    {
        m_plot.m_genericMotionTime = ( System.nanoTime() / 1000000000.0 ) - ( event.getEventTime() / 1000.0 );
    	
        if( ( event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK ) == 0 )
        {
            // Not a joystick movement, so ignore it.
            return false;
        }
        View controllerView = getControllerView( event );
        controllerView.setVisibility( View.VISIBLE );
        return controllerView.onGenericMotionEvent( event );
    }

    private View getControllerView( InputEvent event )
    {
        View result = null;
        int playerNum = OuyaController.getPlayerNumByDeviceId( event.getDeviceId() );
        switch( playerNum )
        {
            default:
            case 0:
                result = findViewById( R.id.controllerView1 );
                break;
            case 1:
                result = findViewById( R.id.controllerView2 );
                break;
            case 2:
                result = findViewById( R.id.controllerView3 );
                break;
            case 3:
                result = findViewById( R.id.controllerView4 );
                break;
        }
        return result;
    }
}
