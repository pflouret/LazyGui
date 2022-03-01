package toolbox.windows.nodes.shaderList;

import processing.core.PGraphics;
import processing.opengl.PShader;
import toolbox.global.ShaderStore;
import toolbox.global.State;
import toolbox.windows.nodes.*;
import toolbox.windows.nodes.colorPicker.ColorPickerFolderNode;

import static processing.core.PApplet.floor;
import static processing.core.PApplet.radians;

public class ShaderListItem extends FolderNode {

    public final String shaderPath;
    ToggleNode activeNode;
    ShaderListFolder parentShaderList;
    boolean setTime;
    boolean isFilter;

    public ShaderListItem(String path, ShaderListFolder parent, String shaderPath, boolean setTime, boolean isFilter) {
        super(path, parent);
        this.shaderPath = shaderPath;
        this.setTime = setTime;
        this.isFilter = isFilter;
        parentShaderList = parent;
        activeNode = new ToggleNode(path + "/active", this, false);
        children.add(activeNode);
    }

    @Override
    protected void updateDrawInlineNode(PGraphics pg) {
        pg.pushMatrix();
        drawToggleHandle(pg, activeNode.valueBoolean);
        pg.popMatrix();
    }

    public void filter(PGraphics pg) {
        if(!activeNode.valueBoolean){
            return;
        }
        PShader shader = ShaderStore.lazyInitGetShader(shaderPath);
        if(setTime){
            shader.set("time", radians(State.app.frameCount));
        }
        for(AbstractNode node : children){
            String className = node.className.toLowerCase(); // TODO REMOVE BAD BAD BAD
            if(className.contains("sliderint")){
                SliderIntNode sliderNode = (SliderIntNode) node;
                shader.set(node.name, floor(sliderNode.valueFloat));
            }else if(className.contains("slider")){
                SliderNode sliderNode = (SliderNode) node;
                shader.set(node.name, sliderNode.valueFloat);
            }else if(className.contains("colorpickerfoldernode")){
                ColorPickerFolderNode colorNode = (ColorPickerFolderNode) node;
                int hex = colorNode.getColor().hex;
                shader.set("targetColor", new float[]{
                        State.normalizedColorProvider.red(hex),
                        State.normalizedColorProvider.green(hex),
                        State.normalizedColorProvider.blue(hex)
                }, 3);
            }
        }
        if(isFilter){
            ShaderStore.hotFilter(shaderPath, pg);
        }else{
            ShaderStore.hotShader(shaderPath, pg);
        }
    }
}
