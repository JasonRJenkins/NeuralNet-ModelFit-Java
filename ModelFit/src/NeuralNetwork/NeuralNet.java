/////////////////////////////////////////////////////////////////////
//
// Implements the NeuralNet class
//
// Author: Jason Jenkins
//
// This class is a representation of a feed forward neural network.
//
// This class enables a neural network to be built comprising single 
// or multiple input and output values along with one or more hidden
// layers.
//
// The output and hidden layers can consist of any number of units
// or neurons and each layer can be given their own activation 
// function, to be used by all the units in that layer, from a 
// selection of available types:
//
// Threshold, Unipolar, Bipolar, Tanh, Gaussian, Arctan, Sine,
// Cosine, Sinc, Elliot, Linear, ISRU, SoftSign and SoftPlus.
//
// The activation function values can also be modified using two
// parameters: slope and amplify (see NNetUnit.cpp for details). 
//
// A NeuralNet object can be serialized to and de-serialized from
// a string representation which can be written to or read from a
// file. This allows a neural network to be used once training is
// complete or to continue training if required.
//
// The following code creates a neural network with 2 input units, 3
// output units and 2 hidden layers with 4 and 6 units respectively.
// The output units will use unipolar activation functions and the
// the hidden layer units will both use bipolar activation functions.
// The hidden layer connections will be randomly initialized in the 
// range -1 to 1, denoted by the value 2 and the slope and amplify
// parameters of the layer activation functions are both set to 1.
/*		
        NeuralNet net = new NeuralNet();
        net.setNumInputs(2);
        net.setNumOutputs(3);
        net.setOutputUnitType(ActiveT.kUnipolar);
        net.addLayer(4, ActiveT.kBipolar, 2, 1, 1);     // the first hidden layer
        net.addLayer(6, ActiveT.kBipolar, 2, 1, 1);     // the second hidden layer
*/
// To use the neural network, once it has been trained, populate an
// ArrayList with the desired input values and call the getResponse
// method to populate another ArrayList with the output values.
/*
        ArrayList<Double> inputs = new ArrayList<>();
        ArrayList<Double> outputs = new ArrayList<>();
        inputs.add(0.5);
        inputs.add(0.2);
        net.getResponse(inputs, outputs);

        double outputValue1 = outputs.get(0);
        double outputValue2 = outputs.get(1);
        double outputValue3 = outputs.get(2);
*/
//
/////////////////////////////////////////////////////////////////////

package NeuralNetwork;

/////////////////////////////////////////////////////////////////////

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/////////////////////////////////////////////////////////////////////

import NeuralNetwork.NetUnit.*;
import NeuralNetwork.WeightConnect.*;

/////////////////////////////////////////////////////////////////////
/**
 * This class is a representation of a feed forward neural network.
 * 
 * @author Jason Jenkins
 */
public class NeuralNet
{
    /////////////////////////////////////////////////////////////////////
    // Private Data Members
    /////////////////////////////////////////////////////////////////////
    /**
     * the number of input units
     */
    private int mNumInputs = 0;
    
    /**
     * the number of output units
     */
    private int mNumOutputs = 0;
    
    /**
     * the number of hidden layers
     */
    private int mNumLayers = 0;

    /**
     * the output layer units activation function type
     */
    private ActiveT mOutUnitType = ActiveT.kThreshold;

    /**
     * the output layer units activation function slope value
     */
    private double mOutUnitSlope = 1;

    /**
     * the output layer units activation function amplify value
     */
    private double mOutUnitAmplify = 1;

    /**
     * the weighted connections linking the network layers
     */
    private final ArrayList<NNetWeightedConnect> mLayers;

    /**
     * the activation values for each of the network layers
     */
    private final ArrayList<ArrayList<Double>> mActivations;

    /**
     * the input values for the layer activation functions
     */
    private final ArrayList<ArrayList<Double>> mUnitInputs;

    /**
     * the hidden layer unit activation function types
     */
    private final ArrayList<ActiveT> mActiveUnits;

    /**
     * the hidden layer unit activation function slope values
     */
    private final ArrayList<Double> mActiveSlope;

    /**
     * the hidden layer unit activation function amplify values
     */
    private final ArrayList<Double> mActiveAmplify;

