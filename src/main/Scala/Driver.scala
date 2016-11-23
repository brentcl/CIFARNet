/**
  * Created by Brent on 11/23/2016.
  */
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.api.buffer.DataBuffer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions

object Driver {

  def main(args: Array[String]): Unit = {

    //Nd4j.dtype = DataBuffer.Type.DOUBLE
    Nd4j.factory().setDType(DataBuffer.Type.DOUBLE)
    Nd4j.ENFORCE_NUMERICAL_STABILITY = true

    val nChannels = 3
    val outputNum = 10
    val batchSize = 64
    val nEpochs = 10
    val iterations = 1
    val seed = 12345
    val learnRate = .0001
    val dropOutRetainProbability = .9


    println("Build model....")
    val builder: MultiLayerConfiguration.Builder = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(iterations)
      .regularization(true).l2(0.0005)
      .learningRate(learnRate)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(Updater.NESTEROVS).momentum(0.9)
      .list()
      .layer(0, new ConvolutionLayer.Builder(5, 5)
        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the # of filters to be applied
        .nIn(nChannels)
        .stride(1, 1)
        .padding(2,2)
        .nOut(32)
        .activation("relu")
        .dropOut(dropOutRetainProbability)
        .build())
      .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(2, 2)
        .build())
      .layer(2, new ConvolutionLayer.Builder(5, 5)
        //Note that nIn needed be specified in later layers
        .stride(1, 1)
        .nOut(64)
        .activation("relu")
        .dropOut(dropOutRetainProbability)
        .build())
      .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(2, 2)
        .build())
      .layer(4, new ConvolutionLayer.Builder(5, 5)
        .stride(1,1)
        .nOut(128)
        .activation("relu")
        .dropOut(dropOutRetainProbability)
        .build())
      .layer(5, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(2, 2)
        .build())
      .layer(6, new DenseLayer.Builder().activation("relu")
        .nOut(1024)
        .dropOut(dropOutRetainProbability).build())
      .layer(7, new DenseLayer.Builder().activation("relu")
        .nOut(512)
        .dropOut(dropOutRetainProbability).build())
      .layer(8, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nOut(outputNum)
        .activation("softmax")
        .build())
      .setInputType(InputType.convolutionalFlat(32,32,3))
      .backprop(true).pretrain(false)

  }

}
