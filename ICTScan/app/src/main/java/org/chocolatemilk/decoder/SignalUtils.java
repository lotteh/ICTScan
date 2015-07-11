package org.chocolatemilk.decoder;

import java.util.ArrayList;
import java.util.Arrays;

public class SignalUtils {
	
	public static int calc_area_mean(byte[][] area, int left, int top, int height, int width)
	{
		double sum = 0;
		for(int x = left; x < left + width; ++x)
		{
			for(int y = top; y < top + height; ++y)
			{
				sum += ((int)area[x][y]) & 0xFF;
			}
		}
		sum /= height*width;
		return (int)sum;
	}	
	
	public static int max_index(int[] data)
	{
		int rtrn = 0;
		
		for(int i = 0; i < data.length; i++)
		{
			if(data[i] > data[rtrn])
				rtrn = i;
		}
		
		return rtrn;
	}
	
	/***
	 * 
	 * @param bin_data
	 * @return Indices in first row, rising = 1, falling = -1 in second row
	 */
	public static int[][] find_edges(int[] bin_data)
	{
		ArrayList<int[]> edges = new ArrayList<int[]>(45);
		int[] edge;
		for(int i = 1; i < bin_data.length; i++)
		{
			if(bin_data[i] > bin_data[i-1])
			{
				edge = new int[2];
				edge[0] = i;
				edge[1] = 1;
				edges.add(edge);
			}
			else if(bin_data[i] < bin_data[i-1])
			{
				edge = new int[2];
				edge[0] = i;
				edge[1] = -1;
				edges.add(edge);
			}
		}
		
		return (int[][])(edges.toArray(new int[0][]));
	}
	
	public static int[] split_to_binary(int[] data, int threshold)
	{
		int[] binary_data = new int[data.length];
		
		for(int i = 0; i < data.length; i++)
		{
			if(data[i] >= threshold)
			{
				binary_data[i] = 1;
			}
			else
			{
				binary_data[i] = 0;
			}
		}
		
		return binary_data;
	}
	
	public static int[] median_filter(int[] x, int filter_length)
    {
        if(filter_length % 2 != 1)
        {
            throw new IllegalArgumentException("Median filter size must be odd");
        }

        // Prepare the helper array
        int[] helper_array = new int[x.length + filter_length-1];
        System.arraycopy(x, 0, helper_array, (filter_length-1)/2, x.length);
        int most_left_value = x[0];
        int most_right_value = x[x.length-1];
        for(int index = 0; index < (filter_length-1)/2; ++index)
        {
            helper_array[index] = most_left_value;
            helper_array[helper_array.length-1-index] = most_right_value;
        }


        // Apply the actual filter
        int[] result = new int[x.length];
        int[] neighbourhood = new int[filter_length];
        for(int index = 0; index < x.length; ++index)
        {
            System.arraycopy(helper_array, index, neighbourhood, 0, filter_length);
            Arrays.sort(neighbourhood);
            result[index] = neighbourhood[neighbourhood.length / 2];
        }
        return result;
    }
	
	public static int[] sum_vertical(byte[][] data, int offset_x, int offset_y, int width, int height)
	{
		int[] sum = new int[width];
		
		for(int x = offset_x; x < offset_x + width; x++)
		{
			for(int y = offset_y; y < offset_y + height; y++)
			{
				sum[x] += ((int)data[x][y]) & 0xFF;
			}
		}
		
		return sum;
	}

	public static int global_mean(int[] data)
	{
		long mean_value = 0;
		for(int i = 0; i < data.length; i++)
		{
			mean_value += data[i];
		}
		mean_value = mean_value / data.length;
		
		return (int)mean_value;
	}
	
	public static int[] floating_mean(int[] data, int length)
	{
		int[] filtered = new int[data.length + length-1];

		if(length > data.length)
		{
			System.err.println("Couldn't calc mean: filterlength is longer than data");
			return null;
		}
		
		//Einlaufen des Filters:
		for(int i = 0; i < length; i++)
		{
			for(int j = 0; j <= i; j++)
			{
				filtered[i] += data[j];
			}
			filtered[i] = filtered[i] / length;
		}
		
		//Hauptteil
		for(int i = length; i < data.length; i++)
		{
			for(int j = i-length+1; j <= i; j++)
			{
				filtered[i] += data[j];
			}
			filtered[i] = filtered[i] / length;
		}
		
		//Auslaufen des Filters:
		for(int i = data.length; i < filtered.length; i++)
		{
			for(int j = i-length+1; j < data.length; j++)
			{
				filtered[i] += data[j];
			}
			filtered[i] = filtered[i] / length;
		}
		return filtered;
	}
}
