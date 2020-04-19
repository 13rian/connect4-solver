# Connect4 Solver


## Goal of this Project
This is the implementation of a Connect 4 solver. The positions are solved with the negamax algorithm. I created this solver to have a test set for the [AlphaZero implementation of Connect4](https://github.com/13rian/alpha-zero-connect4). The solver is based on [this](http://blog.gamesolver.org/) tutorial. The following strategies are used to solve the Connect4 positions:
- Negamax (version of minimax where you assume that the opponent score is the negative value of your score)  
- Alpha-Beta-Pruning   
- Ordering of the move exploration order based on a score function. The score is equal to the number of created
  winning chances as they are most likely moves that lead to a winning path more quickly. If the score is equal
  central columns are explored first because they are often good moves.  
- Transposition table to save the upper bound (saving lower bound seems to slow the search)  
- Iterative deepening  
- Avoid exploring losing moves to prune the tree faster	 

The solver was used to create transposition tables with all 8, 9, 10, 11 and 12 ply positions. Using the 12ply positions as transposition table seems to accelerate the solver the most. 


## Main Programs
- The file Main_CreateTranspositionTable.java solves all n ply positions that are not won within the next 2 moves. In order to change the n ply change the constant nplyTranspositions. The solved positions will be saved in a folder called solved_pos. In config/app.conf the start index and the end index of the positions to solve can be defined in case more than one instance of the program is running. The checkpoint defines how many solved positions will be saved in one file. The folder with the solved positions is pushed to the repository because it takes some time to solve the positions. If you have the 12 ply transposition table the other transposition tables can be created in a few seconds.  
- After all positions are solved there should be some files in the solved_pos folder. The file Main_MergeCsvFiles.java will merge them into one file and save the positions in a folder called transposition_table_csvs. The n ply need to be defined in this file as well with the constant called nply. All solutions for positions with 8, 9, 10, 11 and 12 ply were pushed to the repository as well. In order to use them they need to be merged with Main_MergeCsvFiles.java first to get one csv-file that defines the transposition table. Unfortunately the files are too big to push into the repository. 
- With the file MainCreateTestSet.java some test set can be created which was used to test the AlphaZero implementation of Connect4.


## Position Representation
A Connect4 position is represented with two integers. The the board is represented as follows:  
5 12 19 26 33 40 47  
4 11 18 25 32 39 46  
3 10 17 24 31 38 45  
2 &nbsp; 9 16 23 30 37 44  
1 &nbsp; 8 15 22 29 36 43  
0 &nbsp; 7 14 21 28 35 42  

The numbers in the matrix above correspond to the bit in the integer that is turned on if a disk is there. The transposition table csv-file contains two columns position and disk_mask. The position is the integer that represents all the disks of the current player and the disk_mask represents all played disks using the representation above. 

The test set contains also the positions and the disk mask. Additionally the following columns are in there as well:
- strong_score: the exact score of a position. This corresponds to the number of disks the winner played minus 22.
- weak_score: just 1, 0, -1 for win, draw and loss
- strong_moves: all moves that lead to the best possible score
- weak_moves: all moves that lead to a win but not necessarily to the quickest win.
