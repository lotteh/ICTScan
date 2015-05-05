#!/usr/bin/env python

import sys
import cv2
import numpy as np
point_count = 0;
y = [];
x = [];

def run_prespective_transform():
	global src
	src_quad = np.array([(x[0], y[0]), (x[1], y[1]), (x[2], y[2]), (x[3], y[3])], np.float32);
	dst_quad = np.array([(0.0, 0.0), (1032.0, 0.0), (1032.0, 581.0), (0.0, 581.0)], np.float32);
	transf_matr = cv2.getPerspectiveTransform(src_quad, dst_quad); # src, dst, 
	transf_img = cv2.warpPerspective(src, transf_matr, (1032, 581));
	print  transf_matr

	cv2.imwrite('pers_t.jpg', transf_img);

	cv2.namedWindow("Transformiert", cv2.WINDOW_AUTOSIZE);
	cv2.imshow("Transformiert", transf_img);

	grau = cv2.cvtColor(transf_img, cv2.COLOR_BGR2GRAY);
	cannyImg = cv2.Canny(grau, 50, 150, 3);

	cv2.namedWindow("Canny", cv2.WINDOW_AUTOSIZE);
	cv2.imshow("Canny", cannyImg);
	pass

#par1 = 0 -> Mouse move
#par1 = 1 -> Mouse down
#par1 = 4 -> Mouse up
#par 2 = x-coord
#par3 = y-coord
#par4 = ?
#par5 = userdata
def callback_onMouse(par1, par2, par3, par4, par5):
	global point_count;
	global src;
	if par1 == 1:
		point_count = point_count + 1;
		print("Point{2}: X:{0}; Y:{1}".format(par2, par3,point_count));
		x.append(par2);
		y.append(par3);
		if point_count == 4:
			#cv2.line(src, (x[0], y[0]), (x[1], y[1]), (0, 0, 255), 1);
			#cv2.line(src, (x[1], y[1]), (x[2], y[2]), (0, 0, 255), 1);
			#cv2.line(src, (x[2], y[2]), (x[3], y[3]), (0, 0, 255), 1);
			#cv2.line(src, (x[3], y[3]), (x[0], y[0]), (0, 0, 255), 1);
			run_prespective_transform()
			cv2.imshow("Quelle", src);
			pass
		pass
	pass
help_message = "USAGE: perspective_transform.py [<image>]\nSelect 4 Points in following order:\nupper-left, upper-right, bottom-right, bottom-left\nClose with 'Esc'\n"
try: fn = sys.argv[1]
except: 
	print help_message
	exit()

src = cv2.imread(fn, True);

cv2.namedWindow("Quelle", cv2.WINDOW_AUTOSIZE);
cv2.imshow("Quelle", src);

cv2.cv.SetMouseCallback("Quelle", callback_onMouse, "Hello World!");
c = 0;
while c != 1048603:
	c = cv2.waitKey(0)
	print(c);
 	pass
