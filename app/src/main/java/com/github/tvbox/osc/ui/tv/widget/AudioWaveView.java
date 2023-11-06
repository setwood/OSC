package com.github.tvbox.osc.ui.tv.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class AudioWaveView extends View {
    private int columnCount = 7;
    private Handler handler = new Handler() {
        /* class com.github.tvbox.osc.ui.tv.widget.AudioWaveView.AnonymousClass1 */

        public void handleMessage(Message message) {
            AudioWaveView.this.invalidate();
        }
    };
    private Paint paint;
    private Random random;
    private int randomHeight;
    private RectF rectF1;
    private RectF rectF2;
    private RectF rectF3;
    private RectF rectF4;
    private RectF rectF5;
    private RectF rectF6;
    private RectF rectF7;
    private int rectWidth;
    private final int space = 8;
    private int viewHeight;
    private int viewWidth;

    public AudioWaveView(Context context) {
        super(context);
        init();
    }

    public AudioWaveView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.viewWidth = View.MeasureSpec.getSize(i);
        this.viewHeight = View.MeasureSpec.getSize(i2);
        int i3 = this.viewWidth;
        int i4 = this.columnCount;
        this.rectWidth = (i3 - ((i4 - 1) * 8)) / i4;
    }

    private void init() {
        Paint paint2 = new Paint();
        this.paint = paint2;
        paint2.setColor(-1);
        this.paint.setStyle(Paint.Style.FILL);
        this.random = new Random();
        initRect();
    }

    private void initRect() {
        this.rectF1 = new RectF();
        this.rectF2 = new RectF();
        this.rectF3 = new RectF();
        this.rectF4 = new RectF();
        this.rectF5 = new RectF();
        this.rectF6 = new RectF();
        this.rectF7 = new RectF();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = this.rectWidth + 8;
        int nextInt = this.random.nextInt(this.viewHeight);
        this.randomHeight = nextInt;
        int i2 = i * 0;
        this.rectF1.set((float) i2, (float) nextInt, (float) (i2 + this.rectWidth), (float) this.viewHeight);
        int nextInt2 = this.random.nextInt(this.viewHeight);
        this.randomHeight = nextInt2;
        int i3 = i * 1;
        this.rectF2.set((float) i3, (float) nextInt2, (float) (i3 + this.rectWidth), (float) this.viewHeight);
        int nextInt3 = this.random.nextInt(this.viewHeight);
        this.randomHeight = nextInt3;
        int i4 = i * 2;
        this.rectF3.set((float) i4, (float) nextInt3, (float) (i4 + this.rectWidth), (float) this.viewHeight);
        int nextInt4 = this.random.nextInt(this.viewHeight);
        this.randomHeight = nextInt4;
        int i5 = i * 3;
        this.rectF4.set((float) i5, (float) nextInt4, (float) (i5 + this.rectWidth), (float) this.viewHeight);
        int nextInt5 = this.random.nextInt(this.viewHeight);
        this.randomHeight = nextInt5;
        int i6 = i * 4;
        this.rectF5.set((float) i6, (float) nextInt5, (float) (i6 + this.rectWidth), (float) this.viewHeight);
        int nextInt6 = this.random.nextInt(this.viewHeight);
        this.randomHeight = nextInt6;
        int i7 = i * 5;
        this.rectF6.set((float) i7, (float) nextInt6, (float) (i7 + this.rectWidth), (float) this.viewHeight);
        int nextInt7 = this.random.nextInt(this.viewHeight);
        this.randomHeight = nextInt7;
        int i8 = i * 6;
        this.rectF7.set((float) i8, (float) nextInt7, (float) (i8 + this.rectWidth), (float) this.viewHeight);
        canvas.drawRect(this.rectF1, this.paint);
        canvas.drawRect(this.rectF2, this.paint);
        canvas.drawRect(this.rectF3, this.paint);
        canvas.drawRect(this.rectF4, this.paint);
        canvas.drawRect(this.rectF5, this.paint);
        canvas.drawRect(this.rectF6, this.paint);
        canvas.drawRect(this.rectF7, this.paint);
        this.handler.sendEmptyMessageDelayed(0, 300);
    }
}
