package com.android_dev.rentaly_management.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DonutChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private float[] values = new float[0];
    private int[] colors = new int[0];
    private String centerText = "";

    public DonutChartView(Context context) { super(context); }
    public DonutChartView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

    public void setData(float[] values, int[] colors, String centerText) {
        this.values = values == null ? new float[0] : values;
        this.colors = colors == null ? new int[0] : colors;
        this.centerText = centerText == null ? "" : centerText;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float total = 0;
        for (float value : values) total += Math.max(0, value);
        float size = Math.min(getWidth(), getHeight()) - 32f;
        float left = (getWidth() - size) / 2f;
        float top = (getHeight() - size) / 2f;
        bounds.set(left, top, left + size, top + size);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(34f);
        paint.setStrokeCap(Paint.Cap.BUTT);
        if (total <= 0) {
            paint.setColor(0xFFD5D9DE);
            canvas.drawArc(bounds, 0, 360, false, paint);
        } else {
            float start = -90f;
            for (int index = 0; index < values.length && index < colors.length; index++) {
                float sweep = Math.max(0, values[index]) / total * 360f;
                paint.setColor(colors[index]);
                canvas.drawArc(bounds, start, sweep, false, paint);
                start += sweep;
            }
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF334257);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(18f * getResources().getDisplayMetrics().scaledDensity);
        canvas.drawText(centerText, getWidth() / 2f, getHeight() / 2f + paint.getTextSize() / 3f, paint);
    }
}
