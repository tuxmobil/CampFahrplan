package chaos.fahrplan.congress;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.HorizontalScrollView;


public class HorizontalSnapScrollView extends HorizontalScrollView {
	private GestureDetector gestureDetector;
	private int activeItem = 0;
	private int xStart;
	private float scale;
	private int screenWidth;

	/**
	 * get currently displayed column index
	 *
	 * @return index (0..n)
	 */
	public int getColumn() {
		return activeItem;
	}

	class YScrollDetector extends SimpleOnGestureListener {
	    @Override
	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	        try {
	            if (Math.abs(distanceX) > Math.abs(distanceY)) {
	                return true;
	            } else {
	                return false;
	            }
	        } catch (Exception e) {
	            // nothing
	        }
	        return false;
	    }

	    @Override
	    public boolean onDown(MotionEvent e) {
    		xStart = getScrollX();
    		activeItem = xStart/getMeasuredWidth();
    		return super.onDown(e);
	    }
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
	    return super.onTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	    //Call super first because it does some hidden motion event handling
	    boolean result = super.onInterceptTouchEvent(ev);
	    //Now see if we are scrolling vertically with the custom gesture detector
	    if (gestureDetector.onTouchEvent(ev)) {
	        return result;
	    }
	    //If not scrolling vertically (more y than x), don't hijack the event.
	    else {
	        return false;
	    }
	}

	public void scrollToColumn(int col) {
		int scrollTo = (int)(col * (screenWidth * scale));
	    smoothScrollTo(scrollTo, 0);
        Fahrplan.updateRoomTitle(col);
        activeItem = col;
	}

	public HorizontalSnapScrollView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    gestureDetector = new GestureDetector(new YScrollDetector());
		scale = getResources().getDisplayMetrics().density;
		screenWidth = (int) (getResources().getDisplayMetrics().widthPixels / scale);
		screenWidth -= 38;	// Breite für Zeitenspalte
	    setOnTouchListener(new View.OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {
	                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
	                    int scrollX = getScrollX();
	                    // Berechnung mit getMeasuredWidth() führt zu Rundungsfehlern bei
	                    // steigender Zahl der Spalten, da schon zu früh zu (int)
	                    // gecastet wird.
	                    // Workaround: Breite hier vorgeben (285dp) und selber skalieren
	                    int itemWidth = (int)(screenWidth * scale);
	                    int distance = scrollX - xStart;
	                    int newItem = activeItem;
	                    if (Math.abs(distance) > (itemWidth/4)) {
		                    if (distance > 0) {
		                    	newItem = activeItem + 1;
		                    } else {
		                    	newItem = activeItem - 1;
		                    }
	                    }
	                    scrollToColumn(newItem);

	                    return true;
	                } else {
	                    return false;
	                }
	            }
	        });
	    }
}
