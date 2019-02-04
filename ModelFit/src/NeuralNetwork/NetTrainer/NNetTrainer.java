/////////////////////////////////////////////////////////////////////
//
// Implements the NNetTrainer class
//
// Author: Jason Jenkins
//
// This class provides a framework for training a neural network.
//
// Once a network has been built it can then be trained using a
// suitable set of input and output values - known as the training 
// set.  Typically this set of values is quite large and for each 
// input element there is a corresponding output or target element.
// The training set input values are the inputs into the neural 
// network and the corresponding target values represents the 
// output that you would like the network to reproduce in response 
// to the given input.
// 
// As an example: if the network has three input units and two output
// units then each input element of the training set will have three 
// values (stored in a three element vector) and each target element 
// of the training set will have two values (stored in a two element
// vector).
//
// The calling process of this trainer must first add a suitable 
// training set consisting of the input and output values using the 
// addNewTrainingSet method. The training parameters - the learning 
// constant and the momentum - can then be optionally set using the 
// setLearningConstant and setMomentum methods respectively. If these
// parameters are not set then default values will be used instead. 
// The training routine can then be called by passing the neural 
// network that has been built with the required architecture to the
// trainNeuralNet method which will carry out the training. The 
// following code outlines this process:
/*
    // create the trainer
    NNetTrainer trainer = new NNetTrainer();

    // the training set needs to be stored as follows
    ArrayList<ArrayList<Double>> inputVectors = new ArrayList<>();
    ArrayList<ArrayList<Double>> targetVectors = new ArrayList<>();

    // a routine to populate the training set is required ...

    // initialize the trainer
    trainer.addNewTrainingSet(inputVectors, targetVectors);
    trainer.setLearningConstant(0.05);
    trainer.setMomentum(0.25);	

    // train the network - here Net is the prior built network
    trainer.trainNeuralNet(Net);

    // inspect the total network error
    double netError = trainer.getNetError();

    // if more training is required then reset the network error
    trainer.resetNetError();

    // and call the training routine again
    trainer.trainNeuralNet(net);
    netError = trainer.getNetError();

    // and repeat until training is complete
*/
//
// The training routine feeds the first input vector of the training
// set into the network. The output of the network - known as the 
// response - is calculated and compared to the ideal output in the 
// corresponding target vector of the training set. The difference 
// between the response and target values - known as error - is 
// calculated. The error value is then fed back through the network 
// using a procedure called Backpropagation - the Wikipedia entry 
// for this process can be found here:
//
//         https://en.wikipedia.org/wiki/Backpropagation. 
//
// The weighted connections linking the layers of the network 
// together (initially set to random values) are adjusted by this
// process using the gradient descent method to minimize the error. 
// The next input element of the training set is then fed into the 
// network and the response and associated error are calculated 
// again and used to further adjust the weighted connections. This
// process is continued until all the training set has been fed into
// the network and the errors occurring at each step are totalled
// together to give the total network error.
//
// The calling process of the trainer can then interrogate this 
// value and if the total network error is found to be less than a
// given predetermined value the training is deemed complete. But if
// the total error is still above the predetermined value the network
// error can be reset to zero and the training process can be called
// again and again until the total error has reached the desired 
// level or a set number of iterations has been exceeded.
//
/////////////////////////////////////////////////////////////////////

package NeuralNetwork.NetTrainer;

/////////////////////////////////////////////////////////////////////

import java.util.*;

/////////////////////////////////////////////////////////////////////

import NeuralNetwork.*;
import NeuralNetwork.NetUnit.*;
import NeuralNetwork.WeightConnect.*;

/////////////////////////////////////////////////////////////////////
/**
 * This class provides a framework for training a neural network.
 * 
 * @author Jason Jenkins
 */
public class NNetTrainer
{
    /////////////////////////////////////////////////////////////////////
    // Private Data Members
    /////////////////////////////////////////////////////////////////////
    /**
     * the network error
     */
    private double mNetError = 0;
    
    /**
     * the learning constant
     */
    private double mLearnConst = 0.5;
    
    /**
     * the momentum parameter
     */
    private double mMomentum = 0;

    /**
     * keeps track of the output layer weightings for use by the momentum term
     */
    private final ArrayList<Double> mPrevOutWt;

