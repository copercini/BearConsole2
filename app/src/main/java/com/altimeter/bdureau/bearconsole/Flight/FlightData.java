package com.altimeter.bdureau.bearconsole.Flight;


/**
 *   @description: This class has all the flight arrays and methods to add or remove flight data
 *   @author: boris.dureau@neuf.fr
 **/
import java.util.*;


import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;

public class FlightData {
    // Create a hash map
    //public static HashMap hm;
    public static HashMap<String,XYSeriesCollection > hm;// = new HashMap<String,XYSeriesCollection>();
    public FlightData ()
    {
        //hm = new HashMap();
        hm = new HashMap<String,XYSeriesCollection>();
        // create one empty flight data collection
        //hm.put("Flight 01", createFlight("Flight 01"));
    }
    //this might be a usefull function that I will write later
    public int getNbrOfFlight()
    {
        return hm.entrySet().size();
    }

    public String getFlightName(int FlightNumber)
    {
        String flightName = null;

        return flightName;
    }


    public List<String> getAllFlightNames2()
    {
        List<String> flightNames = new ArrayList<String>();

        Set set = hm.entrySet();
        // Get an iterator
        Iterator i = set.iterator();

        // Display elements
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            flightNames.add((String)me.getKey());
        }
        return flightNames;
    }

    public void ClearFlight()
    {
        hm =null;
        hm = new HashMap();
        // create one empty flight data collection
        //hm.put("Flight 01", createFlight("Flight 01"));
    }

    public XYSeriesCollection  GetFlightData(String flightName)
    {
        XYSeriesCollection  flightData=null;

        Set set = hm.entrySet();
        // Get an iterator
        Iterator i = set.iterator();


        // Display elements
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            if (me.getKey().equals(flightName))
                flightData = (XYSeriesCollection ) me.getValue();
        }
        return flightData;
    }
    public boolean FlightExist(String flightName)
    {
        boolean exist = false;

        Set set = hm.entrySet();
        // Get an iterator
        Iterator i = set.iterator();

        // Display elements
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();

            if (me.getKey().equals(flightName))
                exist=true;
        }

        return exist;
    }
    public void AddToFlight (long X, long Y, String flightName)
    {

        //Find out if the flight exist
        //If it exist append the data to the flight and if not create a new flight
        XYSeriesCollection  flightData=null;
        if (!FlightExist(flightName))
        {
            //System.out.println("flight does not exist\n");
            //if the flight name does not exist let's create it first
            hm.put(flightName, createFlight(flightName));
        }

        flightData = GetFlightData(flightName);
        flightData.getSeries(0).add(X, Y);

    }
    public void AddToFlight (long X, long Y, String flightName, int serie)
    {

        //Find out if the flight exist
        //If it exist append the data to the flight and if not create a new flight
        XYSeriesCollection  flightData=null;
        if (!FlightExist(flightName))
        {
            //System.out.println("flight does not exist\n");
            //if the flight name does not exist let's create it first
            hm.put(flightName, createFlight(flightName));
        }

        flightData = GetFlightData(flightName);
        flightData.getSeries(serie).add(X, Y);


    }
    //not sure that I will be using that one
    public void AddFlightData (XYSeriesCollection  flightData, String flightName)
    {
        hm.put(flightName, flightData);
    }

    public void AddData (int flightNbr , int X, int Y )
    {

    }


    private XYSeriesCollection  createFlight(final String name) {
        XYSeriesCollection ret;
        final XYSeries series = new XYSeries("altitude") ;
        ret = new XYSeriesCollection (series);
        ret.addSeries(new XYSeries("temperature"));
        ret.addSeries(new XYSeries("pressure"));
        ret.addSeries(new XYSeries("latitude"));
        ret.addSeries(new XYSeries("longitude"));
        return ret; // new XYSeriesCollection (series);
    }

}