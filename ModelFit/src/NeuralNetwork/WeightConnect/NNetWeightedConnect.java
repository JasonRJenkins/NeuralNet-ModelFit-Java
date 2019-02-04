/////////////////////////////////////////////////////////////////////
//
// Implements the NNetWeightedConnect class
//
// Author: Jason Jenkins
//
// This class is used by the neural network class (NeuralNet) and
// represents the weighted connections that link the layers of a 
// neural network together.
//
// The layers of a neural network are connected by a system of
// weighted connections. Each unit in a given layer of the network
// (excluding the output layer) has a single connection to every 
// unit in the next layer. These connections are initially given
// a random value which is then updated when the neural network
// is trained.
//
// In this class the weighted connections between two layers 
// consist of a given number of input and output nodes. If the
// first layer of a network contains three units and the second
// layer contains four to connect these layers together we require
// a weighted connection with three input nodes and four output
// nodes.
//
// Each input node is connected to every output node. The input 
// nodes have their values set by the setInputs method and these
// values represent the activated output of a particular layer
// within the network. The value of a specific output node is the
// result of applying the weighted connections between that output
// node and all the connected input nodes - each input node value is
// multiplied by the weighted connection between the input node and 
// the given output node in turn and the results are then summed to 
// give the output node value. The output node values can be obtained
// via the getOutputs method and these values can then be used by the
// network as the input values to the activation functions of the
// next layer.
//
// The weights of the connections between a given output node and
// the input nodes can be retrieved via the getWeightVector method
// and set via the setWeightVector method. These two methods are 
// typically called by the network training process.
//
/////////////////////////////////////////////////////////////////////

package NeuralNetwork.WeightConnect;

/////////////////////////////////////////////////////////////////////

import java.util.*;

/////////////////////////////////////////////////////////////////////
/**
 * This class is used by the neural network class (NeuralNet) and
 * represents the weighted connections that link the layers of a 
 * neural network together.
 * 
 * @author Jason Jenkins
 */
public class NNetWeightedConnect
{
    /////////////////////////////////////////////////////////////////////
    // Private Data Members
    /////////////////////////////////////////////////////////////////////
    /**
     * the number of input nodes
     */
    private int mNumInNodes = -1;
    
    /**
     * the number of output nodes
     */
    private int mNumOutNodes = -1;

    /**
     * the input values
     */
    private final ArrayList<Double> mInputs;
    
    /**
     * the output values
     */
    private final ArrayList<Double> mOutputs;

    /**
     * the weighted connection values
     */
    private final ArrayList<ArrayList<Double>> mWeights;

    /////////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////////
    /**
     * default constructor
     */
    public NNetWeightedConnect()
    {
        mInputs = new ArrayList<>();
        mOutputs = new ArrayList<>();
        mWeights = new ArrayList<>();
    }

    /**
     * copy constructor
     * 
     * @param original the NNetWeightedConnect object to be copied
     */
    public NNetWeightedConnect(NNetWeightedConnect original)
    {
        mInputs = new ArrayList<>();
        mOutputs = new ArrayList<>();
        mWeights = new ArrayList<>();

        // the number of input nodes
        mNumInNodes = original.mNumInNodes;

        // the number of input nodes
        mNumOutNodes = original.mNumOutNodes;

        // the input values
        for (int i = 0; i < original.mInputs.size(); i++)
        {
            mInputs.add(original.mInputs.get(i));
        }

        // the output values
        for (int j = 0; j < original.mOutputs.size(); j++)
        {
            mOutputs.add(original.mOutputs.get(j));
        }
        
        // the weighted connection values
        for (int row = 0; row < mNumOutNodes; row++)
        {
            ArrayList<Double> newVec = new ArrayList<>();
            ArrayList<Double> origVec = original.mWeights.get(row);
                    
            for (int col = 0; col < mNumInNodes; col++)
            {                
                newVec.add(origVec.get(col));
            }

            mWeights.add(newVec);
        }
    }

