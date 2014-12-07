package com.dataart.vyakunin.udacitystudyproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/*
Just a simple bar-chart to show the temperature changes
 */
public class CustomView extends View {
    private final Paint paint;
    private ArrayList<TemperatureItem> values;
    int graphOffset = 12;
    float strokeSize = 10;
    private int backgroundColor;

    public CustomView(Context context) {
        this(context, null);
    }

    public CustomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        values = new ArrayList<TemperatureItem>();
        paint = new Paint();
        paint.setAntiAlias(true);
        backgroundColor = context.getResources().getColor(R.color.custom_view_background);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 100;
        int desiredHeight = 200;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        int maxHeight = getHeight();
        if (maxHeight <= 2 * graphOffset) {
            throw new IllegalStateException("Height is too small to draw a graph");
        }
        canvas.drawColor(backgroundColor); //clear
        paint.reset();
        paint.setTextSize(16);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        canvas.drawLine(graphOffset, graphOffset, graphOffset, maxHeight - graphOffset, paint);//vertical line
        double minT = 1000;
        double maxT = -1000;
        for (TemperatureItem item : values) {
            if (item.getDayTemp() > maxT)
                maxT = item.getDayTemp();
            if (item.getNightTemp() < minT)
                minT = item.getNightTemp();
        }

        canvas.drawLine(graphOffset, maxHeight - graphOffset, getWidth() - graphOffset, maxHeight - graphOffset, paint);

        double scaleY = ((getHeight() - 4 * graphOffset) / (maxT + -1 * minT));
        int xOffset = graphOffset * 2;
        float yAdd = (float) ((-1 * minT) * scaleY);
        float yOffset = 0;
        for (int i = 0; i < values.size(); i++) {
            paint.setColor(Color.RED);
            yOffset = -1 * yAdd + maxHeight - graphOffset * 2 - (float) (values.get(i).getDayTemp() * scaleY);
            canvas.drawRect(xOffset, yOffset, xOffset + strokeSize, maxHeight - graphOffset, paint);
            xOffset += strokeSize;
            paint.setColor(Color.BLUE);
            yOffset = -1 * yAdd + maxHeight - graphOffset * 2 - (float) (values.get(i).getNightTemp() * scaleY);
            canvas.drawRect(xOffset, yOffset, xOffset + strokeSize, maxHeight - graphOffset, paint);
            xOffset += 2 * strokeSize;
        }
    }

    public ArrayList<TemperatureItem> getValues() {
        return values;
    }

    public void setValues(ArrayList<TemperatureItem> values) {
        this.values = values;
        postInvalidate();
    }
}
