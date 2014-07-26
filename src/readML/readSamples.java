package readML;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JesusAntonio
 */
public class readSamples
{
    private int numPartitions;          // Number of Partitions for an X-FCV
    private int[] samplePartitions;
    private int numAttributes;
    private ArrayList<ArrayList<String>> dataSet = new ArrayList<>();
    private ArrayList<String> ySet = new ArrayList<>();
    private ArrayList<ArrayList<String>> xSet = new ArrayList<>();
    private int numSamples;
    
    
    public readSamples(String fileName)
    {
        Charset charset = Charset.forName("US-ASCII");
        FileSystem fs = FileSystems.getDefault();
        Path path1 = fs.getPath(fileName);
        System.out.println("Filename: " + path1.toString());
        numAttributes = 0;
        
        Scanner sc = null;
        try
        {
            sc = new Scanner(new File(path1.toString()));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        while(sc.hasNextLine())
        {
            Scanner s2 = new Scanner(sc.nextLine());
            boolean b;
            while (b = s2.hasNext())
            {
                String line = s2.next();
                String[] lineArray = line.split(",");
                ArrayList<String> mySample = new ArrayList<>();
                mySample.addAll(Arrays.asList(lineArray));
                dataSet.add(mySample);
                this.numSamples++;
            }
        }
    }
    
    public int getNumSamples()
    {
        return this.numSamples;
    }
    
    public void printTrainingSet()
    {
        for(ArrayList<String> innerList : dataSet)
        {
            for(String myAtt : innerList)
            {
                System.out.print(myAtt + ", ");
            }
            System.out.println("");
        }
    }
    
    
    public void setXYSets()
    {
        numAttributes = dataSet.get(0).size();
        for(int i = 0; i < dataSet.size(); i++)
        {
            ySet.add(dataSet.get(i).get(numAttributes - 1));
            xSet.add(dataSet.get(i));
            xSet.get(i).remove(numAttributes - 1);
        }
        System.out.println("Y: " + ySet.get(0));
        System.out.println("X: " + xSet.get(0));
    }
    
    
    public void removeXAttribute(int numAttribute)
    {
        for (ArrayList<String> xSet1 : xSet) {
            xSet1.remove(numAttribute -1);
        }
        System.out.println("Y: " + ySet.get(0));
        System.out.println("X: " + xSet.get(0));
    }
    
    
    public int nextInt(Random r, int lower, int higher) 
    {
        int ran = r.nextInt(higher);
        return ran;
    }    
    
    
    public void partitionDataSet(int numPart)
    {
        this.numPartitions = numPart;
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        int numAssigned;
        int myRan;
        int[] sampXPart = new int[numPart];
        samplePartitions = new int[dataSet.size()];
        
        for(int i = 0; i < dataSet.size(); i++)
        {
            samplePartitions[i]= -1;
        }
        
        for(int i = 0; i < numPart - 1; i++)
        {
            sampXPart[i] = (int) dataSet.size()/numPart;
        }
        sampXPart[numPart-1] = dataSet.size() - (numPart-1)*((int)dataSet.size()/numPart);
        
        for(int j = 0; j < numPart - 1; j++)
        {
            numAssigned = 0;
            while(numAssigned < sampXPart[j])
            {
                myRan = nextInt(r,0,dataSet.size());
                if(samplePartitions[myRan] == -1)
                {
                    samplePartitions[myRan] = j;
                    numAssigned = numAssigned + 1;
                }
            }
        }
        for(int i = 0; i < dataSet.size(); i++)
        {
            if(samplePartitions[i] == -1)
            {
                samplePartitions[i] = numPart - 1;
                
            }
        }
    }
    
   
    public ArrayList<ArrayList<String>> getXPartition(int numPart)
    {
        int size;
        size = xSet.size();
        ArrayList<ArrayList<String>> xPart = new ArrayList<>();
        
        for(int i = 0; i < size; i++)
        {
            if(samplePartitions[i] == numPart - 1)
            {
                xPart.add(xSet.get(i));
            }
        }
        return xPart;
    }    
    
    
    public ArrayList<String> getYPartition(int numPart)
    {
        int size;
        size = ySet.size();
        ArrayList<String> yPart = new ArrayList<>();
        
        for(int i = 0; i < size; i++)
        {
            if(samplePartitions[i] == numPart - 1)
            {
                yPart.add(ySet.get(i));
            }
        }
        return yPart;
    }
    
    
    public ArrayList<ArrayList<String>> getXComplement(int numPart)
    {
        int size;
        size = xSet.size();
        ArrayList<ArrayList<String>> xPart = new ArrayList<>();
        
        for(int i = 0; i < size; i++)
        {
            if(samplePartitions[i] != numPart - 1)
            {
                xPart.add(xSet.get(i));
            }
        }
        return xPart;
    }    
    
    
    public ArrayList<String> getYComplement(int numPart)
    {
        int size;
        size = ySet.size();
        ArrayList<String> yPart = new ArrayList<>();
        
        for(int i = 0; i < size; i++)
        {
            if(samplePartitions[i] != numPart - 1)
            {
                yPart.add(ySet.get(i));
            } else {
            }
        }
        return yPart;
    }    
    
    
    public ArrayList<ArrayList<String>> getXSet()
    {
        return xSet;
    }    

    
    public ArrayList<String> getYSet()
    {
        return ySet;
    }       
    
}
