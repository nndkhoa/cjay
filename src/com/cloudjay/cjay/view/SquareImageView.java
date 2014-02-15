package com.cloudjay.cjay.view;

import com.cloudjay.cjay.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 
 * User can configure fixed along dimension
 * 
 * @author tieubao
 * 
 */
public class SquareImageView extends ImageView {

	public static enum FixedAlong {
		width, height
	}

	private FixedAlong fixedAlong = FixedAlong.width;

	public SquareImageView(Context context) {
		super(context);
	}

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.SquareImage, 0, 0);

		// get fixed along value from xml configuration
		fixedAlong = FixedAlong.valueOf(array
				.getString(R.styleable.SquareImage_fixedAlong));

		// save fixed along value
		if (fixedAlong == null)
			fixedAlong = FixedAlong.width;

		array.recycle();
	}

	public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	int squareDimen = 1;

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int square = (fixedAlong == FixedAlong.width) ? getMeasuredWidth()
				: getMeasuredHeight();

		if (square > squareDimen) {
			squareDimen = square;
		}

		setMeasuredDimension(squareDimen, squareDimen);
	}
}