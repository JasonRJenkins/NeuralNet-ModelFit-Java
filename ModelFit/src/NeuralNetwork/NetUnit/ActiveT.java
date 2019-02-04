/////////////////////////////////////////////////////////////////////
//
// Defines the ActiveT enumeration.
//
// Author: Jason Jenkins
//
// This enumeration defines the different activation functions that 
// are available for use by the NNetUnit class.
//
/////////////////////////////////////////////////////////////////////

package NeuralNetwork.NetUnit;

/**
 * The available neural network unit activation functions as an enumerated type
 * 
 * @author Jason Jenkins
 */
public enum ActiveT
{
    /**
     * Unknown activation function (invalid or not set)
     */
    kUnknown(-1), 
    
    /**
     * Threshold activation function
     */
    kThreshold(0), 
    
    /**
     * Unipolar activation function
     */
    kUnipolar(1), 
    
    /**
     * Bipolar activation function
     */
    kBipolar(2), 
    
    /**
     * Hyperbolic tangent activation function
     */
    kTanh(3), 
    
    /**
     * Gaussian activation function
     */
    kGauss(4), 
    
    /**
     * Inverse tangent activation function
     */
    kArctan(5),
    
    /**
     * Sine tangent activation function
     */
    kSin(6), 
    
    /**
     * Cosine tangent activation function
     */
    kCos(7), 
    
    /**
     * Sinc tangent activation function
     */
    kSinC(8), 
    
    /**
     * Elliot activation function
     */
    kElliot(9), 
    
    /**
     * Linear activation function
     */
    kLinear(10), 
    
    /**
     * Inverse square root unit activation function
     */
    kISRU(11), 
    
    /**
     * SoftSign activation function
     */
    kSoftSign(12), 
    
    /**
     * SoftPlus activation function
     */
    kSoftPlus(13);
    
    /**
     * the integer value of the enumeration
     */
    private final int activeCode;

    /**
     * constructs an ActiveT enum from an integer
     * 
     * @param activeCode the integer value of the enum
     */
    ActiveT(int activeCode)
    {
        this.activeCode = activeCode;
    }
    
    /**
     * @return the integer value of the enum
     */
    public int getActiveCode()
    {
        return this.activeCode;
    }
    
    /**
     * converts an integer value to an ActiveT enum
     * 
     * @param ival the integer value to be converted
     * 
     * @return the ActiveT enum with the given integer value
     */
    static public ActiveT intToActiveT(int ival)
    {
        ActiveT retVal = kUnknown;
        
        switch (ival)
        {
            case -1:
                retVal = kUnknown;
                break;
            case 0:
                retVal = kThreshold;
                break;
            case 1:
                retVal = kUnipolar;
                break;
            case 2:
                retVal = kBipolar;
                break;
            case 3:
                retVal = kTanh;
                break;
            case 4:
                retVal = kGauss;
                break;
            case 5:
                retVal = kArctan;
                break;
            case 6:
                retVal = kSin;
                break;
            case 7:
                retVal = kCos;
                break;
            case 8:
                retVal = kSinC;
                break;
            case 9:
                retVal = kElliot;
                break;
            case 10:
                retVal = kLinear;
                break;
            case 11:
                retVal = kISRU;
                break;
            case 12:
                retVal = kSoftSign;
                break;
            case 13:
                retVal = kSoftPlus;
                break;                
        }
        
        return retVal;
    }

    /**
     * converts an ActiveT enum to its string representation
     * 
     * @param ival the integer value of the ActiveT enum
     * 
     * @return the string representation of the ActiveT enum
     */
    static public String intToString(int ival)
    {
        String retVal = "Unknown";
        
        switch (ival)
        {
            case -1:
                retVal = "Unknown";
                break;
            case 0:
                retVal = "Threshold";
                break;
            case 1:
                retVal = "Unipolar";
                break;
            case 2:
                retVal = "Bipolar";
                break;
            case 3:
                retVal = "Tanh";
                break;
            case 4:
                retVal = "Gauss";
                break;
            case 5:
                retVal = "Arctan";
                break;
            case 6:
                retVal = "Sin";
                break;
            case 7:
                retVal = "Cos";
                break;
            case 8:
                retVal = "SinC";
                break;
            case 9:
                retVal = "Elliot";
                break;
            case 10:
                retVal = "Linear";
                break;
            case 11:
                retVal = "ISRU";
                break;
            case 12:
                retVal = "SoftSign";
                break;
            case 13:
                retVal = "SoftPlus";
                break;                
        }
        
        return retVal;
    }

    /////////////////////////////////////////////////////////////////////
}

/////////////////////////////////////////////////////////////////////
