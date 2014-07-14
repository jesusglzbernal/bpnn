/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bpnn;
import Jama.*;
import java.util.Random;
/**
 *
 * @author JesusAntonio
 */
public class BPNN {

    /**
     * @param args the command line arguments
     */
    
    private int nInput;        // Number of input nodes
    private int nHidden;        // Number of nodes in the hidden layer
    private int nOutput;        // Number of output nodes
    private double alpha;       // The learning rate
    private double lambda;      // The loos rate
    private int numSamples;     // The number of training samples
    private double[][] Wih;     // Weights matrix from input to hidden layer
    private double[][] Who;     // Weights matrix from hidden layer to output layer
    private double[][] WihC;    // Weights matrix change from input to hidden layer
    private double[][] WhoC;    // Weights matrix change from hidden to output layer
    private double[] iNodes;    // The input nodes values
    private double[] hNodes;    // The hidden nodes values
    private double[] oNodes;    // The output nodes values
    private double[] hSum;      // Summation for hidden nodes
    private double[] oSum;      // Summation for output nodes
    private double[] oDeltas;   // The output nodes deltas
    private double[] hDeltas;   // The hidden nodes deltas
    private double[] yNodes;    // The output ground truth values
    private double[] oPrime;    // The derivative of the error for the output nodes
    private double[] hPrime;    // The derivative of the error for the hidden nodes
    private double[] oPartial;  // Partial derivative from the output layer
    private double[] hPartial;  // Partial derivative from the hidden layer
    private final double e = 2.7182818284590452353602875;
    
    // Class constructor, initializes the neural network architecture and working steps
    BPNN(int iN, int hN, int oN, double lR)
    {
        nInput = iN;
        nHidden = hN;
        nOutput = oN;
        alpha = lR;
        lambda = 0.05;
        numSamples = 0;
        Wih = new double[iN][hN];
        Wih = initMatrix(Wih, iN, hN);
        Who = new double[hN][oN];
        Who = initMatrix(Who, hN, oN);
        WihC = new double[iN][hN];
        WihC = initMatrix(WihC,0,0);
        WhoC = new double[hN][oN];
        WhoC = initMatrix(WhoC,0,0);
        iNodes = new double[iN];
        hNodes = new double[hN];
        oNodes = new double[oN];
        oDeltas = new double[oN];
        hDeltas = new double[hN];
        oSum = new double[oN];
        hSum = new double[hN];
        oPartial = new double[oN];
        hPartial = new double[hN];
        
    }
    
    // Function to obtain random numbers
    // """"Still needs to initialize seed""""
    public double nextDouble(Random r, double lower, double higher) 
    {
        double ran = r.nextDouble();
        double x = (double)ran * higher;
        return x + lower;
    }
    
    // Function to initialize an axb matrix with random numbers
    private double[][] initMatrix(double[][] matrix, int a, int b)
    {
        int i, j;
        Random r = new Random();
        
        for(i = 0; i < a; i++)
        {
            for(j = 0; j < b; j++)
            {
                matrix[i][j] = nextDouble(r, 0.01, 0.2);
            }
        }
        return matrix;
    }
    
    // Function to print an axb matrix
    private void printMatrix(double[][] myMatrix, int a, int b)
    {
        int i, j;
        
        for(i = 0; i < a; i++)
        {
            System.out.println("");
            for(j = 0; j < b; j++)
            {
                System.out.print(" " + myMatrix[i][j]);
            }
        }
        System.out.println("");
    }
    
    // Prints the values of the nodes of a layer:
    // 0 for the input layer
    // 1 for the hidden layer
    // 2 for the output layer
    private void printNodes(int layer)
    {
        int i = 0;
        int numNodes = 0;
        switch(layer)
        {
            case 0: numNodes = this.nInput;
                    System.out.println("");
                    for(i = 0; i < numNodes; i++)
                    {
                        System.out.print(" " + iNodes[i]);
                    }
                    System.out.println("");
                    break;
            case 1: numNodes = this.nHidden;
                    System.out.println("");
                    for(i = 0; i < numNodes; i++)
                    {
                        System.out.print(" " + hNodes[i]);
                    }
                    System.out.println("");
                    break;
            case 2: numNodes = this.nOutput;
                    System.out.println("");
                    for(i = 0; i < numNodes; i++)
                    {
                        System.out.print(" " + oNodes[i]);
                    }
                    System.out.println("");
                    break;
            default: numNodes = this.nOutput;
        }


    }
    
    // Function to use matrices from the Jama library (for the vectorized version)
    private Matrix makeMatrix(double[][] myMatrix)
    {
        return new Matrix(myMatrix);
    }
    
    // Returns the value of the sigmoid function applied to a value
    private double sigmoid(double myVal)
    {
        return (double)1/(1 + Math.pow(e, myVal));
    }
    
