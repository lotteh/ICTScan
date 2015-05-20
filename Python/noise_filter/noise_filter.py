import numpy as np
import cv2

help_message = '''USAGE: noise_filter.py [<image>]'''

if __name__ == '__main__':
    import sys
    try: fn = sys.argv[1]
    except:
        print help_message
        sys.exit(-1)

    img = cv2.imread(fn)
    img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    
    dst = cv2.medianBlur(img_gray,3)
    cv2.imwrite('median.jpg', dst)
    print("saved result as \"median.jpg\"")
