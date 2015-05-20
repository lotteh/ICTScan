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
    
    rectpoints = [(1186, 506), (4754,91), (4773,3522), (1029, 3331)]

    def perspective_transform(src_img, rectpoints):
        src_quad = np.array([rectpoints[0], rectpoints[1], rectpoints[2], rectpoints[3]], np.float32);
        dst_quad = np.array([(0.0, 0.0), (1022.0, 0.0), (1022.0, 600.0), (0.0, 600.0)], np.float32);
        transf_matr = cv2.getPerspectiveTransform(src_quad, dst_quad); # src, dst, 
        transf_img = cv2.warpPerspective(img, transf_matr, (1022, 600));
        print transf_matr

        cv2.namedWindow("transformed", cv2.WINDOW_AUTOSIZE);
        cv2.imshow("transformed", transf_img);
        cv2.destroyWindow('image')
        cv2.destroyWindow('magnified') 
        
        
        
        mean_image = np.zeros((600,1022,3), np.uint8)
        width = 6
        for i in range(0, 50*12, 50):
            for j in range(0, 25*41,25):
                for x in range(0, 2, 1):
                    left = j+9;
                    right = j+9;
                    if(x == 0):
                        left -= width
                    else:
                        right += width

                    avg_color = cv2.mean(transf_img[i:i+50, left:right])
                    
                    mean_image[i:i+50, left:right, 0] = avg_color[0]
                    mean_image[i:i+50, left:right, 1] = avg_color[1]
                    mean_image[i:i+50, left:right, 2] = avg_color[2]
        
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
        print(slat_string)
    def mouse_callback(event,x,y,flags,param):
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
    cv2.resizeWindow('image', 640, 480)
    cv2.imshow('image', img)
    cv2.moveWindow('image', 200, 0)

    cv2.namedWindow('magnified', cv2.WINDOW_NORMAL)
    cv2.resizeWindow('magnified', 100, 100)    
    cv2.moveWindow('magnified', 0, 0)

    cv2.waitKey(0)
    cv2.destroyAllWindows()
