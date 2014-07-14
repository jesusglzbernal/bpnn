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
    private String[] fNames;
    private double[][] xTrValues;
    private double[][] xTeValues;
    private double[] yTrValues;
    private double[] yTeValues;
    private int numPartitions;          // Number of Partitions for an X-FCV
    private double[][] xPartitions;
    private double[][] yPartitions;
    
    
    private void readTrF(String fName)
    {
        
    }
    
    private void readTeF(String fNames)
    {
        
    }
    
    private double[][] getTrX()
    {
        return xTrValues;
    }
    
    private double[] getTrY()
    {
        return yTrValues;
    }
    
    private double[][] getTeX()
    {
        return xTeValues;
    }
    
    private double[] getTeY()
    {
        return yTeValues;
    }
    
    private void xPartition()
    {
        
    }
    
    private double[] getXPartition(int nPart)
    {
        return xPartitions[nPart];
    }
    
    
    private double[] getYPartition(int nPart)
    {
        return yPartitions[nPart];
    }
}
