import numpy as np
import cv2

help_message = '''USAGE: contours_test.py [<image>]'''

if __name__ == '__main__':
    import sys
    try:
	fn = sys.argv[1]
    except:
	print help_message
	sys.exit(-1)

    img = cv2.imread(fn)
    img_gray = cv2.cvtColor(img,cv2.COLOR_RGB2GRAY)
    
    ret,thresh = cv2.threshold(img_gray,127,255,0)
    ret, contours, hierarchy = cv2.findContours(thresh,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
    cv2.drawContours(img,contours,-1,(250,250,250),2)
    cv2.imshow('image',img)
    cv2.waitKey()
    cv2.destroyAllWindows()
