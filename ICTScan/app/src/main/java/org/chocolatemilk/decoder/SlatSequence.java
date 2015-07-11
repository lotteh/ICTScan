package org.chocolatemilk.decoder;

import java.util.ArrayList;

public class SlatSequence
{
    public int start_index;
    public int length;

    public SlatSequence(int start_index, int length)
    {
        this.start_index = start_index;
        this.length = length;
    }

    @Override
    public boolean equals(Object seq_)
    {
    	SlatSequence seq = (SlatSequence)seq_;
        return ((this.start_index == seq.start_index)&&(this.length == seq.length));
    }

    @Override
    public String toString()
    {
    	return Integer.toString(start_index) + " -> " + Integer.toString(length);
    }
    
	public static ArrayList<SlatSequence> find_sequences(int[] data, int value)
	{
	    boolean searching = false;
	    int length = 0;
	    int start_index = 0;
	    ArrayList<SlatSequence> result = new ArrayList<SlatSequence>();
	    for(int i = 0; i < data.length; ++i)
	    {
	        if(data[i] == value)
	        {
	            if(searching)
	            {
	                length++;
	            }
	            else
	            {
	                length = 1;
	                start_index = i+1;
	                searching = true;
	            }
	        }
	        else if(searching)
	        {
	            result.add(new SlatSequence(start_index, length));
	            searching = false;
	        }
	    }
	    return result;
	}
} 