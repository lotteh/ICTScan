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

    def perspective_transform(src_img, rectpoints):
        img = src_img
        src_quad = np.array([rectpoints[0], rectpoints[1], rectpoints[2], rectpoints[3]], np.float32);
        dst_height =  np.max(np.asarray(rectpoints)[:,1]) - np.min(np.asarray(rectpoints)[:,1])
        dst_width = np.max(np.asarray(rectpoints)[:,0]) - np.min(np.asarray(rectpoints)[:,0])
        dst_quad = np.array([(0.0, 0.0), (dst_width, 0.0), (dst_width, dst_height), (0.0, dst_height)], np.float32);
        transf_matr = cv2.getPerspectiveTransform(src_quad, dst_quad); # src, dst, 
        transf_img = cv2.warpPerspective(img, transf_matr, (dst_width, dst_height));
        print transf_matr

        cv2.namedWindow("transformed", cv2.WINDOW_NORMAL);
        cv2.imshow("transformed", transf_img);
        cv2.destroyWindow('image')
        cv2.destroyWindow('magnified')      
        
        #edge detection begin
        img_gray = cv2.cvtColor(transf_img, cv2.COLOR_BGR2GRAY);

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
        
        cv2.namedWindow("edge_detection", cv2.WINDOW_NORMAL)
        cv2.imshow("edge_detection", img_edge)
        #edge detection end -> img_edge
        
        #noise filter begin
        kernel = np.ones((4,2), np.uint8);
        opening = cv2.morphologyEx(img_edge, cv2.MORPH_OPEN, kernel)
        
        cv2.namedWindow("noise_filter", cv2.WINDOW_NORMAL)
        cv2.imshow("noise_filter", opening)
        #noise filter end -> opening
        
        #hough_transform begin
        img = opening
    
        height, width = img.shape
        img_wiped = np.zeros((height,width,3), np.uint8)

        for i in range(0, 12, 1):
            if(i%2 != 0):
                img[i*int(height/12):(i+1)*int(height/12),:] = 0
        
        
        img_wiped = img
        lines = cv2.HoughLines(img_wiped,1,np.pi/180,300)
        print(len(lines))
        if(len(lines) > 100):
            lines = lines[0:100]
        good_lines = []
        for line in lines:
            for rho,theta in line:
                if(theta < 0.034906585039887 * 2):
                    a = np.cos(theta)
                    b = np.sin(theta)
                    x0 = a*rho
                    y0 = b*rho
                    x1 = int(x0 + 1000*(-b))
                    y1 = int(y0 + 1000*(a))
                    x2 = int(x0 - 1000*(-b))
                    y2 = int(y0 - 1000*(a))

                    #cv2.line(img_wiped,(x1,y1),(x2,y2),(0,0,255),2)
                    good_lines.append((rho,theta))
        median_angle = good_lines[len(good_lines)/2][1]
        mean_angle = np.mean(np.asarray(good_lines)[:,1])
        
        print("Median line angle: " + str(median_angle))
        print("Mean line angle: " + str(mean_angle))
        
        rho = 1000
        a = np.cos(median_angle)
        b = np.sin(median_angle)
        x0 = a*rho
        y0 = b*rho
        x1 = int(x0 + 10000*(-b))
        y1 = int(y0 + 10000*(a))
        x2 = int(x0 - 10000*(-b))
        y2 = int(y0 - 10000*(a))
        cv2.line(img_wiped,(x1,y1),(x2,y2),255,10)
        rho = 2000
        a = np.cos(mean_angle)
        b = np.sin(mean_angle)
        x0 = a*rho
        y0 = b*rho
        x1 = int(x0 + 10000*(-b))
        y1 = int(y0 + 10000*(a))
        x2 = int(x0 - 10000*(-b))
        y2 = int(y0 - 10000*(a))
        cv2.line(img_wiped,(x1,y1),(x2,y2),255,10)
        
        cv2.namedWindow("hough_lines", cv2.WINDOW_NORMAL)
        cv2.imshow("hough_lines", img_wiped)
        #hough_transform end ->
        
        
        return
        
        #rotate img here with angle found by hough lines
        
        #
        
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
                perspective_transform(img, rectpoints)
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