    /////////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////////
    /**
     * default constructor
     */
    public NeuralNet()
    {
        mLayers = new ArrayList<>();
        mActivations = new ArrayList<>();
        mUnitInputs = new ArrayList<>();
        mActiveUnits = new ArrayList<>();
        mActiveSlope = new ArrayList<>();
        mActiveAmplify = new ArrayList<>();
    }

    /**
     * constructs a NeuralNet object from a file containing a network in serialised form
     * 
     * @param fname the file containing the serialised data
     */
    public NeuralNet(String fname)
    {
        mLayers = new ArrayList<>();
        mActivations = new ArrayList<>();
        mUnitInputs = new ArrayList<>();
        mActiveUnits = new ArrayList<>();
        mActiveSlope = new ArrayList<>();
        mActiveAmplify = new ArrayList<>();

        initializeFromFile(fname);
    }
    
    /**
     * copy constructor
     * 
     * @param original the NeuralNet object to be copied
     */
    public NeuralNet(NeuralNet original)
    {
        mLayers = new ArrayList<>();
        mActivations = new ArrayList<>();
        mUnitInputs = new ArrayList<>();
        mActiveUnits = new ArrayList<>();
        mActiveSlope = new ArrayList<>();
        mActiveAmplify = new ArrayList<>();
        
        copyNeuralNet(original);
    }

    /**
     * copies the contents of the source neural network into this neural network
     * 
     * @param src the source net, to be copied
     */
    public final void copyNeuralNet(NeuralNet src)
    {
        mLayers.clear();
        mActivations.clear();
        mUnitInputs.clear();
        mActiveUnits.clear();
        mActiveSlope.clear();
        mActiveAmplify.clear();
        
        mNumInputs = src.mNumInputs;
        mNumOutputs = src.mNumOutputs;
        mNumLayers = src.mNumLayers;
        mOutUnitType = src.mOutUnitType;
        mOutUnitSlope = src.mOutUnitSlope;
        mOutUnitAmplify = src.mOutUnitAmplify;

        // the weighted connections linking the network layers
        for (int i = 0; i < src.mLayers.size(); i++)
        {
            NNetWeightedConnect wCnct = new NNetWeightedConnect(src.mLayers.get(i));
            mLayers.add(wCnct);
        }

        // the activation values for each of the network layers
        int rowLen = src.mActivations.size();

        for (int row = 0; row < rowLen; row++)
        {
            ArrayList<Double> vec = new ArrayList<>();
            ArrayList<Double> vecRow = src.mActivations.get(row);

            for (int col = 0; col < vecRow.size(); col++)
            {
                vec.add(vecRow.get(col));
            }

            mActivations.add(vec);
        }

        // the input values for the layer activation functions
        rowLen = src.mUnitInputs.size();

        for (int row = 0; row < rowLen; row++)
        {
            ArrayList<Double> vec = new ArrayList<>();
            ArrayList<Double> vecRow = src.mUnitInputs.get(row);

            for (int col = 0; col < vecRow.size(); col++)
            {
                vec.add(vecRow.get(col));
            }

            mUnitInputs.add(vec);
        }

        // the hidden layer unit activation function types
        for (int i = 0; i < src.mActiveUnits.size(); i++)
        {
            mActiveUnits.add(src.mActiveUnits.get(i));
        }

        // the hidden layer unit activation function slope values
        for (int i = 0; i < src.mActiveSlope.size(); i++)
        {
            mActiveSlope.add(src.mActiveSlope.get(i));
        }

        // the hidden layer unit activation function amplify values
        for (int i = 0; i < src.mActiveAmplify.size(); i++)
        {
            mActiveAmplify.add(src.mActiveAmplify.get(i));
        }        
    }
        
    /////////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////////
    /**
     * gets the number of input units
     * 
     * @return the number of input units
     */
    public int getNumInputs()
    {
        return mNumInputs;
    }
   
    /**
     * sets the number of input units
     * 
     * @param value the number of required input units
     */
    public void setNumInputs(int value)
    {
        // ignore invalid values
        if (value > 0)
        {
            mNumInputs = value;
        }
    }

    /**
     * gets the number of output units
     * 
     * @return the number of output units
     */
    public int getNumOutputs()
    {
        return mNumOutputs;
    }

