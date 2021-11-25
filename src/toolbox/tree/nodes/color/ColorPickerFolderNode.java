package toolbox.tree.nodes.color;

import processing.core.PGraphics;
import toolbox.global.State;
import toolbox.tree.nodes.FolderNode;

import static processing.core.PConstants.*;

public class ColorPickerFolderNode extends FolderNode {

    public int hex;
    ColorPreviewNode previewNode;
    HueNode hueNode;
    SaturationNode saturationNode;
    BrightnessNode brightnessNode;
    AlphaNode alphaNode;

    public ColorPickerFolderNode(String path, FolderNode parentFolder, int hex) {
        super(path, parentFolder);
        this.hex = hex;
        previewNode = new ColorPreviewNode(path + "/preview", this);
        PGraphics colorProvider = State.colorProvider;
        hueNode = new HueNode(path + "/h", this, colorProvider.hue(hex));
        saturationNode = new SaturationNode(path + "/s", this, colorProvider.saturation(hex));
        brightnessNode = new BrightnessNode(path + "/b", this, colorProvider.brightness(hex));
        alphaNode = new AlphaNode(path + "/a", this, colorProvider.alpha(hex));
        children.add(previewNode);
        children.add(hueNode);
        children.add(saturationNode);
        children.add(brightnessNode);
        children.add(alphaNode);
    }

    @Override
    protected void updateDrawInlineNode(PGraphics pg) {
        strokeContentBasedOnFocus(pg);
        float previewRectSize = cell * 0.65f;
        pg.translate(size.x - cell * 0.5f, size.y * 0.5f);
        pg.rectMode(CENTER);
        pg.fill(hex);
        pg.rect(0, 0, previewRectSize, previewRectSize);
    }

    public void loadValuesFromHSBA(){
        PGraphics colorProvider = State.colorProvider;
        colorProvider.colorMode(HSB,1,1,1,1);
        hex = colorProvider.color(hueNode.valueFloat, saturationNode.valueFloat, brightnessNode.valueFloat,alphaNode.valueFloat);
    }

    Color outputColor = new Color();
    public Color getColor() {
        outputColor.hex = hex;
        return outputColor;
    }

    public float hue() {
        return hueNode.valueFloat;
    }

    public float saturation() {
        return saturationNode.valueFloat;
    }

    public float brightness() {
        return brightnessNode.valueFloat;
    }
    public float alpha() {
        return alphaNode.valueFloat;
    }
}