    /**
     * constructs a connection between the given number of nodes
     * 
     * @param numInNodes  the number of input nodes
     * @param numOutNodes the number of output nodes
     */
    public NNetWeightedConnect(int numInNodes, int numOutNodes)
    {
        mInputs = new ArrayList<>();
        mOutputs = new ArrayList<>();
        mWeights = new ArrayList<>();

        // ignore invalid data
        if (numInNodes > 0 && numOutNodes > 0)
        {
            mNumInNodes = numInNodes;
            mNumOutNodes = numOutNodes;

            initialiseWeights(2);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////////
    /**
     * @return the number of input nodes
     */
    public int getNumInputNodes()
    {
        return mNumInNodes;
    }

    /**
     * @return the number of output nodes
     */
    public int getNumOutputNodes()
    {
        return mNumOutNodes;
    }

    /////////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////////
    /**
     * sets the number of input and output nodes
     * <p>
     * The weighted connections are randomly initialised over the range
     * -(initRange/2) to +(initRange/2).
     * 
     * @param numInNodes  the number of input nodes
     * @param numOutNodes the number of output nodes
     * @param initRange   the range used for random initialisation
     */
    public void setNumNodes(int numInNodes, int numOutNodes, double initRange)
    {
        // ignore invalid data
        if (numInNodes > 0 && numOutNodes > 0 && initRange > 0)
        {
            mNumInNodes = numInNodes;
            mNumOutNodes = numOutNodes;

            initialiseWeights(initRange);
        }
    }

    /**
     * sets the input values for the weighted connection
     * <p>
     * The input value for the first input node is the first value stored
     * in the list and the input value for the second input node is the
     * second value stored in the list and so on.
     * 
     * @param inputs a list of input values
     */
    public void setInputs(ArrayList<Double> inputs)
    {
        // make sure the size of the input vector corresponds to the number of input nodes
        if (inputs.size() == mNumInNodes)
        {
            if (!mInputs.isEmpty())
            {
                mInputs.clear();
            }

            mInputs.addAll(inputs);
        }
    }

    /**
     * gets the output values for the weighted connection
     * <p>
     * The output values are calculated by applying the weighted
     * connections to the input node values.
     * 
     * @param outputs the output values
     */
    public void getOutputs(ArrayList<Double> outputs)
    {
        // clear the output list if necessary
        if (!outputs.isEmpty())
        {
            outputs.clear();
        }

        calculateOutput();

        outputs.addAll(mOutputs);
    }

    /**
     * gets the weighted connections vector for a given output node
     * <p>
     * This method is typically called when training the network.
     * 
     * @param node    the index of the output node
     * @param weights the weighted connections vector (stored in a list)
     */
    public void getWeightVector(int node, ArrayList<Double> weights)
    {
        if (node < mWeights.size() && node >= 0)
        {
            ArrayList<Double> vecWeights = mWeights.get(node);

            if (!weights.isEmpty())
            {
                weights.clear();
            }

            weights.addAll(vecWeights);
        }
    }

    /**
     * sets the weighted connections vector for a given output node
     * <p>
     * This method is typically called by the training process to update
     * the weighted connections.
     * 
     * @param node    the index of the output node
     * @param weights the weighted connections vector (stored in a list)
     */
    public void setWeightVector(int node, ArrayList<Double> weights)
    {
        if (node < mWeights.size() && node >= 0)
        {
            ArrayList<Double> vecWeights = mWeights.get(node);
            
            if (vecWeights.size() == weights.size())
            {
                for (int i = 0; i < weights.size(); i++)
                {
                    double weight = weights.get(i);                    
                    
                    vecWeights.set(i, weight);
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Private Methods
    /////////////////////////////////////////////////////////////////////
    /**
     * randomly initialises the weighted connections
     * <p>
     * The weighted connections are randomly initialised over the range
     * -(initRange/2) to +(initRange/2).
     * 
     * @param initRange the range used for random initialisation
     */
    private void initialiseWeights(double initRange)
    {
        Random rand = new Random((long)initRange);   // use a fixed seed for the time being

        // initialise a weight list for each of the output nodes
        for (int i = 0; i < mNumOutNodes; i++)
        {
            ArrayList<Double> initVec = new ArrayList<>();

            // the size of the vector is equal to the number of input nodes
            for (int j = 0; j < mNumInNodes; j++)
            {
                double initVal = (double)rand.nextDouble();

                // randomly iniialise a vector component
                initVal = initRange * initVal - (initRange / 2);

                initVec.add(initVal);
            }

            mWeights.add(initVec);
        }
    }

    /**
     * calculates the output values for all the output nodes
     */
    private void calculateOutput()
    {
        if (!mOutputs.isEmpty())
        {
            mOutputs.clear();
        }

        for (int i = 0; i < mNumOutNodes; i++)
        {
            double component = getNodeValue(i);

            mOutputs.add(component);
        }
    }

    /**
     * calculates the output value for the given output node
     * 
     * @param node the index of the output node
     * 
     * @return the value of the output node
     */
    private double getNodeValue(int node)
    {
        double value = 0;
        ArrayList<Double> nodeVec = mWeights.get(node);

        if (mNumInNodes == nodeVec.size())
        {
            for (int i = 0; i < mNumInNodes; i++)
            {
                value += nodeVec.get(i) * mInputs.get(i);
            }
        }

        return value;
    }
    
    /////////////////////////////////////////////////////////////////////
}

/////////////////////////////////////////////////////////////////////