    /**
     * sets the number of output units
     * 
     * @param value the required number of output units
     */
    public void setNumOutputs(int value)
    {
        // ignore invalid values
        if (value > 0)
        {
            mNumOutputs = value;
        }
    }
    
    /**
     * gets the number of hidden layers
     * 
     * @return the number of hidden layers
     */
    public int getNumLayers()
    {
        return mNumLayers;
    }
    
    /**
     * gets the output units activation type
     * 
     * @return the output unit activation type
     */
    public ActiveT getOutputUnitType()
    {
        return mOutUnitType;
    }
    
    /**
     * sets the output units activation type
     * 
     * @param value the required output unit activation type
     */
    public void setOutputUnitType(ActiveT value)
    {
        mOutUnitType = value;
    }

    /**
     * gets the output layer units activation function slope value
     * 
     * @return the output layer unit activation function slope value
     */
    public double getOutputUnitSlope()
    {
        return mOutUnitSlope;
    }
    
    /**
     * sets the output layer units activation function slope value
     * 
     * @param value the required output layer unit activation function slope value
     */
    public void setOutputUnitSlope(double value)
    {
        // ignore invalid values
        if (value > 0)
        {
            mOutUnitSlope = value;
        }
    }

    /**
     * gets the output layer units activation function amplify value
     * 
     * @return the output layer unit activation function amplify value
     */
    public double getOutputUnitAmplify()
    {
        return mOutUnitAmplify;
    }

