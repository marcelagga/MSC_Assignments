#!/usr/bin/python

import os, sys
import json
import numpy as np
import re
from collections import Counter


"""
Student Name: Marcel Aguilar Garcia
ID Number: 20235620
GitHub Link: https://github.com/marcelagga/ARC.git

Summary:

The three solves functions are first identifying a specific figure(s),colour(s)
or pattern(s). This information is stored in lists or dictionaries and used to
process the input array into the final solution.

Numpy functions such as numpy.where and numpy.ndenumerate have been used in
a similar way to do the first identification and processing of the input array.
Array slicing has been used multiple times when searching for figures and
patterns within the input array.

Python iterable objects are common accross the three solutions and used to
store colours, indexes and relations between them. The usage of
lists comprehension and dictionaries comprehension helped the readability of
the code.

While an additional library, Counter, is used to solve c8cbb738, the usage
of this library is very specific and just used to determine the most
common value on the grid.
"""

def solve_c8cbb738(x):
    """
    Figures are represented by sets of four points, each in a different colour.
    The solution places all figures in a grid of the same size than the
    square and always in the same way.

    The algorithm solves all train and test grids correctly
    """
    def get_colour_counts(x):
        return Counter([el for row in x for el in row])

    def get_background_colour(x):
        return get_colour_counts(x).most_common(1)[0][0]

    def get_colours_coordinates(x,background_colour):
        coordinates = {colour:[] for colour in get_colour_counts(x) if colour!=background_colour}
        for index,color in np.ndenumerate(x):
            if color != background_colour:
                coordinates[color].append(index)

        return coordinates

    def get_figures(coordinates):
        figures = {}
        for colour in coordinates:
            figures[determine_figure(coordinates[colour])] = colour

        return figures

    def init_solution(colours_coordinates,background_colour):
        for colour in colours_coordinates:
            coordinates = colours_coordinates[colour]
            if determine_figure(coordinates) == 1:
                (A,B,C,D) = coordinates
                size = B[1]-A[1]+1
                return np.zeros((size,size))+background_colour

    def determine_figure(figure):
        (A,B,C,D) = figure
        side_1 = B[1]-A[1]
        side_2 = C[0]-A[0]
        if side_1 == side_2:
            return 1
        elif A[0] == B[0]:
            if B[1]-A[1] == 2:
                return 2
            else:
                return 3
        else:
            return 0

    def paint_coordinates(solution,colour,coordinates):
        for coordinate in coordinates:
            solution[coordinate] = colour

    #Finds the background colour from the grid
    background_colour = get_background_colour(x)
    #Finds all other colours and its coordinates
    colours_coordinates = get_colours_coordinates(x,background_colour)
    #Determines which figure each set of coordinates represents
    #[0 - rhombus, 1 - square, 2 & 3 - rectangles]
    figures = get_figures(colours_coordinates)
    #Starts solution with colours set to background
    solution = init_solution(colours_coordinates,background_colour)

    end_p = len(solution)-1
    mid_p = int((len(solution)-1)/2)

    #Iterates through each figure and updates the colours of the solution
    #according to its representation
    for figure in figures:
        if figure == 0:
            coordinates_rhombus = [(0,mid_p),(mid_p,0),(mid_p,end_p),(end_p,mid_p)]
            paint_coordinates(solution,figures[figure],coordinates_rhombus)
        elif figure == 1:
            coordinates_square = [(0,0),(0,end_p),(end_p,0),(end_p,end_p)]
            paint_coordinates(solution,figures[figure],coordinates_square)
        elif figure == 2:
            coordinates_rectangle_1 = [(0,mid_p+1),(0,mid_p-1),(end_p,mid_p+1),(end_p,mid_p-1)]
            paint_coordinates(solution,figures[figure],coordinates_rectangle_1)
        else:
            coordinates_rectangle_2 = [(mid_p+1,0),(mid_p-1,0),(mid_p+1,end_p),(mid_p-1,end_p)]
            paint_coordinates(solution,figures[figure],coordinates_rectangle_2)

    return solution.astype(int)


def solve_73251a56(x):
    """
    Each grid has always the same figure representation with some points
    in black. The colour of the black points can be determined by following the
    pattern given by the figure. The solution returns the grid with the correct
    colours.

    The algorithm solves all train and test grids correctly
    """
    black = 0
    blue =  1

    def get_coordinates_missing_colour(x):
        coordinates = np.where(x == black)
        return list(zip(coordinates[0], coordinates[1]))

    def get_sequence_colours(x):
        sequence_colours = [x[0,1]]
        first_line = x[0,2:]
        i = 0
        while first_line[2*i]!=sequence_colours[0]:
            sequence_colours.append(first_line[2*i])
            i=i+1
        return sequence_colours

    def get_colour_coordinate(coordinate,sequence_colours):
        (p_row,p_col) = coordinate
        if x[p_col,p_row]:
            return x[p_col,p_row]
        elif p_col == p_row:
            colour = blue
        elif p_col < p_row:
            colour = int((p_col-p_row)/(p_row+2))
        else:
            colour = int((p_row-p_col)/(p_col+2))
        return sequence_colours[colour]

    #Finds the sequence of colours that the figure follows
    sequence_colours = get_sequence_colours(x)
    #Finds all missing colours from the input grid
    coordinates_missing_colours = get_coordinates_missing_colour(x)
    #Iterates through all coordinates with missing colours and updates the
    #colour of them to the correct one
    for coordinate in coordinates_missing_colours:
        x[coordinate] = get_colour_coordinate(coordinate,sequence_colours)

    return x


