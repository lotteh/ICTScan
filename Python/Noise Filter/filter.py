import numpy as np
import cv2
from matplotlib import pyplot as plt

im = cv2.imread('ict_cube.jpg')
img_gray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
dst = cv2.medianBlur(img_gray,3)
cv2.imwrite('median.jpg', dst)
