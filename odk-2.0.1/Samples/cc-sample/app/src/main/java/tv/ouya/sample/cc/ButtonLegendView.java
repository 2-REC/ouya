package tv.ouya.sample.cc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ButtonLegendView extends FrameLayout {


    public ButtonLegendView(Context context) {
        super(context);
        init();
    }

    public ButtonLegendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonLegendView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addView(View.inflate(getContext(), R.layout.button_legend, null));

        LinearLayout list = (LinearLayout) findViewById(R.id.control_list);

        for(Control c : Control.values()) {
            final View v = View.inflate(getContext(), R.layout.item_control, null);
            ((TextView)v.findViewById(R.id.name)).setText(c.stringRes);
            ((ImageView)v.findViewById(R.id.icon)).setImageResource(c.buttonRes);
            list.addView(v);
        }
    }

    // Our OuyaController ButtonData doesn't currently include icons for generic L/R analog sticks.
    // Once it does, you could alternatively use `OuyaController.getButtonData(button).buttonDrawable`
    private enum Control {
        MOVE(R.drawable.ic_dpad, R.string.move),
        SCALE(R.drawable.ic_ls, R.string.scale),
        ROTATE(R.drawable.ic_rs, R.string.rotate),
        ;
        final public int buttonRes, stringRes;
        Control(int button, int stringRes) {
            this.buttonRes = button;
            this.stringRes = stringRes;
        }
    }
}