    /**
     * keeps track of the hidden layer weightings for use by the momentum term
     */
    private final ArrayList<Double> mPrevHidWt;

    /**
     * the training set input values
     */
    private final ArrayList<ArrayList<Double>> mTrainInput;

    /**
     * the training set target values
     */
    private final ArrayList<ArrayList<Double>> mTrainTarget;

    /////////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////////
    /**
     * default constructor
     */
    public NNetTrainer()
    {
        mPrevOutWt = new ArrayList<>();
        mPrevHidWt = new ArrayList<>();
        mTrainInput = new ArrayList<>();
        mTrainTarget = new ArrayList<>();
    }

    /////////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////////
    /**
     * gets the network error value
     * 
     * @return the network error value
     */
    public double getNetError()
    {
        return mNetError;
    }

    /**
     * gets the learning constant training parameter
     * 
     * @return the learning constant parameter value
     */
    public double getLearningConstant()
    {
        return mLearnConst;
    }

    /**
     * sets the learning constant training parameter
     * <p>
     * The learning constant governs the 'size' of the steps taken down
     * the error surface. Larger values decrease training time but can
     * lead to the system overshooting the minimum value.
     * 
     * @param value the learning constant value
     */
    public void setLearningConstant(double value)
    {
        // ignore invalid values
        if (value > 0)
        {
            mLearnConst = value;
        }
    }

    /**
     * gets the momentum training parameter
     * 
     * @return the momentum training parameter value
     */
    public double getMomentum()
    {
        return mMomentum;
    }

