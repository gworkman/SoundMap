package edu.osu.sphs.soundmap.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;

import edu.osu.sphs.soundmap.R;

/**
 * A view to plot real time data
 */
public class GraphView extends View {

    private static final String TAG = "GraphView";

    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBottom;
    private int graphWidth;
    private int graphHeight;
    private int graphStartX;
    private int graphEndX;
    private int graphStartY;
    private int graphEndY;
    private int spacingX;
    private int spacingY;

    private long startTime;
    private long endTime;
    private LongSparseArray<Float> values = new LongSparseArray<>();
    private long last_time = -1;
    private float last_value = -1;

    private long x_time;
    private float y_value;

    private float xlabelHeight = 0;
    private float xLabelWidth = 0;
    private float ylabelHeight = 0;
    private float ylabelWidth = 0;
    private Paint.FontMetrics labelMetrics;

    private int gridsX;
    private int gridsY;
    private float maxX;
    private float maxY;
    private float minY;

    private float pointSize;

    private String xlabel;
    private String ylabel;
    private float labelSize;

    private Paint mainAxes;
    private Paint gridLines;
    private Paint dataPoints;
    private TextPaint labels;

    private int mainAxesColor;
    private int gridLinesColor;
    private int dataPointsColor;

    public GraphView(Context context) {
        super(context);
        init(null, 0);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.GraphView, defStyle, 0);

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.

        mainAxesColor = a.getColor(R.styleable.GraphView_axes_color, getResources().getColor(android.R.color.primary_text_light_nodisable));
        gridLinesColor = a.getColor(R.styleable.GraphView_line_color, getResources().getColor(android.R.color.primary_text_light_nodisable));
        dataPointsColor = a.getColor(R.styleable.GraphView_point_color, getResources().getColor(R.color.colorPrimary));
        gridsX = a.getInt(R.styleable.GraphView_grids_x, 10);
        gridsY = a.getInt(R.styleable.GraphView_grids_y, 10);
        maxX = a.getFloat(R.styleable.GraphView_max_x, 100f);
        maxY = a.getFloat(R.styleable.GraphView_max_y, 30f);
        minY = a.getFloat(R.styleable.GraphView_min_y, 20f);
        pointSize = a.getDimension(R.styleable.GraphView_point_size, 4f);

        if (a.hasValue(R.styleable.GraphView_x_label)) {
            xlabel = a.getString(R.styleable.GraphView_x_label);
        }
        if (a.hasValue(R.styleable.GraphView_y_label)) {
            ylabel = a.getString(R.styleable.GraphView_y_label);
        }
        if (a.hasValue(R.styleable.GraphView_label_size)) {
            labelSize = a.getDimension(R.styleable.GraphView_label_size, 42);
        }

        mainAxes = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridLines = new Paint(Paint.ANTI_ALIAS_FLAG);
        dataPoints = new Paint(Paint.ANTI_ALIAS_FLAG);
        labels = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mainAxes.setStrokeWidth(4);
        mainAxes.setColor(mainAxesColor);
        mainAxes.setStrokeJoin(Paint.Join.ROUND);
        gridLines.setColor(gridLinesColor);
        dataPoints.setColor(dataPointsColor);
        dataPoints.setStrokeWidth(pointSize);
        dataPoints.setStrokeJoin(Paint.Join.ROUND);
        labels.setTextSize(labelSize);

        if (!xlabel.isEmpty() || !ylabel.isEmpty()) {
            labelMetrics = labels.getFontMetrics();
            xlabelHeight = 1.25f * (labelMetrics.descent - labelMetrics.ascent);
            xLabelWidth = labels.measureText(xlabel);
            ylabelHeight = 1.25f * (labelMetrics.descent - labelMetrics.ascent);
            ylabelWidth = labels.measureText(ylabel);
        }

        Log.d(TAG, "init: maxX is " + maxX);
        Log.d(TAG, "init: maxY is " + maxY);
        Log.d(TAG, "init: graphStartX is " + graphStartX);
        Log.d(TAG, "init: graphStartY is " + graphStartY);
        Log.d(TAG, "init: graphEndX is " + graphEndX);
        Log.d(TAG, "init: graphEndY is " + graphEndY);


        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        graphWidth = getWidth() - paddingLeft - paddingRight - (int) ylabelHeight;
        graphHeight = getHeight() - paddingTop - paddingBottom - (int) xlabelHeight;

        spacingX = graphWidth / gridsX;
        spacingY = graphHeight / gridsY;

        graphStartX = paddingLeft + (int) ylabelHeight;
        graphEndX = getWidth() - paddingRight - (graphWidth % spacingX);
        graphStartY = getHeight() - paddingBottom - (int) xlabelHeight;
        graphEndY = paddingTop + (graphHeight % spacingY);


        // Axes
        canvas.drawLine(graphStartX, graphStartY, graphStartX, graphEndY, mainAxes);
        canvas.drawLine(graphStartX, graphStartY, graphEndX, graphStartY, mainAxes);

        // Horizontal gridlines
        for (int y = graphStartY - spacingY; y > graphEndY; y -= spacingY) {
            canvas.drawLine(graphStartX, y, graphEndX, y, gridLines);
        }

        // Vertical gridlines
        for (int x = graphStartX + spacingX; x < graphEndX; x += spacingX) {
            canvas.drawLine(x, graphStartY, x, graphEndY, gridLines);
        }

        // X Axis title
        canvas.drawText(xlabel, graphStartX + graphWidth / 2 - xLabelWidth / 2, graphStartY + xlabelHeight / 1.25f, labels);

        // Y Axis title
        canvas.save();
        canvas.translate(0, graphHeight);
        canvas.rotate(-90);
        canvas.drawText(ylabel, paddingBottom + graphHeight / 2 - ylabelWidth / 2, paddingTop + ylabelHeight / 1.25f, labels);
        canvas.restore();

        last_time = -1;
        last_value = -1;
        for (int i = 0; i < values.size(); i++) {
            x_time = values.keyAt(i);
            y_value = values.get(x_time) - minY;
            if (last_time > 0) {
                canvas.drawLine(graphWidth * (last_time / maxX) + graphStartX, graphStartY - graphHeight * (last_value / (maxY - minY)),
                        graphWidth * (x_time / maxX) + graphStartX, graphStartY - graphHeight * (y_value / (maxY - minY)), dataPoints);
            }

            last_time = x_time;
            last_value = y_value;

        }

    }

    public void clear() {
        this.values.clear();
        startTime = -1;
        endTime = -1;
        last_time = -1;
        last_value = -1;
        invalidate();
    }

    /**
     * Requires that @time is greater than all of the other times before it in the list
     *
     * @param time  the time in millis to add the value
     * @param value the point value to add
     */
    public void add(long time, float value) {
        if (this.values.size() == 0) startTime = time;
        this.values.append((time - startTime) / 100, value);
        invalidate();
    }

}
