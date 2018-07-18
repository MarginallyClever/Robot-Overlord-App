package com.marginallyclever.robotOverlord;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.junit.Test;

public class DL4JTest {
	@Test
	public void testDL4J() throws Exception {
		final int numRows = 28; // The number of rows of a matrix.
	    final int numColumns = 28; // The number of columns of a matrix.
	    int outputNum = 10; // Number of possible outcomes (e.g. labels 0 through 9).
	    int batchSize = 128; // How many examples to fetch with each step.
	    int rngSeed = 123; // This random-number generator applies a seed to ensure that the same initial weights are used when training. We’ll explain why this matters later.
	    int numEpochs = 15; // An epoch is a complete pass through a given dataset.
	    
		DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
		DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);
		
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	            .seed(rngSeed)
	            .l2(1e-4)
	            .weightInit(WeightInit.XAVIER)
	            .updater(new Nesterovs(0.006,0.9))
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            .list()
	            .layer(0, new DenseLayer.Builder()
	                    .nIn(numRows * numColumns) // Number of input data points.
	                    .nOut(1000) // Number of output data points.
	                    .activation(Activation.IDENTITY) // Activation function.
	                    .weightInit(WeightInit.XAVIER) // Weight initialization.
	                    .build())
	            .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
	                    .nIn(1000)
	                    .nOut(outputNum)
	                    .activation(Activation.SOFTMAX)
	                    .weightInit(WeightInit.XAVIER)
	                    .build())
	            .pretrain(false)
	            .backprop(true)
	            .build();
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        //print the score with every 1 iteration
        model.setListeners(new ScoreIterationListener(1));

        System.out.println("Train model....");
        model.setListeners(new ScoreIterationListener(10)); //Print score every 10 iterations
        for (int i=0; i <numEpochs; ++i) {
            model.fit(mnistTrain);
            System.out.println("*** Completed epoch "+i+" ***");
        }

        System.out.println("Evaluate model....");
        Evaluation eval = model.evaluate(mnistTest);
        System.out.println(eval.stats());
        System.out.println("****************Example finished********************");
	}
}