    /**
     * sets the momentum training parameter
     * <p>
     * This term can be used to weight the search of the error surface
     * to continue along the same 'direction' as the previous step.
     * <p>
     * A value of 1 will add 100% of the previous weighted connection
     * value to the next weighted connection adjustment.  If set to 
     * zero (the default value) the next step of the search will always
     * proceed down the steepest path of the error surface.
     * 
     * @param value the momentum value
     */
    public void setMomentum(double value)
    {
        // ignore invalid values
        if (value > 0)
        {
            mMomentum = value;
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////////
    /**
     * resets the total network error to zero
     */
    public void resetNetError()
    {
        mNetError = 0;
    }

    /**
     * trains the supplied neural network
     * <p>
     * Each time this method is called the order of the training set
     * elements are randomly shuffled to try and avoid any potential
     * bias toward certain patterns that may occur if the data
     * were always presented to the trainer in the same order.
     * 
     * @param nNet the neural network to be trained
     */
    public void trainNeuralNet(NeuralNet nNet)
    {
        int nTrain = mTrainInput.size();

        ArrayList<Integer> idx = new ArrayList<>();

        // populate the index list for the training set
        for (int i = 0; i < nTrain; i++)
        {
            idx.add(i);
        }

        if (nTrain > 0)
        {
            // randomly shuffle the index list
            Random rand = new Random(2);        // fix the random seed for now
            Collections.shuffle(idx, rand);
            
            for (int i = 0; i < nTrain; i++)
            {
                int index = idx.get(i);
                ArrayList<Double> outVec = new ArrayList<>();               // the network output values
                ArrayList<Double> outErrSig = new ArrayList<>();            // the output layer errors
                ArrayList<ArrayList<Double>> hidErrSig = new ArrayList<>(); // the hidden layer errors

                // get the next input values vector from the training set
                ArrayList<Double> trainVec = mTrainInput.get(index);

                // calculate the response from the training set input vector
                nNet.getResponse(trainVec, outVec);

                // calculate the total network error
                mNetError += calcNetworkError(outVec, index);

                // calculate the error signal on each output unit
                calcOutputError(nNet, outErrSig, outVec, index);

                // calculate the error signal on each hidden unit
                calcHiddenError(hidErrSig, outErrSig, nNet);

                // calculate the weight adjustments for the connections into the output layer
                calcOutputWtAdjust(outErrSig, nNet);

                // calculate the weight adjustments for the connections into the hidden layers
                calcHiddenWtAdjust(hidErrSig, trainVec, nNet);
            }
        }
    }

    /**
     * adds an individual input vector (stored in a list)
     * and the corresponding target vector to the training set
     * 
     * @param inVec  the input vector values (stored in a list)
     * @param outVec the corresponding target vector values (stored in a list)
     */
    public void addToTrainingSet(ArrayList<Double> inVec, ArrayList<Double> outVec)
    {
        mTrainInput.add(inVec);
        mTrainTarget.add(outVec);
    }

    /**
     * adds a complete training set of input and corresponding target vectors 
     * to the trainer
     * <p>
     * A single input element of the training set consists of a vector of
     * values (stored in a list) hence the complete set of input values 
     * consists of a list of lists. The complete target set is similarly defined.
     * 
     * @param inVecs  a list of input vector values
     * @param outVecs a list of corresponding target vector values
     */
    public void addNewTrainingSet(ArrayList<ArrayList<Double>> inVecs,
                                  ArrayList<ArrayList<Double>> outVecs)
    {
        mTrainInput.clear();
        mTrainTarget.clear();

        mTrainInput.addAll(inVecs);
        mTrainTarget.addAll(outVecs);
    }

    /////////////////////////////////////////////////////////////////////
    // Private Methods
    /////////////////////////////////////////////////////////////////////
    /**
     * calculates the network error between a given vector of response
     * values and the corresponding vector of target values
     * 
     * @param response the network response values
     * @param nTarget  the index of the corresponding target values in the training set
     * 
     * @return the current network error value
     */
    private double calcNetworkError(ArrayList<Double> response, int nTarget)
    {
        double error = 0;
        ArrayList<Double> targetVec = mTrainTarget.get(nTarget);

        for (int i = 0; i < (int) response.size(); i++)
        {
            error += 0.5 * Math.pow((targetVec.get(i) - response.get(i)), 2);
        }

        return error;
    }

    /**
     * calculates the error signal on each individual unit in the output layer
     * <p>
     * Uses the gradient descent method to search the error surface.
     * 
     * @param nNet     the network undergoing training
     * @param outErr   the calculated output unit errors
     * @param response the network response values
     * @param nTarget  the index of the corresponding target values in the training set
     */
    private void calcOutputError(NeuralNet nNet, ArrayList<Double> outErr, 
                                 ArrayList<Double> response, int nTarget)
    {
        ArrayList<Double> unitInputs = new ArrayList<>();
        ArrayList<Double> targetVec = mTrainTarget.get(nTarget);

        // get the output layer activation unit details
        ActiveT outType = nNet.getOutputUnitType();
        double outSlope = nNet.getOutputUnitSlope();
        double outAmplify = nNet.getOutputUnitAmplify();

        // get the output layer activation unit input values
        nNet.getUnitInputs(unitInputs, nNet.getNumLayers());

        for (int i = 0; i < response.size(); i++)
        {
            double yi = response.get(i);
            double xi = unitInputs.get(i);

            // follow the steepest path on the error function by moving along the gradient
            // of the output units activation function - the gradient descent method
            double err = (targetVec.get(i) - yi) * getGradient(outType, outSlope, outAmplify, xi);

            outErr.add(err);
        }
    }

    /**
     * calculates the error signal on each individual unit within the 
     * networks hidden layers
     * <p>
     * Uses the gradient descent method to search the error surface.
     * 
     * @param hidErr the calculated hidden unit errors
     * @param outErr the output unit errors
     * @param nNet   the network undergoing training
     */
    private void calcHiddenError(ArrayList<ArrayList<Double>> hidErr,
                                 ArrayList<Double> outErr, NeuralNet nNet)
    {
        int nHidden = nNet.getNumLayers();

        // initialise the the previous layer error with the output layer errors
        ArrayList<Double> prevErr = new ArrayList<>();
        prevErr.addAll(outErr);

        // start with the last hidden layer and work back to the first
        for (int i = nHidden; i >= 1; i--)
        {
            ArrayList<Double> unitInputs = new ArrayList<>();
            ArrayList<Double> layerErr = new ArrayList<>();

            // get the weighted connections for the current hidden layer
            NNetWeightedConnect wtConnect = nNet.getWeightedConnect(i);

            int nUnits = wtConnect.getNumInputNodes();
            int nConnect = wtConnect.getNumOutputNodes();

            // get the hidden layer activation unit details
            ActiveT unitType = nNet.getLayerUnit(i - 1);
            double slope = nNet.getLayerSlope(i - 1);
            double amplify = nNet.getLayerAmplify(i - 1);
            
            // get the hidden layer activation unit input values
            nNet.getUnitInputs(unitInputs, i - 1);

            // calculate the hidden layer errors
            for (int j = 0; j < nUnits; j++)
            {
                double error = 0;
                double xj = unitInputs.get(i);

                for (int k = 0; k < nConnect; k++)
                {
                    ArrayList<Double> weights = new ArrayList<>();
                    wtConnect.getWeightVector(k, weights);

                    // follow the steepest path on the error function by moving along the gradient
                    // of the hidden layer units activation function - the gradient descent method
                    error += getGradient(unitType, slope, amplify, xj) * prevErr.get(k) * weights.get(j);
                }

                layerErr.add(error);
            }

            // update the hidden errors with the current layer error
            // N.B. Since we start from the last hidden layer the 
            // hidden layer error signals are stored in reverse order
            hidErr.add(layerErr);

            // back propagate the layer errors
            prevErr.clear();
            prevErr = layerErr;
        }
    }

    /**
     * calculates the weight adjustments for the connections into the output layer
     * 
     * @param outErr the output unit errors
     * @param nNet   the network undergoing training
     */
    private void calcOutputWtAdjust(ArrayList<Double> outErr, NeuralNet nNet)
    {
        int n = nNet.getNumLayers(), prevIdx = 0;
        ArrayList<Double> xVec = new ArrayList<>();

        // get the weighted connections between the last hidden layer and the output layer
        NNetWeightedConnect wtConnect = nNet.getWeightedConnect(n);
	
	// get the input values for the weighted connections
	nNet.getActivations(xVec, n - 1);

        int nOut = wtConnect.getNumOutputNodes();

        // calculate the weight adjustments for each weighted connection output unit
        for (int i = 0; i < nOut; i++)
        {
            double ei = outErr.get(i);
            ArrayList<Double> weights = new ArrayList<>();

            // get the output units weight vector
            wtConnect.getWeightVector(i, weights);

            // calculate the total weight adjustment
            for (int j = 0; j < xVec.size(); j++)
            {
                // the weight adjustment calculation
                double dW = mLearnConst * ei * xVec.get(j);

                // if the momentum term is greater than 0
                // the previous weighting needs to be taken into account
                if (mMomentum > 0)
                {
                    if (mPrevOutWt.size() > prevIdx)
                    {
                        double dWPrev = mPrevOutWt.get(prevIdx);

                        // include a percentage of the previous weighting
                        dW += mMomentum * dWPrev;

                        // store the weighting 
                        mPrevOutWt.set(prevIdx, dW);
                    } 
                    else
                    {
                        // store the first weighting
                        mPrevOutWt.add(dW);
                    }
                }

                // the total weight adjustment
                double wtAdjust = weights.get(j) + dW;
                weights.set(j, wtAdjust);
                prevIdx++;
            }

            wtConnect.setWeightVector(i, weights);
        }

        nNet.setWeightedConnect(wtConnect, n);
    }

    /**
     * calculates the weight adjustments for the connections into the hidden layers
     * 
     * @param hidErrSig the hidden unit errors
     * @param inputVec  the current training set input values
     * @param nNet      the network undergoing training
     */
    private void calcHiddenWtAdjust(ArrayList<ArrayList<Double>> hidErrSig,
                                    ArrayList<Double> inputVec, NeuralNet nNet)
    {
        ArrayList<Double> xVec = new ArrayList<>();
        int maxHidLayIdx = nNet.getNumLayers() - 1, prevIdx = 0;

        // calculate the weight adjustments for the hidden layers
        for (int n = maxHidLayIdx; n >= 0; n--)
        {
            // get the weighted connections between the current layer and the previous hidden layer
            NNetWeightedConnect wtConnect = nNet.getWeightedConnect(n);

            // get the hidden unit errors for the previous hidden layer
            // N.B. the hidden error signals are stored in reverse order
            ArrayList<Double> outErr = hidErrSig.get(maxHidLayIdx - n);

            if (n == 0)
            {
                // we are dealing with the input layer
                xVec = inputVec;
            } 
            else
            {
                // we are dealing with a hidden layer
                nNet.getActivations(xVec, n - 1);
            }

            int nOut = wtConnect.getNumOutputNodes();

            // calculate the weight adjustments for each weighted connection output unit
            for (int i = 0; i < nOut; i++)
            {
                double ei = outErr.get(i);
                ArrayList<Double> weights = new ArrayList<>();

                // get the output units weight vector
                wtConnect.getWeightVector(i, weights);

                // calculate the total weight adjustment
                for (int j = 0; j < xVec.size(); j++)
                {
                    // the weight adjustment calculation
                    double dW = mLearnConst * ei * xVec.get(j);

                    // if the momentum term is greater than 0
                    // the previous weighting needs to be taken into account
                    if (mMomentum > 0)
                    {
                        if (mPrevHidWt.size() > prevIdx)
                        {
                            double dWPrev = mPrevHidWt.get(prevIdx);

                            // include a percentage of the previous weighting
                            dW += mMomentum * dWPrev;

                            // store the weighting 
                            mPrevHidWt.set(prevIdx, dW);
                        } 
                        else
                        {
                            // store the first weighting
                            mPrevHidWt.add(dW);
                        }
                    }

                    // the total weight adjustment
                    double wtAdjust = weights.get(j) + dW;
                    weights.set(j, wtAdjust);
                    prevIdx++;
                }

                wtConnect.setWeightVector(i, weights);
            }

            nNet.setWeightedConnect(wtConnect, n);
        }
    }

    /**
     * gets the gradient of the activation function at the given value of x
     * 
     * @param unitType the activation function type
     * @param slope    the activation function slope value
     * @param amplify  the activation function amplify value
     * @param x        calculates the gradient at this value
     * 
     * @return the gradient of the activation function at the given value
     */
    private double getGradient(ActiveT unitType, double slope, double amplify, double x)
    {
        double gradient = 0;
        double expMX, expMX1, tanMX, absMX1, grad;

        switch (unitType)
        {
            // 0 everywhere except the origin where the derivative is undefined!
            case kThreshold:

                // return the value of the slope parameter if x = 0
                if (x == 0)
                {
                    gradient = slope;
                }
                break;

            case kUnipolar:

                expMX = Math.exp(-slope * x);
                expMX1 = 1 + expMX;

                gradient = (slope * expMX) / (expMX1 * expMX1);
                break;

            case kBipolar:

                expMX = Math.exp(-slope * x);
                expMX1 = 1 + expMX;

                gradient = (2 * slope * expMX) / (expMX1 * expMX1);
                break;

            case kTanh:

                tanMX = Math.tanh(slope * x);

                gradient = slope * (1 - (tanMX * tanMX));
                break;

            case kGauss:

                gradient = -2 * slope * x * Math.exp(-slope * x * x);
                break;

            case kArctan:

                gradient = slope / (1 + slope * slope * x * x);
                break;

            case kSin:

                gradient = slope * Math.cos(slope * x);
                break;

            case kCos:

                gradient = -slope * Math.sin(slope * x);
                break;

            case kSinC:

                if (Math.abs(x) < 0.00001)
                {
                    gradient = 0;
                } 
                else
                {
                    gradient = (slope * x * Math.cos(slope * x) - Math.sin(slope * x)) / (slope * x * x);
                }

                break;

            case kElliot:

                absMX1 = 1 + Math.abs(slope * x);

                gradient = (0.5 * slope) / (absMX1 * absMX1);
                break;

            case kLinear:

                gradient = slope;
                break;

            case kISRU:

                grad = 1 / Math.sqrt(1 + slope * x * x);

                gradient = grad * grad * grad;
                break;

            case kSoftSign:

                absMX1 = 1 + Math.abs(slope * x);

                gradient = slope / (absMX1 * absMX1);
                break;

            case kSoftPlus:

                expMX = Math.exp(slope * x);

                gradient = (slope * expMX) / (1 + expMX);
                break;
        }

        return amplify * gradient;
    }

    /////////////////////////////////////////////////////////////////////
}

/////////////////////////////////////////////////////////////////////
