import numpy as np
import cv2

help_message = '''USAGE: canny.py [<image>]'''

if __name__ == '__main__':
    import sys
    try: fn = sys.argv[1]
    except: print help_message

    img = cv2.imread(fn, True)
    
    cv2.namedWindow('image')
    
    def valueChanged(x):
		threshold1 = cv2.getTrackbarPos('threshold1', 'image')
		threshold2 = cv2.getTrackbarPos('threshold2', 'image')
		edges = cv2.Canny(img,threshold1,threshold2)
		cv2.imshow('image', edges)
    
    cv2.createTrackbar('threshold1', 'image', 0, 1000, valueChanged)
    cv2.createTrackbar('threshold2', 'image', 0, 1000, valueChanged)
    
    valueChanged(0)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