    // Returns the derivative of the sigmoid function
    private double sigmoidPrime(double myVal)
    {
        return (double) myVal * (1 - myVal);
    }
    
    // Performs a forward step for the bpnn
    private void forwardStep(double[] input)
    {
        int i, j;
        double sum;
        
        // Set the values of the input nodes
        for(i = 0; i < this.nInput; i++)
        {
            this.iNodes[i] = input[i];
        }
        
        // Computing the hidden layer nodes values
        for(i = 0; i < this.nHidden; i++)
        {
            sum = 0.0;
            for(j = 0; j < this.nInput; j++)
            {
                sum = sum + input[i]*Wih[j][i];
            }
            this.hNodes[i] = sigmoid(sum);
            this.hSum[i] = sum;
        }
        
        // Computing the output nodes values
        for(i = 0; i < this.nOutput; i++)
        {
            sum = 0.0;
            for(j = 0; j < this.nHidden; j++)
            {
                sum = sum + hNodes[i]*Who[j][i];
            }
            this.oNodes[i] = sigmoid(sum);
            this.oSum[i] = sum;
        }
    }
    
    // Function to obtain the Mean Square Error
    public double getMSE(double y, double f)
    {
        return Math.pow(y-f,2)/2;
    }
    
    // Function to obtain the the difference of the network and the ground truth
    public double getError(double y, double f)
    {
        return y-f;
    }
    
    // Function to obtain the deltas for modifying the weights from the hidden layer to the output layer
    public void computeODeltas()
    {
        int i;
        for(i = 0; i < this.nOutput; i++)
        {
            this.oDeltas[i] = -1 * getError(this.yNodes[i],this.oNodes[i]) * this.sigmoidPrime(oSum[i]);
        }
    }
    
    // Need to generalize this method to work with any number of layers (as well as all the class)
    // Function to compute the deltas to change the weights from the input layer to the hidden layer
    public void computeHDeltas()
    {
        int i;
        for(i = 0; i < this.nHidden; i++)
        {
            this.hDeltas[i] = this.oSum[i]*this.oDeltas[i]*this.sigmoidPrime(this.oSum[i]);
        }
    }
    
    // Function to compute the partial derivatives to modify the weights--Output
    private void computeOPartial()
    {
        int i;
        for(i = 0; i < this.nHidden; i++)
        {
            oPartial[i] = oNodes[i]*oDeltas[i];
        }
    }
    
    // Function to compute the partial derivatives to modify the weights--Hidden
    private void computeHPartial()
    {
        int i;
        for(i = 0; i < this.nInput; i++)
        {
            hPartial[i] = hNodes[i]*hDeltas[i];
        }
    }
    
    // Compute the changes of the hidden-output weights
    private void computeWohChange()
    {
        int i,j;
        for(i = 0; i < this.nHidden; i++)
        {
            for(j = 0; j < this.nOutput; j++)
            {
                WhoC[i][j] = WhoC[i][j] + oPartial[j];
            }
        }
    }
    
    // Function the compute the input to hidden weights change
    private void computeWhiChange()
    {
        int i,j;
        for(i = 0; i < this.nInput; i++)
        {
            for(j = 0; j < this.nHidden; j++)
            {
                WihC[i][j] = WihC[i][j] + hPartial[j];
            }
        }
    }
    
    // Function to update the hidden to output weights
    private void updateWho()
    {
        int i,j;
        for(i = 0; i < this.nHidden; i++)
        {
            for(j = 0; j < this.nOutput; j++)
            {
                Who[i][j] = Who[i][j] - alpha*((WhoC[i][j]/this.numSamples) + lambda * Who[i][j]);
            }
        }
    }
    
    // Function to update the input to hidden weights
    private void updateWih()
    {
        int i,j;
        for(i = 0; i < this.nInput; i++)
        {
            for(j = 0; j < this.nHidden; j++)
            {
                Wih[i][j] = Wih[i][j] - alpha*((WihC[i][j]/this.numSamples) + lambda * Wih[i][j]);
            }
        }
    }
    
    // Function to compute a back step
    private void backProp()
    {
        this.computeODeltas();
        this.computeHDeltas();
        this.computeOPartial();
        this.computeHPartial();
        this.computeWohChange();
        this.computeWhiChange();
        this.updateWho();
        this.updateWih();
    }
    
    private void readTrainingSet()
    {
        
    }
    
    private void trainBP()
    {
        
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        BPNN myNN = new BPNN(3,2,1,0.05);
        myNN.printMatrix(myNN.Wih, myNN.nInput, myNN.nHidden);
        myNN.printMatrix(myNN.Who, myNN.nHidden, myNN.nOutput);
        double[] myFInput = {2.0,3.3,4.2};
        myNN.forwardStep(myFInput);
        myNN.printNodes(0);
        myNN.printNodes(1);
        myNN.printNodes(2);
    }
}
