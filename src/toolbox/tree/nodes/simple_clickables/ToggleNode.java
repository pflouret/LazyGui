package toolbox.tree.nodes.simple_clickables;

import processing.core.PGraphics;
import toolbox.global.Palette;
import toolbox.tree.nodes.FolderNode;
import toolbox.tree.nodes.ValueNode;

import static processing.core.PApplet.lerp;
import static processing.core.PApplet.map;

public class ToggleNode extends ValueNode {

    public boolean valueBooleanDefault;
    public boolean valueBoolean;
    boolean armed = false;
    public float handlePosNorm;

    public ToggleNode(String path, FolderNode folder, boolean defaultValue) {
        super(path, folder);
        valueBooleanDefault = defaultValue;
        valueBoolean = defaultValue;
        handlePosNorm = valueBoolean ? 1 : 0;
    }

    @Override
    protected void updateDrawInlineNode(PGraphics pg) {
        drawToggleHandle(pg);
    }

    private void drawToggleHandle(PGraphics pg) {
        strokeContentBasedOnFocus(pg);
        fillContentBasedOnFocus(pg);
        float handleWidth = size.x * 0.1f;
        float handleXCenter = size.x * 0.9f;
        float handleXLeft = handleXCenter - handleWidth * 0.5f;
        float handleXRight = handleXCenter + handleWidth * 0.5f;
        float lerpAmt = 0.3f;
        if(valueBoolean){
            handlePosNorm = lerp(handlePosNorm,1, lerpAmt);
        }else{
            pg.fill(Palette.normalBackground);
            pg.stroke(Palette.normalForeground);
            handlePosNorm = lerp(handlePosNorm,0, lerpAmt);
        }
        float handleX = map(handlePosNorm, 0, 1, handleXLeft, handleXRight);
        float handleY = size.y * 0.52f;
        float handleDiameter = size.y * 0.5f;
        float handleWeight = 8;
        pg.strokeWeight(handleWeight);
        pg.line(handleXLeft, handleY, handleXRight, handleY);
        pg.noStroke();
        pg.ellipse(handleX, handleY, handleDiameter, handleDiameter);
    }


    @Override
    public void nodePressed(float x, float y) {
        super.nodePressed(x, y);
        armed = true;
    }

    public void mouseReleasedOverNode(float x, float y){
        super.mouseReleasedOverNode(x,y);
        if(armed){
            valueBoolean = !valueBoolean;
        }
        armed = false;
    }
}