def solve_776ffc46(x):
    """
    The square in the grid contains a figure that has a certain colour.
    The transformation changes the colour of all figures matching exactly the
    one in square to that same colour.

    The algorithm solves all train and test grids correctly
    """

    black = 0
    blue = 1
    grey = 5

    def get_square_coordinates(x):
        edge = len(x)
        for (row,col),colour in np.ndenumerate(x):
            if (colour == grey and row+1 < edge and col+1 < edge):
                if (x[(row+1,col)] == grey and x[(row,col+1)] == grey):
                    d_row,d_col = row+1,col+1
                    while (d_row != edge and d_col != edge and x[d_row,d_col]!=grey):
                        d_row+=1
                        d_col+=1
                    if (d_row != edge and d_col != edge):
                        return ((row,col),(d_row,d_col),(row,d_col),(d_row,col))

    def get_grid_inside_square(x,square_coordinates):
        A,B,C,D = square_coordinates
        return x[A[0]+1:B[0],A[1]+1:B[1]]

    def get_figure_inside_square(x,square):
        #Removing all rows and columns that are black
        figure_cropped =  (square[~np.all(square == black, axis=1)])[:,~np.all(square == black, axis=0)]
        #Finding figure colour, ie, only colour from this subgrid that is not black
        figure_colour = [c for c in figure_cropped.flatten() if c!=black][0]
        num_rows,num_cols = figure_cropped.shape[0] + 2,figure_cropped.shape[1] + 2
        figure_with_margins = np.zeros((num_rows,num_cols))
        #All figures from the grid are of blue colour, replacing original colour to blue to find the exact match
        figure_with_margins[1:num_rows-1,1:num_cols-1] = np.where(figure_cropped == figure_colour,blue,figure_cropped)
        return figure_with_margins,figure_colour


    #Finds the coordinates of the four corners of the square (its diagonal has to be in the grid)
    square_coordinates = get_square_coordinates(x)
    #Find the subgrid inside the square
    inside_square = get_grid_inside_square(x,square_coordinates)
    #Finds the figure inside the square and its colour
    figure,colour = get_figure_inside_square(x,inside_square)
    #Iterates through all subgrids of the size of the figure. If the subgrid matches the figure, updates the colours to the correct one
    f_rows,f_cols = figure.shape
    for row in range(0,len(x)-(f_rows-1)):
        for col in range(0,len(x)-(f_cols-1)):
            sub_array = x[row:row+f_rows,col:col+f_cols]
            if np.all(sub_array == figure):
                x[row:row+f_rows,col:col+f_cols] = np.where(sub_array == blue,colour,sub_array)
    return x



def main():
    # Find all the functions defined in this file whose names are
    # like solve_abcd1234(), and run them.

    # regex to match solve_* functions and extract task IDs
    p = r"solve_([a-f0-9]{8})"
    tasks_solvers = []
    # globals() gives a dict containing all global names (variables
    # and functions), as name: value pairs.
    for name in globals():
        m = re.match(p, name)
        if m:
            # if the name fits the pattern eg solve_abcd1234
            ID = m.group(1) # just the task ID
            solve_fn = globals()[name] # the fn itself
            tasks_solvers.append((ID, solve_fn))

    for ID, solve_fn in tasks_solvers:
        # for each task, read the data and call test()
        directory = os.path.join("..", "data", "training")
        json_filename = os.path.join(directory, ID + ".json")
        data = read_ARC_JSON(json_filename)
        test(ID, solve_fn, data)

def read_ARC_JSON(filepath):
    """Given a filepath, read in the ARC task data which is in JSON
    format. Extract the train/test input/output pairs of
    grids. Convert each grid to np.array and return train_input,
    train_output, test_input, test_output."""

    # Open the JSON file and load it
    data = json.load(open(filepath))

    # Extract the train/test input/output grids. Each grid will be a
    # list of lists of ints. We convert to Numpy.
    train_input = [np.array(data['train'][i]['input']) for i in range(len(data['train']))]
    train_output = [np.array(data['train'][i]['output']) for i in range(len(data['train']))]
    test_input = [np.array(data['test'][i]['input']) for i in range(len(data['test']))]
    test_output = [np.array(data['test'][i]['output']) for i in range(len(data['test']))]

    return (train_input, train_output, test_input, test_output)


def test(taskID, solve, data):
    """Given a task ID, call the given solve() function on every
    example in the task data."""
    print(taskID)
    train_input, train_output, test_input, test_output = data
    print("Training grids")
    for x, y in zip(train_input, train_output):
        yhat = solve(x)
        show_result(x, y, yhat)
    print("Test grids")
    for x, y in zip(test_input, test_output):
        yhat = solve(x)
        show_result(x, y, yhat)


def show_result(x, y, yhat):
    print("Input")
    print(x)
    print("Correct output")
    print(y)
    print("Our output")
    print(yhat)
    print("Correct?")
    # if yhat has the right shape, then (y == yhat) is a bool array
    # and we test whether it is True everywhere. if yhat has the wrong
    # shape, then y == yhat is just a single bool.
    print(np.all(y == yhat))

if __name__ == "__main__": main()
