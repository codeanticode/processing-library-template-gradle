package ch.bildspur.vision;

import ch.bildspur.vision.network.NetworkFactory;
import ch.bildspur.vision.result.ClassificationResult;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_dnn.Net;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_dnn.blobFromImage;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGB2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

public abstract class ClassificationNetwork extends LabeledNetwork<ClassificationResult> implements NetworkFactory {
    private Net net;

    private int width;
    private int height;

    private boolean convertToGrayScale;

    private float scaleFactor;
    private Scalar mean;
    private boolean swapRB;
    private boolean crop;

    public ClassificationNetwork(int width, int height, boolean convertToGrayScale, float scaleFactor, Scalar mean, boolean swapRB, boolean crop, String... labels) {
        this.width = width;
        this.height = height;
        this.convertToGrayScale = convertToGrayScale;
        this.scaleFactor = scaleFactor;
        this.mean = mean;
        this.swapRB = swapRB;
        this.crop = crop;

        this.setLabels(labels);
    }

    @Override
    public boolean setup() {
        net = createNetwork();

        if (net.empty()) {
            System.out.println("Can't load network!");
            return false;
        }

        return true;
    }

    @Override
    public ClassificationResult run(Mat frame) {
        // convert to gray
        if (convertToGrayScale)
            cvtColor(frame, frame, COLOR_RGB2GRAY);

        // convert image into batch of images
        Mat inputBlob = blobFromImage(frame, scaleFactor, new Size(width, height), mean, swapRB, crop, CV_32F);

        // set input
        net.setInput(inputBlob);

        // run detection
        Mat out = net.forward();

        // extract result
        FloatPointer data = new FloatPointer(out.row(0).data());

        // todo: use minmaxidx
        int maxIndex = -1;
        float maxProbability = -1.0f;

        for (int i = 0; i < out.cols(); i++) {
            float probability = data.get(i) / 100f;

            if (probability > maxProbability) {
                maxProbability = probability;
                maxIndex = i;
            }
        }

        return new ClassificationResult(maxIndex, getLabelOrId(maxIndex), maxProbability);
    }
}
