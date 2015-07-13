package org.chocolatemilk.decoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class ICTCubeSlatDecoder {
	public static final int ZERO_SEQUENZ_SIZE_THRESHOLD = 15;
	
	private int rows_to_decode = 12;
	
	private byte[][] img;
	private int img_width_, img_height_;
	
	public ICTCubeSlatDecoder(byte[][] img_data, int width, int height, int rows)
	{
		img = img_data;
		img_width_ = width;
		img_height_ = height;
		rows_to_decode = rows;
	}
	
	public String decode()
	{
		String[] lmr_master_string = new String[rows_to_decode];
		StringBuilder lmr_string_builder = new StringBuilder();
		
		ArrayList<int[]> slats = new ArrayList<int[]>(41);
		ArrayList<int[]> spaces = new ArrayList<int[]>(45);
		ArrayList<Integer> big_slat_indices = new ArrayList<Integer>();
		ArrayList<double[]> grid_data = new ArrayList<double[]>(6);
		ArrayList<Integer> color_diffs = new ArrayList<Integer>(41*6);
		
		
		char last_slat_type;
		
		boolean searching_slat = true;
		boolean searching_first_slat = false;
		boolean searching_last_slat = false;
		
		double grid_width;
		double[] current_row_grid_data;
		
		int median_filter_length = (int)(1.0*25/3813 * img_width_);
		int binary_threshold;
		int slat_height = img_height_ / rows_to_decode;
		int first_rising_edge_index;
		int slat_index, maximum_index, widths_threshold;
		int first_center, second_center, distance;
		int start_position, left_distance, right_distance;
		int split_big_middle_space, split_middle_small_space;
		int first_falling_edge, last_rising_edge;
		int first_space, last_space;
		
		int line_width, half_slat_width;
		int left_mean, right_mean;
		int left_threshold, right_threshold;
		int row_count, slat_count;
		int[] sum_row;
		int[] row_mean;
		int[] row_median;
		int[] row_binary;
		int[] space; // space[0] -> start_index; space[1] -> end_index
		int[] slat_widths;
		int[] sorted_slat_widths;
		int[] sorted_widths_diff;
		int[] slat;
		int[] grid_lines;
		int[][] edges;
		
		//****Even rows:
		for(int row = 0; row < rows_to_decode; row += 2)
		{
			lmr_string_builder.setLength(0);
			
			sum_row = SignalUtils.sum_vertical(img, 0,row * slat_height + 25, img_width_, slat_height - 25);
			row_mean = SignalUtils.floating_mean(sum_row, 5);
			
			if(median_filter_length % 2 == 0)
			{
				median_filter_length += 1;
			}
			row_median = SignalUtils.median_filter(row_mean, median_filter_length);
			
			binary_threshold = SignalUtils.global_mean(row_median);
			row_binary = SignalUtils.split_to_binary(row_median, binary_threshold);
			
			//***** find edge_indices *****
			edges = SignalUtils.find_edges(row_binary);

			
			//***** find slats *****
			slats.clear();
			spaces.clear();
			
			searching_slat = true;
			first_rising_edge_index = 0; //in the space of the image (pixels)
			
			for (int i = 1; i < edges.length - 1; i++)
			{
				space = new int[2];
				if(first_rising_edge_index != 0)
				{
					if(searching_slat)
					{
						space[0] = edges[i-1][0];
						space[1] = edges[i][0];
						slats.add(space);
						searching_slat = false;
					}
					else
					{
						space[0] = edges[i-1][0];
						space[1] = edges[i][0];
						spaces.add(space);
						searching_slat = true;
					}
				}
				else
				{
					if(edges[i][1] == 1)
						first_rising_edge_index = edges[i][0];
				}
			}
			
			if(first_rising_edge_index == 0)
			{
				System.err.println("ERROR: no rising edge found");
			}
			
			
			//***** calc grid widths *****
			slat_index = 0;
			slat_widths = new int[slats.size()];
			Iterator<int[]> slats_it = slats.iterator();
			while(slats_it.hasNext())
			{
				slat = slats_it.next();
				slat_widths[slat_index] = slat[1] - slat[0];
				slat_index++;
			}
			
			sorted_slat_widths = slat_widths.clone();
			Arrays.sort(sorted_slat_widths);

			if(sorted_slat_widths.length < 1)
			{
				System.err.println("ERROR: No slats found!");
				return null;
			}

			sorted_widths_diff = new int[sorted_slat_widths.length - 1];
			for(int i = 0; i < sorted_widths_diff.length; i++)
			{
				sorted_widths_diff[i] = sorted_slat_widths[i+1] - sorted_slat_widths[i];
			}
			
			maximum_index = SignalUtils.max_index(sorted_widths_diff);
			widths_threshold = (sorted_slat_widths[maximum_index] + sorted_slat_widths[maximum_index-1])/2;
			
			big_slat_indices.clear();
			for(int i = 0; i < slat_widths.length; i++)
			{
				if(slat_widths[i] > widths_threshold)
				{
					big_slat_indices.add(i);
				}
			}
			
			if(big_slat_indices.size() >= 2)
			{
				first_center = (slats.get(big_slat_indices.get(0))[1] + slats.get(big_slat_indices.get(0))[0]) / 2;
				second_center = (slats.get(big_slat_indices.get(big_slat_indices.size() - 1))[1] + slats.get(big_slat_indices.get(big_slat_indices.size() - 1))[0]) / 2;
				distance = second_center - first_center;
				grid_width = 1.0*distance/(big_slat_indices.get(big_slat_indices.size() - 1) - big_slat_indices.get(0));
				start_position = (int) (first_center - grid_width * big_slat_indices.get(0));
			} 
			else if (big_slat_indices.size() == 1) // use middle and farest slat
			{
				if(big_slat_indices.get(0) < 21)
				{
					first_center = (slats.get(big_slat_indices.get(0))[1] + slats.get(big_slat_indices.get(0))[0]) / 2;
					second_center = (slats.get(slats.size() - 1)[1] + slats.get(slats.size() - 1)[0]) / 2; 
					distance = second_center - first_center;
					grid_width = 1.0 * distance/(slats.size() - 1 - big_slat_indices.get(0));
					start_position = (int)(first_center - grid_width * big_slat_indices.get(0));
				}
				else
				{
					first_center = (slats.get(0)[1] - slats.get(0)[0]) / 2;
					second_center = (slats.get(big_slat_indices.get(0))[1] + slats.get(big_slat_indices.get(0))[0]);
					distance = second_center - first_center;
					grid_width = 1.0*distance/(big_slat_indices.get(0));
					start_position = (int)(second_center - grid_width * big_slat_indices.get(0));
				}
			}
			else
			{
				System.err.println("Error: not enough big slats to sync the grid!");
				return null;
			}
				
			
			grid_lines = new int[slats.size()];
			for(int i = 0; i < slats.size(); i++)
			{
				grid_lines[i] = (int) (start_position + i * grid_width);
			}
			
			for(int i = 0; i < slats.size(); i++)
			{
				left_distance = grid_lines[i] - slats.get(i)[0];
				right_distance = slats.get(i)[1] - grid_lines[i];
				if(left_distance > right_distance)
				{
					lmr_string_builder.append('l');
				}
				else
				{
					lmr_string_builder.append('r');
				}
			}
			
			for(int big_slat_index : big_slat_indices)
			{
				lmr_string_builder.setCharAt(big_slat_index, 'm');
			}
			
			//***** missing slats *****
			split_big_middle_space = (int) ((57.5 + 44.5)*0.5/2554 * img_width_); //("mean of space widths"/"cube size" * "image widths")
			split_middle_small_space = (int) ((44.5 + 31.5)*0.5/2554 * img_width_);
			
			searching_first_slat = false;
			searching_last_slat = false;
			
			if(slats.size() < 41)
			{
				System.out.println("ERROR: found fewer slats");
				if(slats.size() == 39)
				{
					//***** first and last slat missing *****
					start_position = (int) (start_position - grid_width);
					searching_first_slat = true;
					searching_last_slat = true;
				}
				else if(slats.size() == 40)
				{
					//***** find 1 missing slat *****
					if(slats.get(0)[1] > 0.03 * img_width_) //first slat missing(first found slat is too far right)
					{
						start_position = (int) (start_position - grid_width);
						searching_first_slat = true;
					}
					else if(slats.get(slats.size() - 1)[0] < 0.97 * img_width_) //last slat missing(last found slat is too far left)
					{
						searching_last_slat = true;
					}
					else
					{
						System.err.println("ERROR: neither last nor first slat seems to be missing!");
						return null;
					}
				}
			
				first_falling_edge = 0;
				if (searching_first_slat)
				{
					// find first falling edge
					for(int i = 0; i < edges.length; i++)
					{
						if(edges[i][1] == -1)
						{
							first_falling_edge = edges[i][0];
							break;
						}
					}
					first_space = slats.get(0)[0] - first_falling_edge;
					
					if(first_space > split_big_middle_space)
					{
						lmr_string_builder.insert(0, 'l');
					}
					else if(first_space < split_middle_small_space)
					{
						lmr_string_builder.insert(0, 'm');
					}
					else //middle space
					{
						if(lmr_string_builder.charAt(0) == 'm' || lmr_string_builder.charAt(0) == 'l')
						{
							lmr_string_builder.insert(0,'l');
						}
						else if(lmr_string_builder.charAt(0) == 'r')
						{
							lmr_string_builder.insert(0, 'm');
						}
					}
				}
				
				last_rising_edge = 0;
				if(searching_last_slat)
				{
					//find last rising edge
					for(int i = edges.length - 1; i >= 0; i--)
					{
						if(edges[i][1] == 1)
						{
							last_rising_edge = edges[i][0];
							break;
						}
					}
					last_space = last_rising_edge - slats.get(slats.size() - 1)[1];
					
					if(last_space > split_big_middle_space)
					{
						lmr_string_builder.append('r');
					}
					else if(last_space < split_middle_small_space)
					{
						lmr_string_builder.append('m');
					}
					else //middle space
					{
						last_slat_type = lmr_string_builder.charAt(lmr_string_builder.length() - 1);
						if(last_slat_type == 'm' || last_slat_type == 'r')
						{
							lmr_string_builder.append('r');
						}
						else if(last_slat_type == 'l' )
						{
							lmr_string_builder.append('m');
						}
					}
			
				}
			}
			else if(slats.size() < 39)
			{
				System.err.println("3RR0R: more than 2 slats missing! No reconstruction possible!");
				return null;
			}
			
			current_row_grid_data = new double[2];
			current_row_grid_data[0] = start_position;
			current_row_grid_data[1] = grid_width;
			grid_data.add(current_row_grid_data);
			lmr_master_string[row] = lmr_string_builder.toString();
		}
		
		
		//***** odd rows *****
		line_width = (int) (img_width_ * 10.0 / 3800);
		half_slat_width = (int) (15.5/2554 * img_width_);
		for(int row = 1; row < rows_to_decode; row += 2)
		{
			start_position = (int) grid_data.get((row-1)/2)[0];
			grid_width = grid_data.get((row-1)/2)[1];
			
			for(int col = 0; col < 41; col++)
			{
				left_mean =  SignalUtils.calc_area_mean(img, (int)((start_position + col*grid_width) - half_slat_width), (int)((row+0.1)*slat_height), (int)(0.8*slat_height), (int)(half_slat_width - line_width/2));
				right_mean = SignalUtils.calc_area_mean(img, (int)(start_position + col*grid_width + line_width/2), (int)((row+0.1)*slat_height), (int)(0.8*slat_height), (int)(half_slat_width - line_width/2));
				
				color_diffs.add(right_mean - left_mean);
			}
		}

		int[] hist = new int[201];
		for(int color_diff: color_diffs)
		{
			if(color_diff <= 100 && color_diff >= -100)
			{
				hist[color_diff+100]++;
			}
		}
		
		ArrayList<SlatSequence> zero_sequences = SlatSequence.find_sequences(hist, 0);
		
		Collections.sort(zero_sequences);
		
		for(double[] i : grid_data)
		{
			System.out.println(Arrays.toString(i));
		}
		
		left_threshold = 0;
		right_threshold = 0;
		
		if(zero_sequences.get(0).length > ZERO_SEQUENZ_SIZE_THRESHOLD && zero_sequences.get(1).length > ZERO_SEQUENZ_SIZE_THRESHOLD)
		{
			left_threshold = Math.min(zero_sequences.get(0).start_index - 100 + zero_sequences.get(0).length/2 ,
									  zero_sequences.get(1).start_index - 100 + zero_sequences.get(1).length/2);
			
			right_threshold = Math.max(zero_sequences.get(0).start_index - 100 + zero_sequences.get(0).length/2 ,
									  zero_sequences.get(1).start_index - 100 + zero_sequences.get(1).length/2);
		}

		lmr_string_builder.setLength(0);
		row_count = 1;
		slat_count = 0;
		for(int color_diff : color_diffs)
		{
			if(color_diff < left_threshold)
				lmr_string_builder.append('l');
			else if(color_diff > right_threshold)
				lmr_string_builder.append('r');
			else
				lmr_string_builder.append('m');
			
			slat_count++;
			if(slat_count == 41)
			{
				lmr_master_string[row_count] = lmr_string_builder.toString();
				lmr_string_builder.setLength(0);
				row_count += 2;
				slat_count = 0;
			}
		}
		
		lmr_string_builder.setLength(0);
		for(int i = 0; i< lmr_master_string.length; i++)
		{
			lmr_string_builder.append(lmr_master_string[i]);
		}
		return lmr_string_builder.toString();
	}
}
