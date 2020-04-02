package ch.bildspur.vision;

import ch.bildspur.vision.network.DeepNeuralNetwork;
import ch.bildspur.vision.result.TextResult;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.tesseract.TessBaseAPI;

import java.nio.file.Path;

import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

public class TesseractNetwork extends DeepNeuralNetwork<TextResult> {
    private Path model;
    private String language;
    private TessBaseAPI api = new TessBaseAPI();

    public TesseractNetwork(Path model, String language) {
        this.model = model;
        this.language = language;
    }

    @Override
    public boolean setup() {
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(model.toAbsolutePath().getParent().toString(), language) != 0) {
            System.err.println("Could not initialize tesseract.");
            return false;
        }

        return true;
    }

    @Override
    public TextResult run(Mat frame) {
        BytePointer outText;

        Mat gray = new Mat();
        cvtColor(frame, gray, CV_BGR2GRAY);

        api.SetImage(gray.data().asBuffer(),
                gray.size().width(), gray.size().height(),
                gray.channels(), gray.size(1));

        outText = api.GetUTF8Text();
        String text = outText.getString();
        outText.deallocate();

        return new TextResult(text, -1.0f);
    }

    public void release() {
        api.End();
    }
}