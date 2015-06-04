import numpy as np
import cv2

help_message = '''USAGE: noise_filter.py [<image>]'''

if __name__ == '__main__':
    import sys
    try:
        fn = sys.argv[1]
    except:
        print help_message
        sys.exit(-1)

    img = cv2.imread(fn, True);

    kernel = np.ones((4,2), np.uint8);
    opening = cv2.morphologyEx(img, cv2.MORPH_OPEN, kernel)

    cv2.imshow("Opened:", opening);

    cv2.waitKey(0)
    cv2.destroyAllWindows()
