package io.github.some_example_name.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Abstract Engine Output Manager.
 * Handles rendering graphics and text to the screen.
 */
public class OutputManager implements Disposable {

    private SpriteBatch batch;
    private BitmapFont font;
    private Texture pixel;
    private GlyphLayout glyphLayout;
    
    // Abstract Engine requirement: Encapsulate the "How" (LibGDX Batch) 
    // from the "What" (Draw this texture).

    public OutputManager() {
        this.batch = new SpriteBatch();
        this.font = new BitmapFont(); // Default font
        this.font.getData().setScale(1.0f);
        this.glyphLayout = new GlyphLayout();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 1f, 1f);
        pm.fill();
        this.pixel = new Texture(pm);
        pm.dispose();
    }

    /** call this at the start of the render loop */
    public void begin() {
        batch.begin();
    }

    /** call this at the end of the render loop */
    public void end() {
        batch.end();
    }

    /**
     * Draws a texture at the specified coordinates.
     * @param texture The image to draw
     * @param x X position
     * @param y Y position
     */
    public void draw(Texture texture, float x, float y) {
        batch.draw(texture, x, y);
    }

    /**
     * Draws a texture with specified size.
     * @param texture The image to draw
     * @param x X position
     * @param y Y position
     * @param width Target width
     * @param height Target height
     */
    public void draw(Texture texture, float x, float y, float width, float height) {
        batch.draw(texture, x, y, width, height);
    }

    /**
     * Draws a texture with specified size and tint color.
     * Useful for debug overlays (hitboxes, bounding boxes, etc.).
     */
    public void drawTinted(Texture texture, float x, float y, float width, float height, Color tint) {
        if (texture == null || tint == null) return;

        Color prev = batch.getColor();
        float pr = prev.r, pg = prev.g, pb = prev.b, pa = prev.a;
        batch.setColor(tint);
        batch.draw(texture, x, y, width, height);
        batch.setColor(pr, pg, pb, pa);

    }

    /**
     * Draws a filled rectangle using a shared 1x1 white pixel texture.
     */
    public void drawRect(float x, float y, float width, float height, Color color) {
        if (color == null || width <= 0f || height <= 0f) return;
        drawTinted(pixel, x, y, width, height, color);
    }

    /**
     * Draws text to the screen.
     * @param text The string to display
     * @param x X position
     * @param y Y position
     */
    public void drawText(String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    public void drawText(String text, float x, float y, Color color) {
        if (color == null) {
            drawText(text, x, y);
            return;
        }

        Color previous = font.getColor();
        float pr = previous.r;
        float pg = previous.g;
        float pb = previous.b;
        float pa = previous.a;
        font.setColor(color);
        font.draw(batch, text, x, y);
        font.setColor(pr, pg, pb, pa);
    }

    public void drawTextCentered(String text, float centerX, float y) {
        drawTextCentered(text, centerX, y, null);
    }

    public void drawTextCentered(String text, float centerX, float y, Color color) {
        String safeText = text == null ? "" : text;
        glyphLayout.setText(font, safeText);
        drawText(safeText, centerX - glyphLayout.width * 0.5f, y, color);
    }

    public void drawTextRightAligned(String text, float rightX, float y, Color color) {
        String safeText = text == null ? "" : text;
        glyphLayout.setText(font, safeText);
        drawText(safeText, rightX - glyphLayout.width, y, color);
    }

    public void drawTextWithShadow(String text, float x, float y, Color color) {
        drawText(text, x + 2f, y - 2f, new Color(0f, 0f, 0f, 0.7f));
        drawText(text, x, y, color);
    }

    public void drawTextCenteredWithShadow(String text, float centerX, float y, Color color) {
        String safeText = text == null ? "" : text;
        glyphLayout.setText(font, safeText);
        float x = centerX - glyphLayout.width * 0.5f;
        drawTextWithShadow(safeText, x, y, color);
    }

    public void drawPanel(float x, float y, float width, float height, Color fillColor, Color borderColor) {
        drawPanel(x, y, width, height, 3f, fillColor, borderColor);
    }

    public void drawPanel(float x, float y, float width, float height, float borderSize, Color fillColor, Color borderColor) {
        if (fillColor != null) {
            drawRect(x, y, width, height, fillColor);
        }
        if (borderColor == null || borderSize <= 0f) {
            return;
        }

        drawRect(x, y + height - borderSize, width, borderSize, borderColor);
        drawRect(x, y, width, borderSize, borderColor);
        drawRect(x, y, borderSize, height, borderColor);
        drawRect(x + width - borderSize, y, borderSize, height, borderColor);
    }

    // Add this method inside your existing OutputManager class
    public void clearScreen(float r, float g, float b, float a) {
        com.badlogic.gdx.utils.ScreenUtils.clear(r, g, b, a);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        pixel.dispose();
    }
    
    // Helper to get raw batch if absolutely needed (try to avoid using this in Game Scenes)
    public SpriteBatch getBatch() {
        return batch;
    }


}
