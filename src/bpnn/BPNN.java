/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bpnn;
import Jama.*;
import java.util.ArrayList;
import java.util.Random;
import readML.*;

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
    private double[][] WihC;    // Weights matrix gradient from input to hidden layer
    private double[][] WhoC;    // Weights matrix gradient from hidden to output layer
    private double[][] WihD;    // Weights deltas matrix for input to hidden weights
    private double[][] WhoD;    // Weights deltas matrix for hidden to output weights
    private double[][] WihP;    // Previous input to hidden Weights matrix
    private double[][] WhoP;    // Previous hidden to output Weights matrix
    private double[] iNodes;    // The input nodes values
    private double[] hNodes;    // The hidden nodes values
    private double[] oNodes;    // The output nodes values
    private double[] hSum;      // Summation for hidden nodes, before applying the sigmoid f
    private double[] oSum;      // Summation for output nodes, before applying the sigmoid f
    private double[] oDeltas;   // The output nodes error-deltas
    private double[] hDeltas;   // The hidden nodes error-deltas
    private double[] yNodes;    // The output ground truth values
    private final double e = 2.7182818284590452353602875;

    
    // Class constructor, initializes the neural network architecture and working steps
    BPNN(int iN, int hN, int oN, double learningRate)
    {
        this.nInput = iN;
        this.nHidden = hN;
        this.nOutput = oN;
        this.alpha = learningRate;
        this.lambda = 0.1;
        this.numSamples = 0;
        this.Wih = new double[iN][hN];
        this.Wih = initMatrix(Wih, iN, hN);
        this.Who = new double[hN][oN];
        this.Who = initMatrix(Who, hN, oN);
        WihD = new double[iN][hN];
        WihD = ceroMatrix(WihD, iN, hN);
        WhoD = new double[hN][oN];
        WhoD = ceroMatrix(WhoD, hN, oN);
        WihP = new double[iN][hN];
        WihP = ceroMatrix(WihP, iN, hN);
        WhoP = new double[hN][oN];
        WhoP = ceroMatrix(WhoP, hN, oN);
        WihC = new double[iN][hN];
        WihC = ceroMatrix(WihC,iN,hN);
        WhoC = new double[hN][oN];
        WhoC = ceroMatrix(WhoC,hN,oN);
        iNodes = new double[iN];
        hNodes = new double[hN];
        oNodes = new double[oN];
        oDeltas = new double[oN];
        hDeltas = new double[hN];
        oSum = new double[oN];
        hSum = new double[hN];
        yNodes = new double[oN];
        
    }
    
    // Function to obtain random numbers in the range from "lower" to "higher"
    public double nextDouble(Random r, double lower, double higher) 
    {
        double ran = r.nextDouble();
        double x = (double)ran * higher;
        return x + lower;
    }
    
    // Function to initialize an aXb matrix with random numbers in the range from 0.01 to 0.02
    private double[][] initMatrix(double[][] matrix, int a, int b)
    {
        int i, j;
        Random r = new Random(System.currentTimeMillis());
        
        for(i = 0; i < a; i++)
        {
            for(j = 0; j < b; j++)
            {
                matrix[i][j] = nextDouble(r, -0.1, 0.1);
            }
        }
        return matrix;
    }
    
    // Function to initialize an axb matrix with zeroes
    private double[][] ceroMatrix(double[][] matrix, int a, int b)
    {
        int i, j;
        
        for(i = 0; i < a; i++)
        {
            for(j = 0; j < b; j++)
            {
                matrix[i][j] = 0.0;
            }
        }
        return matrix;
    }

    
    
    private void ceroCMatrices()
    {
        int i, j;
        
        for(i = 0; i < nHidden; i++)
        {
            for(j = 0; j < nOutput; j++)
            {
                this.WhoC[i][j] = 0.0;
            }
        }
        
        for(i = 0; i < nInput; i++)
        {
            for(j = 0; j < nHidden; j++)
            {
                this.WihC[i][j] = 0.0;
            }
        }
    }
    
    // Function to make a copy of the weights matrices for momemtum
    // Wih --> WihP
    // Who --> WhoP
    private void copyW()
    {
        int i, j;
        
        for(i = 0; i < nInput; i++)
        {
            for(j = 0; j < nHidden; j++)
            {
                WihP[i][j] = Wih[i][j];
            }
        }
        for(i = 0; i < nHidden; i++)
        {
            for(j = 0; j < nOutput; j++)
            {
                WhoP[i][j] = Who[i][j];
            }
        }
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
        int i;
        int numNodes;
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
        //return (double)1/(1 + Math.pow(e, myVal));
        return Math.tanh(myVal);
    }
    
    private double dsigmoid(double myVal)
    {
        return 1 - Math.pow(myVal,2);
    }
    
    
    // Performs a forward step for the bpnn
    private void forwardStep(ArrayList<String> input, double[] output)
    {
        int i, j;
        double sum;
        String[] strNodes = new String[input.size()];
        
        // Set the values of the input nodes
        for(i = 0; i < this.nInput; i++)
        {
            this.iNodes[i] = Double.parseDouble(input.get(i));
        }
        
        // Set the values of the real "y" output nodes
        for(i = 0; i < this.nOutput; i++)
        {
            this.yNodes[i] = output[i];
        }
        
        // Computing the hidden layer nodes values
        for(i = 0; i < this.nHidden; i++)
        {
            sum = 0.0;
            for(j = 0; j < this.nInput; j++)
            {
                sum = sum + this.iNodes[j]*Wih[j][i];
            }
            this.hNodes[i] = sigmoid(sum);
            this.hSum[i] = sum; // Keep a copy of the summation for future use
        }
        
        // Computing the output nodes values
        for(i = 0; i < this.nOutput; i++)
        {
            sum = 0.0;
            for(j = 0; j < this.nHidden; j++)
            {
                sum = sum + hNodes[j]*Who[j][i];
            }
            this.oNodes[i] = sigmoid(sum);
            this.oSum[i] = sum; // Keep a copy of the summation for future use
        }
    }
    
    
    // Function to obtain the Mean Square Error
    public double getMSE(double sumError)
    {
        return Math.pow(sumError,2)/2;
    }
    
    // Function to obtain the the difference of the network and the ground truth
    // for the output vector
    public double getError(double[] y, double[] f)
    {
        double sum = 0.0;
        for(int i = 0; i < nOutput; i++)
        {
            sum = sum + Math.abs(y[i] - f[i]);
        }
        return sum;
        //return Math.pow(sum,2)/2;
    }
    
    
    // Function to return the difference between the network output and the actual output
    public double getIndError(double yi, double fi)
    {
        return yi - fi;
    }
    
    
    // Function to obtain the output deltas
    // di = -E fi'
    public void computeOutputDeltas()
    {
        for(int i = 0; i < this.nOutput; i++)
        {
            this.oDeltas[i] = -(getIndError(this.oNodes[i],this.yNodes[i])) * dsigmoid(this.oNodes[i]);
        }
    }
    
    // Function to compute the Output Weights Gradients
    public void updateOutputWeights()
    {
        double change = 0.0;
        for(int i = 0; i < this.nHidden; i++)
        {
            for(int j = 0; j < this.nOutput; j++)
            {
                change = this.oDeltas[j] * this.hNodes[i];
                this.Who[i][j] = this.Who[i][j] + this.alpha * change + this.lambda * WhoP[i][j];
                this.WhoP[i][j] = change;
            }
        }
    }
    
    // Function to obtain the Hidden Deltas
    //d = dA(hSumi) * (delta[j] * Weight_i-j 
    public void computeHiddenDeltas()
    {
        for(int i = 0; i < this.nHidden; i++)
        {
            double sum = 0.0;
            this.hDeltas[i] = dsigmoid(this.hSum[i]);
            for(int j = 0; j < this.nOutput; j++)
            {
                sum = sum + this.oDeltas[j] * this.Who[i][j];
            }
            this.hDeltas[i] = this.hDeltas[i] * sum;
        }
    }
    
    // Function to compute the input to hidden Weights Gradients
    public void updateInputWeights()
    {
        double change = 0.0;
        for(int i = 0; i < this.nInput; i++)
        {
            for(int j = 0; j < this.nHidden; j++)
            {
                change = this.hDeltas[j] * this.iNodes[i];
                this.Wih[i][j] = this.Wih[i][j] + this.alpha * change + this.lambda * WihP[i][j];
                this.WihP[i][j] = change;
            }   
        }
    }
    

    
    public void changeWeights()
    {
        for(int i = 0; i < nHidden; i++)
        {
            for(int j = 0; j < nOutput; j++)    
            {
                this.Who[i][j] = this.Who[i][j] + this.alpha * this.WhoC[i][j] + this.lambda * this.WhoP[i][j];
            }
        }
        for(int i = 0; i < nInput; i++)
        {
            for(int j = 0; j < nHidden; j++)
            {
                this.Wih[i][j] = this.Wih[i][j] + this.alpha * this.WihC[i][j] + this.lambda * this.WihP[i][j];
            }
        }
    }

    
    // Function to compute a back step
    private void backProp()
    {
        // Here we calculate a step for the gradient descent
        this.computeOutputDeltas();
        this.computeHiddenDeltas();
        this.updateOutputWeights();
        this.updateInputWeights();
        //this.changeWeights();
        //this.copyW();
    }
    
    
    // Function to convert the single class output to a vector output for a real number class
    private double[] getDoubleOutput(double a)
    {
        double[] yOutput = new double[nOutput];
        
        for(int i = 0; i < nOutput; i++)
        {
            if(i == (int)a - 1)
            {
                yOutput[i] = (double)1;
            }
            else
            {
                yOutput[i]= 0;
            }
        }
        
        return yOutput;
    }
    
    // Function to print the actual output of the sample in vector form
    private void printDoubleOutput(double a[])
    {
        for(int i = 0; i < nOutput; i++)
        {
            System.out.println("y[i]: " + a[i]);
        }
    }
    
    
    // Function to train the BP Network
    private void trainBP(readSamples dSet, int iter)
    {
        ArrayList<ArrayList<String>> xSet;
        ArrayList<String> ySet;
        xSet = dSet.getXSet();
        ySet = dSet.getYSet();
        double sumError;
        double[] yOutput = new double[nOutput];
        
        for(int i = 0; i < iter; i++)
        {
            sumError = 0.0;
            //ceroCMatrices();
            for(int j = 0; j < xSet.size(); j++)
            {
                yOutput = getDoubleOutput(Double.parseDouble(ySet.get(j)));
                forwardStep(xSet.get(j), yOutput);
                backProp();
                sumError = sumError + getError(this.oNodes,yOutput);
            }
            //changeWeights();
            //copyW();
            //if(i%100 == 0)
            //{
            System.out.println("Error: " + sumError);
            //}
        }
    }
    
    private void testBP(readSamples dSet)
    {
        ArrayList<ArrayList<String>> xSet;
        ArrayList<String> ySet;
        xSet = dSet.getXSet();
        ySet = dSet.getYSet();
        double[] yOutput = new double[nOutput];
        
        for(int j = 0; j < xSet.size(); j++)
            {
                yOutput = getDoubleOutput(Double.parseDouble(ySet.get(j)));
                forwardStep(xSet.get(j), yOutput);
                System.out.println("Y: " + yOutput[0]);
                printNodes(2);
            }
    }
    
    public static void main(String[] args) {
        // Create a BPNN new objectas
        BPNN myNN = new BPNN(2,2,1,0.2);
        //readSamples dSet = new readSamples("data/glass.data");
        readSamples dSet = new readSamples("data/and.data");
        dSet.setXYSets();
        //dSet.removeXAttribute(1);
        myNN.numSamples = dSet.getNumSamples();
        myNN.trainBP(dSet, 500);
        myNN.testBP(dSet);
        
        /*BPNN myNN = new BPNN(3,2,1,0.05);
        myNN.printMatrix(myNN.Wih, myNN.nInput, myNN.nHidden);
        myNN.printMatrix(myNN.Who, myNN.nHidden, myNN.nOutput);
        double[] myFInput = {2.0,3.3,4.2};
        //myNN.forwardStep(myFInput);
        myNN.printNodes(0);
        myNN.printNodes(1);
        myNN.printNodes(2);
        
        readSamples dSet = new readSamples("data/glass.data");
        //dSet.printTrainingSet();
        dSet.setXYSets();
        dSet.removeXAttribute(1);
        dSet.partitionDataSet(10); */   
    }
}
