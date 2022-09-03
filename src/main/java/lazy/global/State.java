package lazy.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import lazy.LazyGui;
import lazy.windows.nodes.AbstractNode;
import lazy.windows.nodes.NodeType;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static lazy.global.Utils.prettyPrintTree;
import static processing.core.PApplet.*;

public class State {
    public static float cell = 22;
    public static PFont font = null;
    public static PApplet app = null;
    public static LazyGui gui = null;
    public static PGraphics normalizedColorProvider = null;
    public static float textMarginX = 5;
    public static String sketchName = null;
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    public static final float defaultWindowWidthInPixels = State.cell * 10;
    private static ArrayList<File> saveFilesSorted;
    static Map<String, JsonElement> lastLoadedStateMap = new HashMap<>();
    public static File saveDir;

    static ArrayList<String> undoStack = new ArrayList<>();
    static ArrayList<String> redoStack = new ArrayList<>();

    private static long lastFrameMillis;
    private static final long lastFrameMillisStuckLimit = 1000;
    private static final int undoStackSizeLimit = 1000;

    public static void init(LazyGui gui, PApplet app) {
        State.gui = gui;
        State.app = app;
//        printAvailableFonts();
        try {
            State.font = app.createFont("Calibri", 20);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("createFont() can only be used inside setup() or after setup() has been called")) {
                throw new RuntimeException("the new Gui(this) constructor can only be used inside setup() or after setup() has been called");
            }
        }

        registerExitHandler();

        sketchName = app.getClass().getSimpleName();
        saveDir = new File(State.app.sketchPath() + "/saves/" + sketchName);
        println("Save folder path: " + saveDir.getAbsolutePath());
        if (!saveDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            saveDir.mkdirs();
        }

        normalizedColorProvider = app.createGraphics(256, 256, P2D);
        normalizedColorProvider.colorMode(HSB, 1, 1, 1, 1);
    }

    public static void createTreeSaveFiles(String filenameWithoutSuffix) {
        String jsonPath = getFullPathWithSuffix(filenameWithoutSuffix, ".json");
        overwriteFile(jsonPath, getTreeAsJsonString());
        println("Saved current state to: " + jsonPath);
        String treeViewNotice = "NOTICE: This file contains a preview of the tree found in the json next to it." +
                "\n\t\tDo not edit this file, any changes you make will probably be overwritten." +
                "\n\t\tEdit or delete the corresponding json file instead to change or erase the saved values." +
                "\n\t\tYou can find it here: " + jsonPath + "\n\n";
        overwriteFile(getFullPathWithSuffix(filenameWithoutSuffix, ".txt"), treeViewNotice + prettyPrintTree());
    }

    public static void loadMostRecentSave() {
        reloadSaveFolderContents();
        if(saveFilesSorted.size() > 0){
            loadStateFromFile(saveFilesSorted.get(0));
        }
    }


    private static void reloadSaveFolderContents() {
        File[] saveFiles = saveDir.listFiles();
        assert saveFiles != null;
        saveFilesSorted = new ArrayList<>(Arrays.asList(saveFiles));
        saveFilesSorted.removeIf(file -> !file.isFile() || !file.getAbsolutePath().contains(".json"));
        if (saveFilesSorted.size() == 0) {
            return;
        }
        saveFilesSorted.sort((o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
    }

    public static void loadStateFromFile(String filename) {
        for (File saveFile : saveFilesSorted) {
            if (saveFile.getName().equals(filename)) {
                loadStateFromFile(saveFile);
                return;
            }
        }
    }

    private static String readFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static String getFullPathWithSuffix(String filenameWithoutSuffix, String suffix){
        return getFullPathWithoutTypeSuffix(filenameWithoutSuffix + suffix);
    }

    private static String getFullPathWithoutTypeSuffix(String filenameWithSuffix){
        return saveDir.getAbsolutePath() + "\\" + filenameWithSuffix;
    }

    public static void overwriteFile(String fullPath, String content) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fullPath, false));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTreeAsJsonString() {
        return gson.toJson(NodeTree.getRoot());
    }

    public static ArrayList<File> getSaveFileList() {
        reloadSaveFolderContents();
        return saveFilesSorted;
    }

    public static void loadStateFromFile(File file){
        if(!file.exists()){
            println("Error: save file doesn't exist");
            return;
        }
        String json;
        try {
            json = readFile(file);
        } catch (IOException e) {
            println("Error loading state from file", e.getMessage());
            return;
        }
        JsonElement root = gson.fromJson(json, JsonElement.class);
        loadStateFromJson(root);
    }

    public static void loadStateFromJson(JsonElement root) {
        lastLoadedStateMap.clear();
        Queue<JsonElement> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            JsonElement loadedNode = queue.poll();
            String loadedPath = loadedNode.getAsJsonObject().get("path").getAsString();
            AbstractNode nodeToEdit = NodeTree.findNode(loadedPath);
            if (nodeToEdit != null) {
                overwriteWithLoadedStateIfAny(nodeToEdit, loadedNode);
            }
            lastLoadedStateMap.put(loadedPath, loadedNode);
            String loadedType = loadedNode.getAsJsonObject().get("type").getAsString();
            if (Objects.equals(loadedType, NodeType.FOLDER.toString())) {
                JsonArray loadedChildren = loadedNode.getAsJsonObject().get("children").getAsJsonArray();
                for (JsonElement child : loadedChildren) {
                    queue.offer(child);
                }
            }
        }
    }

    public static void overwriteWithLoadedStateIfAny(AbstractNode abstractNode) {
        overwriteWithLoadedStateIfAny(abstractNode, lastLoadedStateMap.get(abstractNode.path));
    }

    public static void overwriteWithLoadedStateIfAny(AbstractNode abstractNode, JsonElement loadedNodeState) {
        if (loadedNodeState == null) {
            return;
        }
        abstractNode.overwriteState(loadedNodeState);
    }

    private static void registerExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(State::createAutosave));
    }

    public static void createAutosave(){
        if(isSketchStuckInEndlessLoop()){
            println("NOT autosaving," +
                    " because the last frame took more than " + lastFrameMillisStuckLimit + " ms," +
                    " which looks like an endless loop due to bad settings");
            return;
        }
        createTreeSaveFiles("auto");
    }

    public static void updateEndlessLoopDetection(){
        lastFrameMillis = app.millis();
    }

    public static boolean isSketchStuckInEndlessLoop(){
        long timeSinceLastFrame = app.millis() - lastFrameMillis;
        return timeSinceLastFrame > lastFrameMillisStuckLimit;
    }

    public static void onUndoableActionEnded(){
        // TODO print diff for debug, some undos don't change anything
        pushToUndoStack();
    }

    public static void undo() {
        popFromUndoStack();
    }

    public static void redo() {
        popFromRedoStack();
    }

    private static void pushToUndoStack(){
        undoStack.add(getTreeAsJsonString());
        while(undoStack.size() > undoStackSizeLimit){
            undoStack.remove(0);
        }
        redoStack.clear();
    }

    private static void popFromUndoStack() {
        if(undoStack.isEmpty()){
            return;
        }
        String poppedJson = undoStack.remove(undoStack.size() - 1);
        redoStack.add(poppedJson);
        loadStateFromJson(gson.fromJson(poppedJson, JsonElement.class));
    }

    private static void popFromRedoStack(){
        if(redoStack.isEmpty()){
            return;
        }
        String poppedJson = redoStack.remove(redoStack.size() - 1);
        undoStack.add(poppedJson);
        loadStateFromJson(gson.fromJson(poppedJson, JsonElement.class));
    }

}
