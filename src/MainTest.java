import processing.core.PApplet;
import processing.core.PGraphics;
import toolbox.Gui;

public class MainTest extends PApplet {
    Gui gui;
    PGraphics pg;
    float shaderTime;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(1000, 1000, P2D);
//        fullScreen(P2D);
    }

    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);
    }

    @SuppressWarnings("DuplicatedCode")
    public void draw() {
        pg.beginDraw();
        pg.noStroke();
        pg.fill(gui.colorPicker("bg").hex);
        pg.rect(0,0,width, height);
        pg.stroke(gui.colorPicker("stroke").hex);
        pg.strokeWeight(gui.slider("weight", 5));
        if(gui.mousePressedOutsideGui()){
            pg.line(pmouseX, pmouseY, mouseX, mouseY);
        }
        String floatVal = String.valueOf(gui.slider("float", 1));
        String intVal = String.valueOf(gui.sliderInt("int", 1));
        pg.textAlign(LEFT);
        pg.fill(255);
        pg.textSize(64);
        pg.text(floatVal, 200, 200);
        pg.text(intVal, 200, 400);

        pg.endDraw();
        clear();
        image(pg, 0, 0);
        gui.themePicker();
        gui.draw();

        /*
        if(gui.toggle("record")){
            saveFrame("rec5/" + i++ + ".jpg");
        }
        */
    }
}
