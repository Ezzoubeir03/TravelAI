package com.example.travelai;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class RouteMapView extends View {

    private float d;
    private Paint bgPaint, roadPaint, majorRoadPaint, waterPaint, routePaint,
            markerPaint, markerDonePaint, ringPaint, startFill, startRing, textPaint;
    private final Path waterPath = new Path();
    private final Path routePath = new Path();
    private final float[][] roads = new float[22][4];
    private final float[][] pts = {
            {0.14f, 0.80f}, {0.30f, 0.55f}, {0.46f, 0.30f}, {0.60f, 0.54f},
            {0.76f, 0.26f}, {0.88f, 0.58f}, {0.70f, 0.82f}};

    private int stopCount = 4;
    private boolean[] done = new boolean[0];
    private float progress = 1f;
    private ValueAnimator animator;

    public RouteMapView(Context c) {
        super(c);
        init();
    }

    public RouteMapView(Context c, AttributeSet a) {
        super(c, a);
        init();
    }

    private void init() {
        d = getResources().getDisplayMetrics().density;

        bgPaint = fill(getResources().getColor(R.color.map_bg));

        roadPaint = stroke(getResources().getColor(R.color.map_road), 2 * d);
        majorRoadPaint = stroke(getResources().getColor(R.color.map_road), 5 * d);
        waterPaint = stroke(getResources().getColor(R.color.map_water), 26 * d);

        routePaint = stroke(getResources().getColor(R.color.green_primary), 4 * d);
        routePaint.setStrokeJoin(Paint.Join.ROUND);
        routePaint.setStrokeCap(Paint.Cap.ROUND);

        markerPaint = fill(getResources().getColor(R.color.map_marker));
        markerDonePaint = fill(getResources().getColor(R.color.green_primary));
        ringPaint = stroke(Color.WHITE, 2 * d);

        startFill = fill(Color.WHITE);
        startRing = stroke(getResources().getColor(R.color.green_primary), 5 * d);

        textPaint = fill(Color.WHITE);
        textPaint.setTextSize(12 * d);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        Random r = new Random(11);
        for (int i = 0; i < roads.length; i++) {
            if (i % 2 == 0) {
                float y = r.nextFloat();
                roads[i] = new float[]{0f, y, 1f, y + (r.nextFloat() - 0.5f) * 0.4f};
            } else {
                float x = r.nextFloat();
                roads[i] = new float[]{x, 0f, x + (r.nextFloat() - 0.5f) * 0.4f, 1f};
            }
        }
    }

    private Paint fill(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        return p;
    }

    private Paint stroke(int color, float width) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(color);
        p.setStrokeWidth(width);
        return p;
    }

    public void setStops(int count, boolean[] doneFlags) {
        stopCount = Math.max(1, Math.min(count, pts.length - 1));
        done = doneFlags;
        invalidate();
    }

    public void play() {
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1400);
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();

        canvas.drawRect(0, 0, w, h, bgPaint);

        for (int i = 0; i < roads.length; i++) {
            Paint p = (i % 5 == 0) ? majorRoadPaint : roadPaint;
            canvas.drawLine(roads[i][0] * w, roads[i][1] * h, roads[i][2] * w, roads[i][3] * h, p);
        }

        waterPath.reset();
        waterPath.moveTo(0, h * 0.62f);
        waterPath.cubicTo(w * 0.3f, h * 0.40f, w * 0.55f, h * 0.90f, w, h * 0.50f);
        canvas.drawPath(waterPath, waterPaint);

        // Route line (animated)
        routePath.reset();
        for (int i = 0; i <= stopCount; i++) {
            float x = pts[i][0] * w;
            float y = pts[i][1] * h;
            if (i == 0) routePath.moveTo(x, y);
            else routePath.lineTo(x, y);
        }
        PathMeasure pm = new PathMeasure(routePath, false);
        Path part = new Path();
        pm.getSegment(0, pm.getLength() * progress, part, true);
        part.rLineTo(0, 0);
        canvas.drawPath(part, routePaint);

        // Start marker
        float sx = pts[0][0] * w;
        float sy = pts[0][1] * h;
        canvas.drawCircle(sx, sy, 13 * d, startFill);
        canvas.drawCircle(sx, sy, 9 * d, startRing);

        // Stop markers
        for (int i = 1; i <= stopCount; i++) {
            if (progress < (float) i / stopCount - 0.05f) continue;
            float x = pts[i][0] * w;
            float y = pts[i][1] * h;
            boolean isDone = (i - 1) < done.length && done[i - 1];
            canvas.drawCircle(x, y, 15 * d, isDone ? markerDonePaint : markerPaint);
            canvas.drawCircle(x, y, 15 * d, ringPaint);
            canvas.drawText(String.valueOf(i), x, y + 4.5f * d, textPaint);
        }
    }
}