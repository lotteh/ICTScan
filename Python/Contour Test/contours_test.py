import numpy as np
import cv2
im = cv2.imread('median.jpg')
img_gray = cv2.cvtColor(im,cv2.COLOR_RGB2GRAY)
ret,thresh = cv2.threshold(img_gray,127,255,0)
contours, hierarchy = cv2.findContours(thresh,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
cv2.drawContours(im,contours,-1,(250,250,250),2)
cv2.imshow('image.jpg',im)
cv2.waitKey()
cv2.destroyAllWindows()
