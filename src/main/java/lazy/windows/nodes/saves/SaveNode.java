package lazy.windows.nodes.saves;

import processing.core.PGraphics;
import lazy.State;
import lazy.windows.nodes.AbstractNode;
import lazy.windows.nodes.NodeFolder;
import lazy.windows.nodes.NodeType;

class SaveNode extends AbstractNode {
    String fileName, fullPath;

    public SaveNode(String path, NodeFolder parent, String fileName, String fullPath) {
        super(NodeType.TRANSIENT, path, parent);
        this.fileName = fileName;
        this.fullPath = fullPath;
    }

    protected void updateDrawInlineNodeAbstract(PGraphics pg) {
        fillForegroundBasedOnMouseOver(pg);
        drawRightText(pg, "load");
    }

    public void mousePressedOverNode(float x, float y) {
        State.loadStateFromFile(fileName);
    }
}
