import numpy as np
import cv2

help_message = '''USAGE: slat_recognition.py [<image>]'''

if __name__ == '__main__':
    import sys
    try:
        fn = sys.argv[1]
    except:
        print help_message
        sys.exit(-1)
    
    rectpoints = []


    def print_straight_line(img, rho, angle, color = 255, thickness = 10):
        height, width = img.shape
        length = max(height, width) * 1.5
        a = np.cos(angle)
        b = np.sin(angle)
        x0 = a*rho
        y0 = b*rho
        x1 = int(x0 + length*(-b))
        y1 = int(y0 + length*(a))
        x2 = int(x0 - length*(-b))
        y2 = int(y0 - length*(a))
        cv2.line(img,(x1,y1),(x2,y2),color, thickness)

    def show_image(name, img):
        cv2.namedWindow(name, cv2.WINDOW_NORMAL)
        cv2.imshow(name, img)
    
    def erode_dilate(img, kernel = np.ones((4,2), np.uint8)):
        opening = cv2.morphologyEx(img, cv2.MORPH_OPEN, kernel)
        return opening
        
    def custom_edge_detection(img):
        img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY);
        hist, bins = np.histogram(img_gray.flatten(), 256, [0, 256]);
        cdf = hist.cumsum()
        cdf_normalized = cdf * hist.max()/ cdf.max()
        hist_max_index = np.argmax(hist[25:200:1])
        hist_min_index = hist_max_index + np.argmin(hist[hist_max_index:hist_max_index + 50]);
        img_gray[np.where(img_gray > hist_min_index)] = 255
        img_gray[np.where(img_gray <= hist_min_index)] = 0
        img_sobel = cv2.Sobel(img_gray, cv2.CV_32F, 1, 0);
        img_edge = np.zeros(img_gray.shape, dtype = np.uint8)
        img_edge[np.where(img_sobel > 1000)] = 255
        img_edge[np.where(img_sobel < -1000)] = 255
        return img_edge

    # max_angle is n radian
    # returns the calculated angle shift
    def hough_transform(img, minimum_vote, max_lines = 100, max_angle = 2*0.034906585039887):
        height, width= img.shape

        for i in range(0, 12, 1):
            if(i%2 != 0):
                img[i*int(height/12):(i+1)*int(height/12),:] = 0

        lines = cv2.HoughLines(img,1,np.pi/180,minimum_vote)
        print(len(lines))
        if(len(lines) > 100):
            lines = lines[0:100]
        good_lines = []
        for line in lines:
            for rho,theta in line:
                if(theta < max_angle):
                    #print_straight_line(img, rho, theta)
                    good_lines.append((rho,theta))

        median_angle = good_lines[len(good_lines)/2][1]
        mean_angle = np.mean(np.asarray(good_lines)[:,1])
        
        print("Median line angle: " + str(median_angle))
        print("Mean line angle: " + str(mean_angle))
        print_straight_line(img, width/2 - 50, median_angle)
        print_straight_line(img, width/2 + 50, mean_angle)
        return mean_angle

    def rotate_image(img, angle):
        img_shape = img.shape
        rows = img_shape[0]
        cols = img_shape[1]
        rotation_matrix = cv2.getRotationMatrix2D((cols/2, rows/2), angle, 1.0);
        print rotation_matrix
        print img.shape
        rotated_img = cv2.warpAffine(img, rotation_matrix, (cols, rows))
        return rotated_img
        
    def perspective_transform(src_img, rectpoints):
        src_quad = np.array([rectpoints[0], rectpoints[1], rectpoints[2], rectpoints[3]], np.float32);
        dst_height =  np.max(np.asarray(rectpoints)[:,1]) - np.min(np.asarray(rectpoints)[:,1])
        dst_width = np.max(np.asarray(rectpoints)[:,0]) - np.min(np.asarray(rectpoints)[:,0])
        dst_quad = np.array([(0.0, 0.0), (dst_width, 0.0), (dst_width, dst_height), (0.0, dst_height)], np.float32);
        transf_matr = cv2.getPerspectiveTransform(src_quad, dst_quad); # src, dst, 
        transf_img = cv2.warpPerspective(img, transf_matr, (dst_width, dst_height));
        return transf_img
        
        max_slat_width = 0.012137823 * dst_width # (big_slat_width)/(cube_width) : 0.31m / 25.54m
        offset_to_first_slat = 0.010571652 * dst_width # (offset_to_first_slat)/(cube_width) : 0.27m / 25.54m
        for i in range(0, dst_height, dst_height/12):
            cv2.line(transf_img,(0,int(i)),(dst_width,int(i)),(255,0,0),5)
        for i in range(0, dst_width, dst_width/41):
            cv2.line(transf_img,(int(i + offset_to_first_slat - max_slat_width/2),0),(int(i + offset_to_first_slat - max_slat_width/2), dst_height),(0,255,0),5)
            cv2.line(transf_img,(int(i + offset_to_first_slat + max_slat_width/2),0),(int(i + offset_to_first_slat + max_slat_width/2), dst_height),(0,255,0),5)
        
        cv2.namedWindow('mask', cv2.WINDOW_NORMAL)
        
        cv2.imshow("mask", transf_img)
        
        #this needs to be reworked to fit the input image size
        '''
        
        mean_image = np.zeros(transf_img.shape, np.uint8)
        mean_image = cv2.cvtColor(mean_image, cv2.COLOR_BGR2GRAY)
        cv2.imshow("mean", mean_image)

        slat_diff = np.zeros((12,41,1), np.int16)
        
        slat_string = ""
        
        #build output string, print overlay

        for i in range(0, 12, 1):
            for j in range(0, 41, 1):
                slat_diff[i,j] = int(mean_image[i*50,j*25+9-2]) - mean_image[i*50,j*25+9+2]
                if(abs(slat_diff[i,j]) <= 35):
                    cv2.circle(transf_img, (j*25+10, i*50 + 20), 5, (0,255,0), -1)
                    slat_string = slat_string + "m"
                elif(slat_diff[i,j] < 0):
                    cv2.circle(transf_img, (j*25+10, i*50 + 20), 5, (255,0,0), -1)
                    slat_string = slat_string + "r"
                else:
                    cv2.circle(transf_img, (j*25+10, i*50 + 20), 5, (0,0,255), -1)
                    slat_string = slat_string + "l"

        for i in range(50, 50*12, 50):
            cv2.line(transf_img,(0,i),(1022,i),(255,0,0),1)
        for i in range(0, 25*41, 25):
            cv2.line(transf_img,(i+9-width,0),(i+9-width,600),(0,255,0),1)
            cv2.line(transf_img,(i+9+width,0),(i+9+width,600),(0,255,0),1)

        cv2.imshow("transformed", transf_img)
        print(slat_string)'''

    def slat_recognition(img, rectpoints):
        transformed_img = perspective_transform(img, rectpoints)
        show_image("Transformed:", transformed_img)

        edge_img = custom_edge_detection(transformed_img)
        show_image("Edgedetection:", edge_img)

        opened_img = erode_dilate(edge_img) #Todo: custom kernel not working!
        show_image("Opened:", opened_img)

        mean_angle = hough_transform(opened_img, 300)
        print(mean_angle)

        rotated_img = rotate_image( opened_img, mean_angle * 180/np.pi);
        show_image("Rotated:", rotated_img);

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
            if(len(rectpoints) == 4):
                #perspective_transform(img, rectpoints)
                slat_recognition(img, rectpoints)
    img = cv2.imread(fn, True)
    height, width, depth = img.shape
    cv2.namedWindow('image', cv2.WINDOW_NORMAL)
    cv2.setMouseCallback('image', mouse_callback)    
    cv2.resizeWindow('image', 1024, 768)
    cv2.imshow('image', img)
    cv2.moveWindow('image', 200, 0)

    cv2.namedWindow('magnified', cv2.WINDOW_NORMAL)
    cv2.resizeWindow('magnified', 100, 100)    
    cv2.moveWindow('magnified', 0, 0)

    cv2.waitKey(0)
    cv2.destroyAllWindows()

