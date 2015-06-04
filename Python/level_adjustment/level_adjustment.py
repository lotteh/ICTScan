import numpy as np
from matplotlib import pyplot as plt
import cv2

help_message = '''USAGE: calc_histogram.py [<image>]'''

if __name__ == '__main__':
    import sys
    try:
        fn = sys.argv[1]
    except:
        print help_message
        sys.exit(-1)

    img = cv2.imread(fn, True);
    hist, bins = np.histogram(img.flatten(), 256, [0, 256]);

    print(hist);
    print(bins);

    cdf = hist.cumsum()
    cdf_normalized = cdf * hist.max()/ cdf.max()

    plt.plot(cdf_normalized, color = 'b');
    plt.hist(img.flatten(), 256, [0, 256], color = 'r')
    plt.xlim([0, 256]);
    plt.legend(('cdf', 'histogram'), loc = 'upper left');
    plt.show()