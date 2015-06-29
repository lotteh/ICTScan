import numpy as np
import cv2
from matplotlib import pyplot as plt
from matplotlib import patches as patches

help_message = '''USAGE: slat_recognition.py [<image>]'''

if __name__ == '__main__':
    import sys
    try:
        fn = sys.argv[1]
    except:
        print help_message
        sys.exit(-1)
    
    rectpoints = []
    
    def plot_1d(data, graph_color = 'b', use_new_figure = False):
        if use_new_figure:
            plt.figure()

        plt.plot(data, color=graph_color)
        plt.xlim([0, len(data)])


    def show_image(name, img):
        cv2.namedWindow(name, cv2.WINDOW_NORMAL)
        cv2.imshow(name, img)
    
    def perspective_transform(src_img, rectpoints):
        src_quad = np.array([rectpoints[0], rectpoints[1], rectpoints[2], rectpoints[3]], np.float32);
        dst_height =  np.max(np.asarray(rectpoints)[:,1]) - np.min(np.asarray(rectpoints)[:,1])
        dst_width = np.max(np.asarray(rectpoints)[:,0]) - np.min(np.asarray(rectpoints)[:,0])
        dst_quad = np.array([(0.0, 0.0), (dst_width, 0.0), (dst_width, dst_height), (0.0, dst_height)], np.float32);
        transf_matr = cv2.getPerspectiveTransform(src_quad, dst_quad); # src, dst, 
        transf_img = cv2.warpPerspective(img, transf_matr, (dst_width, dst_height));
        return transf_img

    def find_n_max(data, n):
        sorted = np.argsort(data)
        highest_n_values = sorted[0:n-1];
        return highest_n_values

    def median_filter (x, k):
        """Apply a length-k median filter to a 1D array x.
        Boundaries are extended by repeating endpoints.
        """
        assert k % 2 == 1, "Median filter length must be odd."
        assert x.ndim == 1, "Input must be one-dimensional."
        k2 = (k - 1) // 2
        y = np.zeros ((len (x), k), dtype=x.dtype)
        y[:,k2] = x
        for i in range (k2):
            j = k2 - i
            y[j:,i] = x[:-j]
            y[:j,i] = x[0]
            y[:-j,-(i+1)] = x[j:]
            y[-j:,-(i+1)] = x[-1]
        return np.median (y, axis=1)

    def slat_recognition(img, rectpoints):
        #************ FILTER *************:
        filter_diff_1 = [1, -1]

        filter_floating_sum = 0.2 * np.ones(5)

        filter_floating_sum_50 = 1.0/50 * np.ones(50)

        transformed_img = perspective_transform(img, rectpoints)
        show_image("Transformed:", transformed_img)

        gray_img = cv2.cvtColor(transformed_img, cv2.COLOR_BGR2GRAY)
        gray_img_height, gray_img_width = gray_img.shape
        
        for row in range(0,12, 2):
            first_row = gray_img[row * gray_img_height/12 + 25: (row+1) * gray_img_height/12 - 25,:]
            sum_first_row = np.sum(first_row, 0)

            first_row_mean = np.convolve(sum_first_row, filter_floating_sum);
            
            plt.figure()
            #plt.plot(sum_first_row, color='b')
            #plt.xlim([0, len(sum_first_row)])
            
            plt.plot(first_row_mean, color='r')
            plt.xlim([0, len(first_row_mean)])
            
            filter_floating_sum_50 = np.ones(50)
            filter_floating_sum_50 = 1.0/50 * filter_floating_sum_50

            first_row_median = median_filter(first_row_mean, 25)

            plt.figure()
            plt.plot(first_row_median, color='g')
            plt.xlim([0, len(first_row_median)])

            #calculate threshold to split data into binary signal
            temp_threshold = np.mean(first_row_median)
            print "threshold: " + str(temp_threshold)
            rectangle = plt.Rectangle((0, temp_threshold), 4000, 10, color = 'g')
            plt.gca().add_patch(rectangle)

            row_binary = np.zeros(len(first_row_median), )
            row_binary[np.where(first_row_median > temp_threshold )] = 1

            plot_1d(row_binary, 'm', True);

            #***** find edge_indices *****
            first_row_mean_threshold_diff = np.convolve(row_binary, filter_diff_1) # contains 1 for rising and -1 for falling edge
            edge_indices = np.where(first_row_mean_threshold_diff != 0) [0] #np.where returns crappy array structure, so we have to take the first dim.

            print "#found edge_indices: " + str(len(edge_indices))
            print "edge indices: " + str(edge_indices)

            #***** find slats *****
            slats = []
            spaces = []
            
            searching_slat = True
            first_rising_edge_index = 0 # in the space of the image (pixels)
            
            for i in range(1,len(edge_indices)-1): #skip first edge and find next rising edge
                if(first_rising_edge_index != 0):
                    if(searching_slat):
                        slats.append((edge_indices[i-1], edge_indices[i]))
                        searching_slat = False
                    else:
                        spaces.append((edge_indices[i-1], edge_indices[i]))
                        searching_slat = True
                else:
                    if(first_row_mean_threshold_diff[edge_indices[i]] == 1):
                        first_rising_edge_index = edge_indices[i]

            print "Found " + str(len(slats)) + " slats"
            
            if(first_rising_edge_index  == 0):
                print("ERROR: no rising edge found")
            
            plt.figure() 
            plt.plot(row_binary, color='b')
            plt.xlim([0,len(row_binary)])
            
            for slat in slats:
                rectangle = plt.Rectangle((slat[0],0), slat[1] - slat[0], 1)
                plt.gca().add_patch(rectangle)

            #***** calc grid widths *****
            widths = []
            for slat in slats:
                widths.append(slat[1]-slat[0])
            #plt.figure()
            #plt.hist(widths)
            sp_widths = []
            for space in spaces:
                sp_widths.append(space[1] - space[0])
                rectangle = plt.Rectangle((space[0],0), space[1] - space[0], 1, color = 'r')
                plt.gca().add_patch(rectangle)
            plt.figure()
            plt.hist(sp_widths)
            print("Spaces: " + str(sp_widths))

            sorted_widths = sorted(widths, reverse=True)
            sorted_widths_diff = np.diff(sorted_widths)
            
            
            minimum_index = np.argmin(sorted_widths_diff)
            widths_threshold = (sorted_widths[minimum_index] + sorted_widths[minimum_index+1])/2
            
            #todo: noch brauchen wir min 2 big_slats
            big_slat_indices = np.where(widths > widths_threshold)[0]
            grid_width = 0
            #todo: better grid_width detection
            '''for i in range(0, len(big_slat_indices)-1):
                first_center = (slats[big_slat_indices[i]][1] + slats[big_slat_indices[i]][0])/2
                second_center = (slats[big_slat_indices[i+1]][1] + slats[big_slat_indices[i+1]][0])/2
                distance = second_center - first_center
                grid_width += 1.0*distance/(big_slat_indices[i+1] - big_slat_indices[i])
                print(grid_width)
            '''
            first_center = (slats[big_slat_indices[0]][1] + slats[big_slat_indices[0]][0])/2
            second_center = (slats[big_slat_indices[-1]][1] + slats[big_slat_indices[-1]][0])/2
            distance = second_center - first_center
            grid_width = 1.0*distance/(big_slat_indices[-1] - big_slat_indices[0])
            #grid_width /= (len(big_slat_indices)-1)
            #for i in range(0,len
            
            start_position = first_center - grid_width * big_slat_indices[0]
            print(first_center)
            print("grid width: " + str(grid_width))
            print(big_slat_indices[0])
            print(start_position)

            grid_lines = []
            for i in range(0, len(slats)):
                rectangle = plt.Rectangle((start_position + i * grid_width, 0), 1, 1, color = 'g')
                grid_lines.append(start_position + i * grid_width)
                plt.gca().add_patch(rectangle)
            plt.show();
            
            lmr_string_arr = []
            
            for i in range(0, len(slats)):
                left_distance = grid_lines[i] - slats[i][0] 
                right_distance = slats[i][1] - grid_lines[i]
                if( left_distance > right_distance ):
                    lmr_string_arr.append("l")
                else:
                    lmr_string_arr.append('r')
            
            print(lmr_string_arr)
            print(big_slat_indices)
                    
            for i in big_slat_indices:
                if i < len(lmr_string_arr):
                    lmr_string_arr[i] = "m"
                
            #***** missing slats ******
            split_big_middle_space = (57.5+44.5)*0.5/2554 * gray_img_width # ("mean of space widths"/"cube size" * "image widths")
            split_middle_small_space = (44.5+31.5)*0.5/2554 * gray_img_width 

            searching_first_slat = False
            searching_last_slat = False
            if len(slats) < 41:
                print("ERROR: found fewer slats")
                #***** get missing slats based on  spaces ******
                if (len(slats) == 39):
                    #**** first and last slat missing ****
                    searching_first_slat = True
                    searching_last_slat = True
                elif (len(slats) == 40):
                    #**** find 1 missing slat ****
                    if (slats[0][1] > 0.03 * gray_img_width): #first slat missing(first found slat is too far right)
                        searching_first_slat = True
                        print("Searching first slat!")
                    elif (slats[-1][0] < 0.97 * gray_img_width): #last slat missing(last found slat is too far left)
                        searching_last_slat = True
                        print("Searching last slat!")
                    else:
                        print("ERROR: neither last nor first slat seems to be missing!")

                if searching_first_slat:
                    first_falling_edge = np.where(first_row_mean_threshold_diff == -1)[0][0]
                    first_space = slats[0][0] - first_falling_edge

                    if(first_space > split_big_middle_space):
                        lmr_string_arr.insert(0, "l")
                        print("First slat is left")
                    elif(first_space < split_middle_small_space):
                        lmr_string_arr.insert(0, "m")
                        print("First slat is middle")
                    else: #middle space
                        if(lmr_string_arr[0] == "m" or lmr_string_arr[0] == "l"):
                            lmr_string_arr.insert(0,"l")
                            print("First slat is left")
                        elif(lmr_string_arr[0] == "r"):
                            lmr_string_arr.insert(0,"m")
                            print("First slat is middle")

                if searching_last_slat:
                    last_rising_edge = np.where(first_row_mean_threshold_diff == 1)[0][-1]
                    last_space = last_rising_edge - slats[-1][1]

                    if(last_space > split_big_middle_space):
                        lmr_string.append("r")
                        print("Last slat is right")
                    elif(last_space < split_middle_small_space):
                        lmr_string.append("m")
                        print("Last slat is middle")
                    else: #middle space
                        if(lmr_string_arr[-1] == "m" or lmr_string_arr[-1] == "r"):
                            lmr_string.append("r")
                            print("Last slat is right")
                        elif(lmr_string_arr[-1] == "l"):
                            lmr_string.append("m")
                            print("Last slat is middle")
            elif len(slats) < 39:
                print("ERROR: more than 2 slats missing! No reconstruction possible!")


            lmr_string = "".join(lmr_string_arr)
            print(lmr_string_arr)
            print(lmr_string)

    def mouse_callback(event,x,y,flags,param):
        global img
        if event == cv2.EVENT_MOUSEMOVE:                
            y_offset = 0
            x_offset = 0
            if(y < 50):
                y_offset = 50 - y
            elif (y > height - 50):
                y_offset = y - height
            if(x < 50):
                x_offset = 50 - x
            elif (x > width - 50):
                x_offset = x - width
            subimg = img[y-50+y_offset:y+50+y_offset, x-50+x_offset:x+50+x_offset].copy()
            cv2.circle(subimg, (50-x_offset,50-y_offset), 3, (0,255,0), -1)
            cv2.imshow('magnified', subimg)
        elif (event == 1): #left click
            if(len(rectpoints) < 4):
                rectpoints.append((x,y))
                print("Added point @ " + str(x) + "/" + str(y))
                cv2.circle(display_img, (x,y), width/200, (0,255,0), -1)
                if(len(rectpoints) > 1):
                    cv2.line(display_img, rectpoints[len(rectpoints)-2], rectpoints[len(rectpoints)-1], (255,0,0), 10)
                if(len(rectpoints) == 4):
                    cv2.line(display_img, rectpoints[0], rectpoints[3], (255,0,0), 10)
                cv2.imshow('image', display_img)
            if(len(rectpoints) == 4):
                slat_recognition(img, rectpoints)
    img = cv2.imread(fn, True)
    display_img = img.copy()
    height, width, depth = img.shape
    rectpoints.append((1158,522));
    rectpoints.append((4771,87));
    rectpoints.append((4817,3515));
    rectpoints.append((1004,3326));
    slat_recognition(img, rectpoints)
    '''cv2.namedWindow('image', cv2.WINDOW_NORMAL)
    cv2.setMouseCallback('image', mouse_callback)    
    cv2.resizeWindow('image', 1024, 768)
    cv2.imshow('image', display_img)
    cv2.moveWindow('image', 200, 0)

    cv2.namedWindow('magnified', cv2.WINDOW_NORMAL)
    cv2.resizeWindow('magnified', 100, 100)    
    cv2.moveWindow('magnified', 0, 0)
    '''
    cv2.waitKey(0)
    cv2.destroyAllWindows()
