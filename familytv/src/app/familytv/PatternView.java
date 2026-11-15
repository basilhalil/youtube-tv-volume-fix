package app.familytv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/** Background: deep emerald gradient with a faint lattice of eight-pointed stars (khatam) and a soft glow. */
final class PatternView extends View {
    private final Paint bg = new Paint();
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint();
    private final Path star = new Path();

    PatternView(Context c) {
        super(c);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(Ui.dp(c, 1.2f));
        line.setColor(0x14E8C26A);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        bg.setShader(new LinearGradient(0, 0, 0, h, Ui.BG_TOP, Ui.BG_BOTTOM, Shader.TileMode.CLAMP));
        glow.setShader(new RadialGradient(w * 0.5f, -h * 0.1f, Math.max(w, h) * 0.7f,
                0x30E8C26A, 0x00E8C26A, Shader.TileMode.CLAMP));
        // One eight-pointed star made of two overlapping squares, reused at each lattice point.
        float s = Ui.dp(getContext(), 34);
        star.reset();
        star.addRect(-s / 2, -s / 2, s / 2, s / 2, Path.Direction.CW);
        Path rot = new Path();
        rot.moveTo(0, -s * 0.707f);
        rot.lineTo(s * 0.707f, 0);
        rot.lineTo(0, s * 0.707f);
        rot.lineTo(-s * 0.707f, 0);
        rot.close();
        star.addPath(rot);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        canvas.drawRect(0, 0, w, h, bg);
        float step = Ui.dp(getContext(), 96);
        for (float y = step / 2; y < h + step; y += step) {
            for (float x = step / 2; x < w + step; x += step) {
                float ox = ((int) (y / step) % 2 == 0) ? 0 : step / 2;
                canvas.save();
                canvas.translate(x + ox, y);
                canvas.drawPath(star, line);
                canvas.restore();
            }
        }
        canvas.drawRect(0, 0, w, h, glow);
    }
}