    /**
     * sets the output layer units activation function amplify value
     * 
     * @param value the required output layer unit activation function amplify value
     */
    public void setOutputUnitAmplify(double value)
    {
        // ignore invalid values
        if (value > 0)
        {
            mOutUnitAmplify = value;
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////////
    
    /**
     * clears a NeuralNetwork object ready for re-use
     */
    public void clearNeuralNetwork()
    {
        mNumInputs = 0;
        mNumOutputs = 0;
        mNumLayers = 0;
        mOutUnitType = ActiveT.kThreshold;
        mOutUnitSlope = 1;
        mOutUnitAmplify = 1;

        mLayers.clear();
        mActivations.clear();
        mUnitInputs.clear();
        mActiveUnits.clear();
        mActiveSlope.clear();
        mActiveAmplify.clear();
    }
        
    /**
     * adds a new hidden layer
     * <p>
     * The hidden layers are stored in the order of calls to this method
     * so the first call to addLayer creates the first hidden layer, the
     * second call creates the second layer and so on.
     * 
     * @param numUnits  the number of units in the hidden layer
     * @param unitType  the layer unit activation function type
     * @param initRange the range of the initial weighted connections
     * @param slope     the layer unit activation function slope value
     * @param amplify   the layer unit activation function amplify value
     * 
     * @return 0 if the layer is successfully added otherwise -1
     */
    public int addLayer(int numUnits, ActiveT unitType,
                        double initRange, double slope, double amplify)
    {
        NNetWeightedConnect connect = new NNetWeightedConnect();
        NNetWeightedConnect output = new NNetWeightedConnect();

        // ignore invalid values
        if (numUnits > 0 && initRange > 0 && slope > 0 && amplify > 0)
        {
            if (mNumLayers == 0)
            {
                // configure the first hidden layer
                if (mNumInputs > 0)
                {
                    // set up the weighted connections between the input and the first layer
                    // the weighted connections are initialised with random values in the
                    // range: -(initRange / 2) to +(initRange / 2)
                    connect.setNumNodes(mNumInputs, numUnits, initRange);

                    // store the unit type for the layer
                    mActiveUnits.add(unitType);

                    // store the steepness of the activation function's slope
                    mActiveSlope.add(slope);

                    // store the amplification factor of the activation function
                    mActiveAmplify.add(amplify);

                    mNumLayers++;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                // configure subsequent hidden layers
                if (mNumLayers > 0)
                {
                    int nInputs = mLayers.get(mNumLayers - 1).getNumOutputNodes();

                    // set up the weighted connections between the previous layer and the new one
                    // the weighted connections are initialised with random values in the
                    // range: -(initRange / 2) to +(initRange / 2)
                    connect.setNumNodes(nInputs, numUnits, initRange);

                    // store the unit type for the layer
                    mActiveUnits.add(unitType);

                    // store the steepness of the activation function's slope
                    mActiveSlope.add(slope);

                    // store the amplification factor of the activation function
                    mActiveAmplify.add(amplify);

                    mNumLayers++;
                }
                else
                {
                    return -1;
                }
            }

            // connect the last hidden layer to the output layer
            if (mNumLayers > 1)
            {
                // overwrite the old output connections
                mLayers.add(mNumLayers - 1, connect);
            }
            else
            {
                // add the connections for the first layer
                mLayers.add(connect);
            }

            // set up the weighted connections between the last layer and the output
            output.setNumNodes(numUnits, mNumOutputs, initRange);

            // add the output connections
            mLayers.add(output);
        }
        else
        {
            return -1;
        }

        return 0;
    }

    /**
     * gets the specified hidden layer unit activation function type
     * 
     * @param n the specified hidden layer index
     * 
     * @return the specified hidden layer unit activation function type
     */
    public ActiveT getLayerUnit(int n)
    {
        ActiveT result = ActiveT.kUnknown;
        
        if (n >= 0 && n < mNumLayers)
        {
            result = mActiveUnits.get(n);
        }
        
        return result;
    }

    /**
     * gets the specified hidden layer unit activation function slope value
     * 
     * @param n the specified hidden layer index
     * 
     * @return the specified hidden layer unit activation function slope value
     */
    public double getLayerSlope(int n)
    {
        double result = 1;
        
        if (n >= 0 && n < mNumLayers)
        {
            result = mActiveSlope.get(n);
        }
        
        return result;
    }

    /**
     * gets the specified hidden layer unit activation function amplify value
     * 
     * @param n the specified hidden layer index
     * @return the specified hidden layer unit activation function amplify value
     */
    public double getLayerAmplify(int n)
    {
        double result = 1;
        
        if (n >= 0 && n < mNumLayers)
        {
            result = mActiveAmplify.get(n);
        }
        
        return result;
    }
    
    /**
     * gets the response of the network to the given input
     * <p>
     * The number of elements in the inputs vector should correspond to 
     * the number of the input units.  If the inputs vector contains 
     * more elements than this, the additional input values are ignored.
     * 
     * @param inputs  the network input values
     * @param outputs the network output values
     */
    public void getResponse(ArrayList<Double> inputs, ArrayList<Double> outputs)
    {
        if (inputs.size() >= mNumInputs && mNumLayers > 0)
        {
            ArrayList<Double> inputVec = new ArrayList<>();
            ArrayList<Double> outputVec = new ArrayList<>();

            // clear any old activation and unit input values
            mActivations.clear();
            mUnitInputs.clear();

            // 'load' the input vector 
            for (int i = 0; i < mNumInputs; i++)
            {
                inputVec.add(inputs.get(i));
            }

            // get the weighted connections between the input layer and first layer
            NNetWeightedConnect connect = mLayers.get(0);

            // apply the weighted connections
            connect.setInputs(inputVec);
            connect.getOutputs(outputVec);

            // store the output vector - this contains the unit input values
            mUnitInputs.add(outputVec);

            // clear the input vector so it can be used to hold the input for the next layer
            inputVec.clear();

            // set the unit type, slope and amplification for the first layer
            NNetUnit unit = new NNetUnit(mActiveUnits.get(0), mActiveSlope.get(0), mActiveAmplify.get(0));

            // activate the net units
            for (int i = 0; i<(int)outputVec.size(); i++)
            {
                unit.setInput(outputVec.get(i));
                inputVec.add(unit.getActivation());
            }

            // store the activations
            mActivations.add(inputVec);

            // propagate the data through the remaining layers
            for (int i = 1; i <= mNumLayers; i++)	// use <= to include the output layer
            {                    
                // get the weighted connections linking the next layer
                connect = mLayers.get(i);

                // apply the weighted connections
                outputVec = new ArrayList<>();
                connect.setInputs(inputVec);
                connect.getOutputs(outputVec);
                inputVec = new ArrayList<>();

                // store the output vector - this contains the unit input values
                mUnitInputs.add(outputVec);

                if (i < mNumLayers)
                {
                    // set the unit type, slope and amplification for the next hidden layer
                    unit.setActivationType(mActiveUnits.get(i));
                    unit.setSlope(mActiveSlope.get(i));
                    unit.setAmplify(mActiveAmplify.get(i));
                }
                else
                {
                    // set the unit type, slope and amplification for the output layer
                    unit.setActivationType(mOutUnitType);
                    unit.setSlope(mOutUnitSlope);
                    unit.setAmplify(mOutUnitAmplify);
                }

                // activate the net units
                for (int j = 0; j<(int)outputVec.size(); j++)
                {
                    unit.setInput(outputVec.get(j));
                    inputVec.add(unit.getActivation());
                }

                // store the activations
                mActivations.add(inputVec);
            }

            // copy the results into the output vector
            outputs.clear();
            outputs.addAll(inputVec);
        }
    }

    /**
     * gets the activation values for a specified layer
     * <p>
     * This method is typically called by the training process to access
     * the activation values of the hidden and output layers.
     * 
     * @param activations the activation values for the layer
     * @param layer       the specified layer
     */
    public void getActivations(ArrayList<Double> activations, int layer)
    {
        if (layer >= 0 && layer < mActivations.size())
        {
            activations.clear();
            activations.addAll(mActivations.get(layer));
        }
    }

    /**
     * gets the unit input values for a specified layer
     * <p>
     * This method is typically called by the training process to access
     * the input values to the hidden and output layer activation functions.
     * 
     * @param inputs the unit input values for the layer
     * @param layer  the specified layer
     */
    public void getUnitInputs(ArrayList<Double> inputs, int layer)
    {
        if (layer >= 0 && layer < mUnitInputs.size())
        {
            inputs.clear();
            inputs.addAll(mUnitInputs.get(layer));
        }
    }

    /**
     * gets the weighted connections for a specified layer
     * <p>
     * This method is typically called by the training process to access the weighted connections.
     * 
     * @param layer the specified layer
     * 
     * @return the weighted connections between the specified layer 
     *         and the next sequential layer in the network
     */
    public NNetWeightedConnect getWeightedConnect(int layer)
    {
        NNetWeightedConnect wtConnect = null;

        if (layer >= 0 && layer < mLayers.size())
        {
            wtConnect = new NNetWeightedConnect(mLayers.get(layer));
        }

        return wtConnect;
    }

    /**
     * sets the weighted connections for a specified layer
     * <p>
     * This method is typically called by the training process to update the weighted connections.
     * 
     * @param wtConnect the weighted connections between the specified layer
     *                  and the next sequential layer in the network.
     * @param layer     the specified layer
     */
    public void setWeightedConnect(NNetWeightedConnect wtConnect, int layer)
    {
        if (layer >= 0 && layer < mLayers.size())
        {
            mLayers.set(layer, wtConnect);
        }
    }

    /**
     * serialises this network and writes it to a file
     * 
     * @param fname the file to write the data to
     * 
     * @return 0 if successful otherwise -1
     */
    public int writeToFile(String fname)
    {
        try (FileOutputStream  ofstream = new FileOutputStream(fname))
        {
            ofstream.write(serialize().getBytes());

            // tidy up
            ofstream.close();
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
            return -1;
        }

        return 0;
    }

    /////////////////////////////////////////////////////////////////////
    // Private Methods
    /////////////////////////////////////////////////////////////////////
    /**
     * generates a string representation of this network
     * 
     * @return a string representation of this network
     */
    private String serialize()
    {
        ArrayList<Double> weights = new ArrayList<>();
        StringBuilder outStr = new StringBuilder();

        // serialize the main details
        int outUnitType = mOutUnitType.getActiveCode();    // get the output unit type enum integer value

        outStr.append(String.valueOf(mNumInputs));
        outStr.append(" ");
        outStr.append(String.valueOf(mNumOutputs));
        outStr.append(" ");
        outStr.append(String.valueOf(mNumLayers));
        outStr.append(" ");
        outStr.append(String.valueOf(outUnitType));
        outStr.append(" ");
        outStr.append(String.valueOf(mOutUnitSlope));
        outStr.append(" ");
        outStr.append(String.valueOf(mOutUnitAmplify));
        outStr.append(" ");

        // serialize the layer data
        for (int i = 0; i <= mNumLayers; i++)       // use <= to include the output layer
        {
            NNetWeightedConnect connect = mLayers.get(i);
            int nIn = connect.getNumInputNodes();
            int nOut = connect.getNumOutputNodes();
            int nUnit = 0;
            double sUnit = 0.0, aUnit = 0.0;

            // get the unit type, slope and amplification for the hidden layer
            if (i < mNumLayers) nUnit = mActiveUnits.get(i).getActiveCode();
            if (i < mNumLayers) sUnit = mActiveSlope.get(i);
            if (i < mNumLayers) aUnit = mActiveAmplify.get(i);

            outStr.append("L ");
            outStr.append(String.valueOf(nIn));
            outStr.append(" ");
            outStr.append(String.valueOf(nOut));
            outStr.append(" ");
            outStr.append(String.valueOf(nUnit));
            outStr.append(" ");
            outStr.append(String.valueOf(sUnit));
            outStr.append(" ");
            outStr.append(String.valueOf(aUnit));
            outStr.append(" ");

            for (int j = 0; j < nOut; j++)
            {
                connect.getWeightVector(j, weights);

                for (int k = 0; k < nIn; k++)
                {
                    outStr.append(String.valueOf(weights.get(k)));
                    outStr.append(" ");
                }
            }
        }

        // terminate the output string
        outStr.append('\n');

        return outStr.toString();
    }

    /**
     * reads a space separated string from the given stream
     * 
     * @param fis the given file input stream
     * 
     * @return the string read from the stream
     */
    private String readString(FileInputStream fis)
    {
        boolean read = false;
        StringBuilder outStr = new StringBuilder();

        try
        {
            while (!read)
            {
                int ch = fis.read();
                
                // check for eof
                if (ch >= 0)
                {
                    char character = (char)ch;

                    if (character == ' ')
                    {
                        read = true;
                    }
                    else
                    {
                        outStr.append(character);
                    }
                }
                else
                {
                    read = true;
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
            outStr = new StringBuilder();
        }

        return outStr.toString();
    }
    
    /**
     * instantiates this network from a string representation stored in a file
     * 
     * @param fname the file containing a string representation of the network
     */
    private void initializeFromFile(String fname)
    {
        File file = new File(fname);

        try (FileInputStream inStream = new FileInputStream(file))
        {
            int outUnitType;
            
            String sNumInputs = readString(inStream);
            String sNumOutputs = readString(inStream);
            String sNumLayers = readString(inStream);
            String sOutUnitType = readString(inStream);
            String sOutUnitSlope = readString(inStream);
            String sOutUnitAmplify = readString(inStream);
                        
            mNumInputs = Integer.parseInt(sNumInputs);
            mNumOutputs = Integer.parseInt(sNumOutputs);
            mNumLayers = Integer.parseInt(sNumLayers);
            outUnitType = Integer.parseInt(sOutUnitType);
            mOutUnitSlope = Double.parseDouble(sOutUnitSlope);
            mOutUnitAmplify = Double.parseDouble(sOutUnitAmplify);
            mOutUnitType = ActiveT.intToActiveT(outUnitType);
                        
            // deserialize the layer data
            for (int i = 0; i <= mNumLayers; i++)		// use <= to include the output layer
            {
                String sDelim = readString(inStream);
                String sNIn = readString(inStream);
                String sNOut = readString(inStream);
                String sNUnit = readString(inStream);
                String sSUnit = readString(inStream);
                String sAUnit = readString(inStream);

                int nIn = Integer.parseInt(sNIn);
                int nOut = Integer.parseInt(sNOut);
                int nUnit = Integer.parseInt(sNUnit);
                double sUnit = Double.parseDouble(sSUnit);
                double aUnit = Double.parseDouble(sAUnit);

                NNetWeightedConnect connect = new NNetWeightedConnect(nIn, nOut);

                for (int j = 0; j < nOut; j++)
                {
                    ArrayList<Double> weights = new ArrayList<>();

                    for (int k = 0; k < nIn; k++)
                    {					        
                        String sWgt = readString(inStream);
                        double wgt = Double.parseDouble(sWgt);

                        weights.add(wgt);
                    }

                    connect.setWeightVector(j, weights);
                }

                mLayers.add(connect);
                mActiveUnits.add(ActiveT.intToActiveT(nUnit));
                mActiveSlope.add(sUnit);
                mActiveAmplify.add(aUnit);
            }

            // tidy up
            inStream.close();
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
     }

   /////////////////////////////////////////////////////////////////////
   
}

/////////////////////////////////////////////////////////////////////
