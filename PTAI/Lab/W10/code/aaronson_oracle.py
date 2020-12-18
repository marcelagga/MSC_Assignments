import numpy as np
import random
from getchar import getch
import sys
import pickle
from collections import Counter
        


def main(ngrams=None):
    
    if ngrams is None:
        ngrams = Counter()
    
    move_hist = []
    pred_hist = []
    
    while True:

        # generate a prediction
        if len(move_hist) > 5:
            ng = "".join(move_hist[-5:])
            if ngrams[ng, "0"] > ngrams[ng, "1"]:
                prediction = "0"
            elif ngrams[ng, "1"] > ngrams[ng, "0"]:
                prediction = "1"
            else:
                prediction = random.choice("01")
        else:
            prediction = random.choice("01")

        # read user's move and check if valid or exit signal
        # this decode seems necessary on Windows
        user_move = getch()
        if user_move == "C":
            break # exit loop
        if user_move not in "01":
            print(user_move)
            print("Please enter 0 or 1, or type C to quit")
            continue # go to next iteration, don't store
        
        # store the update and print accuracy
        if len(move_hist) > 5:
            ngrams[ng, user_move] += 1
        move_hist.append(user_move)
        pred_hist.append(prediction)
        accuracy = np.mean([p == u for p, u in zip(pred_hist, move_hist)])
        print(f"prediction: {prediction}; user played {user_move}; accuracy {accuracy}")
        
    return ngrams
        
if __name__ == "__main__":
    ngrams = None
    if len(sys.argv) > 1:
        try:
            ngrams = eval(open(sys.argv[1]).read())
            print(f"Reading input from {sys.argv[1]} and will write on exit")
        except FileNotFoundError:
            print(f"No input file found, but will write to {sys.argv[1]} on exit")
    else:
        print(f"No input file specified, will not read or write")
            
    print("Press 0 or 1, or type C to exit")
    ngrams = main(ngrams)
    
    if len(sys.argv) > 1:
        try:
            open(sys.argv[1], "w").write(repr(ngrams) + "\n")
        except:
            print(f"Could not write to output file {sys.argv[1]}")

    print("ngrams")
    print(repr(ngrams))
    print("Finishing")
