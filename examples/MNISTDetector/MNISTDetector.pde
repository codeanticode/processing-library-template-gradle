import ch.bildspur.vision.*;
import ch.bildspur.vision.result.*;

DeepVision vision;
MNISTClassificationNetwork network;

ClassificationResult result;
long inferenceTime = 0;
PImage canvas = new PImage(28, 28, RGB);

boolean mouseDrawing = false;
float factor = 560 / 28;

public void setup() {
  size(560, 560, FX2D);
  colorMode(HSB, 360, 100, 100);

  vision = new DeepVision(this);

  println("creating network...");
  network = vision.createMNISTClassifier();

  println("loading model...");
  network.setup();

  clearCanvas();
  println("ready!");
}

public void draw() {
  background(55);

  if (mouseDrawing) {
    int x = round(mouseX / factor);
    int y = round(mouseY / factor);

    canvas.set(x, y, color(255));
    canvas.updatePixels();
  }

  //image(canvas, 0, 0, width, height);
  for (int y = 0; y < canvas.width; y++) {
    for (int x = 0; x < canvas.width; x++) {
      color c = canvas.get(x, y);
      fill(c);
      noStroke();
      rect(x * factor, y * factor, factor, factor);
    }
  }

  // display info
  fill(140, 80, 100);
  textSize(16);
  text("space: run inference / c: clear canvas", 10, 20);

  if (result != null) {
    text("Detected '" + result.getClassName() + "' with " + result.getConfidence() + "%", 10, 50);
  }

  surface.setTitle("MNIST Detector");
}

void mousePressed() {
  mouseDrawing = true;
}

void mouseReleased() {
  mouseDrawing = false;
}

void keyPressed() {
  if (key == ' ') {
    println("inferencing...");
    int start = millis();
    result = network.run(canvas);
    inferenceTime = millis() - start;
  }

  if (key == 'c' || key == 'C') {
    println("clearing canvas...");
    clearCanvas();
    result = null;
  }
}

void clearCanvas() {
  for (int y = 0; y < canvas.width; y++) {
    for (int x = 0; x < canvas.width; x++) {
      canvas.set(x, y, color(0));
    }
  }
  canvas.updatePixels();
}
